package com.secqme.domain.model.notification.sns;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.ar.ARMessageTemplateField;
import com.secqme.domain.model.notification.MessageType;
import com.secqme.domain.model.notification.sms.ContentBasedSMSVO;
import com.secqme.domain.model.notification.sms.SMSRecipientVO;
import com.secqme.domain.model.notification.sms.TemplateBasedSMSVO;
import com.secqme.util.ar.ARTemplateEngine;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edward on 22/05/2015.
 */
public class ContentBasedSNSMessageVO extends SNSMessageVO {
    private static Logger myLog = Logger.getLogger(ContentBasedSNSMessageVO.class);

    private String body;

    public ContentBasedSNSMessageVO(MessageType messageType, UserVO userVO, String body) {
        super(messageType, userVO);
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public static ContentBasedSNSMessageVO fromTemplateBasedSNSMessageVO(TemplateBasedSNSMessageVO templateBasedSNSMessageVO, ARTemplateEngine arTemplateEngine, ARMessageTemplateField field) {
        myLog.debug("Generate ContentBasedSNSMessageVO from TemplateBasedSNSMessageVO: " + templateBasedSNSMessageVO.getMessageType());

        String body;
        if (StringUtils.isNotEmpty(templateBasedSNSMessageVO.getCustomTemplateCode())) {
            body = arTemplateEngine.getProcessedMessageText(templateBasedSNSMessageVO.getMarket(), templateBasedSNSMessageVO.getCustomTemplateCode(), templateBasedSNSMessageVO.getLangCode(), field, templateBasedSNSMessageVO.getParams());
        } else {
            body = arTemplateEngine.getProcessedMessageText(templateBasedSNSMessageVO.getMarket(), templateBasedSNSMessageVO.getMessageType().getDefaultTemplateCode(), templateBasedSNSMessageVO.getLangCode(), field, templateBasedSNSMessageVO.getParams());
        }

        return new ContentBasedSNSMessageVO(templateBasedSNSMessageVO.getMessageType(), templateBasedSNSMessageVO.getUserVO(), body);
    }
}
