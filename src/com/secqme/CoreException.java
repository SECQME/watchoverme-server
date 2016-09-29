package com.secqme;

import com.secqme.util.spring.DefaultSpringUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author jameskhoo
 */
public class CoreException extends RuntimeException {

    private ErrorType errorType;
    private String langCode;
    private static final String DEFAULT_LANG_CODE = "en_US";
    /**
     */
    private Object[] errorMessageParameters;


    public CoreException(ErrorType errType, String langCode, Object... errMsgParameters) {
        this(errType, langCode, null, errMsgParameters);
    }

    public CoreException(ErrorType errType, String langCode, Throwable cause, Object[] errMsgParameters) {

        // TODO, lookup the i18n for English local mconnectedcore_message.properties
        // and format the message accordingly;
        super(cause);
        this.errorType = errType;
        this.errorMessageParameters = errMsgParameters == null ? null : errMsgParameters.clone();
        this.langCode = langCode == null ? DEFAULT_LANG_CODE : langCode;
    }


    public CoreException(ErrorType errType, String langCode) {
        super();
        this.errorType = errType;
        this.langCode = langCode == null ? DEFAULT_LANG_CODE : langCode;
    }

    public String getMessage() {
        String msg = null;
        if (errorMessageParameters != null) {
            msg = DefaultSpringUtil.getInstance().getMessage(errorType.getErrorCode(), langCode, errorMessageParameters);
        } else {
            msg = DefaultSpringUtil.getInstance().getMessage(errorType.getErrorCode(), langCode);
        }

        return msg;
    }

    public JSONObject getMessageAsJSON() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("errorCode", this.errorType.getErrorCode());
            jobj.put("errorMessage", this.getMessage());
        } catch (JSONException je) {
        }

        return jobj;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public Object[] getErrorMessageParameters() {
        return errorMessageParameters == null ? null : errorMessageParameters.clone();
    }

    public void setErrorMessageParameters(Object[] errMsgParameters) {
        this.errorMessageParameters = errMsgParameters == null ? null : errMsgParameters.clone();
    }
}
