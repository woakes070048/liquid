package liquid.controller;

import liquid.metadata.ChargeWay;
import liquid.metadata.IncomeType;
import liquid.persistence.domain.Charge;
import liquid.persistence.domain.Customer;
import liquid.persistence.domain.Income;
import liquid.persistence.domain.ServiceProvider;
import liquid.service.ChargeService;
import liquid.service.IncomeService;
import liquid.service.SpService;
import liquid.utils.RoleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * TODO: Comments.
 * User: tao
 * Date: 10/30/13
 * Time: 1:29 PM
 */
@Controller
@RequestMapping("/task/{taskId}/ajustement")
public class AjustementController extends BaseTaskController {
    private static final Logger logger = LoggerFactory.getLogger(AjustementController.class);

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ChargeService chargeService;

    @Autowired
    private SpService spService;

    @ModelAttribute("incomeTypes")
    public Map<Integer, String> populateCustomers() {
        return IncomeType.toMap();
    }

    @ModelAttribute("cts")
    public Map<Long, String> populateChargeTypes() {
        return chargeService.getChargeTypes();
    }

    @ModelAttribute("chargeWays")
    public ChargeWay[] populateChargeWays() {
        return ChargeWay.values();
    }

    @ModelAttribute("sps")
    public Iterable<ServiceProvider> populateSps() {
        return spService.findAll();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String init(@PathVariable String taskId,
                       Model model, Principal principal) {
        logger.debug("taskId: {}", taskId);

        // for incomes
        List<Income> incomes = incomeService.findByTaskId(taskId);
        model.addAttribute("incomes", incomes);
        model.addAttribute("incomesTotal", incomeService.total(incomes));

        // for charges
        model.addAttribute("cts", chargeService.getChargeTypes());
        model.addAttribute("chargeWays", ChargeWay.values());
        Iterable<Charge> charges = chargeService.findByTaskId(taskId);
        model.addAttribute("charges", charges);
        model.addAttribute("chargesTotal", chargeService.total(charges));
        return "order/ajustement";
    }

    @RequestMapping(value = "/income", method = RequestMethod.GET)
    public String initAddIncome(@PathVariable String taskId,
                                Model model, Principal principal) {
        logger.debug("taskId: {}", taskId);

        model.addAttribute("incomeTypes", IncomeType.toMap());
        model.addAttribute("income", new Income());
        return "order/add_income";
    }

    @RequestMapping(value = "/income", method = RequestMethod.POST)
    public String addIncome(@PathVariable String taskId,
                            @Valid Income income,
                            BindingResult result, Model model, Principal principal) {
        logger.debug("taskId: {}", taskId);
        logger.debug("income: {}", income);
        if (result.hasErrors()) {
            return "order/add_income";
        }

        incomeService.addIncome(taskId, income, principal.getName());

        return "redirect:/task/" + taskId + "/ajustement";
    }

    @RequestMapping(value = "/income/{incomeId}", method = RequestMethod.GET)
    public String delIncome(@PathVariable String taskId,
                            @PathVariable long incomeId,
                            Model model, Principal principal) {
        logger.debug("taskId: {}", taskId);

        incomeService.delIncome(incomeId);
        return "redirect:/task/" + taskId + "/ajustement";
    }

    @RequestMapping(value = "/charge", method = RequestMethod.GET)
    public String initAddCharge(@PathVariable String taskId,
                                Model model, Principal principal) {
        logger.debug("taskId: {}", taskId);

        model.addAttribute("charge", new Charge());
        return "order/add_charge";
    }

    @RequestMapping(value = "/charge", method = RequestMethod.POST)
    public String addCharge(@PathVariable String taskId,
                            @Valid Charge charge,
                            BindingResult result, Model model, Principal principal) {
        logger.debug("taskId: {}", taskId);
        logger.debug("charge: {}", charge);
        if (result.hasErrors()) {
            return "order/add_charge";
        }

        charge.setCreateRole(RoleHelper.getRole(principal));
        chargeService.addCharge(charge, principal.getName());

        return "redirect:/task/" + taskId + "/ajustement";
    }

    @RequestMapping(value = "/charge/{chargeId}", method = RequestMethod.GET)
    public String delCharge(@PathVariable String taskId,
                            @PathVariable long chargeId,
                            Model model, Principal principal) {
        logger.debug("taskId: {}", taskId);

        chargeService.removeCharge(chargeId);
        return "redirect:/task/" + taskId + "/ajustement";
    }
}