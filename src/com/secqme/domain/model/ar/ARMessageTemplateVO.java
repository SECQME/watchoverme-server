package com.secqme.domain.model.ar;


import javax.persistence.*;

import com.secqme.domain.model.LanguageVO;

/**
 *
 * @author coolboykl
 */
@Entity
@Table(name="arMsgTemplate")
@IdClass(ARMessageTemplateKey.class)
@NamedQueries({
    @NamedQuery(name = ARMessageTemplateVO.QUERY_FIND_ALL,
    query = "SELECT o FROM ARMessageTemplateVO o ")
})
public class ARMessageTemplateVO {
    
    public static final String QUERY_FIND_ALL = "arMessageTemplateVO.findAll";
    public static final String ENGINE_FREE_MARKER = "FreeMarker";
    public static final String ENGINE_MANDRILL = "Mandrill";

    @Id
    @Column(name = "templateCode", insertable = false, updatable = false)
    private String templateCode;

    @Id
    @Column(name = "langCode", insertable = false, updatable = false)
    private String langCode;
    
    @ManyToOne
    @JoinColumn(name = "templateCode")
    private ARMarketMessageTemplateVO arMarketMessageTemplateVO;
    
    @ManyToOne
    @JoinColumn(name = "langCode")
    private LanguageVO languageVO;
    
    private String emailTemplate;
    private String emailSubject;
    private String smsTemplate;
    private String pushMessageTemplate;
    private String fbTemplate;
    private String mandrillTemplateName;

    public String getTemplateValue(String engine, ARMessageTemplateField type) {
        if (ENGINE_FREE_MARKER.contentEquals(engine)) {
            switch (type) {
                case EMAIL_BODY:
                    return emailTemplate;
                case EMAIL_SUBJECT:
                    return emailTemplate;
                case FACEBOOK_POST:
                    return fbTemplate;
                case PUSH_NOTIFICATION_MESSAGE:
                    return pushMessageTemplate;
                case SMS_BODY:
                    return smsTemplate;
            }
        } else if (ENGINE_MANDRILL.contentEquals(engine)) {
            switch (type) {
                case EMAIL_TEMPLATE_NAME:
                    return mandrillTemplateName;
            }
        }
        return null;
    }

    public String getEmailTemplate() {
        return emailTemplate;
    }

    public void setEmailTemplate(String emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    public String getSmsTemplate() {
        return smsTemplate;
    }

    public void setSmsTemplate(String smsTemplate) {
        this.smsTemplate = smsTemplate;
    }

    public String getPushMessageTemplate() {
        return pushMessageTemplate;
    }

    public void setPushMessageTemplate(String pushMessageTemplate) {
        this.pushMessageTemplate = pushMessageTemplate;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getFbTemplate() {
        return fbTemplate;
    }

    public void setFbTemplate(String fbTemplate) {
        this.fbTemplate = fbTemplate;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public ARMarketMessageTemplateVO getArMarketMessageTemplateVO() {
        return arMarketMessageTemplateVO;
    }

    public void setArMarketMessageTemplateVO(ARMarketMessageTemplateVO arMarketMessageTemplateVO) {
        this.arMarketMessageTemplateVO = arMarketMessageTemplateVO;
    }

    public LanguageVO getLanguageVO() {
        return languageVO;
    }

    public void setLanguageVO(LanguageVO languageVO) {
        this.languageVO = languageVO;
    }

    public String getMandrillTemplateName() {
        return mandrillTemplateName;
    }

    public void setMandrillTemplateName(String mandrillTemplateName) {
        this.mandrillTemplateName = mandrillTemplateName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.templateCode != null ? this.templateCode.hashCode() : 0);
        hash = 31 * hash + (this.langCode != null ? this.langCode.hashCode() : 0);
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
        final ARMessageTemplateVO other = (ARMessageTemplateVO) obj;
        if ((this.templateCode == null) ? (other.templateCode != null) : !this.templateCode.equals(other.templateCode)) {
            return false;
        }
        if ((this.langCode == null) ? (other.langCode != null) : !this.langCode.equals(other.langCode)) {
            return false;
        }
        return true;
    }
    
    
    
}
