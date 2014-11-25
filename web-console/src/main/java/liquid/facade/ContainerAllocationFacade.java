package liquid.facade;

import liquid.container.domain.ContainerType;
import liquid.container.persistence.domain.ContainerEntity;
import liquid.domain.ContainerAllocation;
import liquid.domain.RouteContainerAllocation;
import liquid.domain.SelfContainerAllocation;
import liquid.order.persistence.domain.OrderEntity;
import liquid.order.service.OrderService;
import liquid.transport.persistence.domain.ShipmentEntity;
import liquid.transport.persistence.domain.ShippingContainerEntity;
import liquid.transport.service.ContainerAllocationService;
import liquid.transport.service.TransportService;
import liquid.transport.service.ShippingContainerService;
import liquid.util.CollectionUtil;
import liquid.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by redbrick9 on 5/20/14.
 */
@Service
public class ContainerAllocationFacade {

    @Autowired
    private ShippingContainerService shippingContainerService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TransportService transportService;

    @Autowired
    private ContainerAllocationService containerAllocationService;

    public List<RouteContainerAllocation> computeContainerAllocation(Long orderId) {
        OrderEntity order = orderService.find(orderId);
        return computeContainerAllocation(order);
    }

    public List<RouteContainerAllocation> computeContainerAllocation(OrderEntity order) {
        List<RouteContainerAllocation> routeContainerAllocations = new ArrayList<>();

        int type = order.getContainerType();
        String subtypeName = order.getContainerSubtype().getName();

        Iterable<ShipmentEntity> routes = transportService.findByOrderId(order.getId());
        for (ShipmentEntity route : routes) {
            RouteContainerAllocation routeContainerAllocation = getRouteContainerAllocation(type, subtypeName, route);
            routeContainerAllocations.add(routeContainerAllocation);
        }

        return routeContainerAllocations;
    }

    public RouteContainerAllocation getRouteContainerAllocation(int type, String subtypeName, ShipmentEntity route) {
        RouteContainerAllocation routeContainerAllocation = new RouteContainerAllocation();
        routeContainerAllocation.setRouteId(route.getId());
        routeContainerAllocation.setRoute(route);
        routeContainerAllocation.setType(type);
        List<ContainerAllocation> containerAllocations = null;
        if (ContainerType.RAIL.getType() == type) {
            containerAllocations = computeRailContainerAllocations(subtypeName, route);
        } else {
            containerAllocations = computeSelfContainerAllocations(subtypeName, route);
        }
        routeContainerAllocation.setContainerAllocations(containerAllocations);
        return routeContainerAllocation;
    }

    public List<ContainerAllocation> computeSelfContainerAllocations(String subtypeName, ShipmentEntity route) {
        List<ContainerAllocation> containerAllocations = new ArrayList<>();

        List<ShippingContainerEntity> shippingContainers = shippingContainerService.findByRouteId(route.getId());
        int allocatedQuantity = shippingContainers == null ? 0 : shippingContainers.size();
        for (int j = 0; j < allocatedQuantity; j++) {
            ContainerAllocation containerAllocation = new ContainerAllocation();
            ShippingContainerEntity shippingContainer = shippingContainers.get(j);
            if (null != shippingContainer.getContainer()) {
                containerAllocation.setAllocationId(shippingContainer.getId());
                containerAllocation.setRouteId(route.getId());
                containerAllocation.setTypeNameKey(ContainerType.SELF.getI18nKey());
                containerAllocation.setSubtypeName(subtypeName);

                containerAllocation.setContainerId(shippingContainers.get(j).getContainer().getId());
                containerAllocation.setBicCode(shippingContainers.get(j).getContainer().getBicCode());
                containerAllocation.setOwner(shippingContainers.get(j).getContainer().getOwner().getName());
                containerAllocation.setYard(shippingContainers.get(j).getContainer().getYard().getName());
                containerAllocations.add(containerAllocation);
            }
        }

        return containerAllocations;
    }

    private List<ContainerAllocation> computeRailContainerAllocations(String subtypeName, ShipmentEntity route) {
        List<ContainerAllocation> containerAllocations = new ArrayList<>();
        List<ShippingContainerEntity> shippingContainers = shippingContainerService.findByRouteId(route.getId());
        int allocatedQuantity = shippingContainers == null ? 0 : shippingContainers.size();
        for (int j = 0; j < allocatedQuantity; j++) {
            ContainerAllocation containerAllocation = new ContainerAllocation();
            containerAllocation.setAllocationId(shippingContainers.get(j).getId());
            containerAllocation.setRouteId(route.getId());
            containerAllocation.setTypeNameKey(ContainerType.RAIL.getI18nKey());
            containerAllocation.setSubtypeName(subtypeName);
            containerAllocation.setBicCode(shippingContainers.get(j).getBicCode());
            containerAllocations.add(containerAllocation);
        }
        for (int j = allocatedQuantity; j < route.getContainerQty(); j++) {
            ContainerAllocation containerAllocation = new ContainerAllocation();
            containerAllocation.setRouteId(route.getId());
            containerAllocation.setTypeNameKey(ContainerType.RAIL.getI18nKey());
            containerAllocation.setSubtypeName(subtypeName);
            containerAllocation.setBicCode("");
            containerAllocations.add(containerAllocation);
        }
        return containerAllocations;
    }

    public void allocate(RouteContainerAllocation routeContainerAllocation) {
        List<ContainerAllocation> containerAllocations = routeContainerAllocation.getContainerAllocations();

        List<ShippingContainerEntity> shippingContainers = new ArrayList<ShippingContainerEntity>();
        for (int i = 0; i < containerAllocations.size(); i++) {
            ContainerAllocation containerAllocation = containerAllocations.get(i);
            ShippingContainerEntity shippingContainer = new ShippingContainerEntity();
            shippingContainer.setId(containerAllocation.getAllocationId());
            if (!StringUtil.valid(containerAllocation.getBicCode())) shippingContainer.setBicCode("");
            else shippingContainer.setBicCode(containerAllocation.getBicCode());
            shippingContainer.setRoute(ShipmentEntity.newInstance(ShipmentEntity.class, routeContainerAllocation.getRouteId()));
            shippingContainers.add(shippingContainer);
        }

        shippingContainerService.save(shippingContainers);
    }

    public void allocate(SelfContainerAllocation selfContainerAllocation) {
        ShipmentEntity route = transportService.find(selfContainerAllocation.getRouteId());

        List<ShippingContainerEntity> shippingContainers = shippingContainerService.findByRouteId(selfContainerAllocation.getRouteId());
        if (CollectionUtil.isEmpty(shippingContainers))
            shippingContainers = new ArrayList<ShippingContainerEntity>();

        for (int i = shippingContainers.size(); i < route.getContainerQty(); i++) {
            ShippingContainerEntity shippingContainer = new ShippingContainerEntity();
            shippingContainer.setRoute(route);
            shippingContainers.add(shippingContainer);
        }

        for (int i = 0, j = 0; i < shippingContainers.size() && j < selfContainerAllocation.getContainerIds().length; i++) {
            ShippingContainerEntity shippingContainer = shippingContainers.get(i);
            if (null == shippingContainer.getContainer()) {
                shippingContainer.setContainer(ContainerEntity.newInstance(ContainerEntity.class,
                        selfContainerAllocation.getContainerIds()[j]));
                shippingContainer.setRoute(route);
                j++;
            }
        }
        containerAllocationService.allocate(shippingContainers);
    }

    public void undo(long allocationId) {
        ShippingContainerEntity shippingContainer = shippingContainerService.find(allocationId);
        containerAllocationService.undo(shippingContainer);
    }
}
