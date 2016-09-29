package com.secqme.domain.model.notification.push;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.MessageType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edward on 22/05/2015.
 */
public abstract class PushMessageVO {

    public MessageType messageType;
    public UserVO sender;
    public List<PushMessageRecipientVO> recipients;
    public Map<String, String> payloads;

    public PushMessageVO(MessageType messageType, UserVO sender, PushMessageRecipientVO recipient) {
        this(messageType, sender, Arrays.asList(recipient));
    }

    public PushMessageVO(MessageType messageType, UserVO sender, List<PushMessageRecipientVO> recipients) {
        this.messageType = messageType;
        this.sender = sender;
        this.recipients = recipients;
        this.payloads = new HashMap<>();
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public UserVO getSender() {
        return sender;
    }

    public void setSender(UserVO sender) {
        this.sender = sender;
    }

    public List<PushMessageRecipientVO> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<PushMessageRecipientVO> recipients) {
        this.recipients = recipients;
    }

    public Map<String, String> getPayloads() {
        return this.payloads;
    }

    public void setPayloads(Map<String, String> payloads) {
        this.payloads = payloads;
    }

    public void clearPayloads() {
        payloads.clear();
    }

    public void putPayload(String key, String value) {
        payloads.put(key, value);
    }

    public Object removePayload(String key) {
        return payloads.remove(key);
    }
}
