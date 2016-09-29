package com.secqme.web.jsf.util;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;

/**
 *
 * @author jameskhoo
 */
public class FacesUtils {
    private final static Logger myLog = Logger.getLogger(FacesUtils.class);

    public static String getMessageResourceString(
            FacesContext context,
            String key,
            Object params[]) {

        String text = null;
        String bundleName = context.getApplication().getMessageBundle();
        Locale locale = context.getViewRoot().getLocale();

        ResourceBundle bundle =
                ResourceBundle.getBundle(bundleName, locale);

        try {
            text = bundle.getString(key);
        } catch (MissingResourceException ex) {
            myLog.error(ex.getMessage(), ex);
            text = "?? key " + key + " not found ??";
        }

        if (params != null) {
            MessageFormat mf = new MessageFormat(text, locale);
            text = mf.format(params, new StringBuffer(), null).toString();
        }
        System.out.println("bundle->" + bundle + ", locale:" + locale + ", key->" + key + ", text->" + text);
        return text;
    }
}
