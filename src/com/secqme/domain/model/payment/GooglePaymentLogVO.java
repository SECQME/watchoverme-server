package com.secqme.domain.model.payment;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.secqme.domain.model.UserVO;

/**
 *
 * @author coolboykl
 */
@Entity 
@Table(name="googlePaymentLogs")
@NamedQueries({
            
    @NamedQuery(name = GooglePaymentLogVO.QUERY_FIND_LATEST_LOG_BY_USER,
    query = "SELECT o "
    + "FROM GooglePaymentLogVO o "
    + "WHERE o.id = ( SELECT MAX(t.id) FROM GooglePaymentLogVO t WHERE t.userVO.userid = :userid)")
 })
public class GooglePaymentLogVO {

	public static final String QUERY_FIND_LATEST_LOG_BY_USER = "GooglePaymentLogVO.findLatestByUser";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    @Temporal(TemporalType.TIMESTAMP)
    private Date receiptDate;
    private String productID;
    private String purchaseToken;
    private String purchaseReceipt;
    private String status;
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;
    private int renew;
    
    public GooglePaymentLogVO() {
        // Empty Constuctor
    }

    public GooglePaymentLogVO(GooglePaymentLogVO googlePaymentLogVO) {
    	this.userVO = googlePaymentLogVO.userVO;
    	this.productID = googlePaymentLogVO.productID;
    	this.purchaseToken = googlePaymentLogVO.purchaseToken;
    	this.purchaseReceipt = googlePaymentLogVO.purchaseReceipt;
    	this.status = googlePaymentLogVO.status;
    	this.renew = googlePaymentLogVO.renew + 1;
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

    public Date getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public String getPurchaseReceipt() {
        return purchaseReceipt;
    }

    public void setPurchaseReceipt(String purchaseReceipt) {
        this.purchaseReceipt = purchaseReceipt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public int getRenew() {
		return renew;
	}

	public void setRenew(int renew) {
		this.renew = renew;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GooglePaymentLogVO that = (GooglePaymentLogVO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (productID != null ? !productID.equals(that.productID) : that.productID != null) return false;
        if (purchaseReceipt != null ? !purchaseReceipt.equals(that.purchaseReceipt) : that.purchaseReceipt != null)
            return false;
        if (purchaseToken != null ? !purchaseToken.equals(that.purchaseToken) : that.purchaseToken != null)
            return false;
        if (receiptDate != null ? !receiptDate.equals(that.receiptDate) : that.receiptDate != null) return false;
        if (userVO != null ? !userVO.equals(that.userVO) : that.userVO != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userVO != null ? userVO.hashCode() : 0);
        result = 31 * result + (receiptDate != null ? receiptDate.hashCode() : 0);
        result = 31 * result + (productID != null ? productID.hashCode() : 0);
        result = 31 * result + (purchaseToken != null ? purchaseToken.hashCode() : 0);
        result = 31 * result + (purchaseReceipt != null ? purchaseReceipt.hashCode() : 0);
        return result;
    }
}
