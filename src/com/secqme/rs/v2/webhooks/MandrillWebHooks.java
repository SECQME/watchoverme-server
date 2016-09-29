package com.secqme.rs.v2.webhooks;

import com.secqme.domain.model.notification.email.EmailLogVO;
import com.secqme.domain.model.notification.email.EmailStatus;
import com.secqme.rs.BaseResource;
import com.secqme.util.notification.email.MandrillEmailService;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import java.io.UnsupportedEncodingException;

/**
 * Created by Edmund on 4/30/15.
 */
@Path("/webhooks/mandrill")
public class MandrillWebHooks extends BaseResource {

    private final static Logger myLog = Logger.getLogger(MandrillWebHooks.class);

    private static final String MANDRILL_JSON_KEY_EVENT = "event";
    private static final String MANDRILL_JSON_KEY_LOCATION = "location";
    private static final String MANDRILL_JSON_KEY_MSG = "msg";
    private static final String MANDRILL_JSON_KEY_ID = "_id";

    private static final String MANDRILL_JSON_KEY_COUNTRY = "country_short";
    private static final String MANDRILL_JSON_KEY_REGION = "region";
    private static final String MANDRILL_JSON_KEY_CITY = "city";
    private static final String MANDRILL_JSON_KEY_BOUNCE_DESCRIPTION = "bounce_description";

    private static final String MANDRILL_JSON_EVENT_OPEN = "open";
    private static final String MANDRILL_JSON_EVENT_CLICK = "click";

    @HEAD
    @Path("/events")
    public String headEvents(){
        return "";
    }

    @POST
    @Path("/events")
    @Produces("application/json")
    public void postEvents(String requestBody) {
        myLog.debug("Mandrill webhook events: " + requestBody);
        try {
            JSONArray mandrillEvents = convertMandrillEventsToJson(requestBody);
            for (int i = 0, length = mandrillEvents.length(); i < length; i++) {
                JSONObject mandrillEvent = mandrillEvents.getJSONObject(i);

                String messageId = mandrillEvent.getString(MANDRILL_JSON_KEY_ID);
                EmailStatus status = convertEmailStatus(mandrillEvent.getString(MANDRILL_JSON_KEY_EVENT));
                String city = null;
                String state = null;
                String country = null;
                String failedReason = mandrillEvent.getJSONObject(MANDRILL_JSON_KEY_MSG).optString(MANDRILL_JSON_KEY_BOUNCE_DESCRIPTION, null);

                try {
                    if (mandrillEvent.has(MANDRILL_JSON_KEY_LOCATION) && !mandrillEvent.isNull(MANDRILL_JSON_KEY_LOCATION)) {
                        JSONObject location = mandrillEvent.getJSONObject(MANDRILL_JSON_KEY_LOCATION);
                        city = location.optString(MANDRILL_JSON_KEY_CITY, null);
                        state = location.optString(MANDRILL_JSON_KEY_REGION, null);
                        country = location.optString(MANDRILL_JSON_KEY_COUNTRY, null);

                        // Handle "INVALID IPV4 ADDRESS" location
                        if (country == null || country.length() > 2) {
                            city = null;
                            state = null;
                            country = null;
                        }
                    }
                } catch (JSONException ex) {
                    myLog.error("Failed to process Mandrill webhook location, skip location information: " + mandrillEvent, ex);
                }

                myLog.debug("Update log for Mandrill event: " + mandrillEvent);
                EmailLogVO emailLogVO = emailLogManager.updateLog(MandrillEmailService.EMAIL_SERVICE_NAME, messageId, status, city, state, country, failedReason);

                if (emailLogVO != null) {
                    userManager.unsubscribeMarketingEmailIfNeeded(emailLogVO);
                }
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
    }

    private JSONArray convertMandrillEventsToJson(String requestBody) throws JSONException {
        try {
            requestBody = requestBody.replace("mandrill_events=", "").replace("mandrill_event=", "");
            String result = java.net.URLDecoder.decode(requestBody, "UTF-8");
            return new JSONArray(result);
        } catch (UnsupportedEncodingException ex) {
            myLog.debug("Failed to convert Mandrill events");
            return null;
        }
    }

    private EmailStatus convertEmailStatus(String mandrillEventName) {
        if (MANDRILL_JSON_EVENT_CLICK.contentEquals(mandrillEventName)) {
            return EmailStatus.CLICKED;
        } else if (MANDRILL_JSON_EVENT_OPEN.contentEquals(mandrillEventName)) {
            return EmailStatus.OPENED;
        } else if ("send".contentEquals(mandrillEventName)) {
            return EmailStatus.SENT;
        } else if ("deferral".contentEquals(mandrillEventName)) {
            return EmailStatus.DEFERRED;
        } else if ("hard_bounce".contentEquals(mandrillEventName)) {
            return EmailStatus.HARD_BOUNCED;
        } else if ("soft_bounce".contentEquals(mandrillEventName)) {
            return EmailStatus.SOFT_BOUNCED;
        } else if ("spam".contentEquals(mandrillEventName)) {
            return EmailStatus.MARKED_AS_SPAM;
        } else if ("unsub".contentEquals(mandrillEventName)) {
            return EmailStatus.UNSUBSCRIBED;
        } else if ("reject".contentEquals(mandrillEventName)) {
            return EmailStatus.REJECTED;
        } else {
            throw new IllegalArgumentException("Unknown Mandrill event name: " + mandrillEventName);
        }
    }
}
