package com.secqme.domain.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @author james
 */
@Entity
@Table(name = "contacts")
@NamedQueries({
        @NamedQuery(name = ContactVO.QUERY_FIND_BY_ACCEPTED_CONTACT_EMAIL_OR_MOBILE_NO,
                query = "SELECT o " +
                        "FROM ContactVO o " +
                        "WHERE (" +
                            "(o.countryVO.iso = :iso AND o.mobileNo = :mobileNo) " +
                            "OR (o.emailAddress = :emailAddress)" +
                        ") " +
                        "AND o.status IN (:status1, :status2) "),
        @NamedQuery(name = ContactVO.QUERY_FIND_BY_CONTACT_EMAIL_OR_MOBILE_NO,
                query = "SELECT o " +
                        "FROM ContactVO o " +
                        "WHERE " +
                        "(o.countryVO.iso = :iso AND o.mobileNo = :mobileNo) " +
                        "OR (o.emailAddress = :emailAddress)"),
        @NamedQuery(name = ContactVO.QUERY_FIND_BY_CONTACT_MOBILE_NO,
                query = "SELECT o " +
                        "FROM ContactVO o " +
                        "WHERE o.countryVO.iso = :iso " +
                        "AND o.mobileNo = :mobileNo " +
                        "AND (o.status = :status1 " +
                        "OR o.status = :status2) "),
        @NamedQuery(name = ContactVO.QUERY_FIND_BY_CONTACT_EMAIL_ADDRESS,
                query = "SELECT o " +
                        "FROM ContactVO o " +
                        "WHERE o.emailAddress = :emailAddress " +
                        "AND (o.status = :status1 " +
                        "OR o.status = :status2) "),
        @NamedQuery(name = ContactVO.QUERY_FIND_CONTACT_NEED_TO_BE_REMINDED,
                query = "SELECT o " +
                        "FROM ContactVO o " +
                        "WHERE o.status = com.secqme.domain.model.ContactInvitationStatus.INVITED " +
                        "AND o.reminderSent = FALSE " +
                        "AND o.createdAt <= :beforeDate"),
        @NamedQuery(name = ContactVO.QUERY_FIND_ALL_CONTACT,
                query = "SELECT o " +
                        "FROM ContactVO o " +
                        "WHERE o.countryVO IS NOT NULL "),
        @NamedQuery(name = ContactVO.QUERY_FIND_BY_CONTACT_TOKEN,
                query = "SELECT o " +
                        "FROM ContactVO o " +
                        "WHERE o.contactToken = :rejectCode ")
})
public class ContactVO implements Serializable, Comparable<ContactVO> {

    public static final String QUERY_FIND_BY_ACCEPTED_CONTACT_EMAIL_OR_MOBILE_NO = "ContactVO.findByAcceptedContactEmailAndMobileNo";
    public static final String QUERY_FIND_BY_CONTACT_EMAIL_OR_MOBILE_NO = "ContactVO.findByContactEmailAndMobileNo";
    public static final String QUERY_FIND_BY_CONTACT_MOBILE_NO = "ContactVO.findByContactMobileNo";
    public static final String QUERY_FIND_BY_CONTACT_EMAIL_ADDRESS = "ContactVO.findByContactEmailAddress";
    public static final String QUERY_FIND_CONTACT_NEED_TO_BE_REMINDED = "ContactVO.findContactNeedToBeReminded";
    public static final String QUERY_FIND_ALL_CONTACT = "ContactVO.findAllContacts";
    public static final String QUERY_FIND_BY_CONTACT_TOKEN = "ContactVO.findByContactToken";

    public static final String MOBILE_NO_KEY = "mobileNo";
    public static final String CONTACT_ID_KEY = "contactId";
    public static final String EMAIL_ADDR_KEY = "emailAddr";
    public static final String LAST_NAME_KEY = "lastName";
    public static final String FIRST_NAME_KEY = "firstName";
    public static final String NICK_NAME_KEY = "nickName";
    public static final String MOBILE_COUNTRY_KEY = "mobileCountry";
    public static final String MOBILE_COUNTRY_CODE_KEY = "mobileCountryCode";
    public static final String SMS_EMERGENCY_KEY = "smsNotifyEmergency";
    public static final String EMAIL_EMERGENCY_KEY = "emailNotifyEmergency";
    public static final String SMS_SAFETY_KEY = "smsNotifySafety";
    public static final String EMAIL_SAFETY_KEY = "emailNotifySafety";
    public static final String RELATIONSHIP_KEY = "relationship";

