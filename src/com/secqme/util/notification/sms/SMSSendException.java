package com.secqme.util.notification.sms;

/**
 *
 * @author jameskhoo
 */
public class SMSSendException extends Exception {
    public SMSSendException(String msg, Exception e) {
        super(msg, e);
    }

    public SMSSendException(String msg) {
        super(msg);
    }
}
