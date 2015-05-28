package liquid.order.controller;

import liquid.container.domain.ContainerCap;
import liquid.container.domain.ContainerSubtype;
import liquid.container.domain.ContainerType;
import liquid.container.service.ContainerSubtypeService;
import liquid.core.controller.BaseController;
import liquid.core.security.SecurityContext;
import liquid.operation.domain.*;
import liquid.operation.service.CustomerService;
import liquid.operation.service.GoodsService;
import liquid.operation.service.LocationService;
import liquid.operation.service.ServiceTypeService;
import liquid.order.domain.OrderStatus;
import liquid.order.domain.ReceivingContainer;
import liquid.order.domain.ReceivingOrder;
import liquid.order.model.OrderSearchBar;
import liquid.order.service.ReceivingOrderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: tao
 * Date: 10/13/13
 * Time: 4:33 PM
 */
@Controller
@RequestMapping("/recv_order")
public class ReceivingOrderController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(ReceivingOrderController.class);

    @Autowired
    private ReceivingOrderServiceImpl recvOrderService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private ContainerSubtypeService containerSubtypeService;

    @ModelAttribute("serviceTypes")
    public Iterable<ServiceType> populateServiceTypes() {
        return serviceTypeService.findAll();
    }

    @ModelAttribute("customers")
    public Iterable<Customer> populateCustomers() {
        return customerService.findAll();
    }

    @ModelAttribute("cargos")
    public Iterable<Goods> populateCargoTypes() {
        return goodsService.findAll();
    }

    @ModelAttribute("locations")
    public Iterable<Location> populateLocations() {
        return locationService.findByTypeId(LocationType.CITY);
    }

    @ModelAttribute("containerTypeMap")
    public Map<Integer, String> populateContainerTypes() {
        return ContainerType.toMap();
    }

    @Deprecated
    @ModelAttribute("containerCaps")
    public ContainerCap[] populateContainerCaps() {
        return ContainerCap.values();
    }

    @ModelAttribute("railContainerSubtypes")
    public Iterable<ContainerSubtype> populateRailContainerSubtypes() {
        return containerSubtypeService.findByContainerType(ContainerType.RAIL);
    }

    @ModelAttribute("selfContainerSubtypes")
    public Iterable<ContainerSubtype> populateOwnContainerSubtypes() {
        return containerSubtypeService.findByContainerType(ContainerType.SELF);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String list(@ModelAttribute(value = "searchBarForm") OrderSearchBar orderSearchBar,
                       Model model, HttpServletRequest request) {
        PageRequest pageRequest = new PageRequest(orderSearchBar.getNumber(), size, new Sort(Sort.Direction.DESC, "id"));
        Long id = null;
        Long customerId = null;
        String username = null;
        switch (SecurityContext.getInstance().getRole()) {
            case "ROLE_SALES":
            case "ROLE_MARKETING":
                username = SecurityContext.getInstance().getUsername();
                break;
            default:
                break;
        }
        if ("customer".equals(orderSearchBar.getType())) {
            customerId = orderSearchBar.getId();
        } else if ("order".equals(orderSearchBar.getType())) {
            id = orderSearchBar.getId();
        }
        Page<ReceivingOrder> page = recvOrderService.findAll(id, customerId, username, pageRequest);

        orderSearchBar.prepand(request.getRequestURI());
        model.addAttribute("page", page);
        return "recv_order/find";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String initCreationForm(Model model) {
        List<Location> locationEntities = locationService.findByTypeId(LocationType.CITY);

        ReceivingOrder order = new ReceivingOrder();
        order.setServiceType(serviceTypeService.find(7L));

        List<ReceivingContainer> containers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            containers.add(new ReceivingContainer());
        }
        order.setContainers(containers);
        model.addAttribute("order", order);
        return "recv_order/form";
    }

    @RequestMapping(method = RequestMethod.POST, params = "addContainer")
    public String addContainer(@ModelAttribute("order") ReceivingOrder order) {
        logger.debug("order: {}", order);

        order.getContainers().add(new ReceivingContainer());

        return "recv_order/form";
    }

    @RequestMapping(method = RequestMethod.POST, params = "removeContainer")
    public String removeContainer(@ModelAttribute("order") ReceivingOrder order,
                                  final HttpServletRequest request) {
        logger.debug("order: {}", order);
        final int index = Integer.valueOf(request.getParameter("removeContainer"));
        order.getContainers().remove(index);

        return "recv_order/form";
    }

    @RequestMapping(method = RequestMethod.POST, params = "save")
    public String save(@Valid @ModelAttribute("order") ReceivingOrder order,
                       BindingResult bindingResult, Model model, HttpServletRequest request) {
        logger.debug("order: {}", order);
        String sourceName = request.getParameter("sourceName");
        logger.debug("sourceName: {}", sourceName);
        String destinationName = request.getParameter("destinationName");
        logger.debug("destinationName: {}", destinationName);
        order.setStatus(OrderStatus.SAVED.getValue());

        if (bindingResult.hasErrors()) {
            model.addAttribute("sourceName", sourceName);
            model.addAttribute("destinationName", destinationName);
            return "recv_order/form";
        } else {
            Iterator<ReceivingContainer> containerIterator = order.getContainers().iterator();
            while (containerIterator.hasNext()) {
                ReceivingContainer container = containerIterator.next();
                if (container.getBicCode() == null || container.getBicCode().trim().length() == 0)
                    containerIterator.remove();
            }
            order.setOrderNo(recvOrderService.computeOrderNo(SecurityContext.getInstance().getRole(), order.getServiceType().getCode()));
            recvOrderService.save(order);
            return "redirect:/recv_order";
        }
    }

    @RequestMapping(method = RequestMethod.POST, params = "submit")
    public String submit(@Valid @ModelAttribute("order") ReceivingOrder order,
                         BindingResult bindingResult, Model model, HttpServletRequest request) {
        logger.debug("order: {}", order);
        String sourceName = request.getParameter("sourceName");
        logger.debug("sourceName: {}", sourceName);
        String destinationName = request.getParameter("destinationName");
        logger.debug("destinationName: {}", destinationName);
        order.setStatus(OrderStatus.SUBMITTED.getValue());
        if (bindingResult.hasErrors()) {
            model.addAttribute("sourceName", sourceName);
            model.addAttribute("destinationName", destinationName);
            return "recv_order/form";
        } else {
            Iterator<ReceivingContainer> containerIterator = order.getContainers().iterator();
            while (containerIterator.hasNext()) {
                ReceivingContainer container = containerIterator.next();
                if (container.getBicCode() == null || container.getBicCode().trim().length() == 0)
                    containerIterator.remove();
            }
            order.setOrderNo(recvOrderService.computeOrderNo(SecurityContext.getInstance().getRole(), order.getServiceType().getCode()));
            recvOrderService.save(order);
            return "redirect:/recv_order";
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String detail(@PathVariable Long id, Model model) {
        logger.debug("id: {}", id);

        ReceivingOrder order = recvOrderService.find(id);
        List<Location> locationEntities = locationService.findByTypeId(LocationType.STATION);
        model.addAttribute("locations", locationEntities);
        model.addAttribute("order", order);
        model.addAttribute("tab", "detail");
        return "recv_order/detail";
    }

    @RequestMapping(value = "/{id}/edit", method = RequestMethod.GET)
    public String getOrder(@PathVariable Long id, Model model) {
        logger.debug("id: {}", id);

        ReceivingOrder order = recvOrderService.find(id);
        logger.debug("order: {}", order);

        List<Location> locationEntities = locationService.findByTypeId(LocationType.STATION);
        model.addAttribute("order", order);
        return "recv_order/form";
    }
}
