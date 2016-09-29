package com.secqme.domain.model.billing;

/**
 *
 * @author coolboykl
 */
public enum BillingLogType {
    PROMO_CODE,
    PROVISION,          // For new user activation, provision of new Billing Cycle
    RENEW,              // When user purchase a new billing Package, resulted in renew of billing Cycle
                        // Link to purchase History
    TRANSACTION,        // For event Registration, SMS Credit Used.. 
    CANCEL,             // When doing refund..-- JK May16 Not used at this moment
    REFERRAL,           // Referral
    ADDSMSCREDIT;		// When SMS credits are added to current bill cycle
}
