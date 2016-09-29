package com.secqme.domain.model.notification.sns;

import com.secqme.domain.model.notification.MessageType;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by edward on 30/07/2015.
 */
@Entity
@Table(name="snsLogs")
public class SNSLogVO {
    @Id
    private Long id;
    private String snsName;
    private String snsUid;
    private String snsMessageId;
    private String userid;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public SNSLogVO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSnsName() {
        return snsName;
    }

    public void setSnsName(String snsName) {
        this.snsName = snsName;
    }

    public String getSnsUid() {
        return snsUid;
    }

    public void setSnsUid(String snsUid) {
        this.snsUid = snsUid;
    }

    public String getSnsMessageId() {
        return snsMessageId;
    }

    public void setSnsMessageId(String snsMessageId) {
        this.snsMessageId = snsMessageId;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
