package com.secqme.domain.model.util;

import javax.persistence.*;
import java.io.Serializable;

/**
 * User: James Khoo
 * Date: 3/4/14
 * Time: 4:28 PM
 */
@Entity
@Table(name="marketingTextConfig")
@IdClass(MarketingTextConfigVOPK.class)
@NamedQueries({
        @NamedQuery(name= MarketingTextConfigVO.QUERY_FIND_ALL,
        query = "SELECT o " +
                "FROM MarketingTextConfigVO o ")
})
public class MarketingTextConfigVO implements Serializable {

    public static final String QUERY_FIND_ALL = "MarketingTextConfig.findAll";

    @Column(name = "code")
    @Id
    private String code;
    @Column(name = "langCode")
    @Id
    private String langCode;
    private String parameters;

    private String marketingText;

    public MarketingTextConfigVO() {
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

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarketingTextConfigVO that = (MarketingTextConfigVO) o;

        if (!code.equals(that.code)) return false;
        if (!langCode.equals(that.langCode)) return false;
        if (!marketingText.equals(that.marketingText)) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + langCode.hashCode();
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + marketingText.hashCode();
        return result;
    }
}
