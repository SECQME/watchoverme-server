package com.secqme.domain.model.promocode;

import com.secqme.domain.model.billing.BillingPkgVO;

import javax.persistence.*;

/**
 * Created by edward on 14/08/2015.
 */
@Entity
@Table(name = "promoCodeRewards")
public class PromoCodeRewardVO {

    @Id
    private Long id;

    private String rewardName;

    @ManyToOne
    @JoinColumn(name = "forBillingPackage")
    private BillingPkgVO forBillingPackageVO;

    @ManyToOne
    @JoinColumn(name = "rewardBillingPackage")
    private BillingPkgVO rewardBillingPackageVO;

    private int rewardBillingCycleDays;
    private int rewardSMS;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public BillingPkgVO getForBillingPackageVO() {
        return forBillingPackageVO;
    }

    public void setForBillingPackageVO(BillingPkgVO forBillingPackageVO) {
        this.forBillingPackageVO = forBillingPackageVO;
    }

    public BillingPkgVO getRewardBillingPackageVO() {
        return rewardBillingPackageVO;
    }

    public void setRewardBillingPackageVO(BillingPkgVO rewardBillingPackageVO) {
        this.rewardBillingPackageVO = rewardBillingPackageVO;
    }

    public int getRewardBillingCycleDays() {
        return rewardBillingCycleDays;
    }

    public void setRewardBillingCycleDays(int rewardBillingCycleDays) {
        this.rewardBillingCycleDays = rewardBillingCycleDays;
    }

    public int getRewardSMS() {
        return rewardSMS;
    }

    public void setRewardSMS(int rewardSMS) {
        this.rewardSMS = rewardSMS;
    }
}
