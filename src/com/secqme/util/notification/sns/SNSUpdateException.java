package com.secqme.util.notification.sns;

import com.secqme.util.notification.sms.*;

/**
 *
 * @author jameskhoo
 */
public class SNSUpdateException extends Exception {

    public SNSUpdateException(String msg, Exception e) {
        super(msg, e);
    }

    public SNSUpdateException(String msg) {
        super(msg);
    }

}
