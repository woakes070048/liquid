package liquid.accounting.restfulapi;

import liquid.operation.domain.ServiceProvider;
import liquid.operation.service.ServiceProviderService;
import liquid.order.domain.Order;
import liquid.order.service.OrderService;
import liquid.core.model.SearchBarForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tao Ma on 1/15/15.
 */
@Controller
@RequestMapping("/api/purchase")
public class PurchaseRestController {

    @Autowired
    private ServiceProviderService serviceProviderService;

    @Autowired
    private OrderService orderService;

    @RequestMapping(method = RequestMethod.GET, params = "text")
    @ResponseBody
    public List<SearchBarForm> listCharges(@RequestParam String text) {
        List<SearchBarForm> searchBarFormList = new ArrayList<>();
        Iterable<ServiceProvider> serviceProviderList = serviceProviderService.findByQueryNameLike(text);
        for (ServiceProvider serviceProvider : serviceProviderList) {
            SearchBarForm searchBarForm = new SearchBarForm();
            searchBarForm.setType("sp");
            searchBarForm.setId(serviceProvider.getId());
            searchBarForm.setText(serviceProvider.getName());
            searchBarFormList.add(searchBarForm);
        }

        PageRequest pageRequest = new PageRequest(0, 10, new Sort(Sort.Direction.DESC, "id"));
        Page<Order> page = orderService.findByOrderNoLike(text, pageRequest);
        for (Order order : page.getContent()) {
            SearchBarForm searchBarForm = new SearchBarForm();
            searchBarForm.setId(order.getId());
            searchBarForm.setType("order");
            searchBarForm.setText(order.getOrderNo());
            searchBarFormList.add(searchBarForm);
        }

        return searchBarFormList;
    }
}
