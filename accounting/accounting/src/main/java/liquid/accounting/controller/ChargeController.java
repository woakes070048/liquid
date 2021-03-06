package liquid.accounting.controller;


import liquid.accounting.domain.Charge;
import liquid.accounting.domain.ChargeWay;
import liquid.accounting.domain.ExchangeRate;
import liquid.accounting.model.ChargeStatus;
import liquid.accounting.model.Earning;
import liquid.accounting.service.ExchangeRateService;
import liquid.accounting.service.InternalChargeService;
import liquid.accounting.service.ReceivableSummaryObsoloteService;
import liquid.container.domain.ContainerCap;
import liquid.container.domain.ContainerType;
import liquid.core.model.Alert;
import liquid.core.model.SearchBarForm;
import liquid.operation.domain.ServiceProvider;
import liquid.operation.domain.ServiceSubtype;
import liquid.operation.service.ServiceProviderService;
import liquid.operation.service.ServiceSubtypeService;
import liquid.order.domain.LoadingType;
import liquid.order.domain.OrderStatus;
import liquid.order.domain.TradeType;
import liquid.order.service.OrderService;
import liquid.process.service.TaskService;
import liquid.transport.domain.LegEntity;
import liquid.transport.domain.TransMode;
import liquid.transport.service.LegService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * User: tao
 * Date: 10/2/13
 * Time: 9:17 PM
 */
@Controller
@RequestMapping("/charge")
public class ChargeController {
    private static final int size = 20;

