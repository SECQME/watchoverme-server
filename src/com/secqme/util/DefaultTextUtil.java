package com.secqme.util;

import com.secqme.CoreException;
import com.secqme.domain.dao.MarketingTextConfigDAO;
import com.secqme.domain.model.util.MarketingTextConfigVO;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author james
 */
public class DefaultTextUtil implements TextUtil {
    private Logger myLog  = Logger.getLogger(DefaultTextUtil.class);

    private static final String DEFAULT_RESOURCE_BUNDLE = "lang.secqme_message";
    public static Pattern emailPattern = null;
    private final Map<Locale, ResourceBundle> bundleHashMap;
    private final Locale defaultLocale;
    HashMap<String, MarketingTextConfigVO> marketingTextHashMap = null;
    private MarketingTextConfigDAO marketingTextConfigDAO = null;


    public DefaultTextUtil(MarketingTextConfigDAO textConfigDAO) {
        bundleHashMap = new HashMap<Locale, ResourceBundle>();
        defaultLocale = Locale.ENGLISH;
        emailPattern = Pattern.compile(".+@.+\\.[a-z]+");
        this.marketingTextConfigDAO = textConfigDAO;
        initMarketingTextHashMap();

    }

    private static int randomInt(int min, int max) {
        return (int) (Math.random() * (max - min) + min);
    }

    @Override
    public String getLocalizedMarketingText(MarketingTextType marketingTextType, String langCode, JSONObject parameters) {

        String marketingText = null;
        MarketingTextConfigVO textConfigVO = marketingTextHashMap.get(marketingTextType.getMarketingTextCode() + "_" + langCode);
        try {
            if (textConfigVO != null) {
                marketingText = textConfigVO.getMarketingText();
                if (parameters != null) {
                    Iterator<String> paramKeys = parameters.keys();
                    while (paramKeys.hasNext()) {
                        String key = paramKeys.next();
                        marketingText = marketingText.replaceAll(key, parameters.getString(key));
                    }
                }
            }
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }

        return marketingText;
    }

    public String getLocalizedErrorMessage(CoreException ce, Locale locale) {
        ResourceBundle bundle = bundleHashMap.get(locale);
        String errorMsg;
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(DEFAULT_RESOURCE_BUNDLE, locale);
            bundleHashMap.put(locale, bundle);
        }

        if (ce.getErrorMessageParameters() == null) {
            errorMsg = bundle.getString(ce.getErrorType().getErrorCode());
            errorMsg = MessageFormat.format(
                    bundle.getString(ce.getErrorType().getErrorCode()),
                    (Object[]) ce.getErrorMessageParameters());
        } else {
            errorMsg = MessageFormat.format(
                    bundle.getString(ce.getErrorType().getErrorCode()),
                    (Object[]) ce.getErrorMessageParameters());
        }

        return errorMsg;

    }

    public String getLocalizedErrorMessage(CoreException ce) {
        return this.getLocalizedErrorMessage(ce, defaultLocale);
    }

    public String generateRandomString(int minLen, int maxLen) {
        int num = randomInt(minLen, maxLen);
        return RandomStringUtils.randomAlphanumeric(num);
    }

    public boolean isValidEmailAddress(String emailAddr) {
        Matcher m = emailPattern.matcher(emailAddr);
        return m.matches();

    }

    public boolean msgContainsUnicode(String msg) {
        boolean containsUnicode = false;
        byte[] msgBytes = msg.getBytes();
        for (byte b : msgBytes) {
            if (b < 0) {
                containsUnicode = true;
                break;
            }
        }

        return containsUnicode;

    }

    public String convertToHex(String aStr) {
        char[] chars = aStr.toCharArray();
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            output.append(StringUtils.leftPad(Integer.toHexString((int) chars[i]), 4, "0").toUpperCase());
        }
        return output.toString();
    }

    private void initMarketingTextHashMap() {
        marketingTextHashMap = new HashMap<String, MarketingTextConfigVO>();
        List<MarketingTextConfigVO> marketingTextConfigList = marketingTextConfigDAO.findAll();
        if (marketingTextConfigList != null)
            for (MarketingTextConfigVO textConfigVO : marketingTextConfigList) {
                marketingTextHashMap.put(textConfigVO.getCode() + "_" + textConfigVO.getLangCode(), textConfigVO);
            }
    }

}
