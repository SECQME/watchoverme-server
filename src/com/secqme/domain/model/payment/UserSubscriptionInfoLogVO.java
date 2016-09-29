package com.secqme.domain.model.payment;

import com.secqme.domain.model.UserVO;
import org.apache.commons.lang.time.DateUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author coolboykl
 */
@Entity
@Table(name = "subscriptionInfoLog")
public class UserSubscriptionInfoLogVO implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pricePkgCode;
    private String userid;
    private String gwName;
    @Temporal(TemporalType.TIMESTAMP)
    private Date effectiveDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextPaymentDate;
    private Double paymentAmt;
    // subscription Profile id
    private String profileid;
    private String status;
    private String additionalConfig;

    public UserSubscriptionInfoLogVO() {
    	
    }
    
    public UserSubscriptionInfoLogVO(UserSubscriptionInfoVO userSubscriptionInfoVO) {
    	this.pricePkgCode = userSubscriptionInfoVO.getPricePkgVO().getPkgCode();
    	this.userid = userSubscriptionInfoVO.getUserid();
    	this.gwName = userSubscriptionInfoVO.getGwName();
    	this.effectiveDate = userSubscriptionInfoVO.getEffectiveDate();
    	this.nextPaymentDate = userSubscriptionInfoVO.getNextPaymentDate();
    	this.paymentAmt = userSubscriptionInfoVO.getPaymentAmt();
    	this.profileid = userSubscriptionInfoVO.getProfileid();
    	this.status = userSubscriptionInfoVO.getStatus();
    	this.additionalConfig = userSubscriptionInfoVO.getAdditionalConfig();
    }
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPricePkgCode() {
		return pricePkgCode;
	}

	public void setPricePkgCode(String pricePkgCode) {
		this.pricePkgCode = pricePkgCode;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public void setGwName(String gwName) {
		this.gwName = gwName;
	}

	public String getAdditionalConfig() {
        return additionalConfig;
    }

    public void setAdditionalConfig(String additionalConfig) {
        this.additionalConfig = additionalConfig;
    }

    public String getGwName() {
        return gwName;
    }

    public String getUserid() {
        return userid;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getNextPaymentDate() {
        return nextPaymentDate;
    }

    public void setNextPaymentDate(Date nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
    }

    public String getProfileid() {
        return profileid;
    }

    public void setProfileid(String profileid) {
        this.profileid = profileid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPaymentAmt() {
        return paymentAmt;
    }

    public void setPaymentAmt(Double paymentAmt) {
        this.paymentAmt = paymentAmt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserSubscriptionInfoLogVO other = (UserSubscriptionInfoLogVO) obj;
        if ((this.pricePkgCode == null) ? (other.pricePkgCode != null) : !this.pricePkgCode.equals(other.pricePkgCode)) {
            return false;
        }
        if ((this.userid == null) ? (other.userid != null) : !this.userid.equals(other.userid)) {
            return false;
        }
        if ((this.gwName == null) ? (other.gwName != null) : !this.gwName.equals(other.gwName)) {
            return false;
        }
        if (this.effectiveDate != other.effectiveDate && (this.effectiveDate == null || !this.effectiveDate.equals(other.effectiveDate))) {
            return false;
        }
        if (this.nextPaymentDate != other.nextPaymentDate && (this.nextPaymentDate == null || !this.nextPaymentDate.equals(other.nextPaymentDate))) {
            return false;
        }
        if ((this.profileid == null) ? (other.profileid != null) : !this.profileid.equals(other.profileid)) {
            return false;
        }
        if ((this.additionalConfig == null) ? (other.additionalConfig != null) : !this.additionalConfig.equals(other.additionalConfig)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + (this.pricePkgCode != null ? this.pricePkgCode.hashCode() : 0);
        hash = 31 * hash + (this.userid != null ? this.userid.hashCode() : 0);
        hash = 31 * hash + (this.gwName != null ? this.gwName.hashCode() : 0);
        hash = 31 * hash + (this.effectiveDate != null ? this.effectiveDate.hashCode() : 0);
        hash = 31 * hash + (this.nextPaymentDate != null ? this.nextPaymentDate.hashCode() : 0);
        hash = 31 * hash + (this.profileid != null ? this.profileid.hashCode() : 0);
        hash = 31 * hash + (this.additionalConfig != null ? this.additionalConfig.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "UserSubscriptionInfoVO{" + "pricePkgCode=" + pricePkgCode + ", userid=" + userid + ", gwName=" + gwName + ", effectiveDate=" + effectiveDate + ", nextPaymentDate=" + nextPaymentDate + ", paymentAmt=" + paymentAmt + ", profileid=" + profileid + ", status=" + status + ", additionalConfig=" + additionalConfig + '}';
    }
    
    
}
