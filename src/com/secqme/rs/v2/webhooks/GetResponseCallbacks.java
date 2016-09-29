package com.secqme.rs.v2.webhooks;

import com.secqme.rs.BaseResource;
import org.apache.log4j.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Edmund on 5/6/15.
 */
@Path("/webhooks/getresponse")
public class GetResponseCallbacks extends BaseResource {

    private static final Logger myLog = Logger.getLogger(GetResponseCallbacks.class);
    public static final String ACTION_UNSUBSCRIBE = "unsubscribe";

    @Path("/events")
    @GET
    public String getEvents(@DefaultValue("") @QueryParam("action") String action,
                          @DefaultValue("") @QueryParam("ACCOUNT_ID") String accountId,
                          @DefaultValue("") @QueryParam("account_login") String accountLogin,
                          @DefaultValue("") @QueryParam("CAMPAIGN_ID") String campaignID,
                          @DefaultValue("") @QueryParam("campaign_name") String campaignName,
                          @DefaultValue("") @QueryParam("MESSAGE_ID") String messageID,
                          @DefaultValue("") @QueryParam("message_name") String messageName,
                          @DefaultValue("") @QueryParam("message_subject") String messageSubject,
                          @DefaultValue("") @QueryParam("CONTACT_ID") String contactId,
                          @DefaultValue("") @QueryParam("contact_name") String contactName,
                          @DefaultValue("") @QueryParam("contact_email") String contactEmail) {
        try {
            action = URLDecoder.decode(action, "UTF-8");
            contactEmail = URLDecoder.decode(contactEmail, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            myLog.error("Cannot encode request", ex);
        }

        myLog.debug("GetResponse callback: " + action + " - " + accountId + " - " + accountLogin + " - " + campaignID + " - " + campaignName + " - " + messageID + " - " + messageName + " - " + messageSubject + " - " + contactId + " - " + contactName + " - " + contactEmail + " ");
        if (ACTION_UNSUBSCRIBE.contentEquals(action)) {
            userManager.unsubscribeMarketingEmail(contactEmail);
        }

        return BaseResource.OK_STATUS;
    }
}

