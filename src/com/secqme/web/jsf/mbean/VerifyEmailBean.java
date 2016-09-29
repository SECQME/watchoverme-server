package com.secqme.web.jsf.mbean;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.manager.UserManager;
import org.apache.log4j.Logger;

import javax.ws.rs.NotFoundException;

/**
 * Created by edward on 01/09/2015.
 */
public class VerifyEmailBean {

    private static final Logger myLog = Logger.getLogger(VerifyEmailBean.class);

    private UserManager userManager = null;
    private String emailVerificationToken = null;

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    public void verifyEmail() {
        myLog.debug("verifyEmail - " + emailVerificationToken);

        try {
            userManager.verifyEmailAddress(emailVerificationToken);
        } catch (CoreException ex) {
            if (ex.getErrorType() == ErrorType.USER_BY_EMAIL_VERIFICATION_TOKEN_NOT_FOUND_ERROR) {
                throw new NotFoundException(ex.getMessage());
            } else if (ex.getErrorType() == ErrorType.USER_EMAIL_ALREADY_VERIFIED_ERROR) {
                // Ignore
            } else {
                myLog.debug(ex.getMessage(), ex);
                throw ex;
            }
        }
    }
}
