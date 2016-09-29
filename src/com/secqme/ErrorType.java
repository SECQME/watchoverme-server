package com.secqme;

/**
 * @author james
 */
public enum ErrorType {
    // Common error type
    HTTP_ERROR_400("error.400", 400),
    HTTP_ERROR_404("error.404", 404),
    HTTP_ERROR_405("error.405", 405),
    HTTP_ERROR_415("error.415", 415),
    HTTP_ERROR_500("error.500", 500),

    // Generic
    UNSUPPORTED_OPERATION_ERROR("unsupported.operation", 422),

    // Client Application Error
    CLIENT_APPLICATION_AUTHENTICATION_ERROR("client.application.authentication.error", 401),

    // User Related Error
    USER_BYID_NOT_FOUND_ERROR("user.not.found.by.id", 200, 404),
    USER_BYPHONE_NOT_FOUND_ERROR("user.not.found.by.phone", 200, 404),
    USER_BYEMAIL_NOT_FOUND_ERROR("user.not.found.by.email", 200, 404),
    USER_BY_ACVI_CODE_NOT_FOUND_ERROR("user.not.found.by.activation.code", 200, 401),
    USER_BY_PASSWORD_RESET_TOKEN_NOT_FOUND_ERROR("user.not.found.by.password.reset.token", 401),
    USER_BY_EMAIL_VERIFICATION_TOKEN_NOT_FOUND_ERROR("user.not.found.by.email.verification.token", 401),
    USER_PASSWORD_MISMATCH_ERROR("user.password.mismatch.error", 200, 401),
    USER_EMAIL_AND_TOKEN_MISMATCH_ERROR("user.email.and.token.mismatch.error", 200, 401),
    USER_ID_ALREADY_EXITS_ERROR("user.id.already.exists.error", 200, 422),
    USER_EMAIL_ALREADY_EXISTS_ERROR("user.email.already.exists.error", 200, 422),
    USER_EMAIL_ALREADY_VERIFIED_ERROR("user.email.already.verified", 422),
    USER_MOBILE_NO_ALREADY_EXITS_ERROR("user.phone.already.exists.error", 200, 422),
    USER_EMAIL_ADDRESS_IS_EMPTY_ERROR("user.email.address.is.empty", 200, 422),
    USER_COUNTRY_NOT_SUPPORTED_ERROR("user.country.not.supporter.error", 200, 422),
    USER_WRONG_RESET_PASSWORD_PIN("user.wrong.password.reset.pin", 200, 401),
    RESET_PASSWORD_REQUEST_EXPIRE_ERROR("user.reset.password.pin.request.expire", 200, 401),
    USER_RESET_PASSWORD_MISMATCH_ERROR("user.password.reset.pin.mismatch.error", 200, 401),
    USER_NO_EMAIL_ERROR("user.no.email", 422),
    USER_SNS_NOT_FOUND("user.sns.not.found", 404),

    // CONTACT Related Error
    MAX_CONTACT_ALLOWS_ERROR("max.sms.contact.allow.error", 200, 422),
    USER_MOBILE_NUMBER_INVALID("user.mobile.number.invalid", 200, 422),
    USER_EMAIL_ADDRESS_INVALID("user.email.address.invalid", 200, 422),
    USER_CONTACT_MOBILE_NUMBER_INVALID("user.contact.mobile.number.invalid", 200, 422),
    USER_CONTACT_EXIST_ERROR("user.contact.exist", 200, 422),

    //Event Related Error
    MOBILE_NUMBER_MISMATCH("mobile.number.mismatch", 200, 422),
    CURRENT_PASSWORD_MISMATCH("user.change.password.mismatch", 200, 422),
    REQUIRE_FIELD_MISSING_ERROR("missing.require.field.error", 200, 400),
//    EVENT_NOT_FOUND_FOR_USER("event.not.found.for.user"),
    EVENT_NOT_FOUND_FOR_THIS_TRACKING_PIN("event.not.found.tracking.pin", 200, 404),
    EVENT_NOT_FOUND_BY_EVENT_ID("event.not.found.event.id", 200, 404),
    TRACKING_LOG_NOT_FOUND_ERROR("tracking.log.not.found.error", 200, 404),
    EVENT_ARCHIEVED_ERROR("event.archived.error", 200, 422),
    QUICK_EVENT_NOT_FOUND_ERROR("quick.event.not.found.error", 200, 404),

    //  Payment and Billing Error
    BILLING_SUBSCRPTION_EXPIRED_ERROR("billing.subscription.expired.error", 200, 422),
    BILLING_INSUFFICIENT_EVENT_CREDIT_ERROR("billing.eventcredit.not.enough.error", 200, 422),
    PAYMENT_CHARGE_SUBSCRIPTION_ERROR("payment.charge.subscription.error", 200, 422),
    USER_HAS_ACTIVATED_TRIAL_BEFORE_ERROR("user.activated.trial.package.before.error", 200, 422),
    USER_ALREADY_HAS_SUBSCRIPTION_ERROR("user.already.has.subscription.error", 200, 422),
    USER_GIFT_MOBILE_NUMBER_INVALID("user.gift.mobile.number.invalid", 200, 422),

    // Promo Code Error
    PROMO_CODE_INVALID("promocode.invalid", 422),
    
    // Promotion and Referral Error
    PROMOTION_NOT_FOUND("promotion.not.found.error", 200, 404),
    REFERRAL_MISSING_MOBILE_COUNTRY("referral.missing.mobile.country", 200, 400),

    // SafeZone related Error
    DUPLICATIE_SAFEZONE_NAME_ERROR("duplicate.safezone.name.error", 200, 422),

    // Contact related error
    CONTACT_NOT_FOUND("contact.not.found", 404),
    CONTACT_MODIFY_PERMISSION_DENIED("contact.modify.permission.denied", 403);

    String errorCode;
    int httpStatusCodeLegacy;
    int httpStatusCode;

    ErrorType(String errorCode) {
        this(errorCode, 200, 200);
    }

    ErrorType(String errorCode, int httpStatusCode) {
        this(errorCode, httpStatusCode, httpStatusCode);
    }

    ErrorType(String errorCode, int httpStatusCodeLegacy, int httpStatusCode) {
        this.errorCode = errorCode;
        this.httpStatusCodeLegacy = httpStatusCodeLegacy;
        this.httpStatusCode = httpStatusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public int getHttpStatusCodeLegacy() {
        return httpStatusCodeLegacy;
    }

    public void setHttpStatusCodeLegacy(int httpStatusCodeLegacy) {
        this.httpStatusCodeLegacy = httpStatusCodeLegacy;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
}
