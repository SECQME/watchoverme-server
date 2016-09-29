package com.secqme.domain.model.payment;

import com.secqme.domain.model.UserVO;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author coolboykl
 */
@Entity
@Table(name="pendingPayments")
public class PendingPaymentVO implements Serializable {
    @Id
    private String txnid;

    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;

    @ManyToOne
    @JoinColumn(name = "pricePkgCode")
    private PricingPackageVO pricePkgVO;

    @Temporal(TemporalType.TIMESTAMP)
    private Date receiveTime;

    private String payerId;
    private Double grossAmt;
    private Double paymentFee;
    private String status;

    public Double getGrossAmt() {
        return grossAmt;
    }

    public void setGrossAmt(Double grossAmt) {
        this.grossAmt = grossAmt;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public Double getPaymentFee() {
        return paymentFee;
    }

    public void setPaymentFee(Double paymentFee) {
        this.paymentFee = paymentFee;
    }

    public PricingPackageVO getPricePkgVO() {
        return pricePkgVO;
    }

    public void setPricePkgVO(PricingPackageVO pricePkgVO) {
        this.pricePkgVO = pricePkgVO;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTxnid() {
        return txnid;
    }

    public void setTxnid(String txnid) {
        this.txnid = txnid;
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
        final PendingPaymentVO other = (PendingPaymentVO) obj;
        if ((this.txnid == null) ? (other.txnid != null) : !this.txnid.equals(other.txnid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.txnid != null ? this.txnid.hashCode() : 0);
        return hash;
    }




}