    private static final Logger myLog = Logger.getLogger(ContactVO.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mobileNo;
    private String emailAddress;
    private String lastName;
    private String firstName;
    private String nickName;
    private Boolean notifyEmail;
    private Boolean notifySMS;
    private Boolean safetyNotifySMS;
    private Boolean safetyNotifyEmail;

    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    @OneToOne
    @JoinColumn(name = "countryISO")
    private CountryVO countryVO;
    private String relationship;
    @ManyToOne
    @JoinColumn(name = "contactUserId")
    private UserVO contactUserVO;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Enumerated(EnumType.STRING)
    private ContactInvitationStatus status;

    private boolean reminderSent;

    private String contactToken;

    public ContactVO() {
        notifyEmail = true;
        notifySMS = true;
        safetyNotifyEmail = true;
        safetyNotifySMS = true;

        contactToken = UUID.randomUUID().toString();

        reminderSent = false;
        createdAt = new Date();
        updatedAt = new Date();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCallingCode() {
        String callingCode = null;
        if (countryVO != null) {
            callingCode = countryVO.getCallingCode();
        }

        return callingCode;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public CountryVO getCountryVO() {
        return countryVO;
    }

    public void setCountryVO(CountryVO countryVO) {
        this.countryVO = countryVO;
    }

    public Boolean getSafetyNotifyEmail() {
        return safetyNotifyEmail == null ? false : safetyNotifyEmail;
    }

    public void setSafetyNotifyEmail(Boolean safetyNotifyEmail) {
        this.safetyNotifyEmail = safetyNotifyEmail;
    }

    public Boolean getSafetyNotifySMS() {
        return safetyNotifySMS == null ? false : safetyNotifySMS;
    }

    public void setSafetyNotifySMS(Boolean safetyNotifySMS) {
        this.safetyNotifySMS = safetyNotifySMS;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Boolean getNotifyEmail() {
        return notifyEmail == null ? false : notifyEmail;
    }

    public void setNotifyEmail(Boolean notifyEmail) {
        this.notifyEmail = notifyEmail;
    }

    public Boolean getNotifySMS() {
        return notifySMS == null ? false : notifySMS;
    }

    public void setNotifySMS(Boolean notifySMS) {
        this.notifySMS = notifySMS;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }

    public UserVO getContactUserVO() {
        return contactUserVO;
    }

    public void setContactUserVO(UserVO contactUserVO) {
        this.contactUserVO = contactUserVO;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updateDate) {
        this.updatedAt = updateDate;
    }

    public ContactInvitationStatus getStatus() {
        return status;
    }

    public void setStatus(ContactInvitationStatus status) {
        this.status = status;
    }

    public String getContactToken() {
        return contactToken;
    }

    public void setContactToken(String rejectCode) {
        this.contactToken = rejectCode;
    }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getAliasName() {
        String aliasName = null;
        if (nickName != null && StringUtils.isNotEmpty(nickName)) {
            aliasName = nickName;
        } else if (lastName != null && StringUtils.isNotEmpty(lastName)) {
            aliasName = lastName;
            if (firstName != null && StringUtils.isNotEmpty(firstName)) {
                aliasName = lastName + " " + firstName;
            }
        }
        return aliasName;
    }

    public JSONObject getJSONObject() {
        try {
            JSONObject contactJson = new JSONObject();

            contactJson.put(CONTACT_ID_KEY, this.getId());
            contactJson.put(NICK_NAME_KEY, this.getNickName());
            if (this.getCountryVO() != null) {
                contactJson.put(MOBILE_COUNTRY_KEY, this.getCountryVO().getIso());
                contactJson.put(MOBILE_COUNTRY_CODE_KEY, this.getCountryVO().getCallingCode());
                contactJson.put(MOBILE_NO_KEY, this.getMobileNo());
            }
            contactJson.put(EMAIL_ADDR_KEY, this.getEmailAddress());
            contactJson.put(RELATIONSHIP_KEY, this.getRelationship());

            return contactJson;
        } catch (JSONException ex) {
            myLog.error("Failed to create JSONObject from: " + this, ex);
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ContactVO otherVO = (ContactVO) obj;
        return new EqualsBuilder().append(this.id, otherVO.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 11).append(this.id).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("mobileNo", mobileNo)
                .append("emailAddress", emailAddress)
                .append("nickName", nickName)
                .append("mobileCountryISO", countryVO == null ? null : countryVO.getIso())
                .append("reminderSent", reminderSent)
                .toString();
    }

    @Override
    public int compareTo(ContactVO aContactVO) {
        return this.nickName.compareToIgnoreCase(aContactVO.nickName);
    }
}