    private static final Logger logger = LoggerFactory.getLogger(ChargeController.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private InternalChargeService chargeService;

    @Autowired
    private LegService legService;

    @Autowired
    private ServiceSubtypeService serviceSubtypeService;

    @Autowired
    private ServiceProviderService serviceProviderService;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ReceivableSummaryObsoloteService receivableSummaryService;

    @ModelAttribute("transModes")
    public Map<Integer, String> populateTransModes() {
        return TransMode.toMap();
    }

    @ModelAttribute("chargeWays")
    public ChargeWay[] populateChargeWays() {
        return ChargeWay.values();
    }

    @ModelAttribute("tradeTypes")
    public TradeType[] populateTradeTypes() {
        return TradeType.values();
    }

    @ModelAttribute("loadingTypes")
    public LoadingType[] populateLoadings() {
        return LoadingType.values();
    }

    @ModelAttribute("containerTypes")
    public ContainerType[] populateContainerTypes() {
        return ContainerType.values();
    }

    @ModelAttribute("containerCaps")
    public ContainerCap[] populateContainerCaps() {
        return ContainerCap.values();
    }

    @ModelAttribute("status")
    public OrderStatus[] populateStatus() {
        return OrderStatus.values();
    }

    @ModelAttribute("exchangeRate")
    public BigDecimal populateExchangeRate() {
        return exchangeRateService.getExchangeRate().getValue();
    }

    @RequestMapping(method = RequestMethod.GET)
    public void init() {
        logger.debug("init");
    }

    @RequestMapping(method = RequestMethod.POST)
    public String addCharge(@Valid @ModelAttribute(value = "charge") Charge charge, BindingResult bindingResult, Model model) {
        logger.debug("charge: {}", charge);

        if (bindingResult.hasErrors()) {
            Iterable<ServiceSubtype> serviceSubtypes = serviceSubtypeService.findEnabled();
            List<ServiceProvider> sps = serviceProviderService.findByServiceSubtypeId(charge.getServiceSubtype().getId());

            Iterable<Charge> charges = chargeService.findByLegId(charge.getLeg().getId());

            charge.setShipment(charge.getLeg().getShipment());

            model.addAttribute("serviceSubtypes", serviceSubtypes);
            model.addAttribute("sps", sps);
            model.addAttribute("charge", charge);
            model.addAttribute("shipment", charge.getLeg().getShipment());
            model.addAttribute("leg", charge.getLeg());
            model.addAttribute("charges", charges);
            model.addAttribute("backToTask", taskService.computeTaskMainPath(charge.getTaskId()));
            return "charge/console";
        } else {
            charge.setShipment(charge.getLeg().getShipment());
            chargeService.save(charge);
            String redirect = "/charge/console?taskId=" + charge.getTaskId() + "&legId=" + charge.getLeg().getId();
            return "redirect:" + redirect;
        }
    }

    @RequestMapping(value = "/console", method = RequestMethod.GET, params = "legId")
    public String console(@RequestParam(value = "taskId", required = false) String taskId,
                          @RequestParam(value = "legId") Long legId,
                          Model model) {
        logger.debug("taskId: {}", taskId);
        logger.debug("legId: {}", legId);

        LegEntity leg = legService.find(legId);
        Iterable<Charge> charges = chargeService.findByLegId(legId);

        Charge charge = new Charge();
        charge.setShipment(leg.getShipment());
        charge.setLeg(leg);
        charge.setWay(ChargeWay.PER_CONTAINER.getValue());
        if (null != leg.getSp()) charge.setSp(leg.getSp());

        Long defaultServiceSubtypeId = 1L;
        switch (TransMode.valueOf(leg.getTransMode())) {
            case RAIL:
                defaultServiceSubtypeId = 2L;
                break;
            case BARGE:
                defaultServiceSubtypeId = 4L;
                break;
            case VESSEL:
                defaultServiceSubtypeId = 3L;
                break;
            case ROAD:
                defaultServiceSubtypeId = 5L;
                break;
        }
        charge.setServiceSubtype(serviceSubtypeService.find(defaultServiceSubtypeId));

        Iterable<ServiceSubtype> serviceSubtypes = serviceSubtypeService.findEnabled();
        List<ServiceProvider> sps = serviceProviderService.findByServiceSubtypeId(defaultServiceSubtypeId);
        charge.setTaskId(taskId);

        model.addAttribute("serviceSubtypes", serviceSubtypes);
        model.addAttribute("sps", sps);
        model.addAttribute("charge", charge);
        model.addAttribute("shipment", leg.getShipment());
        model.addAttribute("leg", leg);
        model.addAttribute("charges", charges);
        model.addAttribute("taskId", taskId);
        return "charge/console";
    }

    @RequestMapping(value = "/payable", method = RequestMethod.GET)
    public String payable(@RequestParam(defaultValue = "0", required = false) int number, Model model) {
        PageRequest pageRequest = new PageRequest(number, size, new Sort(Sort.Direction.DESC, "id"));
        Page<Charge> page = chargeService.findAll(pageRequest);
        model.addAttribute("page", page);
        model.addAttribute("contextPath", "/charge/payable?");

        SearchBarForm searchBarForm = new SearchBarForm();
        searchBarForm.setAction("/charge/payable");
        searchBarForm.setTypes(new String[][]{{"orderNo", "order.no"}, {"spName", "sp.name"}});
        model.addAttribute("searchBarForm", searchBarForm);
        return "charge/payable";
    }

    @RequestMapping(value = "/payable", method = RequestMethod.GET, params = {"type", "text"})
    public String payableSearch(@RequestParam(defaultValue = "0", required = false) int number, SearchBarForm searchBarForm, Model model) {
        PageRequest pageRequest = new PageRequest(number, size, new Sort(Sort.Direction.DESC, "id"));

        String orderNo = "orderNo".equals(searchBarForm.getType()) ? searchBarForm.getText() : null;
        String spName = "spName".equals(searchBarForm.getType()) ? searchBarForm.getText() : null;

        Page<Charge> page = chargeService.findAll(orderNo, spName, pageRequest);

        searchBarForm.setAction("/charge/payable");
        searchBarForm.setTypes(new String[][]{{"orderNo", "order.no"}, {"spName", "sp.name"}});
        model.addAttribute("searchBarForm", searchBarForm);
        model.addAttribute("page", page);

        model.addAttribute("contextPath", "/charge/payable?type=" + searchBarForm.getType() + "&content=" + searchBarForm.getText() + "&");
        return "charge/payable";
    }

    @RequestMapping(value = "/{chargeId}", method = RequestMethod.POST)
    public String pay(@PathVariable long chargeId,
                      Model model, Principal principal) {
        logger.debug("chargeId: {}", chargeId);
        Charge charge = chargeService.find(chargeId);
        charge.setStatus(ChargeStatus.PAID.getValue());
        chargeService.save(charge);
        return "redirect:/charge";
    }

    @RequestMapping(value = "/{chargeId}", method = RequestMethod.POST, params = "delete")
    public String delete(@RequestHeader(value = "referer", required = false) final String referer,
                         @PathVariable Long chargeId) {
        logger.debug("chargeId: {}", chargeId);
        chargeService.removeCharge(chargeId);
        return "redirect:" + referer;
    }

    @RequestMapping(value = "/order/{orderId}", method = RequestMethod.GET)
    public String order(@PathVariable long orderId, Model model) {
        logger.debug("orderId: {}", orderId);

        Iterable<ServiceSubtype> serviceSubtypes = serviceSubtypeService.findEnabled();
        Iterable<Charge> charges = chargeService.findByOrderId(orderId);
        model.addAttribute("charges", charges);

        model.addAttribute("chargeWays", ChargeWay.values());
        model.addAttribute("serviceSubtypes", serviceSubtypes);

        Earning earning = receivableSummaryService.calculateEarning(orderId);
        model.addAttribute("earning", earning);
        return "charge/order_detail";
    }

    @RequestMapping(value = "/{chargeId}/settle", method = RequestMethod.GET)
    public String initSettlement(@PathVariable long chargeId,
                                 @RequestHeader(value = "referer") String referer,
                                 Model model, Principal principal) {
        logger.debug("chargeId: {}", chargeId);
        logger.debug("referer: {}", referer);

        Charge charge = chargeService.find(chargeId);
        Iterable<ServiceSubtype> serviceSubtypes = serviceSubtypeService.findEnabled();
        model.addAttribute("charge", charge);
        model.addAttribute("chargeWays", ChargeWay.values());
        model.addAttribute("serviceSubtypes", serviceSubtypes);
        model.addAttribute("redirectTo", referer);

        return "charge/settlement_detail";
    }

    @RequestMapping(value = "/{chargeId}/settle", method = RequestMethod.POST)
    public String settle(@PathVariable long chargeId,
                         @RequestParam("redirectTo") String redirectTo,
                         Model model, Principal principal) {
        logger.debug("chargeId: {}", chargeId);
        logger.debug("redirectTo: {}", redirectTo);

        Charge charge = chargeService.find(chargeId);
        charge.setStatus(ChargeStatus.PAID.getValue());
        chargeService.save(charge);

        return "redirect:" + redirectTo;
    }

    @RequestMapping(value = "/exchange_rate", method = RequestMethod.GET)
    public String getExchangeRate(Model model) {
        ExchangeRate exchangeRate = exchangeRateService.getExchangeRate();
        Iterable<ExchangeRate> exchangeRateList = exchangeRateService.findAll();

        model.addAttribute("exchangeRateForm", exchangeRate);
        model.addAttribute("exchangeRateList", exchangeRateList);
        return "charge/exchange_rate";
    }

    @RequestMapping(value = "/exchange_rate", method = RequestMethod.POST)
    public String setExchangeRate(@ModelAttribute("exchangeRateForm") ExchangeRate exchangeRate, RedirectAttributes redirectAttributes) {
        logger.debug("exchangeRate: {}", exchangeRate);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        exchangeRate.setMonth(calendar.getTime());

        exchangeRateService.save(exchangeRate);
        redirectAttributes.addFlashAttribute("alert", new Alert("save.success"));

        return "redirect:/charge/exchange_rate";
    }
}
