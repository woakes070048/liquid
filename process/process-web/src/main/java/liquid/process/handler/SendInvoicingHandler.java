package liquid.process.handler;

import liquid.accounting.domain.ReceivableSummary;
import liquid.accounting.service.ReceivableSummaryService;
import liquid.process.domain.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Map;

/**
 * Created by redbrick9 on 6/7/14.
 */
@Component
public class SendInvoicingHandler extends AbstractTaskHandler {
    @Autowired
    private ReceivableSummaryService receivableSummaryService;

    @Override
    public void preComplete(String taskId, Map<String, Object> variableMap) {
        Long orderId = taskService.getOrderIdByTaskId(taskId);
        ReceivableSummary receivableSummaryEntity = receivableSummaryService.findByOrderId(orderId);
        variableMap.put("salesPrice", "CNY: " + receivableSummaryEntity.getCny() + "; USD: " + receivableSummaryEntity.getUsd());
    }

    @Override
    public boolean isRedirect() {
        return true;
    }

    @Override
    public void init(Task task, Model model) {

    }
}
