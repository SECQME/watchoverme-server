package com.secqme.util.notification.email;

import com.secqme.domain.dao.EmailLogDAO;
import com.secqme.domain.model.ar.ARMessageTemplateField;
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
import java.util.List;
import java.util.Properties;

/**
 * Created by edward on 28/03/2016.
 */
public class SparkPostEmailService implements EmailService {

    public static final String EMAIL_SERVICE_NAME = "SparkPost";

    private static final Logger myLog = Logger.getLogger(SparkPostEmailService.class);

    private static final String SPARKPOST_PROP_API_KEY = "sparkpost.api_key";
    private static final String SPARKPOST_PROP_BASE_URL = "sparkpost.base_url";

    private ARTemplateEngine arTemplateEngine;
    private EmailLogDAO emailLogDAO;
    private RestUtil restUtil = null;
    private String baseUrl = null;
    private String apiKey = null;

    public SparkPostEmailService(ARTemplateEngine arTemplateEngine, EmailLogDAO emailLogDAO, RestUtil restUtil) {
        this.arTemplateEngine = arTemplateEngine;
        this.emailLogDAO = emailLogDAO;
        this.restUtil = restUtil;
        initEmailService();
    }

    private void initEmailService(){
        SystemProperties sysProp = SystemProperties.instance;
        apiKey = sysProp.getString(SPARKPOST_PROP_API_KEY);
        baseUrl = sysProp.getString(SPARKPOST_PROP_BASE_URL);
    }

    @Override
    public boolean checkEmail(String messageId, String recipientEmail) {
        return false;
    }

    @Override
    public List<EmailLogVO> sendEmail(EmailVO emailVO, OnBeforeEmailLogSavedListener listener) throws JSONException {
        myLog.debug("SparkPostEmailService.sendEmail(): " + emailVO);

        Properties headers = new Properties();
        headers.put("Content-Type", MediaType.APPLICATION_JSON);
        headers.put("Accept", MediaType.APPLICATION_JSON);
        headers.put("Authorization", apiKey);

        String endpointUrl = baseUrl + "/api/v1/transmissions";

        JSONObject jsonRequest = createTransmissionRequest(emailVO);

        myLog.debug("SparkPost request: " + jsonRequest);

        String response = restUtil.executePost(endpointUrl, jsonRequest.toString(), headers);

        myLog.debug("SparkPost response: " + response);

        // TODO: SparkPost email logging
        return null;
    }

    private JSONObject createTransmissionRequest(EmailVO emailVO) throws JSONException {
        JSONObject jsonRequest = new JSONObject();
        JSONObject jsonContent = new JSONObject();

        if (emailVO instanceof TemplateBasedEmailVO) {
            TemplateBasedEmailVO templateBasedEmailVO = (TemplateBasedEmailVO) emailVO;

            String templateName;
            if (StringUtils.isNotEmpty(templateBasedEmailVO.getCustomTemplateCode())) {
                templateName = arTemplateEngine.getTemplateMessageText(templateBasedEmailVO.getMarket(), templateBasedEmailVO.getCustomTemplateCode(), templateBasedEmailVO.getLangCode(), ARMessageTemplateField.EMAIL_TEMPLATE_NAME);
            } else {
                templateName = arTemplateEngine.getTemplateMessageText(templateBasedEmailVO.getMarket(), templateBasedEmailVO.getMessageType().getDefaultTemplateCode(), templateBasedEmailVO.getLangCode(), ARMessageTemplateField.EMAIL_TEMPLATE_NAME);
            }

            jsonContent.put("template_id", templateName);

            if (templateBasedEmailVO.getParams() != null) {
                JSONObject jsonSubstitution = new JSONObject(templateBasedEmailVO.getParams());
                jsonRequest.put("substitution_data", jsonSubstitution);
            }
        }

        jsonRequest.put("content", jsonContent);
        jsonRequest.put("recipients", createRecipients(emailVO.getRecipients()));

        return jsonRequest;
    }

    private JSONArray createRecipients(List<EmailRecipientVO> emailRecipientVOs) throws JSONException {
        JSONArray recipients = new JSONArray();
        for (EmailRecipientVO recipientVO : emailRecipientVOs) {
            JSONObject jsonRecipient = new JSONObject();

            JSONObject jsonAddress = new JSONObject();
            jsonAddress.put("email", recipientVO.getEmail());
            jsonAddress.put("name", recipientVO.getName());

            jsonRecipient.put("address", jsonAddress);

            if (recipientVO.getParams() != null) {
                JSONObject jsonSubstitution = new JSONObject(recipientVO.getParams());
                jsonRecipient.put("substitution_data", jsonSubstitution);
            }

            recipients.put(jsonRecipient);
        }

        return recipients;
    }
}
