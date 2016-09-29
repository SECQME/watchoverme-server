package com.secqme.domain.model.notification.sms;

import com.secqme.domain.model.notification.MessageType;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by edward on 15/05/2015.
 */
public abstract class SMSVO implements Serializable {

    private static Logger myLog = Logger.getLogger(SMSVO.class);

    protected MessageType messageType;
    protected List<SMSRecipientVO> recipients;

    public SMSVO(MessageType messageType, List<SMSRecipientVO> recipients) {
        this.messageType = messageType;
        this.recipients = recipients;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public List<SMSRecipientVO> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<SMSRecipientVO> recipients) {
        this.recipients = recipients;
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(writer, this);
        } catch (IOException e) {
            myLog.error("Failed to serialize " + this.getClass().getName());
        }
        return writer.toString();
    }
}
