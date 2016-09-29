package com.secqme.domain.model.billing;

import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.UserVO;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
 * @author jameskhoo
 */
@Entity
@Table(name = "billingLog")
@NamedQueries({
    @NamedQuery(name = BillingLogVO.QUERY_FIND_BY_BILLING_CYCLE_ID,
    query = "SELECT o "
    + "FROM BillingLogVO o "
    + "WHERE o.billingCycleVO.id = :id "),
    @NamedQuery(name = BillingLogVO.QUERY_FIND_BY_SECQME_EVENT_ID,
    query = "SELECT o "
    + "FROM BillingLogVO o "
    + "WHERE o.secqMeEventVO.id = :id "),
    @NamedQuery(name = BillingLogVO.QUERY_FIND_BY_USER_ID_WITH_DATE_RANGE,
    query = "SELECT o "
    + "FROM BillingLogVO o "
    + "WHERE o.userVO.userid = :userid "
    + "AND o.logTime >= :startTime "
    + "AND o.logTime <= :endTime "
    + "ORDER BY o.logTime desc")
   })
public class BillingLogVO implements Serializable {
    
    public static final String QUERY_FIND_BY_USER_ID_WITH_DATE_RANGE = "BillingLogVO.findByUserIdDateRange";
    public static final String QUERY_FIND_BY_BILLING_CYCLE_ID = "BillingLogVO.findByBillingCycleId";
    public static final String QUERY_FIND_BY_SECQME_EVENT_ID = "BillingLogVO.findBySecQMeEventId";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name="userid")
    private UserVO userVO;
    
    @Enumerated(EnumType.STRING)
    private BillingLogType logType;
   
    private String remark;
   
    @Temporal(TemporalType.TIMESTAMP)
    private Date logTime;
    
    @ManyToOne
    @JoinColumn(name = "secqMeEventId")
    private SecqMeEventVO secqMeEventVO;
    
    @ManyToOne
    @JoinColumn(name = "billingCycleid")
    private BillingCycleVO billingCycleVO;
    
    private Integer smsCreditUsed;
    private Integer eventRegCreditUsed;

    public BillingLogVO() {
        //Empty Constructor
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public BillingCycleVO getBillingCycleVO() {
        return billingCycleVO;
    }

    public void setBillingCycleVO(BillingCycleVO billingCycleVO) {
        this.billingCycleVO = billingCycleVO;
    }
    

    public SecqMeEventVO getSecqMeEventVO() {
        return secqMeEventVO;
    }

    public void setSecqMeEventVO(SecqMeEventVO secqMeEventVO) {
        this.secqMeEventVO = secqMeEventVO;
    }

    public BillingLogType getLogType() {
        return logType;
    }

    public void setLogType(BillingLogType logType) {
        this.logType = logType;
    }
    
    public Integer getEventRegCreditUsed() {
        return eventRegCreditUsed;
    }

    public void setEventRegCreditUsed(Integer eventRegCreditUsed) {
        this.eventRegCreditUsed = eventRegCreditUsed;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getSmsCreditUsed() {
        return smsCreditUsed;
    }

    public void setSmsCreditUsed(Integer smsCreditUsed) {
        this.smsCreditUsed = smsCreditUsed;
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
        final BillingLogVO other = (BillingLogVO) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if (this.userVO != other.userVO && (this.userVO == null || !this.userVO.equals(other.userVO))) {
            return false;
        }
        if (this.logType != other.logType) {
            return false;
        }
        if ((this.remark == null) ? (other.remark != null) : !this.remark.equals(other.remark)) {
            return false;
        }
        if (this.logTime != other.logTime && (this.logTime == null || !this.logTime.equals(other.logTime))) {
            return false;
        }
        if (this.secqMeEventVO != other.secqMeEventVO && (this.secqMeEventVO == null || !this.secqMeEventVO.equals(other.secqMeEventVO))) {
            return false;
        }
        if (this.smsCreditUsed != other.smsCreditUsed && (this.smsCreditUsed == null || !this.smsCreditUsed.equals(other.smsCreditUsed))) {
            return false;
        }
        if (this.eventRegCreditUsed != other.eventRegCreditUsed && (this.eventRegCreditUsed == null || !this.eventRegCreditUsed.equals(other.eventRegCreditUsed))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 23 * hash + (this.userVO != null ? this.userVO.hashCode() : 0);
        hash = 23 * hash + (this.logType != null ? this.logType.hashCode() : 0);
        hash = 23 * hash + (this.remark != null ? this.remark.hashCode() : 0);
        hash = 23 * hash + (this.logTime != null ? this.logTime.hashCode() : 0);
        hash = 23 * hash + (this.secqMeEventVO != null ? this.secqMeEventVO.hashCode() : 0);
        hash = 23 * hash + (this.smsCreditUsed != null ? this.smsCreditUsed.hashCode() : 0);
        hash = 23 * hash + (this.eventRegCreditUsed != null ? this.eventRegCreditUsed.hashCode() : 0);
        return hash;
    }
    

}
