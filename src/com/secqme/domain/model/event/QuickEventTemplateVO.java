package com.secqme.domain.model.event;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.io.Serializable;

/**
 * User: James Khoo
 * Date: 11/18/13
 * Time: 11:28 AM
 */
@Entity
@javax.persistence.IdClass(QuickEventTemplateVOPK.class)
@javax.persistence.Table(name = "quickEventTemplate")
@NamedQueries({
        @NamedQuery(name=QuickEventTemplateVO.QUERY_FIND_ALL,
                query = "SELECT o FROM QuickEventTemplateVO o ")
})
public class QuickEventTemplateVO implements Serializable {

    public static final String QUERY_FIND_ALL = "QuickEventTemplateVO.findAll";

    @javax.persistence.Column(name = "code")
    @Id
    private String code;
    @javax.persistence.Column(name = "langCode")
    @Id
    private String langCode;
    private String eventName;
    private Integer eventDuration;
    private String optionalDescription;

    public QuickEventTemplateVO() {
        // EmptyConstructor
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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Integer getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(Integer eventDuration) {
        this.eventDuration = eventDuration;
    }

    public String getOptionalDescription() {
        return optionalDescription;
    }

    public void setOptionalDescription(String optionalDescription) {
        this.optionalDescription = optionalDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuickEventTemplateVO that = (QuickEventTemplateVO) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (eventDuration != null ? !eventDuration.equals(that.eventDuration) : that.eventDuration != null)
            return false;
        if (eventName != null ? !eventName.equals(that.eventName) : that.eventName != null) return false;
        if (langCode != null ? !langCode.equals(that.langCode) : that.langCode != null) return false;
        if (optionalDescription != null ? !optionalDescription.equals(that.optionalDescription) : that.optionalDescription != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (langCode != null ? langCode.hashCode() : 0);
        result = 31 * result + (eventName != null ? eventName.hashCode() : 0);
        result = 31 * result + (eventDuration != null ? eventDuration.hashCode() : 0);
        result = 31 * result + (optionalDescription != null ? optionalDescription.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("QuickEventTemplateVO{");
        sb.append("code='").append(code).append('\'');
        sb.append(", langCode='").append(langCode).append('\'');
        sb.append(", eventName='").append(eventName).append('\'');
        sb.append(", eventDuration=").append(eventDuration);
        sb.append(", optionalDescription='").append(optionalDescription).append('\'');
        sb.append('}');
        return sb.toString();
    }


}
