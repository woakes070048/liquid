package liquid.controller;

import liquid.facade.BookingFacade;
import liquid.order.persistence.domain.OrderEntity;
import liquid.persistence.domain.ServiceProviderEntity;
import liquid.service.ServiceProviderService;
import liquid.shipping.domain.Booking;
import liquid.shipping.domain.TaskDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

/**
 * Created by redbrick9 on 8/16/14.
 */
@Controller
@RequestMapping("/task/{taskId}/booking")
public class BookingController extends BaseTaskController {
    @Autowired
    private BookingFacade bookingFacade;

    @Autowired
    private ServiceProviderService serviceProviderService;

    @RequestMapping(method = RequestMethod.GET)
    public String booking(@PathVariable String taskId, @RequestParam(value = "alert_key", required = false) String alertKey, Model model) {
        TaskDto task = taskService.getTask(taskId);
        model.addAttribute("task", task);
        OrderEntity order = taskService.findOrderByTaskId(taskId);

        Booking booking = bookingFacade.computeBooking(order.getId());
        model.addAttribute("booking", booking);

        Iterable<ServiceProviderEntity> shipowners = serviceProviderService.findByType(3);
        model.addAttribute("shipowners", shipowners);

        if (null != alertKey)
            model.addAttribute("alert", messageSource.getMessage(alertKey, new String[]{}, Locale.CHINA));
        return "booking/form";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String booking(@PathVariable String taskId, Booking booking) {
        OrderEntity order = taskService.findOrderByTaskId(taskId);
        bookingFacade.save(order.getId(), booking);
        return "redirect:/task/" + taskId + " /booking?alert_key=save.success";
    }
}