package com.secqme.domain.model.notification.email;

import com.secqme.domain.model.notification.MessageType;
import com.secqme.util.SystemProperties;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward on 08/05/2015.
 */
public abstract class EmailVO implements Serializable {

    private static Logger myLog = Logger.getLogger(EmailVO.class);

    protected String senderName;
    protected String senderEmail;
    protected String replyToEmail;
    protected MessageType messageType;
    protected List<EmailRecipientVO> recipients;
    protected List<String> tags;

    public EmailVO(MessageType messageType, List<EmailRecipientVO> recipients) {
        this.messageType = messageType;
        this.recipients = recipients;

        SystemProperties systemProperties = SystemProperties.instance;
        this.senderName = systemProperties.getString(SystemProperties.PROP_FROM_NAME);
        this.senderEmail = systemProperties.getString(SystemProperties.PROP_FROM_EMAIL);
        this.replyToEmail = systemProperties.getString(SystemProperties.PROP_REPLY_TO_EMAIL);
        this.tags = new ArrayList<>();
        this.tags.add(messageType.toString());
    }

    public EmailVO(String senderName, String senderEmail, String replyToEmail, MessageType messageType, List<EmailRecipientVO> recipients) {
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.replyToEmail = replyToEmail;
        this.messageType = messageType;
        this.recipients = recipients;
        this.tags = new ArrayList<>();
        this.tags.add(messageType.toString());
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReplyToEmail() {
        return replyToEmail;
    }

    public void setReplyToEmail(String replyToEmail) {
        this.replyToEmail = replyToEmail;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public List<EmailRecipientVO> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<EmailRecipientVO> recipients) {
        this.recipients = recipients;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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
