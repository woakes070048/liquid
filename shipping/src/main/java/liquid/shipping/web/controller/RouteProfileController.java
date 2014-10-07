package liquid.shipping.web.controller;

import liquid.persistence.domain.LocationEntity;
import liquid.shipping.persistence.domain.LegEntity;
import liquid.shipping.persistence.domain.RouteEntity;
import liquid.shipping.persistence.repository.RouteRepository;
import liquid.shipping.web.domain.Leg;
import liquid.shipping.web.domain.RouteProfile;
import liquid.shipping.web.domain.TransMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mat on 10/6/14.
 */
@Controller
@RequestMapping("/route_profile")
public class RouteProfileController {
    @Autowired
    private RouteRepository routeRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model) {
        Iterable<RouteEntity> routes = routeRepository.findAll();
        model.addAttribute("routes", routes);
        return "route_profile/list";
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String initForm(Model model) {
        RouteEntity route = new RouteEntity();
        route = routeRepository.save(route);
        List<Leg> legs = new ArrayList<>();
        for (int i = 0; i < 5; i++) legs.add(new Leg());
        model.addAttribute("route", new RouteProfile());
        model.addAttribute("legs", legs);
        return "route_profile/form";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String initForm(@PathVariable Long id, Model model) {
        RouteEntity route = routeRepository.findOne(id);

        Leg leg = new Leg();

        if (route.getLegs().size() == 0) {
            // First Line
            leg.setHead(Boolean.TRUE);
        } else {
            // Other Lines
            leg.setPrevId(route.getLegs().get(route.getLegs().size() - 1).getId());
        }

        model.addAttribute("transportOptions", TransMode.values());
        model.addAttribute("route", route);
        model.addAttribute("leg", leg);
        return "route_profile/form";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String addLeg(@PathVariable Long id, Leg leg) {
        RouteEntity route = routeRepository.findOne(id);
        LegEntity entity = new LegEntity();

        entity.setRoute(route);
        entity.setHead(leg.getHead());
        entity.setTransMode(leg.getTransMode());
        entity.setSrcLoc(LocationEntity.newInstance(LocationEntity.class, leg.getSourceId()));
        entity.setDstLoc(LocationEntity.newInstance(LocationEntity.class, leg.getDestinationId()));
        if (null != leg.getPrevId())
            entity.setPrev(LegEntity.newInstance(LegEntity.class, leg.getPrevId()));

        route.getLegs().add(entity);
        routeRepository.save(route);
        return "redirect:/route_profile/" + id;
    }

    @RequestMapping(value = "/{id}/leg/{legId}", method = RequestMethod.POST)
    public String updateLeg(@PathVariable Long id, Model model) {
        RouteEntity route = routeRepository.findOne(id);
        model.addAttribute("route", route);
        model.addAttribute("leg", new LegEntity());
        return "redirect:route_profile/" + id;
    }
}
