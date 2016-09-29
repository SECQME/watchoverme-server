package com.secqme.domain.model.promocode;

import com.secqme.domain.model.UserVO;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by edward on 28/07/2015.
 */
@Entity
@Table(name = "promoCodeLogs")
@NamedQueries({
        @NamedQuery(
                name = PromoCodeLogVO.FIND_BY_CODE_AND_USER,
                query = "SELECT o " +
                        "FROM PromoCodeLogVO o " +
                        "WHERE o.promoCode = :promoCode " +
                        "AND o.userid = :userid"
        ),
        @NamedQuery(
                name = PromoCodeLogVO.FIND_USER_PROMO_CODE,
                query = "SELECT o " +
                        "FROM PromoCodeLogVO o " +
                        "WHERE o.userid = :userid"
        )
})
public class PromoCodeLogVO {

    public static final String FIND_BY_CODE_AND_USER = "findByPromoCodeAndUser";
    public static final String FIND_USER_PROMO_CODE = "findUserPromoCode";

    @Id
    private Long id;

    private String promoCode;
    private String userid;
    private String rewardName;
    private String forBillingPackage;
    private String rewardBillingPackage;
    private int rewardBillingCycleDays;
    private int rewardSMS;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public String getForBillingPackage() {
        return forBillingPackage;
    }

    public void setForBillingPackage(String forBillingPackage) {
        this.forBillingPackage = forBillingPackage;
    }

    public String getRewardBillingPackage() {
        return rewardBillingPackage;
    }

    public void setRewardBillingPackage(String rewardBillingPackage) {
        this.rewardBillingPackage = rewardBillingPackage;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
