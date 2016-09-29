package com.secqme.domain.model.payment;

import com.secqme.domain.model.UserVO;

import javax.persistence.*;
import java.util.Date;

/**
 *
 * @author coolboykl
 */
@Entity 
@Table(name="itunePaymentLog")
@NamedQueries({
    @NamedQuery(name = ItunePaymentLogVO.QUERY_FIND_BY_USER_ID,
    query = "SELECT o "
    + "FROM ItunePaymentLogVO o "
    + "WHERE o.userVO.userid >= :userid "),
    @NamedQuery(name= ItunePaymentLogVO.QUERY_FIND_BY_RECEIPT_DATA,
        query="SELECT o " +
              "FROM ItunePaymentLogVO o " +
              "WHERE " +
              " o.id = ( SELECT MAX(t.id) FROM ItunePaymentLogVO t WHERE t.receiptData = :receiptData)" )
})
public class ItunePaymentLogVO {
    
    public static final String QUERY_FIND_BY_USER_ID = "ITunePaymentVO.findByUserVO";
    public static final String QUERY_FIND_BY_RECEIPT_DATA = "ITunePaymentVO.findByReceiptData";
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    private String market;
    @Temporal(TemporalType.TIMESTAMP)
    private Date receiveTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTime;


    @Column(columnDefinition = "TEXT")
    private String receiptData;
    private int status;

    @Column(columnDefinition = "TEXT")
    private String receipt;

    @Column(columnDefinition = "TEXT")
    private String latestReceipt;

    @Column(columnDefinition = "TEXT")
    private String latestReceiptInfo;

    @Column(columnDefinition = "TEXT")
    private String verifyResult;
    
    public ItunePaymentLogVO() {
        // Empty Constuctor
    }
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLatestReceipt() {
        return latestReceipt;
    }

    public void setLatestReceipt(String latestReceipt) {
        this.latestReceipt = latestReceipt;
    }

    public String getLatestReceiptInfo() {
        return latestReceiptInfo;
    }

    public void setLatestReceiptInfo(String latestReceiptInfo) {
        this.latestReceiptInfo = latestReceiptInfo;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public String getReceiptData() {
        return receiptData;
    }

    public void setReceiptData(String receiptData) {
        this.receiptData = receiptData;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getVerifyResult() {
        return verifyResult;
    }

    public void setVerifyResult(String verifyResult) {
        this.verifyResult = verifyResult;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ItunePaymentLogVO other = (ItunePaymentLogVO) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if (this.userVO != other.userVO && (this.userVO == null || !this.userVO.equals(other.userVO))) {
            return false;
        }
        if (this.receiveTime != other.receiveTime && (this.receiveTime == null || !this.receiveTime.equals(other.receiveTime))) {
            return false;
        }
        if ((this.receiptData == null) ? (other.receiptData != null) : !this.receiptData.equals(other.receiptData)) {
            return false;
        }
        if (this.status != other.status) {
            return false;
        }
        if ((this.receipt == null) ? (other.receipt != null) : !this.receipt.equals(other.receipt)) {
            return false;
        }
        if ((this.latestReceipt == null) ? (other.latestReceipt != null) : !this.latestReceipt.equals(other.latestReceipt)) {
            return false;
        }
        if ((this.latestReceiptInfo == null) ? (other.latestReceiptInfo != null) : !this.latestReceiptInfo.equals(other.latestReceiptInfo)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 43 * hash + (this.userVO != null ? this.userVO.hashCode() : 0);
        hash = 43 * hash + (this.receiveTime != null ? this.receiveTime.hashCode() : 0);
        hash = 43 * hash + (this.receiptData != null ? this.receiptData.hashCode() : 0);
        hash = 43 * hash + this.status;
        hash = 43 * hash + (this.receipt != null ? this.receipt.hashCode() : 0);
        hash = 43 * hash + (this.latestReceipt != null ? this.latestReceipt.hashCode() : 0);
        hash = 43 * hash + (this.latestReceiptInfo != null ? this.latestReceiptInfo.hashCode() : 0);
        return hash;
    }
    
    
    
    
}
