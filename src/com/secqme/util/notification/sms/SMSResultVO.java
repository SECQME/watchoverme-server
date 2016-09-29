package com.secqme.util.notification.sms;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: coolboykl
 * Date: 4/25/13
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class SMSResultVO implements Serializable {
    private String transactionId;
    private String sendStatus;
    private String errorMessage;
    private Boolean smsSendOut;

    public SMSResultVO() {
        // Empty Constructor
    }



    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean getSmsSendOut() {
        return smsSendOut;
    }

    public void setSmsSendOut(Boolean smsSendOut) {
        this.smsSendOut = smsSendOut;
    }
}
