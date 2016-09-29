package com.secqme.web.jsf.mbean;

import com.secqme.manager.UserManager;
import com.secqme.web.jsf.util.NavigationRules;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * Created by edward on 23/04/2015.
 */
public class UnsubscribeNewsletterBean implements Serializable {

    private static final Logger myLog = Logger.getLogger(UnsubscribeNewsletterBean.class);

    private String subscribeToken;

    private String recipientEmail;

    private UserManager userManager;

    public String getSubscribeToken() {
        return subscribeToken;
    }

    public void setSubscribeToken(String subscribeToken) {
        this.subscribeToken = subscribeToken;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public String processUnsubscribe() {
        try {
            userManager.unsubscribeMarketingEmail(recipientEmail, subscribeToken);
            return NavigationRules.UNSUBSCRIBE_EMAIL_SUCCESS.getNaviPath();
        } catch (Exception ex) {
            myLog.error("Failed to unsubscribe user", ex);
            return NavigationRules.ERROR.getNaviPath();
        }
    }
}
