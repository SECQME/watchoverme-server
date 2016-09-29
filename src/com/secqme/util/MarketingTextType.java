package com.secqme.util;

/**
 * User: James Khoo
 * Date: 3/31/14
 * Time: 11:15 AM
 */
public enum MarketingTextType {

    ACTIVATE_TRIAL_PACKAGE("activateTrialPackage"),
    PREMIUM_PACKAGE("premiumPackages");
    private String marketingTextCode;

    MarketingTextType(String marktetingTxtCode) {
        this.marketingTextCode = marktetingTxtCode;
    }

    public String getMarketingTextCode() {
        return marketingTextCode;
    }

    public void setMarketingTextCode(String marketingTextCode) {
        this.marketingTextCode = marketingTextCode;
    }
}
