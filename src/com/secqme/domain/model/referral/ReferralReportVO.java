package com.secqme.domain.model.referral;

import com.secqme.domain.model.UserVO;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * User: James Khoo
 * Date: 3/17/14
 * Time: 2:17 PM
 */
@Entity
@Table(name="referralReport")
@NamedQueries({
        @NamedQuery(name= ReferralReportVO.QUERY_FIND_BY_ORG_USER_ID,
        query= "SELECT o " +
                "FROM ReferralReportVO o " +
                "WHERE o.orgUserVO.userid = :userid")
})
public class ReferralReportVO implements Serializable {

    public static final String QUERY_FIND_BY_ORG_USER_ID = "referralReportVO.findByOrgUserID";
    // TODO Only records transactions related to INSTALL or REGISTER
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orgUserId")
    private UserVO orgUserVO;
    private String orgBillPkgRewarded;
    private Integer orgBillCycleDaysRewarded;
    private Integer orgSMSRewarded;
    @ManyToOne
    @JoinColumn(name = "refUserId")
    private UserVO refUserVO;
    private String refBillPkgRewarded;
    private Integer refBillCycleDaysRewarded;
    private Integer refSMSRewarded;
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;

    public ReferralReportVO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserVO getOrgUserVO() {
        return orgUserVO;
    }

    public void setOrgUserVO(UserVO orgUserVO) {
        this.orgUserVO = orgUserVO;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getOrgBillPkgRewarded() {
		return orgBillPkgRewarded;
	}

	public void setOrgBillPkgRewarded(String orgBillPkgRewarded) {
		this.orgBillPkgRewarded = orgBillPkgRewarded;
	}

	public Integer getOrgBillCycleDaysRewarded() {
		return orgBillCycleDaysRewarded;
	}

	public void setOrgBillCycleDaysRewarded(Integer orgBillCycleDaysRewarded) {
		this.orgBillCycleDaysRewarded = orgBillCycleDaysRewarded;
	}

	public Integer getOrgSMSRewarded() {
		return orgSMSRewarded;
	}

	public void setOrgSMSRewarded(Integer orgSMSRewarded) {
		this.orgSMSRewarded = orgSMSRewarded;
	}

	public UserVO getRefUserVO() {
		return refUserVO;
	}

	public void setRefUserVO(UserVO refUserVO) {
		this.refUserVO = refUserVO;
	}

	public String getRefBillPkgRewarded() {
		return refBillPkgRewarded;
	}

	public void setRefBillPkgRewarded(String refBillPkgRewarded) {
		this.refBillPkgRewarded = refBillPkgRewarded;
	}

	public Integer getRefBillCycleDaysRewarded() {
		return refBillCycleDaysRewarded;
	}

	public void setRefBillCycleDaysRewarded(Integer refBillCycleDaysRewarded) {
		this.refBillCycleDaysRewarded = refBillCycleDaysRewarded;
	}

	public Integer getRefSMSRewarded() {
		return refSMSRewarded;
	}

	public void setRefSMSRewarded(Integer refSMSRewarded) {
		this.refSMSRewarded = refSMSRewarded;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferralReportVO that = (ReferralReportVO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
