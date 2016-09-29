package com.secqme.domain.model.notification;

import com.secqme.domain.converter.JSONObjectConverter;
import com.secqme.domain.converter.JsonMapConverter;
import com.secqme.domain.model.UserVO;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author jameskhoo
 */
@Entity
@Table(name="userSnsConfigs")
@IdClass(UserSNSID.class)
@NamedQueries({
    @NamedQuery(name = UserSNSConfigVO.QUERY_FIND_BY_SNSUID_SNSNAME,
    query = "SELECT o "
    + "FROM UserSNSConfigVO o "
    + "WHERE o.snsName = :snsName "
    + "AND o.snsuid = :snsuid "
    + "ORDER BY o.updatedDate DESC ")
})
public class UserSNSConfigVO implements Serializable {
	
	public static final String QUERY_FIND_BY_SNSUID_SNSNAME = "UserSNSConfigVO.findBySnsuidAndSnsName";
	
    public static final String SNS_NAME_KEY =  "snsName";
    public static final String SNS_UID_KEY = "snsUID";
    public static final String SNS_NOTIFY_KEY = "notify";
    public static final String SNS_ADDITONAL_CONFIG_KEY = "snsAdditionalConfig";

    private static final Logger myLog = Logger.getLogger(UserSNSConfigVO.class);

    @Id
    @Column(name = "snsName", insertable = false, updatable = false)
    private String snsName;
    @Id
    @Column(name = "userid", insertable = false, updatable = false)
    private String userid;
    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    @ManyToOne
    @JoinColumn(name = "snsName")
    private SocialNetworkVO socialNetworkVO;

    private String snsuid;

    @Convert(converter = JSONObjectConverter.class)
    private JSONObject additionalConfig;

    private Boolean notify;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    public UserSNSConfigVO() {
        // Empty Constructor
    }
    
    public UserSNSConfigVO(JSONObject jobj) throws JSONException {
        this.snsName = jobj.getString(SNS_NAME_KEY);
        if(jobj.has(SNS_NOTIFY_KEY)) {
            this.notify = jobj.getBoolean(SNS_NOTIFY_KEY);
        }
        
        if(jobj.has(SNS_ADDITONAL_CONFIG_KEY)) {
            this.additionalConfig = jobj.getJSONObject(SNS_ADDITONAL_CONFIG_KEY);
        }
        
        if(jobj.has(SNS_UID_KEY)) {
            this.snsuid = jobj.getString(SNS_UID_KEY);
        }
        
    }

    public JSONObject getAdditionalConfig() {
        return additionalConfig;
    }

    public void setAdditionalConfig(JSONObject additionalConfig) {
        this.additionalConfig = additionalConfig;
    }

    public Boolean isNotify() {
        return notify;
    }

    public void setNotify(Boolean notify) {
        this.notify = notify;
    }

    public String getSnsName() {
        return snsName;
    }

    public void setSnsName(String snsName) {
        this.snsName = snsName;
    }

    public SocialNetworkVO getSocialNetworkVO() {
        return socialNetworkVO;
    }

    public void setSocialNetworkVO(SocialNetworkVO socialNetworkVO) {
        this.socialNetworkVO = socialNetworkVO;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getSnsuid() {
        return snsuid;
    }

    public void setSnsuid(String snsuid) {
        this.snsuid = snsuid;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    
    public JSONObject toJSONObj() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put(SNS_NAME_KEY, snsName);
            jobj.put(SNS_UID_KEY, snsuid);
            jobj.put(SNS_NOTIFY_KEY, notify);
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
        final UserSNSConfigVO other = (UserSNSConfigVO) obj;
        if ((this.snsName == null) ? (other.snsName != null) : !this.snsName.equals(other.snsName)) {
            return false;
        }
        if ((this.userid == null) ? (other.userid != null) : !this.userid.equals(other.userid)) {
            return false;
        }
        if (this.userVO != other.userVO && (this.userVO == null || !this.userVO.equals(other.userVO))) {
            return false;
        }
        if (this.socialNetworkVO != other.socialNetworkVO && (this.socialNetworkVO == null || !this.socialNetworkVO.equals(other.socialNetworkVO))) {
            return false;
        }
        if ((this.additionalConfig == null) ? (other.additionalConfig != null) : !this.additionalConfig.equals(other.additionalConfig)) {
            return false;
        }
        if (this.notify != other.notify && (this.notify == null || !this.notify.equals(other.notify))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.snsName != null ? this.snsName.hashCode() : 0);
        hash = 97 * hash + (this.userid != null ? this.userid.hashCode() : 0);
        hash = 97 * hash + (this.userVO != null ? this.userVO.hashCode() : 0);
        hash = 97 * hash + (this.socialNetworkVO != null ? this.socialNetworkVO.hashCode() : 0);
        hash = 97 * hash + (this.additionalConfig != null ? this.additionalConfig.hashCode() : 0);
        hash = 97 * hash + (this.notify != null ? this.notify.hashCode() : 0);
        return hash;
    }



}
