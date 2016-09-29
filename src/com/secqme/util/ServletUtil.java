package com.secqme.util;

import java.net.URLDecoder;
import java.util.Enumeration;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 *
 * @author jameskhoo
 */
public class ServletUtil {

    private static String BASE_SERVER_PATH = null;
    private static String SNS_AUTH_REDIRECT_URL = null;
    private static final String ACCT_MGMT_PAGE = "/core_new/accountMgmt.jsf";
    private static final String BILLING_MGMT_PAGE = "/core_new/billingMgmt.jsf";
    private static final String AUTH_PAYMENT_PARAM = "?action=authpayment";
    private static final String PAYMENT_CANCEL_PARAM = "?action=cancelpayment";
    private static final String PROMOTION_BASE_PAGE = "/promotion.jsf?promotecode=";
    private static final String GIFT_PAGE = "/wom/gift.jsf";
    private static final String GIFT_RESULT_PAGE = "/wom/gift_result.jsf";
    private static Logger myLog = Logger.getLogger(ServletUtil.class);

    public static String getCookieValue(HttpServletRequest req, String cookieName) {
        String cookieValue = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie c : cookies) {
                if (c.getName().equals(cookieName)) {
                    cookieValue = c.getValue();
                }
            }
        }
        return cookieValue;
    }

    public static void printRequestInfo(HttpServletRequest req) {
        myLog.debug("CallbackURL->" + req.getContextPath() + ", "
                + req.getRequestURI() + ", "
                + req.getPathInfo() + ", "
                + req.getServerName() + ", " + req.getServerPort());
        myLog.debug("ReqParam->" + getHttpRequstAsJsonString(req));
    }

    public static String getHttpRequstAsJsonString(HttpServletRequest req) {
        StringBuffer strBuf = new StringBuffer();
        Enumeration en = req.getParameterNames();
        String paramName = null;
        strBuf.append("{");
        while (en.hasMoreElements()) {
            paramName = (String) en.nextElement();
            strBuf.append("\"" + paramName + "\"");
            strBuf.append(":\"");
            strBuf.append(URLDecoder.decode(req.getParameter(paramName)));
            strBuf.append("\",");
        }
        // remove the last ","
        strBuf.deleteCharAt(strBuf.length() - 1);
        strBuf.append("}");
        return strBuf.toString();
    }

    public static String getBaseServerPath(HttpServletRequest req) {
        if (BASE_SERVER_PATH == null) {
            if (req.getServerPort() == 443 || req.getServerPort() == 8443) {
                BASE_SERVER_PATH = "https://" + req.getServerName()
                        + req.getContextPath();
            } else if (req.getServerPort() == 9999) {
                BASE_SERVER_PATH = "https://" + req.getServerName() + ":" + 9999
                        + req.getContextPath();
            } else {
                BASE_SERVER_PATH = "http://" + req.getServerName()
                        + req.getContextPath();
            }
        }
        return BASE_SERVER_PATH;

    }

    public static String getSNSAuthFinishRedirectURL(HttpServletRequest req) {
        return getBaseServerPath(req) + ACCT_MGMT_PAGE;
    }

    public static String getAuthPaymentRedirectURL(HttpServletRequest req) {
        return getBaseServerPath(req) + BILLING_MGMT_PAGE + AUTH_PAYMENT_PARAM;
    }

    public static String getPaymentCancelRedirectURL(HttpServletRequest req) {
        return getBaseServerPath(req) + BILLING_MGMT_PAGE + PAYMENT_CANCEL_PARAM;
    }
    
    public static String getAuthGiftPaymentRedirectURL(HttpServletRequest req) {
        return getBaseServerPath(req) + GIFT_RESULT_PAGE + AUTH_PAYMENT_PARAM;
    }

    public static String getGiftPaymentCancelRedirectURL(HttpServletRequest req) {
        return getBaseServerPath(req) + GIFT_PAGE + PAYMENT_CANCEL_PARAM;
    }
    
    public static String getPromotionCancelRedirectURL(HttpServletRequest req, String promoteCode) {
        return getBaseServerPath(req) + PROMOTION_BASE_PAGE + promoteCode;
    }
    
    public static String getPromotionCompletedRedirectURL(HttpServletRequest req, String promotoCode, String userid){
        return getBaseServerPath(req) + PROMOTION_BASE_PAGE + promotoCode + "&userid="  + userid + "&promotiontaken=true"; 
    }
}
