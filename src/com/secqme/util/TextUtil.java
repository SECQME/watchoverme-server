package com.secqme.util;

import com.secqme.CoreException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Locale;

/**
 *
 * @author james
 */
public interface TextUtil {

    public String generateRandomString(int minLength, int maxLength);
    public String getLocalizedErrorMessage(CoreException ce, Locale locale);
    public String getLocalizedErrorMessage(CoreException ce);
    public boolean isValidEmailAddress(String emailAddr);
    public boolean msgContainsUnicode(String msg);
    public String convertToHex(String aStr);
    public String getLocalizedMarketingText(MarketingTextType marketingTextType, String langCode, JSONObject parameters);
}
