package com.secqme.domain.model.util;

import java.io.Serializable;

/**
 * User: James Khoo
 * Date: 3/27/14
 * Time: 2:40 PM
 */
public class MarketingTextConfigVOPK implements Serializable {

    private String code;
    private String langCode;

    public MarketingTextConfigVOPK() {
        // Empty Constructor;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarketingTextConfigVOPK that = (MarketingTextConfigVOPK) o;

        if (!code.equals(that.code)) return false;
        if (!langCode.equals(that.langCode)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + langCode.hashCode();
        return result;
    }
}