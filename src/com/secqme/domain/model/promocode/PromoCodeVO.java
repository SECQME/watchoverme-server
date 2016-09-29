package com.secqme.domain.model.promocode;

import com.secqme.domain.converter.JSONObjectConverter;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.util.List;

/**
 * Created by edward on 28/07/2015.
 */
@Entity
@Table(name = "promoCodes")
@NamedQueries({
        @NamedQuery(
                name = PromoCodeVO.FIND_ACTIVE_PROMO_CODE,
                query = "SELECT o " +
                        "FROM PromoCodeVO o " +
                        "WHERE o.code = :code " +
                        "AND o.enabled = TRUE"
        )
})
public class PromoCodeVO {

    public static final String FIND_ACTIVE_PROMO_CODE = "findActivePromoCode";

    @Id
    private String code;
    private boolean enabled;

    private String rewardName;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name="rewardName", referencedColumnName="rewardName")
    private List<PromoCodeRewardVO> rewards;

    @Enumerated(EnumType.STRING)
    private PromoCodeType type;

    @Convert(converter = JSONObjectConverter.class)
    private JSONObject config;

    @Transient
    private PromoCodeRewardVO userReward;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public List<PromoCodeRewardVO> getRewards() {
        return rewards;
    }

    public void setRewards(List<PromoCodeRewardVO> rewards) {
        this.rewards = rewards;
    }

    public PromoCodeType getType() {
        return type;
    }

    public void setType(PromoCodeType type) {
        this.type = type;
    }

    public JSONObject getConfig() {
        return config;
    }

    public void setConfig(JSONObject config) {
        this.config = config;
    }

    public PromoCodeRewardVO getUserReward() {
        return userReward;
    }

    public void setUserReward(PromoCodeRewardVO userReward) {
        this.userReward = userReward;
    }
}
