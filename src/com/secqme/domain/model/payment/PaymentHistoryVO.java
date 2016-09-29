package com.secqme.domain.model.payment;

import com.secqme.domain.model.UserVO;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author coolboykl
 */
@Entity
@Table(name="paymentHistory")
@NamedQueries({
    @NamedQuery(name = PaymentHistoryVO.QUERY_FIND_BY_USER_ID_WITH_DATA_RANGE,
    query = "SELECT o "
    + "FROM PaymentHistoryVO o "
    + "WHERE o.userVO.userid = :userid "
    + "AND o.paymentDate >= :startTime "
    + "AND o.paymentDate <= :endTime "
    + "ORDER BY o.paymentDate desc"),
    @NamedQuery(name = PaymentHistoryVO.QUERY_FIND_BY_USER_ID,
        query = "SELECT o "
              + "FROM PaymentHistoryVO o "
              + "WHERE o.userVO.userid = :userid "
              +  "ORDER BY o.paymentDate desc")
   })
public class PaymentHistoryVO implements Serializable {
    
    public static final String QUERY_FIND_BY_USER_ID_WITH_DATA_RANGE = "PaymentHistroyVO.findByUserDateRange";
    public static final String QUERY_FIND_BY_USER_ID = "PaymentHistory.findByUserId";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;
    
    private String paymentType;
    private Double paymentAmt;
    private Double paymentFee;
    
    @ManyToOne
    @JoinColumn(name = "gwName")
    private PaymentGWVO paymentGWVO;
    
    @ManyToOne
    @JoinColumn(name="pricingPkgCode")
    private PricingPackageVO pricingPgkVO;
    
    private String remark;
    private String giftedUserid;

    
    public PaymentHistoryVO() {
        // Emptry Constructure
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPaymentAmt() {
        return paymentAmt;
    }

    public void setPaymentAmt(Double paymentAmt) {
        this.paymentAmt = paymentAmt;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public PricingPackageVO getPricingPgkVO() {
        return pricingPgkVO;
    }
    
    public void setPaymentFee(Double paymentFee) {
        this.paymentFee = paymentFee;
    }

    public Double getPaymentFee() {
        return paymentFee;
    }
    
    public void setPricingPgkVO(PricingPackageVO pricingPgkVO) {
        this.pricingPgkVO = pricingPgkVO;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGiftedUserid() {
		return giftedUserid;
	}

	public void setGiftedUserid(String giftedUserid) {
		this.giftedUserid = giftedUserid;
	}

	public PaymentGWVO getPaymentGWVO() {
		return paymentGWVO;
	}

	public void setPaymentGWVO(PaymentGWVO paymentGWVO) {
		this.paymentGWVO = paymentGWVO;
	}

	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PaymentHistoryVO other = (PaymentHistoryVO) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if (this.userVO != other.userVO && (this.userVO == null || !this.userVO.equals(other.userVO))) {
            return false;
        }
        if (this.paymentDate != other.paymentDate && (this.paymentDate == null || !this.paymentDate.equals(other.paymentDate))) {
            return false;
        }
        if ((this.paymentType == null) ? (other.paymentType != null) : !this.paymentType.equals(other.paymentType)) {
            return false;
        }
        if (this.paymentAmt != other.paymentAmt && (this.paymentAmt == null || !this.paymentAmt.equals(other.paymentAmt))) {
            return false;
        }
        if (this.pricingPgkVO != other.pricingPgkVO && (this.pricingPgkVO == null || !this.pricingPgkVO.equals(other.pricingPgkVO))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 41 * hash + (this.userVO != null ? this.userVO.hashCode() : 0);
        hash = 41 * hash + (this.paymentDate != null ? this.paymentDate.hashCode() : 0);
        hash = 41 * hash + (this.paymentType != null ? this.paymentType.hashCode() : 0);
        hash = 41 * hash + (this.paymentAmt != null ? this.paymentAmt.hashCode() : 0);
        hash = 41 * hash + (this.pricingPgkVO != null ? this.pricingPgkVO.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "PaymentHistoryVO{" + "userid=" + userVO.getUserid() + ", paymentDate=" + paymentDate + ", paymentType=" + paymentType + ", paymentAmt=" + paymentAmt + ", paymentFee=" + paymentFee + ", gwName=" + paymentGWVO.getGwName() + ", pricingPgkVO=" + pricingPgkVO + ", remark=" + remark + '}';
    }
    
    
    
    
}
