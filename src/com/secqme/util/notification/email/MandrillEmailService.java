package com.secqme.util.notification.email;

import com.secqme.domain.dao.EmailLogDAO;
import com.secqme.domain.model.ar.ARMessageTemplateField;
import com.secqme.domain.model.notification.*;
import com.secqme.domain.model.notification.email.*;
import com.secqme.util.SystemProperties;
import com.secqme.util.ar.ARTemplateEngine;
import com.secqme.util.rest.RestUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created by Edmund on 3/5/15.
 */
public class MandrillEmailService implements EmailService {
    public static final String EMAIL_SERVICE_NAME = "Mandrill";

    private static final Logger myLog = Logger.getLogger(MandrillEmailService.class);

    private static final String MANDRILL_API_MESSAGE_INFO = "/messages/info.json";
    private static final String MANDRILL_API_MESSAGES_SEND_NORMAL = "/messages/send.json";
    private static final String MANDRILL_API_MESSAGES_SEND_TEMPLATE = "/messages/send-template.json";

    private static final String MANDRILL_PROP_BASE_URL = "mandrill.rest.url";
    private static final String MANDRILL_PROP_API_KEY = "mandrill.rest.api.key";

    private static final String MANDRILL_JSON_KEY = "key";
    private static final String MANDRILL_JSON_TEMPLATE_CONTENT = "template_content";
    private static final String MANDRILL_JSON_TEMPLATE_NAME = "template_name";
    private static final String MANDRILL_JSON_ID = "id";
    private static final String MANDRILL_JSON_MSG = "message";
    private static final String MANDRILL_JSON_HTML = "html";
    private static final String MANDRILL_JSON_SUBJECT = "subject";
    private static final String MANDRILL_JSON_FROMEMAIL = "from_email";
    private static final String MANDRILL_JSON_FROMNAME = "from_name";
    private static final String MANDRILL_JSON_EMAIL = "email";
    private static final String MANDRILL_JSON_TO = "to";
    private static final String MANDRILL_JSON_TYPE = "type";
    private static final String MANDRILL_JSON_HEADER = "headers";
    private static final String MANDRILL_JSON_REPLYTO = "Reply-To";
    private static final String MANDRILL_JSON_MERGE_VARS = "merge_vars";
    private static final String MANDRILL_JSON_RCPT = "rcpt";
    private static final String MANDRILL_JSON_NAME = "name";
    private static final String MANDRILL_JSON_CONTENT = "content";
    private static final String MANDRILL_JSON_VARS = "vars";
    private static final String MANDRILL_JSON_TAGS = "tags";

    private static final String MANDRILL_STATUS_SENT = "sent";
    private static final String MANDRILL_STATUS_QUEUED = "queued";
    private static final String MANDRILL_STATUS_REJECTED = "rejected";
    private static final String MANDRILL_STATUS_INVALID = "invalid";
    public static final String MANDRILL_JSON_GLOBAL_MERGE_VARS = "global_merge_vars";

    private ARTemplateEngine arTemplateEngine;
    private EmailLogDAO emailLogDAO;
    private RestUtil restUtil = null;
    private String baseUrl = null;
    private String apiKey = null;

    public MandrillEmailService(ARTemplateEngine arTemplateEngine, EmailLogDAO emailLogDAO, RestUtil restUtil) {
        this.arTemplateEngine = arTemplateEngine;
        this.emailLogDAO = emailLogDAO;
        this.restUtil = restUtil;
        initEmailService();
    }

    private void initEmailService(){
        SystemProperties sysProp = SystemProperties.instance;

        baseUrl = sysProp.getString(MANDRILL_PROP_BASE_URL);
        apiKey = sysProp.getString(MANDRILL_PROP_API_KEY);
    }

