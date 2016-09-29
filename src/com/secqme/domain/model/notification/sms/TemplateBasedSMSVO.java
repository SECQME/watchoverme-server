package com.secqme.domain.model.notification.sms;

import com.secqme.domain.model.notification.MessageType;
import com.secqme.domain.model.ar.Parameterizable;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edward on 15/05/2015.
 */
public class TemplateBasedSMSVO extends SMSVO implements Parameterizable<String, Object> {

    private String market;
    private String langCode;
    private String customTemplateCode;
    private Map<String, Object> params;

    public TemplateBasedSMSVO(@NotNull MessageType messageType, @NotNull SMSRecipientVO recipient, String market, String langCode) {
        this(messageType, recipient, market, langCode, null);
    }

    public TemplateBasedSMSVO(@NotNull MessageType messageType, @NotNull SMSRecipientVO recipient, String market, String langCode, String customTemplateCode) {
        super(messageType, new ArrayList<SMSRecipientVO>());
        this.recipients.add(recipient);
        this.market = market;
        this.langCode= langCode;
        this.customTemplateCode = customTemplateCode;
        this.params = new HashMap<String, Object>();
    }

    public TemplateBasedSMSVO(@NotNull MessageType messageType, @NotNull List<SMSRecipientVO> recipients, String market, String langCode) {
        this(messageType, recipients, market, langCode, null);
    }

    public TemplateBasedSMSVO(@NotNull MessageType messageType, @NotNull List<SMSRecipientVO> recipients, String market, String langCode, String customTemplateCode) {
        super(messageType, recipients);
        this.market = market;
        this.langCode= langCode;
        this.customTemplateCode = customTemplateCode;
        this.params = new HashMap<String, Object>();
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public String getCustomTemplateCode() {
        return customTemplateCode;
    }

    public void setCustomTemplateCode(String customTemplateCode) {
        this.customTemplateCode = customTemplateCode;
    }

    @Override
    public void clearParam() {
        params.clear();
    }

    @Override
    public void putParam(String key, Object value) {
        params.put(key, value);
    }

    @Override
    public Object removeParam(String key) {
        return params.remove(key);
    }

    @Override
    public Map<String, Object> getParams() {
        return params;
    }
}
