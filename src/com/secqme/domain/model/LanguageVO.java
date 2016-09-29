package com.secqme.domain.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author coolboykl
 */
@Entity
@Table(name = "languages")
@NamedQueries({
        @NamedQuery(name = LanguageVO.QUERY_FIND_ALL,
                query = "SELECT o "
                        + "FROM LanguageVO o "),
        @NamedQuery(name = LanguageVO.QUERY_FIND_DEFAULT_LANGUAGE,
                query = "SELECT o " +
                        "FROM LanguageVO o " +
                        "WHERE o.defaultLanguage = true")
})
public class LanguageVO implements Serializable {

    public static final String QUERY_FIND_ALL = "LanguageVO.findAll";
    public static final String QUERY_FIND_DEFAULT_LANGUAGE = "LanguageVO.findDefaultLanguage";

    // in the format of zh_CH
    @Id
    private String code;

    @Column(name = "defaultLang")
    private Boolean defaultLanguage;

    private String direction;
    private String abbreviation;
    private String remark;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public Boolean isDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(Boolean defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    @Override
    public String toString() {
        return "LanguageVO{" + "code=" + code + ", defaultLanguage=" + defaultLanguage + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.code != null ? this.code.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LanguageVO other = (LanguageVO) obj;
        if ((this.code == null) ? (other.code != null) : !this.code.equals(other.code)) {
            return false;
        }
        return true;
    }


}
