package com.secqme.web.jsf.util;

/**
 *
 * @author jameskhoo
 */
public enum NavigationRules {

    REGISTRATION_SUCCESS("registrationSuccess"),
    REGISTRATION_FAIL("registrationFail"),
    RESET_PASSWORD_SUCCESS("resetPasswordSuccess"),
    RESET_PASSWORD_INVALID("invalidToken"),
    LOGIN_SUCCESS("loginSuccess"),
    LOGOUT("logout"),
    LOGIN_SYSTEM_ADMIN("systemAdminLogin"),
    LOGIN_FAILURE("failLogin"),
    PURCHASE_VALIDATION_SUCCESS("purchaseValidationSuccess"),
    PURCHASE_VALIDATION_FAILED("purchaseValidationFailed"),
    UNSUBSCRIBE_EMAIL_SUCCESS("processUnsubscribeSuccess"),
    REJECT_EMERGENCY_CONTACT_SUCCESS("successRemoveEmergencyContact"),
    EMERGENCY_CONTACT_INVALID("invalidToken"),
    ERROR("error");
    
    private String naviPath;

    NavigationRules(String naviPath) {
        this.naviPath = naviPath;
    }

    public String getNaviPath() {
        return naviPath;
    }

}
