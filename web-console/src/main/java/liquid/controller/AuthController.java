package liquid.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Collection;

/**
 *  
 * User: tao
 * Date: 9/22/13
 * Time: 3:13 PM
 */
@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @RequestMapping("/")
    public String auth(Model model, Principal principal) {
        logger.debug("Principal: {}", principal);
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;

        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            logger.debug("GrantedAuthority: {}", authority);
            if ("ROLE_SALES".equals(authority.getAuthority())) {
                return "redirect:/task";
            }
            if ("ROLE_MARKETING".equals(authority.getAuthority().trim())) {
                return "redirect:/task";
            }
        }

        return "redirect:/task";
    }

    @RequestMapping("/login")
    public void signin(Model model, Principal principal, @RequestParam(required = false) String error) {
        logger.debug("error: '{}'", error);
    }
}
