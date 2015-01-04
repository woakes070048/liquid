package liquid.order.facade;

import liquid.container.domain.ContainerType;
import liquid.container.service.ContainerSubtypeService;
import liquid.domain.Currency;
import liquid.domain.LoadingType;
import liquid.domain.ServiceItem;
import liquid.domain.TradeType;
import liquid.order.domain.Order;
import liquid.order.domain.ReceivableSummary;
import liquid.order.persistence.domain.OrderEntity;
import liquid.order.persistence.domain.OrderRailEntity;
import liquid.order.persistence.domain.ReceivableSummaryEntity;
import liquid.order.persistence.domain.ServiceItemEntity;
import liquid.order.service.OrderService;
import liquid.order.service.RailwayService;
import liquid.persistence.domain.CustomerEntity;
import liquid.persistence.domain.LocationEntity;
import liquid.persistence.domain.ServiceSubtypeEntity;
import liquid.persistence.domain.ServiceTypeEntity;
import liquid.security.SecurityContext;
import liquid.service.*;
import liquid.task.domain.BusinessKey;
import liquid.task.service.ActivitiEngineService;
import liquid.util.DateUtil;
import liquid.validation.FormValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by redbrick9 on 5/28/14.
 */
@Service
public class OrderFacade {
    @Autowired
    private Environment env;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private ActivitiEngineService bpmService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RailwayService railwayService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ContainerSubtypeService containerSubtypeService;

    @Autowired
    private RailwayPlanTypeService railwayPlanTypeService;

    public Order initOrder() {
        Order order = new Order();
        order.setOriginId(Long.valueOf(env.getProperty("default.origin.id")));
        order.setOrigination(locationService.find(Long.valueOf(env.getProperty("default.origin.id"))).getName());
        order.setDestinationId(Long.valueOf(env.getProperty("default.destination.id")));
        order.setDestination(locationService.find(Long.valueOf(env.getProperty("default.destination.id"))).getName());
        order.setLoadingEstimatedTime(DateUtil.stringOf(new Date()));

        order.setPlanReportTime(DateUtil.stringOf(new Date()));
        return order;
    }

    @Transactional("transactionManager")
    public OrderEntity save(Order order) {
        OrderEntity orderEntity = convert(order);
        OrderRailEntity railwayEntity = convertRailway(order);
        railwayEntity.setOrder(orderEntity);
        orderEntity.setRailway(railwayEntity);
        orderEntity = orderService.save(orderEntity);
        return orderEntity;
    }

    public OrderEntity submit(Order order) {
        // set role
        order.setRole(SecurityContext.getInstance().getRole());
        ServiceTypeEntity serviceType = serviceTypeService.find(order.getServiceTypeId());
        // compute order no.
        order.setOrderNo(orderService.computeOrderNo(order.getRole(), serviceType.getCode()));
        OrderEntity orderEntity = save(order);

        boolean hasDelivery = false;
        List<ServiceItem> serviceItems = order.getServiceItems();
        for (ServiceItem serviceItem : serviceItems) {
            if (serviceItem.getServiceSubtypeId() == Integer.valueOf(env.getProperty("service.subtype.delivery.id"))) {
                hasDelivery = true;
                break;
            }
        }

        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("loadingType", orderEntity.getLoadingType());
        variableMap.put("hasDelivery", hasDelivery);
        variableMap.put("orderOwner", orderEntity.getUpdatedBy());
        variableMap.put("tradeType", orderEntity.getTradeType());
        bpmService.startProcess(orderEntity.getUpdatedBy(), BusinessKey.encode(orderEntity.getId(), orderEntity.getOrderNo()), variableMap);
        return orderEntity;
    }

    public Order find(long id) {
        OrderEntity orderEntity = orderService.find(id);
        Order order = convert(orderEntity);
        return order;
    }

    public Page<Order> findByCreateUser(String username, Pageable pageable) {
        Page<OrderEntity> page = orderService.findByCreateUser(username, pageable);

        List<OrderEntity> entities = page.getContent();
        List<Order> orders = convert(entities);

        return new PageImpl<Order>(orders, pageable, page.getTotalElements());
    }

    public List<Order> convert(List<OrderEntity> entities) {
        List<Order> orders = new ArrayList<>(entities.size());
        for (OrderEntity entity : entities) {
            orders.add(convertBasic(entity));
        }
        return orders;
    }

    public Page<Order> findAll(Pageable pageable) {
        Page<OrderEntity> page = orderService.findAll(pageable);

        List<OrderEntity> entities = page.getContent();
        List<Order> orders = convert(entities);

        return new PageImpl<Order>(orders, pageable, page.getTotalElements());
    }

