package liquid.process.controller;

import liquid.process.handler.DefinitionKey;
import liquid.process.model.RailContainerListForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Created by Tao Ma on 4/20/15.
 */
@Controller
public class LoadByTruckController extends AbstractTruckController {
    @RequestMapping(method = RequestMethod.POST, params = "definitionKey=" + DefinitionKey.loadByTruck)
    public String save(@PathVariable String taskId, RailContainerListForm railContainerListForm,
                       Model model, RedirectAttributes redirectAttributes) {
        return super.save(taskId, railContainerListForm, model, redirectAttributes);
    }
}
