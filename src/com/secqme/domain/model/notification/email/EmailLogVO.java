package com.secqme.domain.model.notification.email;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.MessageType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by edward on 07/05/2015.
 */
@Entity
@Table(name="emailLogs")
@NamedQueries({
        @NamedQuery(name = EmailLogVO.QUERY_FIND_BY_PROVIDER_AND_MESSAGE_ID,
                query = "SELECT o "
                        + "FROM EmailLogVO o "
                        + "WHERE o.emailServiceProvider = :provider "
                        + "AND o.emailServiceMessageId = :messageId")
})
public class EmailLogVO implements Serializable {
    public static final String QUERY_FIND_BY_PROVIDER_AND_MESSAGE_ID = "EmailLogVO.findByProviderAndMessageId";

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    private String emailServiceProvider;
    private String emailServiceMessageId;

    @ManyToOne
    @JoinColumn(name = "requesterUserId")
    private UserVO requesterUser;

    @ManyToOne
    @JoinColumn(name = "recipientUserId")
    private UserVO recipientUser;

    private String recipientName;
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    private EmailStatus status;

    private String city;
    private String state;
    private String country;

    private String failedReason;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    private Long campaignId;
    private Long eventId;
    private Long contactId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getEmailServiceProvider() {
        return emailServiceProvider;
    }

    public void setEmailServiceProvider(String emailServiceName) {
        this.emailServiceProvider = emailServiceName;
    }

    public String getEmailServiceMessageId() {
        return emailServiceMessageId;
    }

    public void setEmailServiceMessageId(String emailServiceMessageId) {
        this.emailServiceMessageId = emailServiceMessageId;
    }

    public UserVO getRequesterUser() {
        return requesterUser;
    }

    public void setRequesterUser(UserVO requesterUser) {
        this.requesterUser = requesterUser;
    }

    public UserVO getRecipientUser() {
        return recipientUser;
    }

    public void setRecipientUser(UserVO recipientUser) {
        this.recipientUser = recipientUser;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public EmailStatus getStatus() {
        return status;
    }

    public void setStatus(EmailStatus status) {
        this.status = status;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String region) {
        this.state = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    @Override
    public String toString() {
        return "EmailLogVO{" +
                "id=" + id +
                ", messageType=" + messageType +
                ", emailServiceProvider='" + emailServiceProvider + '\'' +
                ", emailServiceMessageId='" + emailServiceMessageId + '\'' +
                ", recipientName='" + recipientName + '\'' +
                ", recipientEmail='" + recipientEmail + '\'' +
                '}';
    }
}
