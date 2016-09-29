package com.secqme.domain.model.event;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * User: James Khoo
 * Date: 11/18/13
 * Time: 11:28 AM
 */
public class QuickEventTemplateVOPK implements Serializable {
    private String code;
    private String langCode;

    @Id
    @Column(name = "code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Id
    @Column(name = "langCode")
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

        QuickEventTemplateVOPK that = (QuickEventTemplateVOPK) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (langCode != null ? !langCode.equals(that.langCode) : that.langCode != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (langCode != null ? langCode.hashCode() : 0);
        return result;
    }
}
