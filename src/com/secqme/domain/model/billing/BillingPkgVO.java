package com.secqme.domain.model.billing;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author james
 */
@Entity
@Table(name = "billingPackages")
@NamedQueries({
    @NamedQuery(name = BillingPkgVO.QUERY_FIND_ALL,
    query = "SELECT o "
    + "FROM BillingPkgVO o")
})
public class BillingPkgVO implements Serializable {

    public final static String QUERY_FIND_ALL = "billingPkg.findAll";
    public final static String PACKAGE_NAME_KEY = "packageName";
    public final static String ENABLE_EVENT_REG_WITH_VOICE_KEY = "enableEventRegWithVoice";
    public final static String ENABLE_EVENT_REG_WITH_PICTURE_KEY = "enableEventRegWithPicture";
    public final static String ENABLE_EMERGENCY_WITH_VOICE_KEY = "enableEmergencyWithVoice";
    public final static String ENABLE_EMERGENCY_WITH_SHAKE_KEY = "enableEmergencyWithShake";
    public static final String ENABLE_EMERGENCY_WITH_SHAKE_AND_VIDEO_KEY = "enableEmergencyWithShakeVideo";
    public static final String MAX_EVENT_HISTORY_KEY = "maxEventHistory";
    public static final String MAX_EVENT_DURATION_KEY = "maxEventDuration";
    public final static String ACCESS_RIGHT = "accessRight";

    private static final Logger myLog = Logger.getLogger(BillingPkgVO.class);

    @Id
    private String pkgName;
    private String pkgDesc;
    @Enumerated(EnumType.STRING)
    private BillingPkgType pkgType;
    private Boolean renewable;
    private Integer eventRegCredit;
    private Integer trialPeriod; // Billing Trial Period in days
    private Integer smsCredit;
    @Column(name= "maxSMSContactAllow")
    private Integer maxContactAllow;
    private Boolean defaultPkg;
    private Boolean enableEventRegWithVoice;
    private Boolean enableEventRegWithPicture;
    private Boolean enableEmergencyWithVoice;
    private Boolean enableEmergencyWithShake;

    // JK Feb-11 updated as at Feb14
    private Boolean enableEmergencyWithShakeAndVideo;
    private Integer maxEventHistory;
    private Integer maxEventDuration;



    public BillingPkgVO() {
        // Empty Constructor
    }

    public Integer getTrialPeriod() {
        return trialPeriod;
    }

    public void setTrialPeriod(Integer trialPeriod) {
        this.trialPeriod = trialPeriod;
    }

    public Integer getEventRegCredit() {
        return eventRegCredit;
    }

    public void setEventRegCredit(Integer eventRegCredit) {
        this.eventRegCredit = eventRegCredit;
    }

    public String getPkgDesc() {
        return pkgDesc;
    }

    public void setPkgDesc(String pkgDesc) {
        this.pkgDesc = pkgDesc;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public Boolean isDefaultPkg() {
        return defaultPkg;
    }

    public void setDefaultPkg(Boolean defaultPkg) {
        this.defaultPkg = defaultPkg;
    }



    public Integer getMaxSMSContactAllow() {
        return maxContactAllow;
    }

    public void setMaxSMSContactAllow(Integer maxSMSContactAllow) {
        this.maxContactAllow = maxSMSContactAllow;
    }

    public BillingPkgType getPkgType() {
        return pkgType;
    }

    public void setPkgType(BillingPkgType pkgType) {
        this.pkgType = pkgType;
    }

    public Boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(Boolean renewable) {
        this.renewable = renewable;
    }

    public Integer getSmsCredit() {
        return smsCredit;
    }

    public void setSmsCredit(Integer smsCredit) {
        this.smsCredit = smsCredit;
    }


    public Boolean getEnableEmergencyWithShake() {
        return enableEmergencyWithShake;
    }

    public void setEnableEmergencyWithShake(Boolean enableEmergencyWithShake) {
        this.enableEmergencyWithShake = enableEmergencyWithShake;
    }

    public Boolean getEnableEmergencyWithVoice() {
        return enableEmergencyWithVoice;
    }

    public void setEnableEmergencyWithVoice(Boolean enableEmergencyWithVoice) {
        this.enableEmergencyWithVoice = enableEmergencyWithVoice;
    }

    public Boolean getEnableEventRegWithPicture() {
        return enableEventRegWithPicture;
    }

    public void setEnableEventRegWithPicture(Boolean enableEventRegWithPicture) {
        this.enableEventRegWithPicture = enableEventRegWithPicture;
    }

    public Boolean getEnableEventRegWithVoice() {
        return enableEventRegWithVoice;
    }

    public void setEnableEventRegWithVoice(Boolean enableEventRegWithVoice) {
        this.enableEventRegWithVoice = enableEventRegWithVoice;
    }

    public Boolean getRenewable() {
        return renewable;
    }

    public Integer getMaxContactAllow() {
        return maxContactAllow;
    }

    public void setMaxContactAllow(Integer maxContactAllow) {
        this.maxContactAllow = maxContactAllow;
    }

    public Boolean getDefaultPkg() {
        return defaultPkg;
    }

    public Boolean getEnableEmergencyWithShakeAndVideo() {
        return enableEmergencyWithShakeAndVideo;
    }

    public void setEnableEmergencyWithShakeAndVideo(Boolean enableEmergencyWithShakeAndVideo) {
        this.enableEmergencyWithShakeAndVideo = enableEmergencyWithShakeAndVideo;
    }

    public Integer getMaxEventHistory() {
        return maxEventHistory;
    }

    public void setMaxEventHistory(Integer maxEventHistory) {
        this.maxEventHistory = maxEventHistory;
    }

    public Integer getMaxEventDuration() {
        return maxEventDuration;
    }

    public void setMaxEventDuration(Integer maxEventDuration) {
        this.maxEventDuration = maxEventDuration;
    }

    public JSONObject getAccessRightJSON() {
        JSONObject authoritiesObj = new JSONObject();
        try {
            authoritiesObj.put(ENABLE_EMERGENCY_WITH_SHAKE_KEY, this.getEnableEmergencyWithShake());
            authoritiesObj.put(ENABLE_EMERGENCY_WITH_VOICE_KEY, this.getEnableEmergencyWithVoice());
            authoritiesObj.put(ENABLE_EVENT_REG_WITH_PICTURE_KEY, this.getEnableEventRegWithPicture());
            authoritiesObj.put(ENABLE_EVENT_REG_WITH_VOICE_KEY, this.getEnableEventRegWithVoice());
            authoritiesObj.put(ENABLE_EMERGENCY_WITH_SHAKE_AND_VIDEO_KEY, this.getEnableEmergencyWithShakeAndVideo());
            authoritiesObj.put(MAX_EVENT_DURATION_KEY, this.getMaxEventDuration());
            authoritiesObj.put(MAX_EVENT_HISTORY_KEY, this.getMaxEventHistory());
        } catch (JSONException ex) {
            myLog.error("Failed to create JSONObject from: " + this, ex);
        }

        return authoritiesObj;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BillingPkgVO other = (BillingPkgVO) obj;
        if ((this.pkgName == null) ? (other.pkgName != null) : !this.pkgName.equals(other.pkgName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.pkgName != null ? this.pkgName.hashCode() : 0);
        return hash;
    }
}
