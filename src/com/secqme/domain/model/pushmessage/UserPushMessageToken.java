package com.secqme.domain.model.pushmessage;

import com.secqme.domain.model.*;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import org.apache.commons.lang.StringUtils;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author james
 */
@Entity
@Table(name = "userPushMessageToken")
public class UserPushMessageToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    private String token;
    private String platform;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date activateDate;
    
    @Transient
    private MobilePlatformType platformType;

    public UserPushMessageToken() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getActivateDate() {
        return activateDate;
    }

    public void setActivateDate(Date activateDate) {
        this.activateDate = activateDate;
    }

    public MobilePlatformType getPlatformType() {
        return MobilePlatformType.valueOf(this.platform);
    }

    public void setPlatformType(MobilePlatformType platformType) {
        this.platformType = platformType;
        this.platform = platformType.name();
    }

    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 67 * hash + (this.userVO != null ? this.userVO.hashCode() : 0);
        hash = 67 * hash + (this.token != null ? this.token.hashCode() : 0);
        hash = 67 * hash + (this.platform != null ? this.platform.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserPushMessageToken other = (UserPushMessageToken) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if (this.userVO != other.userVO && (this.userVO == null || !this.userVO.equals(other.userVO))) {
            return false;
        }
        if ((this.token == null) ? (other.token != null) : !this.token.equals(other.token)) {
            return false;
        }
        if ((this.platform == null) ? (other.platform != null) : !this.platform.equals(other.platform)) {
            return false;
        }
        return true;
    }
    
    
    
    
}
