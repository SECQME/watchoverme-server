package com.secqme.domain.model.notification.sms;

import com.secqme.domain.model.notification.MessageType;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author jameskhoo
 */
@Entity
@Table(name="smsLogs")
@NamedQueries({
    @NamedQuery(name = SMSLogVO.QUERY_FIND_SMS_LOGS_USER_BETWEEN_DATE,
    query =   "SELECT s "
            + "FROM SMSLogVO s "
            + "WHERE s.eventId IN ("
               + "SELECT o.id "
               + "FROM SecqMeEventVO o "
               + "WHERE o.userVO.userid = :userid "
               + "AND o.startTime >= :startTime "
               + "AND o.startTime <= :endTime )"
               + "ORDER BY s.sendTime DESC")
})
public class SMSLogVO implements Serializable {
        
    public static final String QUERY_FIND_SMS_LOGS_USER_BETWEEN_DATE = "query.smsLogUserBetweenDate";

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String gwName;
    private String mobileNo;
    private String message;
    @Temporal(TemporalType.TIMESTAMP)
    private Date sendTime;
    private String transactionId;
    private String status;

    private Integer credit;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    private String errorMessage;

    private boolean sendOut = false;

    private Long eventId;
    private Long contactId;

    public SMSLogVO() {
        // Empty Constructor
    }

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    public Date getSendTime() {
        return sendTime == null ? null : new Date(sendTime.getTime());
    }

    public void setSendTime(Date sndTime) {
        this.sendTime = sndTime == null? null : new Date(sndTime.getTime());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSendOut() {
        return sendOut;
    }

    public void setSendOut(boolean sendOut) {
        this.sendOut = sendOut;
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
}
