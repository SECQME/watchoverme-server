package com.secqme.domain.model.billing;

import com.secqme.domain.model.UserVO;
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

/**
 *
 * @author coolboykl
 */
@Entity
@Table(name="billingCycle")
@NamedQueries({
    @NamedQuery(name = BillingCycleVO.QUERY_FIND_ALL_BY_USER,
    query = "SELECT o "
    + "FROM BillingCycleVO o "
    + "WHERE o.userVO.userid = :userid "),
            
    @NamedQuery(name = BillingCycleVO.QUERY_FIND_LATEST_CYCLE_BY_USER,
    query = "SELECT o "
    + "FROM BillingCycleVO o "
    + "WHERE o.id = ( SELECT MAX(t.id) FROM BillingCycleVO t WHERE t.userVO.userid = :userid)")
 })
public class BillingCycleVO implements Serializable {
    
    public static final String QUERY_FIND_ALL_BY_USER = "BillingCycleVO.findAllByUserID";
    public static final String QUERY_FIND_LATEST_CYCLE_BY_USER = "BillingCycleVO.findLatestByUser";
    
    public final static String BILL_EXPIRED_AT_KEY = "packageExpiredAt";
    public final static String EVENT_CREDIT_BALANCE_KEY = "eventCreditBalance";
    public final static String SMS_CREDIT_BALANCE_KEY = "smsCreditBalance";
    public final static String NO_LIMIT_MSG_VALUE = "No Limit";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    
    @ManyToOne
    @JoinColumn(name = "market")
    private MarketVO marketVO;
    
    @ManyToOne
    @JoinColumn(name = "billingPkg")
    private BillingPkgVO billingPkgVO;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    
    private int allocateEventRegCredit;
    private int allocateSMSCredit;
    private int totalEventCreditUsed;
    private int totalSMSCreditUsed;
    
    public BillingCycleVO() {
        // Empty Constructor
    }

    public int getAllocateEventRegCredit() {
        return allocateEventRegCredit;
    }

    public void setAllocateEventRegCredit(int allocateEventRegCredit) {
        this.allocateEventRegCredit = allocateEventRegCredit;
    }

    public int getAllocateSMSCredit() {
        return allocateSMSCredit;
    }

    public void setAllocateSMSCredit(int allocateSMSCredit) {
        this.allocateSMSCredit = allocateSMSCredit;
    }

    public BillingPkgVO getBillingPkgVO() {
        return billingPkgVO;
    }

    public void setBillingPkgVO(BillingPkgVO billingPkgVO) {
        this.billingPkgVO = billingPkgVO;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MarketVO getMarketVO() {
        return marketVO;
    }

    public void setMarketVO(MarketVO marketVO) {
        this.marketVO = marketVO;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    

    public int getTotalEventCreditUsed() {
        return totalEventCreditUsed;
    }

    public void setTotalEventCreditUsed(int totalEventCreditUsed) {
        this.totalEventCreditUsed = totalEventCreditUsed;
    }

    public int getTotalSMSCreditUsed() {
        return totalSMSCreditUsed;
    }

    public void setTotalSMSCreditUsed(int totalSMSCreditUsed) {
        this.totalSMSCreditUsed = totalSMSCreditUsed;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }
    
    // If return null, means the allocated event Credit Balance is 
    public Integer getEventCreditBalance() {
        Integer eventCreditBalance = null;
        if(allocateEventRegCredit != -1) {
            eventCreditBalance = allocateEventRegCredit - totalEventCreditUsed;
        }
        return eventCreditBalance;
    }
    
    public Integer getSMSCreditBalance() {
        Integer smsCreditBalance = null;
        if(allocateSMSCredit != -1) {
            smsCreditBalance = allocateSMSCredit - totalSMSCreditUsed;
        }
        return smsCreditBalance;
    }
    
    // Return either Event Credit Balance as String
    // Either "0", "1" or "No Limit"
    public String getEventCreditBalanceText() {
        return this.getEventCreditBalance() != null? this.getEventCreditBalance().toString()  : NO_LIMIT_MSG_VALUE;
    }
    
    
    // Return either Event Credit Balance as String
    // Either "0", "1" or "No Limit"
    public String getSMSCreditBalanceText() {
        return this.getSMSCreditBalance() != null? this.getSMSCreditBalance().toString()  : NO_LIMIT_MSG_VALUE;
    }

    public Long getBillCycleRemainDays() {
        Long remainDays = 0l;

        if(getEndDate() != null) {
            remainDays = (getEndDate().getTime() - new Date().getTime())/ (24*60*60*1000);
        }

        return remainDays;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BillingCycleVO other = (BillingCycleVO) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if (this.userVO != other.userVO && (this.userVO == null || !this.userVO.equals(other.userVO))) {
            return false;
        }
        if (this.marketVO != other.marketVO && (this.marketVO == null || !this.marketVO.equals(other.marketVO))) {
            return false;
        }
        if (this.billingPkgVO != other.billingPkgVO && (this.billingPkgVO == null || !this.billingPkgVO.equals(other.billingPkgVO))) {
            return false;
        }
        if (this.startDate != other.startDate && (this.startDate == null || !this.startDate.equals(other.startDate))) {
            return false;
        }
        if (this.endDate != other.endDate && (this.endDate == null || !this.endDate.equals(other.endDate))) {
            return false;
        }
        if (this.allocateEventRegCredit != other.allocateEventRegCredit) {
            return false;
        }
        if (this.allocateSMSCredit != other.allocateSMSCredit) {
            return false;
        }
        if (this.totalEventCreditUsed != other.totalEventCreditUsed) {
            return false;
        }
        if (this.totalSMSCreditUsed != other.totalSMSCreditUsed) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 47 * hash + (this.userVO != null ? this.userVO.hashCode() : 0);
        hash = 47 * hash + (this.marketVO != null ? this.marketVO.hashCode() : 0);
        hash = 47 * hash + (this.billingPkgVO != null ? this.billingPkgVO.hashCode() : 0);
        hash = 47 * hash + (this.startDate != null ? this.startDate.hashCode() : 0);
        hash = 47 * hash + (this.endDate != null ? this.endDate.hashCode() : 0);
        hash = 47 * hash + this.allocateEventRegCredit;
        hash = 47 * hash + this.allocateSMSCredit;
        hash = 47 * hash + this.totalEventCreditUsed;
        hash = 47 * hash + this.totalSMSCreditUsed;
        return hash;
    }
    

    
    
    
    
    
}
