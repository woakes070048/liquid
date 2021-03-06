package liquid.core.controller;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * User: tao
 * Date: 10/16/13
 * Time: 9:24 PM
 */
public abstract class BaseController {
    protected static final int size = 25;

    public void addFieldError(BindingResult result, String objectName, String fieldName, Object rejectedValue,
                              Object... arguments) {
        FieldError filedError = new FieldError(objectName, fieldName, rejectedValue, false,
                new String[]{objectName + "." + fieldName}, new String[]{String.valueOf(rejectedValue)}, "");
        result.addError(filedError);
    }
}
