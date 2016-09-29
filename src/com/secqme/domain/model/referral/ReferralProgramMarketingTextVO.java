package com.secqme.domain.model.referral;

import javax.persistence.*;
import java.io.Serializable;

/**
 * User: James Khoo
 * Date: 3/4/14
 * Time: 4:28 PM
 */
@Entity
@Table(name="referralProgramMarketingText")
@IdClass(ReferralProgramMarketingTextVOPK.class)
@NamedQueries({
        @NamedQuery(name=ReferralProgramMarketingTextVO.QUERY_FIND_ALL,
        query = "SELECT o " +
                "FROM ReferralProgramMarketingTextVO o ")
})
public class ReferralProgramMarketingTextVO implements Serializable {

    public static final String QUERY_FIND_ALL = "ReferralProgramMarketingText.findAll";

    @javax.persistence.Column(name = "code")
    @Id
    private String code;
    @javax.persistence.Column(name = "langCode")
    @Id
    private String langCode;

    private String marketingText;

    public ReferralProgramMarketingTextVO() {
        // Empty Constructor
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public String getMarketingText() {
        return marketingText;
    }

    public void setMarketingText(String marketingText) {
        this.marketingText = marketingText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferralProgramMarketingTextVO that = (ReferralProgramMarketingTextVO) o;

        if (!code.equals(that.code)) return false;
        if (!langCode.equals(that.langCode)) return false;
        if (!marketingText.equals(that.marketingText)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + langCode.hashCode();
        result = 31 * result + marketingText.hashCode();
        return result;
    }
}
