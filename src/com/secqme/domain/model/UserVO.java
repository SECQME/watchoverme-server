package com.secqme.domain.model;

import com.secqme.domain.converter.JsonMapConverter;
import com.secqme.domain.model.billing.BillingPkgType;
import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.billing.MarketVO;
import com.secqme.domain.model.event.SavedEventVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.payment.UserPaymentInfoVO;
import com.secqme.domain.model.payment.UserSubscriptionInfoVO;
import com.secqme.domain.model.pushmessage.UserPushMessageToken;
import com.secqme.domain.model.safezone.SafeZoneVO;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.eclipse.persistence.annotations.PrivateOwned;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * UserVO
 *
 * @author jameskhoo
 */
@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = UserVO.QUERY_FIND_BY_ACTIVATIONCODE,
                query = "SELECT o " +
                        "FROM UserVO o " +
                        "WHERE o.activationCode = :activateCode"),
        @NamedQuery(name = UserVO.QUERY_FIND_BY_EMAIL,
                query = "SELECT o " +
                        "FROM UserVO o " +
                        "WHERE o.emailAddress = :email " +
                        "ORDER BY o.createdDate DESC"),
        @NamedQuery(name = UserVO.QUERY_FIND_ALL_USERS,
                query = "SELECT o " +
                        "FROM UserVO o WHERE o.mobileNo IS NOT NULL"),
        @NamedQuery(name = UserVO.QUERY_FIND_BY_MOBILE_COUNTRY_NUMER,
                query = "SELECT  o " +
                        "FROM UserVO o " +
                        "WHERE o.mobileNo = :mobileNumber " +
                        "AND o.mobileCountry.iso = :mobileCountryISO " +
                        "ORDER BY o.createdDate DESC"),
        @NamedQuery(name=UserVO.QUERY_FIND_BY_USER_ID,
                query = "SELECT o " +
                        "FROM UserVO o "+
                        "WHERE o.userid = :userid"),
        @NamedQuery(name = UserVO.QUERY_FIND_ACTIVE_USER_BY_DEVICE_AND_TIMEZONE,
                query = "SELECT o " +
                        "FROM UserVO o " +
                        "WHERE o.device LIKE :device " +
                        "AND o.updatedDate >= :updatedDate " +
                        "AND o.timeZone LIKE :timeZone " +
                        "AND o.emailAddress IS NOT NULL " +
                        "AND o.mobileNo IS NOT NULL"),
        @NamedQuery(name = UserVO.QUERY_FIND_BY_PASSWORD_RESET_TOKEN,
                query = "SELECT o " +
                        "FROM UserVO o " +
                        "WHERE o.passwordResetToken = :passwordResetToken"),
        @NamedQuery(name = UserVO.QUERY_FIND_BY_EMAIL_VERIFICATION_TOKEN,
                query = "SELECT o " +
                        "FROM UserVO o " +
                        "WHERE o.emailVerificationToken = :emailVerificationToken"),
})
public class UserVO implements Serializable {

    public static final String QUERY_FIND_BY_ACTIVATIONCODE = "userVO.findByActivationCode";
    public static final String QUERY_FIND_BY_EMAIL = "userVO.findByEmail";
    public static final String QUERY_FIND_BY_MOBILE_COUNTRY_NUMER =  "userVO.findByMobileCountryMobileNumber";
    public static final String QUERY_FIND_BY_USER_ID = "userVO.findByUserId";
    public static final String QUERY_FIND_ALL_USERS = "userVO.findAllUsers";
    public static final String QUERY_FIND_ACTIVE_USER_BY_DEVICE_AND_TIMEZONE = "userVO.findActiveUserByCountryDateDevice";
    public static final String QUERY_FIND_BY_PASSWORD_RESET_TOKEN = "userVO.findByPasswordResetToken";
    public static final String QUERY_FIND_BY_EMAIL_VERIFICATION_TOKEN = "userVO.findByEmailVerificationToken";
    public static final String USER_ID_KEY = "userid";
    public static final String MOBILE_NO_KEY = "mobileNumber";
    public static final String MOBILE_COUNTRY_KEY = "mobileCountry";
    public static final String USER_NICK_NAME_KEY = "nickName";
    
