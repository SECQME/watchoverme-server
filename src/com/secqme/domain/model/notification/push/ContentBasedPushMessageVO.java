package com.secqme.domain.model.notification.push;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.ar.ARMessageTemplateField;
import com.secqme.domain.model.notification.MessageType;
import com.secqme.util.ar.ARTemplateEngine;
import com.secqme.util.ar.ParameterizableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by edward on 22/05/2015.
 */
public class ContentBasedPushMessageVO extends PushMessageVO {

    private static final Logger myLog = Logger.getLogger(ContentBasedPushMessageVO.class);

    private String body;

    public ContentBasedPushMessageVO(MessageType messageType, UserVO sender, PushMessageRecipientVO recipient, String body) {
        super(messageType, sender, recipient);
        this.body = body;
    }

    public ContentBasedPushMessageVO(MessageType messageType, UserVO sender, List<PushMessageRecipientVO> recipients, String body) {
        super(messageType, sender, recipients);
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public static List<ContentBasedPushMessageVO> fromTemplateBasedPushMessageVO(TemplateBasedPushMessageVO templateBasedPushMessageVO, ARTemplateEngine arTemplateEngine) {
        myLog.debug("Generate list of ContentBasedPushMessageVO from TemplateBasedPushMessageVO: " + templateBasedPushMessageVO.getMessageType());

        List<ContentBasedPushMessageVO> contentBasedPushMessageVOs = new ArrayList<ContentBasedPushMessageVO>(templateBasedPushMessageVO.getRecipients().size());
        for (PushMessageRecipientVO recipientVO : templateBasedPushMessageVO.getRecipients()) {
            Map<String, Object> params = ParameterizableUtils.mergeParams(templateBasedPushMessageVO, recipientVO);

            String body;
            if (StringUtils.isNotEmpty(templateBasedPushMessageVO.getCustomTemplateCode())) {
                body = arTemplateEngine.getProcessedMessageText(templateBasedPushMessageVO.getMarket(), templateBasedPushMessageVO.getCustomTemplateCode(), templateBasedPushMessageVO.getLangCode(), ARMessageTemplateField.PUSH_NOTIFICATION_MESSAGE, params);
            } else {
                body = arTemplateEngine.getProcessedMessageText(templateBasedPushMessageVO.getMarket(), templateBasedPushMessageVO.getMessageType().getDefaultTemplateCode(), templateBasedPushMessageVO.getLangCode(), ARMessageTemplateField.PUSH_NOTIFICATION_MESSAGE, params);
            }

            ContentBasedPushMessageVO contentBasedPushMessageVO = new ContentBasedPushMessageVO(templateBasedPushMessageVO.getMessageType(), templateBasedPushMessageVO.getSender(), recipientVO, body);
            contentBasedPushMessageVO.setPayloads(templateBasedPushMessageVO.getPayloads());
            contentBasedPushMessageVOs.add(contentBasedPushMessageVO);
        }

        return contentBasedPushMessageVOs;
    }

    public static List<ContentBasedPushMessageVO> separateToListContentBasedPushMessageVO(ContentBasedPushMessageVO contentBasedPushMessageVO) {
        myLog.debug("Generate list of ContentBasedPushMessageVO from ContentBasedPushMessageVO: " + contentBasedPushMessageVO.getMessageType());

        List<ContentBasedPushMessageVO> contentBasedPushMessageVOs = new ArrayList<ContentBasedPushMessageVO>(contentBasedPushMessageVO.getRecipients().size());
        for (PushMessageRecipientVO recipientVO : contentBasedPushMessageVO.getRecipients()) {
            ContentBasedPushMessageVO separatedContentBasedPushMessageVO = new ContentBasedPushMessageVO(contentBasedPushMessageVO.getMessageType(), contentBasedPushMessageVO.getSender(), recipientVO, contentBasedPushMessageVO.getBody());
            separatedContentBasedPushMessageVO.setPayloads(contentBasedPushMessageVO.getPayloads());
            contentBasedPushMessageVOs.add(separatedContentBasedPushMessageVO);
        }

        return contentBasedPushMessageVOs;
    }
}
