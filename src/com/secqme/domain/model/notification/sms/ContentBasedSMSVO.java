package com.secqme.domain.model.notification.sms;

import com.secqme.domain.model.ar.ARMessageTemplateField;
import com.secqme.domain.model.notification.MessageType;
import com.secqme.util.ar.ARTemplateEngine;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edward on 18/05/2015.
 */
public class ContentBasedSMSVO extends SMSVO {
    private static Logger myLog = Logger.getLogger(ContentBasedSMSVO.class);

    private String body;

    public ContentBasedSMSVO(MessageType messageType, @NotNull SMSRecipientVO recipient, String body) {
        this(messageType, new ArrayList<SMSRecipientVO>(1), body);
        this.recipients.add(recipient);
    }

    public ContentBasedSMSVO(MessageType messageType, @NotNull List<SMSRecipientVO> recipients, String body) {
        super(messageType, recipients);
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public static List<ContentBasedSMSVO> fromTemplateBasedSMSVO(TemplateBasedSMSVO templateBasedSMSVO, ARTemplateEngine arTemplateEngine) {
        myLog.debug("Generate list of ContentBasedSMSVO from TemplateBasedSMSVO: " + templateBasedSMSVO.getMessageType());

        List<ContentBasedSMSVO> contentBasedSMSVOs = new ArrayList<ContentBasedSMSVO>(templateBasedSMSVO.getRecipients().size());
        for (SMSRecipientVO recipientVO : templateBasedSMSVO.getRecipients()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.putAll(templateBasedSMSVO.getParams());
            params.putAll(recipientVO.getParams());

            String body;
            if (StringUtils.isNotEmpty(templateBasedSMSVO.getCustomTemplateCode())) {
                body = arTemplateEngine.getProcessedMessageText(templateBasedSMSVO.getMarket(), templateBasedSMSVO.getCustomTemplateCode(), templateBasedSMSVO.getLangCode(), ARMessageTemplateField.SMS_BODY, params);
            } else {
                body = arTemplateEngine.getProcessedMessageText(templateBasedSMSVO.getMarket(), templateBasedSMSVO.getMessageType().getDefaultTemplateCode(), templateBasedSMSVO.getLangCode(), ARMessageTemplateField.SMS_BODY, params);
            }
            contentBasedSMSVOs.add(new ContentBasedSMSVO(templateBasedSMSVO.getMessageType(), recipientVO, body));
        }

        return contentBasedSMSVOs;
    }

    public static List<ContentBasedSMSVO> separateToListContentBasedSMSVO(ContentBasedSMSVO contentBasedSMSVO) {
        myLog.debug("Generate list of ContentBasedSMSVO from ContentBasedSMSVO: " + contentBasedSMSVO.getMessageType());

        List<ContentBasedSMSVO> contentBasedSMSVOs = new ArrayList<ContentBasedSMSVO>(contentBasedSMSVO.getRecipients().size());
        for (SMSRecipientVO recipientVO : contentBasedSMSVO.getRecipients()) {
            contentBasedSMSVOs.add(new ContentBasedSMSVO(contentBasedSMSVO.getMessageType(), recipientVO, contentBasedSMSVO.getBody()));
        }

        return contentBasedSMSVOs;
    }
}