    public Page<Order> findAll(final String orderNo, final String customerName, final String username, final Pageable pageable) {
        Page<OrderEntity> page = orderService.findAll(orderNo, customerName, username, pageable);

        List<OrderEntity> entities = page.getContent();
        List<Order> orders = convert(entities);

        return new PageImpl<Order>(orders, pageable, page.getTotalElements());
    }

    public Page<Order> findByStatus(Integer status, Pageable pageable) {
        Page<OrderEntity> page = orderService.findByStatus(status, pageable);

        List<OrderEntity> entities = page.getContent();
        List<Order> orders = convert(entities);

        return new PageImpl<Order>(orders, pageable, page.getTotalElements());
    }

    public Order duplicate(long id) {
        OrderEntity orderEntity = orderService.find(id);
        Order order = convert(orderEntity);
        order.setId(null);
        order.setRailwayId(null);
        order.setOrderNo(null);

        List<ServiceItem> serviceItems = order.getServiceItems();
        for (ServiceItem serviceItem : serviceItems) {
            serviceItem.setId(null);
        }

        order.setReceivableSummaryId(null);

        return order;
    }

    public OrderEntity convert(Order order) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(order.getId());
        orderEntity.setOrderNo(order.getOrderNo());
        orderEntity.setServiceTypeId(order.getServiceTypeId());
        orderEntity.setCustomerId(order.getCustomerId());
        orderEntity.setTradeType(order.getTradeType());
        orderEntity.setVerificationSheetSn(order.getVerificationSheetSn());
        orderEntity.setSrcLocId(order.getOriginId());
        orderEntity.setDstLocId(order.getDestinationId());
        orderEntity.setConsignee(order.getConsignee());
        orderEntity.setConsigneePhone(order.getConsigneePhone());
        orderEntity.setConsigneeAddress(order.getConsigneeAddress());
        orderEntity.setGoodsId(order.getGoodsId());
        orderEntity.setGoodsWeight(order.getGoodsWeight());
        orderEntity.setGoodsDimension(order.getGoodsDimension());
        orderEntity.setLoadingType(order.getLoadingType());
        orderEntity.setLoadingAddress(order.getLoadingAddress());
        orderEntity.setLoadingContact(order.getLoadingContact());
        orderEntity.setLoadingPhone(order.getLoadingPhone());
        orderEntity.setLoadingEt(DateUtil.dateOf(order.getLoadingEstimatedTime()));
        orderEntity.setContainerType(order.getContainerType());
        if (order.getContainerType() == ContainerType.RAIL.getType())
            orderEntity.setContainerSubtypeId(order.getRailContainerSubtypeId());
        else
            orderEntity.setContainerSubtypeId(order.getSelfContainerSubtypeId());
        orderEntity.setContainerQty(order.getContainerQuantity());
        orderEntity.setContainerAttribute(order.getContainerAttribute());

        for (ServiceItem serviceItem : order.getServiceItems()) {
            ServiceItemEntity serviceItemEntity = new ServiceItemEntity();
            serviceItemEntity.setId(serviceItem.getId());
            serviceItemEntity.setOrder(orderEntity);
            serviceItemEntity.setServiceSubtype(ServiceSubtypeEntity.
                    newInstance(ServiceSubtypeEntity.class, serviceItem.getServiceSubtypeId()));
            serviceItemEntity.setCurrency(serviceItem.getCurrency());
            serviceItemEntity.setQuotation(serviceItem.getQuotation());
            orderEntity.getServiceItems().add(serviceItemEntity);
        }
        ReceivableSummaryEntity receivableSummaryEntity = new ReceivableSummaryEntity();
        receivableSummaryEntity.setId(order.getReceivableSummaryId());
        receivableSummaryEntity.setOrder(orderEntity);
        receivableSummaryEntity.setCny(order.getCnyTotal());
        receivableSummaryEntity.setUsd(order.getUsdTotal());
        receivableSummaryEntity.setRemainingBalanceCny(order.getCnyTotal());
        receivableSummaryEntity.setRemainingBalanceUsd(order.getUsdTotal());
        orderEntity.setReceivableSummary(receivableSummaryEntity);

        orderEntity.setCreateRole(SecurityContext.getInstance().getRole());
        orderEntity.setStatus(order.getStatus());

