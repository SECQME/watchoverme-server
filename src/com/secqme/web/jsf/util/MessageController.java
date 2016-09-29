package com.secqme.web.jsf.util;

import com.secqme.CoreException;
import com.secqme.util.spring.DefaultSpringUtil;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 *
 * @author jameskhoo
 */
public class MessageController {

    private static void addMessage(FacesMessage.Severity msgSeverity, UIComponent component, String summary, String detailMsg) {
        if (component != null) {
            FacesContext.getCurrentInstance().addMessage(component.getClientId(FacesContext.getCurrentInstance()),
                    new FacesMessage(msgSeverity,
                    summary, detailMsg));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(msgSeverity,
                    summary, detailMsg));
        }
    }

    public static void addInfowithMsgCode(UIComponent component, String messageCode, Object[] msgParameters) {
        String msg = msgParameters == null ?
                     DefaultSpringUtil.getInstance().getMessage(messageCode) :
                     DefaultSpringUtil.getInstance().getMessage(messageCode, msgParameters);
        addInfo(component, messageCode, msg);

    }

    public static void addErrorwithMsgCode(UIComponent component, String messageCode, Object[] msgParameters) {
        String msg = msgParameters == null ?
                     DefaultSpringUtil.getInstance().getMessage(messageCode) :
                     DefaultSpringUtil.getInstance().getMessage(messageCode, msgParameters);
        addError(component, messageCode, msg);
        
    }

    public static void addInfo(UIComponent component, String summaryMessage, String detailMsg) {
        addMessage(FacesMessage.SEVERITY_INFO, component, summaryMessage, detailMsg);
    }

    public static void addWarn(UIComponent component, String summaryMessage, String detailMsg) {
        addMessage(FacesMessage.SEVERITY_WARN, component, summaryMessage, detailMsg);
    }

    public static void addError(UIComponent component, String summaryMessage, String detailMsg) {
        addMessage(FacesMessage.SEVERITY_ERROR, component, summaryMessage, detailMsg);
    }

    public static void addFatal(UIComponent component, String summaryMessage, String detailMsg) {
        addMessage(FacesMessage.SEVERITY_FATAL, component, summaryMessage, detailMsg);
    }

    public static void addCoreExceptionError(UIComponent component, CoreException coreException) {
        addError(component, coreException.getErrorType().getErrorCode(), coreException.getMessage());
    }
}
