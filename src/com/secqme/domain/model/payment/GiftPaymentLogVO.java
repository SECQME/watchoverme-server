package com.secqme.domain.model.payment;

import java.io.Serializable;
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

import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.payment.PaymentGWVO;

@Entity
@Table(name="giftPaymentLog")
@NamedQueries({
        @NamedQuery(name = GiftPaymentLogVO.QUERY_FIND_BY_COUNTRY_AND_PHONE_NUMBER,
                query = "SELECT o FROM GiftPaymentLogVO o WHERE o.recipientMobileNumber = :mobileNumber " +
                        "AND o.recipientMobileCountry.iso = :mobileCountryISO " +
                        "AND o.redeemed = :redeemed "),
        @NamedQuery(name = GiftPaymentLogVO.QUERY_FIND_BY_EMAIL_ADDRESS,
                query = "SELECT o FROM GiftPaymentLogVO o WHERE LOWER(o.recipientEmail) = LOWER(:emailAddress) " +
                        "AND o.redeemed = :redeemed ")
})
public class GiftPaymentLogVO implements Serializable {
	
	public final static String QUERY_FIND_BY_COUNTRY_AND_PHONE_NUMBER = "PaymentGWVO.findByCountryAndPhoneNumber";
    public final static String QUERY_FIND_BY_EMAIL_ADDRESS = "PaymentGWVO.findByEmailAddress";
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
    private String name;
    @ManyToOne
    @JoinColumn(name = "mobileCountry")
    private CountryVO mobileCountry;
    private String mobileNumber;
    private String recipientName;
    @ManyToOne
    @JoinColumn(name = "recipientMobileCountry")
    private CountryVO recipientMobileCountry;
    private String recipientMobileNumber;
    private boolean redeemed;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    @ManyToOne
    @JoinColumn(name = "paymentHistoryId")
    private PaymentHistoryVO paymentHistoryVO;
    private String email;
    private String recipientEmail;
    private String message;
    private String paymentId;
    private String additionalConfig;
    private boolean autoRenew;
    
    public GiftPaymentLogVO() {
    	
    }
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public CountryVO getMobileCountry() {
		return mobileCountry;
	}
	public void setMobileCountry(CountryVO mobileCountry) {
		this.mobileCountry = mobileCountry;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getRecipientName() {
		return recipientName;
	}
	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}
	public CountryVO getRecipientMobileCountry() {
		return recipientMobileCountry;
	}
	public void setRecipientMobileCountry(CountryVO recipientMobileCountry) {
		this.recipientMobileCountry = recipientMobileCountry;
	}
	public String getRecipientMobileNumber() {
		return recipientMobileNumber;
	}
	public void setRecipientMobileNumber(String recipientMobileNumber) {
		this.recipientMobileNumber = recipientMobileNumber;
	}
	public boolean isRedeemed() {
		return redeemed;
	}
	public void setRedeemed(boolean redeemed) {
		this.redeemed = redeemed;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public PaymentHistoryVO getPaymentHistoryVO() {
		return paymentHistoryVO;
	}
	public void setPaymentHistoryVO(PaymentHistoryVO paymentHistoryVO) {
		this.paymentHistoryVO = paymentHistoryVO;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRecipientEmail() {
		return recipientEmail;
	}
	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	public String getAdditionalConfig() {
		return additionalConfig;
	}
	public void setAdditionalConfig(String additionalConfig) {
		this.additionalConfig = additionalConfig;
	}
	public boolean isAutoRenew() {
		return autoRenew;
	}
	public void setAutoRenew(boolean autoRenew) {
		this.autoRenew = autoRenew;
	}
    
}
