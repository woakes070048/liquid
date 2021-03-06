package liquid.accounting.controller;

import liquid.accounting.model.Invoice;
import liquid.accounting.model.Receipt;
import liquid.accounting.model.Statement;
import liquid.accounting.service.InternalInvoiceService;
import liquid.accounting.service.InternalReceiptService;
import liquid.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

/**
 * Created by Tao Ma on 1/8/15.
 */
@Controller
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InternalInvoiceService invoiceService;

    @Autowired
    private InternalReceiptService receiptService;

    @RequestMapping(method = RequestMethod.POST)
    public String add(@Valid @ModelAttribute Invoice invoice,
                      BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            Receipt receipt = new Receipt();
            receipt.setOrderId(invoice.getOrderId());
            receipt.setIssuedAt(DateUtil.dayStrOf());
            model.addAttribute("receipt", receipt);
            return "receivable/panel";
        }
        return "redirect:" + "/receivable/120";
    }
}
