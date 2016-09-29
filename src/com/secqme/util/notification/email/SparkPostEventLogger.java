package com.secqme.util.notification.email;

import com.secqme.domain.dao.EmailLogDAO;
import com.secqme.domain.model.notification.email.EmailLogVO;
import com.secqme.util.rest.RestUtil;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.util.ISO8601DateFormat;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParsePosition;
import java.util.List;
import java.util.Properties;

/**
 * This feature is POSTPONE.
 *
 * Created by edward on 31/03/2016.
 */
public class SparkPostEventLogger {
    private static final Logger myLog = Logger.getLogger(SparkPostEventLogger.class);

    private RestUtil restUtil;
    private EmailLogDAO emailLogDAO;
    private String baseUrl;
    private String apiKey;

    public SparkPostEventLogger(RestUtil restUtil) {
        this.restUtil = restUtil;
    }

    public List<EmailLogVO> saveTransmissionLog(String transmissionId) throws JSONException {
        int perPage = 1000;
        int page = 0;
        int total = 0;

        do {
            page++;
            JSONObject jsonResponse = getTransmissionLog(transmissionId, page, perPage);

            JSONArray jsonLogs = jsonResponse.getJSONArray("results");
            for (int i = 0, len = jsonLogs.length(); i < len; i++) {
                JSONObject jsonLog = jsonLogs.getJSONObject(i);

                EmailLogVO emailLogVO = emailLogDAO.findByProviderAndMessageId(SparkPostEmailService.EMAIL_SERVICE_NAME, jsonLog.getString("message_id"));
                if (emailLogVO != null) {
                    emailLogDAO.update(emailLogVO);
                } else {
                    emailLogVO = new EmailLogVO();

                    ISO8601DateFormat dateFormat = new ISO8601DateFormat();
                    dateFormat.parse(jsonLog.getString("tdate"), new ParsePosition(0));

                    emailLogDAO.create(emailLogVO);
                }
            }

            total = jsonResponse.optInt("total_count", 0);
        } while(page * perPage < total);

        return null;
    }


    private JSONObject getTransmissionLog(String transmissionId, int page, int perPage) throws JSONException {
        try {
            Properties headers = new Properties();
            headers.put("Content-Type", MediaType.APPLICATION_JSON);
            headers.put("Accept", MediaType.APPLICATION_JSON);
            headers.put("Authorization", apiKey);

            String endpoint = baseUrl + "/api/v1/message-events?transmission_ids=" + URLEncoder.encode(transmissionId, "UTF-8") + "&page=" + page + "&per_page=" + perPage;
            return new JSONObject(restUtil.executeGet(endpoint, headers));
        } catch (UnsupportedEncodingException e) {
            myLog.error("Unsupported encoding", e);
        }

        return null;
    }

    private void fillLog(EmailLogVO emailLogVO, JSONObject jsonLog) throws JSONException {
        emailLogVO.setEmailServiceProvider(SparkPostEmailService.EMAIL_SERVICE_NAME);
        emailLogVO.setEmailServiceMessageId(jsonLog.getString("message_id"));
        emailLogVO.setRecipientEmail(jsonLog.getString("raw_rcpt_to"));

        if (jsonLog.has("geo_ip")) {
            JSONObject jsonGeo = jsonLog.getJSONObject("geo_ip");
            emailLogVO.setCity(jsonGeo.getString("city"));
            emailLogVO.setState(jsonGeo.getString("region"));
            emailLogVO.setCountry(jsonGeo.getString("country"));
        }
    }
}