        return orderEntity;
    }

    // TODO: Should enhance it in one to one way.
    public Order convert(OrderEntity orderEntity) {
        Order order = convertBasic(orderEntity);

        List<ServiceItemEntity> serviceItemEntities = orderEntity.getServiceItems();
        for (ServiceItemEntity serviceItemEntity : serviceItemEntities) {
            ServiceItem serviceItem = new ServiceItem();
            serviceItem.setId(serviceItemEntity.getId());
            serviceItem.setServiceSubtypeId(serviceItemEntity.getServiceSubtype().getId());
            serviceItem.setServiceSubtype(serviceItemEntity.getServiceSubtype().getName());
            serviceItem.setCurrency(serviceItemEntity.getCurrency());
            serviceItem.setCurrencyText(Currency.valueOf(serviceItemEntity.getCurrency()).toString());
            serviceItem.setQuotation(serviceItemEntity.getQuotation());
            serviceItem.setComment(serviceItemEntity.getComment());
            order.getServiceItems().add(serviceItem);
        }
        return order;
    }

    public Order convertBasic(OrderEntity orderEntity) {
        Order order = new Order();
        order.setId(orderEntity.getId());
        order.setOrderNo(orderEntity.getOrderNo());
        order.setServiceTypeId(orderEntity.getServiceTypeId());
        order.setServiceType(serviceTypeService.find(orderEntity.getServiceTypeId()).getName());
        order.setCustomerId(orderEntity.getCustomerId());
        order.setCustomerName(customerService.find(orderEntity.getCustomerId()).getName());
        order.setTradeType(orderEntity.getTradeType());
        order.setTradeTypeName(TradeType.valueOf(orderEntity.getTradeType()).getI18nKey());
        order.setVerificationSheetSn(orderEntity.getVerificationSheetSn());
        order.setOriginId(orderEntity.getSrcLocId());
        order.setOrigination(locationService.find(orderEntity.getSrcLocId()).getName());
        order.setDestinationId(orderEntity.getDstLocId());
        order.setDestination(locationService.find(orderEntity.getDstLocId()).getName());
        order.setConsignee(orderEntity.getConsignee());
        order.setConsigneePhone(orderEntity.getConsigneePhone());
        order.setConsigneeAddress(orderEntity.getConsigneeAddress());
        order.setGoodsId(orderEntity.getGoodsId());
        order.setGoodsName(goodsService.find(orderEntity.getGoodsId()).getName());
        order.setGoodsWeight(orderEntity.getGoodsWeight());
        order.setGoodsDimension(orderEntity.getGoodsDimension());
        order.setLoadingType(orderEntity.getLoadingType());
        order.setLoadingTypeName(LoadingType.valueOf(orderEntity.getLoadingType()).getI18nKey());
        order.setLoadingAddress(orderEntity.getLoadingAddress());
        order.setLoadingContact(orderEntity.getLoadingContact());
        order.setLoadingPhone(orderEntity.getLoadingPhone());
        order.setLoadingEstimatedTime(DateUtil.stringOf(orderEntity.getLoadingEt()));
        order.setContainerType(orderEntity.getContainerType());
        order.setContainerTypeName(ContainerType.valueOf(orderEntity.getContainerType()).getI18nKey());
        if (order.getContainerType() == ContainerType.RAIL.getType())
            order.setRailContainerSubtypeId(orderEntity.getContainerSubtypeId());
        else
            order.setSelfContainerSubtypeId(orderEntity.getContainerSubtypeId());
        order.setContainerSubtype(containerSubtypeService.find(orderEntity.getContainerSubtypeId()).getName());
        order.setContainerQuantity(orderEntity.getContainerQty());
        order.setContainerAttribute(orderEntity.getContainerAttribute());

        OrderRailEntity railwayEntity = orderEntity.getRailway();
        if (null != railwayEntity) {
            order.setRailwayId(railwayEntity.getId());
            order.setPlanReportTime(DateUtil.stringOf(railwayEntity.getPlanReportTime()));
            order.setRailwayPlanTypeId(railwayEntity.getPlanType());
            order.setRailwayPlanType(railwayPlanTypeService.find(railwayEntity.getPlanType()).getName());
            order.setProgramNo(railwayEntity.getProgramNo());
            if (null != railwayEntity.getSourceId()) {
                order.setRailSourceId(railwayEntity.getSourceId());
                order.setRailSource(locationService.find(railwayEntity.getSourceId()).getName());
            }
            if (null != railwayEntity.getDestinationId()) {
                order.setRailDestinationId(railwayEntity.getDestinationId());
                order.setRailDestination(locationService.find(railwayEntity.getDestinationId()).getName());
            }
            order.setComment(railwayEntity.getComment());
            order.setSameDay(railwayEntity.getSameDay());
        } else {
            order.setPlanReportTime(DateUtil.stringOf(new Date()));
        }

        order.setReceivableSummaryId(orderEntity.getReceivableSummary().getId());
        order.setUsdTotal(orderEntity.getReceivableSummary().getUsd());
        order.setCnyTotal(orderEntity.getReceivableSummary().getCny());
        order.setGrandTotal(orderEntity.getGrandTotal());
        order.setDistyPrice(orderEntity.getDistyPrice());
        order.setDistyUsd(orderEntity.getDistyUsd());
        order.setReceivableSummary(convert(orderEntity.getReceivableSummary()));

        order.setCreatedAt(DateUtil.stringOf(orderEntity.getCreatedAt()));
        order.setUpdatedAt(DateUtil.stringOf(orderEntity.getUpdatedAt()));
        order.setRole(orderEntity.getCreateRole());
        order.setStatus(orderEntity.getStatus());
        return order;
    }

    private OrderRailEntity convertRailway(Order order) {
        OrderRailEntity railwayEntity = new OrderRailEntity();
        railwayEntity.setId(order.getRailwayId());
        railwayEntity.setPlanReportTime(DateUtil.dateOf(order.getPlanReportTime()));
        railwayEntity.setPlanType(order.getRailwayPlanTypeId());
        railwayEntity.setSourceId(order.getRailSourceId());
        railwayEntity.setDestinationId(order.getRailDestinationId());
        railwayEntity.setProgramNo(order.getProgramNo());
        railwayEntity.setComment(order.getComment());
        railwayEntity.setSameDay(order.getSameDay());
        return railwayEntity;
    }

    private ReceivableSummary convert(ReceivableSummaryEntity entity) {
        ReceivableSummary receivableSummary = new ReceivableSummary();
        receivableSummary.setId(entity.getId());
        receivableSummary.setCny(entity.getCny());
        receivableSummary.setUsd(entity.getUsd());
        receivableSummary.setPrepaidTime(DateUtil.stringOf(entity.getPrepaidTime()));
        receivableSummary.setRemainingBalanceCny(entity.getRemainingBalanceCny());
        receivableSummary.setRemainingBalanceUsd(entity.getRemainingBalanceUsd());
        return receivableSummary;
    }

    /**
     * If customer name is exactly equals to the one in database, the customer id is set.
     *
     * @param id
     * @param name
     * @return
     */
    public FormValidationResult validateCustomer(Long id, String name) {
        if (null == id) {
            if (null != name && name.trim().length() > 0) {
                CustomerEntity customer = customerService.findByName(name);
                if (null != customer) {
                    return FormValidationResult.newSuccess(customer.getId(), name);
                }
            }
            return FormValidationResult.newFailure();
        }

        CustomerEntity customer = customerService.find(id);
        if (name == null) return FormValidationResult.newFailure();
        if (null == customer) return FormValidationResult.newFailure();

        if (name.equals(customer.getName()))
            return FormValidationResult.newSuccess(customer.getId(), customer.getName());
        else return FormValidationResult.newFailure();
    }

    public FormValidationResult validateLocation(Long id, String name) {
        if (null == id) {
            if (null != name && name.trim().length() > 0) {
                LocationEntity location = locationService.findByName(name);
                if (null != location) {
                    return FormValidationResult.newSuccess(location.getId(), location.getName());
                }
            }
            return FormValidationResult.newFailure();
        }

        LocationEntity location = locationService.find(id);
        if (name == null) return FormValidationResult.newFailure();
        if (null == location) return FormValidationResult.newFailure();
        if (name.equals(location.getName()))
            return FormValidationResult.newSuccess(location.getId(), location.getName());
        else return FormValidationResult.newFailure();
    }

    public FormValidationResult validateLocation(Long id, String name, int type) {
        if (null == id) {
            if (null != name && name.trim().length() > 0) {
                LocationEntity location = locationService.findByTypeAndName(type, name);
                if (null != location) {
                    return FormValidationResult.newSuccess(location.getId(), location.getName());
                }
            }
            return FormValidationResult.newFailure();
        }

        LocationEntity location = locationService.find(id);
        if (name == null) return FormValidationResult.newFailure();
        if (null == location) return FormValidationResult.newFailure();
        if (name.equals(location.getName()))
            return FormValidationResult.newSuccess(location.getId(), location.getName());
        else return FormValidationResult.newFailure();
    }

    public Page<Order> findByCustomerId(Long customerId, Pageable pageable) {
        Page<OrderEntity> page = orderService.findByCustomerId(customerId, SecurityContext.getInstance().getUsername(), pageable);

        List<OrderEntity> entities = page.getContent();
        List<Order> orders = convert(entities);

        return new PageImpl<Order>(orders, pageable, page.getTotalElements());
    }
}
