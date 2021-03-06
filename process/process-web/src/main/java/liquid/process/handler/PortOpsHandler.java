package liquid.process.handler;

import liquid.order.domain.Order;
import liquid.order.service.OrderService;
import liquid.process.domain.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Map;

/**
 * Created by mat on 4/24/16.
 */
@Component
public class PortOpsHandler extends AbstractTaskHandler {

    @Autowired
    private OrderService orderService;

    @Override
    public void preComplete(String taskId, Map<String, Object> variableMap) {}

    @Override
    public boolean isRedirect() {
        return false;
    }

    @Override
    public void init(Task task, Model model) {
        Order order = orderService.find(task.getOrderId());
        buildPurchase(task, model, order);
    }
}