    @Override
    public boolean checkEmail(String messageId, String recipientEmail) {
        String endpointUrl = baseUrl + MANDRILL_API_MESSAGE_INFO;

        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put(MANDRILL_JSON_KEY, apiKey);
            jsonRequest.put(MANDRILL_JSON_ID, messageId);

            Properties headerProperties = new Properties();
            headerProperties.put("content-type", MediaType.APPLICATION_JSON);
            String response = restUtil.executePost(endpointUrl, jsonRequest.toString(), headerProperties);

            myLog.debug(String.format("MandrillEmailService.checkEmail(%s, %s) -> %s", messageId, recipientEmail, response));

            JSONObject jsonResponse = new JSONObject(response);
            return (jsonResponse.has("email") && jsonResponse.getString("email").equals(recipientEmail));
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public List<EmailLogVO> sendEmail(EmailVO emailVO, OnBeforeEmailLogSavedListener listener) {
        myLog.debug("MandrillEmailService.sendEmail(): " + emailVO);

        try {
            JSONObject jsonRequest = createMandrillRequest(emailVO);
            myLog.debug("Mandrill request: " + jsonRequest.toString());

            String endpointUrl = getSendMessageEndpoint(emailVO);
            Properties headerProperties = new Properties();
            headerProperties.put("content-type", MediaType.APPLICATION_JSON);

            String response = restUtil.executePost(endpointUrl, jsonRequest.toString(), headerProperties);
            myLog.debug("Mandrill response: " + response);

            return saveEmailLogs(emailVO, response, listener);
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
            return null;
        }
    }

    private JSONObject createMandrillRequest(EmailVO emailVO) throws JSONException {
        JSONObject jsonRequest = new JSONObject();

        jsonRequest.put(MANDRILL_JSON_KEY, apiKey);

        if (emailVO instanceof TemplateBasedEmailVO) {
            TemplateBasedEmailVO templateBasedEmailVO = (TemplateBasedEmailVO) emailVO;

            String templateName;
            if (StringUtils.isNotEmpty(templateBasedEmailVO.getCustomTemplateCode())) {
                templateName = arTemplateEngine.getTemplateMessageText(templateBasedEmailVO.getMarket(), templateBasedEmailVO.getCustomTemplateCode(), templateBasedEmailVO.getLangCode(), ARMessageTemplateField.EMAIL_TEMPLATE_NAME);
            } else {
                templateName = arTemplateEngine.getTemplateMessageText(templateBasedEmailVO.getMarket(), templateBasedEmailVO.getMessageType().getDefaultTemplateCode(), templateBasedEmailVO.getLangCode(), ARMessageTemplateField.EMAIL_TEMPLATE_NAME);
            }
            jsonRequest.put(MANDRILL_JSON_TEMPLATE_NAME, templateName);
            jsonRequest.put(MANDRILL_JSON_TEMPLATE_CONTENT, new JSONArray());
        }

        JSONObject msgObj = createMsg(emailVO);
        jsonRequest.put(MANDRILL_JSON_MSG, msgObj);

        return jsonRequest;
    }

    private JSONObject createMsg(EmailVO emailVO) throws JSONException {
        JSONObject msgObj = new JSONObject();

        if (emailVO instanceof ContentBasedEmailVO) {
            ContentBasedEmailVO contentBasedEmailVO = (ContentBasedEmailVO) emailVO;
            msgObj.put(MANDRILL_JSON_HTML, contentBasedEmailVO.getBody());
            msgObj.put(MANDRILL_JSON_SUBJECT, contentBasedEmailVO.getSubject());
        } else if (emailVO instanceof TemplateBasedEmailVO) {
            TemplateBasedEmailVO templateBasedEmailVO = (TemplateBasedEmailVO) emailVO;
            msgObj.put(MANDRILL_JSON_GLOBAL_MERGE_VARS, createGlobalMergeVars(templateBasedEmailVO));
            msgObj.put(MANDRILL_JSON_MERGE_VARS, createMergeVars(emailVO.getRecipients()));
        }

        msgObj.put(MANDRILL_JSON_TAGS, createEmailTags(emailVO));
        if (emailVO.getSenderEmail() != null) msgObj.put(MANDRILL_JSON_FROMEMAIL, emailVO.getSenderEmail());
        if (emailVO.getSenderName() != null) msgObj.put(MANDRILL_JSON_FROMNAME, emailVO.getSenderName());
        msgObj.put(MANDRILL_JSON_TO, createRecipients(emailVO.getRecipients()));
        if (emailVO.getReplyToEmail() != null) msgObj.put(MANDRILL_JSON_HEADER, new JSONObject().put(MANDRILL_JSON_REPLYTO, emailVO.getReplyToEmail()));

        return msgObj;
    }

    private JSONArray createRecipients(List<EmailRecipientVO> emailRecipientVOs) throws JSONException {
        JSONArray recipients = new JSONArray();
        for (EmailRecipientVO recipientVO : emailRecipientVOs) {
            JSONObject emailTo = new JSONObject();
            emailTo.put(MANDRILL_JSON_NAME, recipientVO.getName());
            emailTo.put(MANDRILL_JSON_EMAIL, recipientVO.getEmail());
            emailTo.put(MANDRILL_JSON_TYPE, MANDRILL_JSON_TO);
            recipients.put(emailTo);
        }

        return recipients;
    }

    private JSONArray createEmailTags(EmailVO emailVO) {
        return new JSONArray(emailVO.getTags());
    }

    private JSONArray createGlobalMergeVars(TemplateBasedEmailVO templateBasedEmailVO) throws JSONException {
        JSONArray mergeVars = new JSONArray();
        for (Map.Entry<String, Object> param : templateBasedEmailVO.getParams().entrySet()) {
            JSONObject var = new JSONObject();
            var.put(MANDRILL_JSON_NAME, param.getKey());
            var.put(MANDRILL_JSON_CONTENT, param.getValue());

            mergeVars.put(var);
        }
        return mergeVars;
    }

    private JSONArray createMergeVars(List<EmailRecipientVO> emailRecipientVOs) throws JSONException {
        JSONArray mergeVars = new JSONArray();
        for (EmailRecipientVO emailRecipientVO : emailRecipientVOs) {
            JSONObject mergeVar = new JSONObject();
            mergeVar.put(MANDRILL_JSON_RCPT, emailRecipientVO.getEmail());

            JSONArray vars = new JSONArray();
            for (Map.Entry<String, Object> param :  emailRecipientVO.getParams().entrySet()) {
                JSONObject var = new JSONObject();
                var.put(MANDRILL_JSON_NAME, param.getKey());
                var.put(MANDRILL_JSON_CONTENT, param.getValue());

                vars.put(var);
            }
            mergeVar.put(MANDRILL_JSON_VARS, vars);

            mergeVars.put(mergeVar);
        }

        return mergeVars;
    }

    private String getSendMessageEndpoint(EmailVO emailVO) {
        if (emailVO instanceof ContentBasedEmailVO) {
            return baseUrl + MANDRILL_API_MESSAGES_SEND_NORMAL;
        } else if (emailVO instanceof TemplateBasedEmailVO) {
            return baseUrl + MANDRILL_API_MESSAGES_SEND_TEMPLATE;
        }
        return null;
    }

    private List<EmailLogVO> saveEmailLogs(EmailVO emailVO, String mandrillResponse, OnBeforeEmailLogSavedListener listener) throws JSONException {
        JSONArray jsonArray = new JSONArray(mandrillResponse);

        int length = jsonArray.length();
        List<EmailLogVO> emailLogVOs = new ArrayList<EmailLogVO>(length);

        for (int i = 0; i < length; i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            EmailLogVO emailLogVO = createEmailTransactionVO(item.getString("_id"), emailVO.getMessageType(), item.getString("email"), item.getString("status"), item.getString("reject_reason"));
            EmailRecipientVO emailRecipientVO = findEmailRecipientVO(emailVO, emailLogVO.getRecipientEmail());
            if (listener != null) {
                listener.onBeforeEmailLogSaved(emailVO, emailRecipientVO, emailLogVO);
            }
            emailLogDAO.create(emailLogVO);
            emailLogVOs.add(emailLogVO);
        }

        return emailLogVOs;
    }

    private EmailRecipientVO findEmailRecipientVO(EmailVO emailVO, String recipientEmail) {
        for (EmailRecipientVO emailRecipientVO : emailVO.getRecipients()) {
            if (emailRecipientVO.getEmail().equalsIgnoreCase(recipientEmail)) {
                return emailRecipientVO;
            }
        }
        return null;
    }

    private EmailLogVO createEmailTransactionVO(String messageId, MessageType messageType, String email, String status, String rejectReason) {
        EmailLogVO emailLogVO = new EmailLogVO();
        emailLogVO.setEmailServiceProvider(EMAIL_SERVICE_NAME);
        emailLogVO.setEmailServiceMessageId(messageId);
        emailLogVO.setMessageType(messageType);
        emailLogVO.setRequesterUser(null);
        emailLogVO.setRecipientUser(null);
        emailLogVO.setRecipientName(null);
        emailLogVO.setRecipientEmail(email);

        emailLogVO.setStatus(EmailStatus.INVALID);

        if (MANDRILL_STATUS_SENT.equals(status)) {
            emailLogVO.setStatus(EmailStatus.SENT);
        } else if (MANDRILL_STATUS_QUEUED.equals(status)) {
            emailLogVO.setStatus(EmailStatus.DEFERRED);
        } else if (MANDRILL_STATUS_REJECTED.equals(status)) {
            emailLogVO.setStatus(EmailStatus.REJECTED);
        } else if (MANDRILL_STATUS_INVALID.equals(status)) {
            emailLogVO.setStatus(EmailStatus.INVALID);
        }

        emailLogVO.setCity(null);
        emailLogVO.setState(null);
        emailLogVO.setCountry(null);

        if (!"null".equals(rejectReason)) {
            emailLogVO.setFailedReason(rejectReason);
        }
        emailLogVO.setCreatedAt(new Date());

        emailLogVO.setCampaignId(null);
        emailLogVO.setContactId(null);
        emailLogVO.setEventId(null);

        return emailLogVO;
    }
}
