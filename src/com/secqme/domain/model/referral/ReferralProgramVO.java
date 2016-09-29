package com.secqme.domain.model.referral;

import com.secqme.domain.model.billing.BillingPkgVO;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * User: James Khoo
 * Date: 2/17/14
 * Time: 6:25 PM
 */
@Entity
@Table(name="referralProgram")
@NamedQueries({
        @NamedQuery(name=ReferralProgramVO.QUERY_FIND_ALL,
        query = "SELECT o " +
                "FROM ReferralProgramVO  o ")
})
public class ReferralProgramVO implements Serializable {

    public static final String QUERY_FIND_ALL = "referralProgram.findAll";

    @Id
    private String referralCode;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    @ManyToOne
    @JoinColumn(name="orgBillPkg")
    private BillingPkgVO orgBillPkg;

    @ManyToOne
    @JoinColumn(name="orgUpgradeBillPkg")
    private BillingPkgVO orgUpgradeBillPkg;

    private Integer orgBillCycleDaysExtension;
    private Integer orgSMSAddition;

    @ManyToOne
    @JoinColumn(name="referrerBillPkg")
    private BillingPkgVO referrerBillPkg;

    private Integer referrerBillCycleDays;
    private Integer referrerSMSAddition;
    private String additionalConfiguration;
    private String marketingTextCode;
    private Double version;
    
    @Transient
    private String marketingText;

    public ReferralProgramVO() {
        // Empty Constructor
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public BillingPkgVO getOrgBillPkg() {
        return orgBillPkg;
    }

    public void setOrgBillPkg(BillingPkgVO orgBillPkg) {
        this.orgBillPkg = orgBillPkg;
    }

    public BillingPkgVO getOrgUpgradeBillPkg() {
        return orgUpgradeBillPkg;
    }

    public void setOrgUpgradeBillPkg(BillingPkgVO orgUpgradeBillPkg) {
        this.orgUpgradeBillPkg = orgUpgradeBillPkg;
    }

    public Integer getOrgBillCycleDaysExtension() {
        return orgBillCycleDaysExtension;
    }

    public void setOrgBillCycleDaysExtension(Integer orgBillCycleDaysExtension) {
        this.orgBillCycleDaysExtension = orgBillCycleDaysExtension;
    }



    public BillingPkgVO getReferrerBillPkg() {
        return referrerBillPkg;
    }

    public void setReferrerBillPkg(BillingPkgVO referrerBillPkg) {
        this.referrerBillPkg = referrerBillPkg;
    }

    public Integer getReferrerBillCycleDays() {
        return referrerBillCycleDays;
    }

    public void setReferrerBillCycleDays(Integer referrerBillCycleDays) {
        this.referrerBillCycleDays = referrerBillCycleDays;
    }

    public Integer getOrgSMSAddition() {
		return orgSMSAddition;
	}

	public void setOrgSMSAddition(Integer orgSMSAddition) {
		this.orgSMSAddition = orgSMSAddition;
	}

	public Integer getReferrerSMSAddition() {
		return referrerSMSAddition;
	}

	public void setReferrerSMSAddition(Integer referrerSMSAddition) {
		this.referrerSMSAddition = referrerSMSAddition;
	}

	public String getAdditionalConfiguration() {
        return additionalConfiguration;
    }

    public void setAdditionalConfiguration(String additionalConfiguration) {
        this.additionalConfiguration = additionalConfiguration;
    }

    public String getMarketingTextCode() {
        return marketingTextCode;
    }

    public void setMarketingTextCode(String marketingTextCode) {
        this.marketingTextCode = marketingTextCode;
    }

    public String getMarketingText() {
        return marketingText;
    }

    public void setMarketingText(String marketingText) {
        this.marketingText = marketingText;
    }

    public Double getVersion() {
		return version;
	}

	public void setVersion(Double version) {
		this.version = version;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferralProgramVO that = (ReferralProgramVO) o;

        if (additionalConfiguration != null ? !additionalConfiguration.equals(that.additionalConfiguration) : that.additionalConfiguration != null)
            return false;
        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) return false;
        if (orgBillCycleDaysExtension != null ? !orgBillCycleDaysExtension.equals(that.orgBillCycleDaysExtension) : that.orgBillCycleDaysExtension != null)
            return false;
        if (orgBillPkg != null ? !orgBillPkg.equals(that.orgBillPkg) : that.orgBillPkg != null) return false;
        if (orgUpgradeBillPkg != null ? !orgUpgradeBillPkg.equals(that.orgUpgradeBillPkg) : that.orgUpgradeBillPkg != null)
            return false;
        if (!referralCode.equals(that.referralCode)) return false;
        if (referrerBillCycleDays != null ? !referrerBillCycleDays.equals(that.referrerBillCycleDays) : that.referrerBillCycleDays != null)
            return false;
        if (referrerBillPkg != null ? !referrerBillPkg.equals(that.referrerBillPkg) : that.referrerBillPkg != null)
            return false;
        if (!startDate.equals(that.startDate)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = referralCode.hashCode();
        result = 31 * result + startDate.hashCode();
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (orgBillPkg != null ? orgBillPkg.hashCode() : 0);
        result = 31 * result + (orgUpgradeBillPkg != null ? orgUpgradeBillPkg.hashCode() : 0);
        result = 31 * result + (orgBillCycleDaysExtension != null ? orgBillCycleDaysExtension.hashCode() : 0);
        result = 31 * result + (referrerBillPkg != null ? referrerBillPkg.hashCode() : 0);
        result = 31 * result + (referrerBillCycleDays != null ? referrerBillCycleDays.hashCode() : 0);
        result = 31 * result + (additionalConfiguration != null ? additionalConfiguration.hashCode() : 0);
        return result;
    }
}
