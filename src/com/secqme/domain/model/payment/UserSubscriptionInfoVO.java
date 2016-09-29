package com.secqme.domain.model.payment;

import com.secqme.domain.model.UserVO;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
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
@Table(name = "subscriptionInfo")
@IdClass(UserSubscriptionID.class)
@NamedQueries({
    @NamedQuery(name = UserSubscriptionInfoVO.QUERY_FIND_ALL_SUBSCRIPTION_INFO,
    query= "SELECT o " +
    "FROM UserSubscriptionInfoVO o ")
})
public class UserSubscriptionInfoVO implements Serializable {
    private static Logger myLog = Logger.getLogger(UserSubscriptionInfoVO.class);

	public static final String QUERY_FIND_ALL_SUBSCRIPTION_INFO = "userVO.findExpiredSubscriptionInfo";
	
    public static final String NEXT_PAYMENT_DATE_KEY = "nextPaymentDate";
    public static final String EFFECTIVE_DATE_KEY = "effectiveDate";
    public static final String PAYMENT_GW_NAME_KEY = "paymentGateway";
    public static final String PAYMENT_PROFILE_ID_KEY = "profileId";
    public static final String PRICE_PKG_CODE_KEY = "pricePkgCode";
    public static final String PAYMENT_AMT_KEY = "paymentAmount";
    public static final String EXPIRE_AT_KEY = "expireAt";
    
    @Id
    @Column(name = "pricePkgCode", insertable = false, updatable = false)
    private String pricePkgCode;
    @Id
    @Column(name = "userid", insertable = false, updatable = false)
    private String userid;
    @Id
    @Column(name = "gwName", insertable = false, updatable = false)
    private String gwName;
    @OneToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    @ManyToOne
    @JoinColumn(name = "pricePkgCode")
    private PricingPackageVO pricePkgVO;
    @ManyToOne
    @JoinColumn(name = "gwName")
    private PaymentGWVO gatewayVO;
    @Temporal(TemporalType.TIMESTAMP)
    private Date effectiveDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextPaymentDate;
    private Double paymentAmt;
    // subscription Profile id
    private String profileid;
    private String status;
    private String additionalConfig;

    public String getAdditionalConfig() {
        return additionalConfig;
    }

    public void setAdditionalConfig(String additionalConfig) {
        this.additionalConfig = additionalConfig;
    }

    public PaymentGWVO getGatewayVO() {
        return gatewayVO;
    }

    public void setGatewayVO(PaymentGWVO gatewayVO) {
        this.gatewayVO = gatewayVO;
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

    public PricingPackageVO getPricePkgVO() {
        return pricePkgVO;
    }

    public void setPricePkgVO(PricingPackageVO pricePkgVO) {
        this.pricePkgVO = pricePkgVO;
    }

    public String getProfileid() {
        return profileid;
    }

    public void setProfileid(String profileid) {
        this.profileid = profileid;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
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

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        // JK There are two type of subscription, one with AutoRenew,
        // One with onetime payment, for AutoRenew, it is import that we 
        // put in the next payment date, and auto renew Gateway
        // For onetime payment, we needs to put in the expire date
        //
        try {
            if (this.getEffectiveDate() != null) {
                obj.put(EFFECTIVE_DATE_KEY, this.getEffectiveDate().getTime());
            }

            if(this.getPricePkgVO().isAutoRenew()) {
                if (this.getNextPaymentDate() != null) {
                    obj.put(NEXT_PAYMENT_DATE_KEY, this.getNextPaymentDate().getTime());
                }
                obj.put(PAYMENT_GW_NAME_KEY, this.gwName);
            } else {
                Date expireDate = DateUtils.addMonths(this.getEffectiveDate(), this.getPricePkgVO().getQuantity());
                obj.put(EXPIRE_AT_KEY, expireDate.getTime());
            }

            obj.put(PAYMENT_PROFILE_ID_KEY, this.getProfileid());
            if (this.getPricePkgVO() != null) {
                obj.put(PRICE_PKG_CODE_KEY, this.getPricePkgVO().getPricePkgName());
            }

            obj.put(PAYMENT_AMT_KEY, this.getPaymentAmt());
        } catch (JSONException ex) {
            myLog.error("Failed to create JSONObject from: " + this, ex);
        }
        return obj;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserSubscriptionInfoVO other = (UserSubscriptionInfoVO) obj;
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
        return "UserSubscriptionInfoVO{" + "pricePkgCode=" + pricePkgCode + ", userid=" + userid + ", gwName=" + gwName + ", userVO=" + userVO + ", pricePkgVO=" + pricePkgVO + ", gatewayVO=" + gatewayVO + ", effectiveDate=" + effectiveDate + ", nextPaymentDate=" + nextPaymentDate + ", paymentAmt=" + paymentAmt + ", profileid=" + profileid + ", status=" + status + ", additionalConfig=" + additionalConfig + '}';
    }
    
    
}
