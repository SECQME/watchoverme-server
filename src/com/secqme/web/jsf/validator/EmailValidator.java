package com.secqme.web.jsf.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author jameskhoo
 */
public class EmailValidator implements Validator {
     
    public static Pattern emailPattern  = null;

    public EmailValidator() {
        emailPattern = Pattern.compile(".+@.+\\.[a-z]+");
    }

    public void validate(FacesContext facesContext, UIComponent uIComponent, Object object)
            throws ValidatorException {
        String enteredEmail = (String) object;
        Matcher m = emailPattern.matcher(enteredEmail);
        boolean matchFound = m.matches();

        if (!matchFound) {
            FacesMessage message = new FacesMessage();
            message.setSummary("Invalid Email ID.");
            throw new ValidatorException(message);
        }
    }
}
