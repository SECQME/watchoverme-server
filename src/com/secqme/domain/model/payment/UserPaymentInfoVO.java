package com.secqme.domain.model.payment;

import com.secqme.domain.model.UserVO;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author coolboykl
 */
@Entity
@Table(name="userPaymentInfos")
@IdClass(UserPaymentInfoID.class)
public class UserPaymentInfoVO implements Serializable {
    
    @Id
    @Column(name = "gwName", insertable = false, updatable = false)
    private String gwName;
    @Id
    @Column(name = "userid", insertable = false, updatable = false)
    private String userid;
    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    @ManyToOne
    @JoinColumn(name = "gwName")
    private PaymentGWVO gatewayVO;
    
    private String paymentid;
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
    
    
    public String getPaymentid() {
        return paymentid;
    }

    public void setPaymentid(String paymentid) {
        this.paymentid = paymentid;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserPaymentInfoVO other = (UserPaymentInfoVO) obj;
        if ((this.gwName == null) ? (other.gwName != null) : !this.gwName.equals(other.gwName)) {
            return false;
        }
        if ((this.userid == null) ? (other.userid != null) : !this.userid.equals(other.userid)) {
            return false;
        }
        if ((this.paymentid == null) ? (other.paymentid != null) : !this.paymentid.equals(other.paymentid)) {
            return false;
        }
        if ((this.additionalConfig == null) ? (other.additionalConfig != null) : !this.additionalConfig.equals(other.additionalConfig)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.gwName != null ? this.gwName.hashCode() : 0);
        hash = 19 * hash + (this.userid != null ? this.userid.hashCode() : 0);
        hash = 19 * hash + (this.paymentid != null ? this.paymentid.hashCode() : 0);
        hash = 19 * hash + (this.additionalConfig != null ? this.additionalConfig.hashCode() : 0);
        return hash;
    }
    
    
    
}