    private static final String USER_DEFAULT_LANGUAGE = "en_US";

    private static final Logger myLog = Logger.getLogger(UserVO.class);

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    // By default, all userid is base on email address
    // For userid base on mobile number, is a combination of
    // Mobile Country Code-With the given mobile number;
    @Id
    @Deprecated
    private String userid;

    @Enumerated(EnumType.STRING)
    @Deprecated
    private UserIdType userIdType;

    private String emailAddress;
    private boolean subscribeEmailNewsLetter;
    private String subscribeEmailNewsLetterToken;
    private String password;
    private String nickName;
    private String mobileNo;

    private Boolean activated;
    @ManyToOne
    @JoinColumn(name = "billingPkg")
    private BillingPkgVO packageVO;

    private Integer currentBalance;
    @ManyToOne
    @JoinColumn(name = "countryISO")
    private CountryVO countryVO;
    
    @ManyToOne
    @JoinColumn(name= "market")
    private MarketVO marketVO;

    private String timeZone;

    @OneToMany(mappedBy = "userVO",
    fetch = FetchType.EAGER,
    cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @PrivateOwned
    private List<ContactVO> contactList;
    
    @OneToMany(mappedBy = "userVO",
    fetch = FetchType.EAGER,
    cascade = {CascadeType.ALL})
    @PrivateOwned
    private List<UserPushMessageToken> pushMessageTokenList;

    @OneToMany(mappedBy = "userVO",
            fetch = FetchType.EAGER,
            cascade = {CascadeType.ALL})
    @PrivateOwned
    private List<SavedEventVO> savedEventVOList = new ArrayList<SavedEventVO>();

    @OneToMany(mappedBy = "createdByUserVO",
            fetch = FetchType.EAGER,
            cascade = {CascadeType.ALL})
    @PrivateOwned
    private List<SafeZoneVO> userCreatedSafeZoneList = new ArrayList<SafeZoneVO>();
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginDate;
    private String activationCode;

    @OneToMany(mappedBy = "userVO",
              fetch = FetchType.EAGER,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @PrivateOwned
    private List<UserSNSConfigVO> snsConfigList;
    
    @OneToMany(mappedBy = "userVO",
              fetch = FetchType.EAGER,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @PrivateOwned
    private List<UserPaymentInfoVO> userPaymentInfoList;

    @OneToOne(mappedBy = "userVO",
              fetch = FetchType.EAGER,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @PrivateOwned
    private UserSubscriptionInfoVO userCurrentSubscription;
    

    private String device;   // User's mobile device, e.g iPhone or Android
    private String langCode; // Added LanguageCode

    private String promoteCode;
    @Temporal(TemporalType.TIMESTAMP)
    private Date promoteSignUpDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date trialPackageActivatedAt;


    @ManyToOne
    @JoinColumn(name = "mobileCountry")
    private CountryVO mobileCountry;

    private String referralURL;
    private String referralShortURL;

    private String passwordResetPin;
    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordResetPinDate;

    private String passwordResetToken;
    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordResetTokenDate;

    private String emailVerificationToken;
    @Temporal(TemporalType.TIMESTAMP)
    private Date emailVerificationDate;

    private double clientVersion;
    private double latitude;
    private double longitude;
    private String profilePictureURL;

    @Column
    @Convert(converter = JsonMapConverter.class)
    private Map<String, String> analyticsIds;

    // Temporary: Walky user mark
    private boolean walky;
    
    public UserVO() {
        userIdType = UserIdType.EMAIL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CountryVO getCountryVO() {
        return countryVO;
    }

    public void setCountryVO(CountryVO countryVO) {
        this.countryVO = countryVO;
    }

    public Date getCreatedDate() {
        return this.createdDate == null ? null : new Date(this.createdDate.getTime());
    }

    public void setCreatedDate(Date newDate) {
        this.createdDate = newDate == null ? null : new Date(newDate.getTime());
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public boolean isSubscribeEmailNewsLetter() {
        return subscribeEmailNewsLetter;
    }

    public void setSubscribeEmailNewsLetter(boolean subscribeEmailNewsLetter) {
        this.subscribeEmailNewsLetter = subscribeEmailNewsLetter;
    }

    public String getSubscribeEmailNewsLetterToken() {
        return subscribeEmailNewsLetterToken;
    }

    public void setSubscribeEmailNewsLetterToken(String subscribeEmailNewsLetterToken) {
        this.subscribeEmailNewsLetterToken = subscribeEmailNewsLetterToken;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public List<UserPaymentInfoVO> getUserPaymentInfoList() {
        return userPaymentInfoList;
    }

    public void setUserPaymentInfoList(List<UserPaymentInfoVO> userPaymentInfoList) {
        this.userPaymentInfoList = userPaymentInfoList;
    }

    public UserSubscriptionInfoVO getUserCurrentSubscription() {
        return userCurrentSubscription;
    }

    public void setUserCurrentSubscription(UserSubscriptionInfoVO userSubscriptionVO) {
        this.userCurrentSubscription= userSubscriptionVO;
    }


    public int getNumberofContacts() {
        int  noUserContacts = 0;
        if (contactList != null && contactList.size() > 0) {
             noUserContacts = contactList.size();
        }
        return  noUserContacts;
    }

    public void setSnsConfigList(List<UserSNSConfigVO> snsConfigList) {
        this.snsConfigList = snsConfigList;
    }


    public List<UserSNSConfigVO> getSnsConfigList() {
        return snsConfigList;
    }

    public List<SavedEventVO> getSavedEventVOList() {
        return savedEventVOList;
    }

    public void setSavedEventVOList(List<SavedEventVO> savedEventVOList) {
        this.savedEventVOList = savedEventVOList;
    }

    @Deprecated
    public UserIdType getUserIdType() {
        return userIdType;
    }

    @Deprecated
    public void setUserIdType(UserIdType userIdType) {
        this.userIdType = userIdType;
    }

    public String getAliasName() {
        return nickName;
    }

    public Date getUpdatedDate() {
        return this.updatedDate == null ? null : new Date(this.updatedDate.getTime());
    }

    public void setUpdatedDate(Date newDate) {
        this.updatedDate = newDate == null ? null : new Date(newDate.getTime());
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public Date getLastLoginDate() {
        return this.lastLoginDate == null ? null : new Date(this.lastLoginDate.getTime());
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate == null ? null : new Date(lastLoginDate.getTime());
    }

    public MarketVO getMarketVO() {
        return marketVO;
    }

    public void setMarketVO(MarketVO marketVO) {
        this.marketVO = marketVO;
    }

    @Deprecated
    public String getUserid() {
        return userid;
    }

    @Deprecated
    public void setUserid(String userid) {
        this.userid = userid;
    }

    public List<ContactVO> getContactList() {
        return contactList;
    }

    public void setContactList(List<ContactVO> contactList) {
        this.contactList = contactList;
    }

    public Boolean isActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Integer getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(Integer currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BillingPkgVO getPackageVO() {
        return packageVO;
    }

    public void setPackageVO(BillingPkgVO packageVO) {
        this.packageVO = packageVO;
        if(BillingPkgType.PREMIUM.equals(this.packageVO.getPkgType())) {
            this.setTrialPackageActivatedAt(new Date());
        }
    }

    public String getLangCode() {
        return langCode == null? USER_DEFAULT_LANGUAGE : langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }
    

    public Boolean getActivated() {
        return activated;
    }

    public String getPromoteCode() {
        return promoteCode;
    }

    public void setPromoteCode(String promoteCode) {
        this.promoteCode = promoteCode;
    }

    public Date getPromoteSignUpDate() {
        return promoteSignUpDate;
    }

    public void setPromoteSignUpDate(Date promoteSignUpDate) {
        this.promoteSignUpDate = promoteSignUpDate;
    }

    public List<SafeZoneVO> getUserCreatedSafeZoneList() {
        return userCreatedSafeZoneList;
    }

    public void setUserCreatedSafeZoneList(List<SafeZoneVO> userCreatedSafeZoneList) {
        this.userCreatedSafeZoneList = userCreatedSafeZoneList;
    }

    public Date getTrialPackageActivatedAt() {
        return trialPackageActivatedAt;
    }

    public void setTrialPackageActivatedAt(Date trialPackageActivatedAt) {
        this.trialPackageActivatedAt = trialPackageActivatedAt;
    }

    public CountryVO getMobileCountry() {
        return mobileCountry;
    }

    public void setMobileCountry(CountryVO mobileCountry) {
        this.mobileCountry = mobileCountry;
    }

    public String getReferralURL() {
        return referralURL;
    }

    public void setReferralURL(String referralURL) {
        this.referralURL = referralURL;
    }

    public String getReferralShortURL() {
        return referralShortURL;
    }

    public void setReferralShortURL(String referralShortURL) {
        this.referralShortURL = referralShortURL;
    }

    public String getPasswordResetPin() {
        return passwordResetPin;
    }

    public void setPasswordResetPin(String passwordResetPin) {
        this.passwordResetPin = passwordResetPin;
    }

    public Date getPasswordResetPinDate() {
        return passwordResetPinDate;
    }

    public void setPasswordResetPinDate(Date passwordResetPinDate) {
        this.passwordResetPinDate = passwordResetPinDate;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public Date getPasswordResetTokenDate() {
        return passwordResetTokenDate;
    }

    public void setPasswordResetTokenDate(Date passwordResetTokenDate) {
        this.passwordResetTokenDate = passwordResetTokenDate;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailActivationToken) {
        this.emailVerificationToken = emailActivationToken;
    }

    public Date getEmailVerificationDate() {
        return emailVerificationDate;
    }

    public boolean isEmailVerified() {
        return emailAddress != null && emailVerificationDate != null;
    }

    public void setEmailVerificationDate(Date emailActivationDate) {
        this.emailVerificationDate = emailActivationDate;
    }

    public double getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(double clientVersion) {
		this.clientVersion = clientVersion;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getProfilePictureURL() {
		return profilePictureURL;
	}

	public void setProfilePictureURL(String profilePictureURL) {
		this.profilePictureURL = profilePictureURL;
	}

    public Map<String, String> getAnalyticsIds() {
        return analyticsIds;
    }

    public void setAnalyticsIds(Map<String, String> mixpanelIds) {
        this.analyticsIds = mixpanelIds;
    }

    public boolean isWalky() {
        return walky;
    }

    public void setWalky(boolean walky) {
        this.walky = walky;
    }

    public JSONObject getJSONObject() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put(USER_ID_KEY, this.getUserid());
            jobj.put(MOBILE_NO_KEY, this.getMobileNo());
            if (this.getContactList() != null && this.getContactList().size() > 0) {
                JSONArray jArray = new JSONArray();
                for (ContactVO contactVO : this.getContactList()) {
                    jArray.put(contactVO.getJSONObject());
                }
            }
        } catch (JSONException ex) {
            myLog.error("Failed to create JSONObject from: " + this, ex);
        }

        return jobj;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final UserVO other = (UserVO) obj;
        if ((this.userid == null) ? (other.userid != null) : !this.userid.equals(other.userid)) {
            return false;
        }
        if ((this.password == null) ? (other.password != null) : !this.password.equals(other.password)) {
            return false;
        }
        if ((this.nickName == null) ? (other.nickName != null) : !this.nickName.equals(other.nickName)) {
            return false;
        }
        if ((this.mobileNo == null) ? (other.mobileNo != null) : !this.mobileNo.equals(other.mobileNo)) {
            return false;
        }
        if (this.activated != other.activated && (this.activated == null || !this.activated.equals(other.activated))) {
            return false;
        }
        if (this.packageVO != other.packageVO && (this.packageVO == null || !this.packageVO.equals(other.packageVO))) {
            return false;
        }
        if (this.currentBalance != other.currentBalance && (this.currentBalance == null || !this.currentBalance.equals(other.currentBalance))) {
            return false;
        }
        if (this.countryVO != other.countryVO && (this.countryVO == null || !this.countryVO.equals(other.countryVO))) {
            return false;
        }
        if (this.marketVO != other.marketVO && (this.marketVO == null || !this.marketVO.equals(other.marketVO))) {
            return false;
        }
        if ((this.timeZone == null) ? (other.timeZone != null) : !this.timeZone.equals(other.timeZone)) {
            return false;
        }
        if (this.createdDate != other.createdDate && (this.createdDate == null || !this.createdDate.equals(other.createdDate))) {
            return false;
        }
        if (this.updatedDate != other.updatedDate && (this.updatedDate == null || !this.updatedDate.equals(other.updatedDate))) {
            return false;
        }
        if (this.lastLoginDate != other.lastLoginDate && (this.lastLoginDate == null || !this.lastLoginDate.equals(other.lastLoginDate))) {
            return false;
        }
        if ((this.activationCode == null) ? (other.activationCode != null) : !this.activationCode.equals(other.activationCode)) {
            return false;
        }
        if ((this.device == null) ? (other.device != null) : !this.device.equals(other.device)) {
            return false;
        }
        if ((this.analyticsIds == null) ? (other.analyticsIds != null) : !this.analyticsIds.equals(other.analyticsIds)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.userid != null ? this.userid.hashCode() : 0);
        hash = 11 * hash + (this.password != null ? this.password.hashCode() : 0);
        hash = 11 * hash + (this.nickName != null ? this.nickName.hashCode() : 0);
        hash = 11 * hash + (this.mobileNo != null ? this.mobileNo.hashCode() : 0);
        hash = 11 * hash + (this.activated != null ? this.activated.hashCode() : 0);
        hash = 11 * hash + (this.packageVO != null ? this.packageVO.hashCode() : 0);
        hash = 11 * hash + (this.currentBalance != null ? this.currentBalance.hashCode() : 0);
        hash = 11 * hash + (this.countryVO != null ? this.countryVO.hashCode() : 0);
        hash = 11 * hash + (this.marketVO != null ? this.marketVO.hashCode() : 0);
        hash = 11 * hash + (this.timeZone != null ? this.timeZone.hashCode() : 0);
        hash = 11 * hash + (this.createdDate != null ? this.createdDate.hashCode() : 0);
        hash = 11 * hash + (this.updatedDate != null ? this.updatedDate.hashCode() : 0);
        hash = 11 * hash + (this.lastLoginDate != null ? this.lastLoginDate.hashCode() : 0);
        hash = 11 * hash + (this.activationCode != null ? this.activationCode.hashCode() : 0);
        hash = 11 * hash + (this.device != null ? this.device.hashCode() : 0);
        hash = 11 * hash + (this.analyticsIds != null ? this.analyticsIds.hashCode() : 0);
        return hash;
    }

    public List<UserPushMessageToken> getPushMessageTokenList() {
        return pushMessageTokenList;
    }

    public void setPushMessageTokenList(List<UserPushMessageToken> pushMessageTokenList) {
        this.pushMessageTokenList = pushMessageTokenList;
    }

    @Override
    public String toString() {
        StringBuffer userStrBuffer = new StringBuffer();
        userStrBuffer.append(new ToStringBuilder(this).append(this.userid).append(this.mobileNo) //.append(this.countryVO.getCountryName())
                .toString());

        if (this.getContactList() != null && this.getContactList().size() > 0) {
            userStrBuffer.append(", Contacts:[");
            for (ContactVO contactVO : this.getContactList()) {
                userStrBuffer.append(contactVO.getMobileNo());
            }
            userStrBuffer.append(']');
        }

        return userStrBuffer.toString();
    }
}
