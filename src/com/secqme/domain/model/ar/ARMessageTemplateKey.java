package com.secqme.domain.model.ar;

import java.io.Serializable;

/**
 *
 * @author coolboykl
 */
public class ARMessageTemplateKey implements Serializable {

    private String templateCode;
    private String langCode;

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.templateCode != null ? this.templateCode.hashCode() : 0);
        hash = 17 * hash + (this.langCode != null ? this.langCode.hashCode() : 0);
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
        final ARMessageTemplateKey other = (ARMessageTemplateKey) obj;
        if ((this.templateCode == null) ? (other.templateCode != null) : !this.templateCode.equals(other.templateCode)) {
            return false;
        }
        if ((this.langCode == null) ? (other.langCode != null) : !this.langCode.equals(other.langCode)) {
            return false;
        }
        return true;
    }
}