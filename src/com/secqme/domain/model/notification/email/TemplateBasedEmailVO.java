package com.secqme.domain.model.notification.email;

import com.secqme.domain.model.notification.MessageType;
import com.secqme.domain.model.ar.Parameterizable;
import com.secqme.util.ar.ParameterizableUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edward on 08/05/2015.
 */
public class TemplateBasedEmailVO extends EmailVO implements Parameterizable<String, Object> {

    private String market;
    private String langCode;
    private String customTemplateCode;
    private Map<String, Object> params;

    public TemplateBasedEmailVO(@NotNull MessageType messageType, @NotNull EmailRecipientVO emailRecipientVO, String market, String langCode) {
        this(messageType, emailRecipientVO, market, langCode, null);
    }

    public TemplateBasedEmailVO(@NotNull MessageType messageType, @NotNull EmailRecipientVO emailRecipientVO, String market, String langCode, String customTemplateCode) {
        super(messageType, new ArrayList<EmailRecipientVO>());
        this.recipients.add(emailRecipientVO);
        this.market = market;
        this.langCode= langCode;
        this.customTemplateCode = customTemplateCode;
        this.params = new HashMap<>();

        ParameterizableUtils.fillWithBaseUrl(this);
    }

    public TemplateBasedEmailVO(@NotNull MessageType messageType, @NotNull List<EmailRecipientVO> recipients, String market, String langCode) {
        this(messageType, recipients, market, langCode, null);
    }

    public TemplateBasedEmailVO(@NotNull MessageType messageType, @NotNull List<EmailRecipientVO> recipients, String market, String langCode, String customTemplateCode) {
        super(messageType, recipients);
        this.market = market;
        this.langCode= langCode;
        this.customTemplateCode = customTemplateCode;
        this.params = new HashMap<>();

        ParameterizableUtils.fillWithBaseUrl(this);
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
        return this.params;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
