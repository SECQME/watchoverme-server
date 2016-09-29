package com.secqme.rs.v2;

import com.secqme.domain.model.UserVO;
import com.secqme.rs.BaseResource;
import com.secqme.util.marketing.MarketingCampaign;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/v2/payment")
public class PaymentResource extends BaseResource {

	private final static Logger myLog = Logger.getLogger(PaymentResource.class);
    public PaymentResource() {
    }

    @POST
    @Path("/google")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    /*
       {
          "authToken":"234234-234-234-234-234234",
          "googleInAppReceipt":
          {
             "orderId":"12999763169054705758.1371079406387615",
             "packageName":"com.example.app",
             "productId":"ar_pp_1m",
             "purchaseTime":1345678900000,
             "purchaseState":0,
             "developerPayload":"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ",
             "purchaseToken":"rojeslcdyyiapnqcynkjyyjh"
          }
        }
     */
    public String processGooglePayment(String reqBody) {
        try {
            JSONObject jobj = new JSONObject(reqBody);
            String authToken = jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
            String receiptData = jobj.getJSONObject(CommonJSONKey.GOOGLE_IN_APP_RECEIPT).toString();
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            googlePaymentUtil.processPayment(receiptData, userVO);
            //add user to marketing email after payment process
            //TODO - only put user once to marketing email
            userManager.addMarketingEmailContact(userVO, MarketingCampaign.PREMIUM_PACKAGE_CAMPAIGN);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return OK_STATUS;
    }

    /*
        {
            "authToken":"0416f2af-2a58-401b-8a29-f19c90f2f084",
            "appleVerifiedReceipt":
                   {"original_purchase_date_pst":"2014-05-14 03:22:39 America/Los_Angeles",
                   "original_transaction_id":"270000086861261",
                   "is_trial_period":"false",
                   "product_id":"1_PP_3M",
                   "original_purchase_date_ms":"1400062959000",
                   "original_purchase_date":"2014-05-14 10:22:39 Etc/GMT",
                   "purchase_date_pst":"2014-05-14 03:22:39 America/Los_Angeles","purchase_date_ms":"1400062959000",
                   "quantity":"1","purchase_date":"2014-05-14 10:22:39 Etc/GMT",
                   "transaction_id":"270000086861261"}


        }
     */
    @POST
    @Path("/apple/verifiedreceipt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String processApplePaymentWithReceipt(String reqBody) {

        try {
        	myLog.debug("receive verified receipt " + reqBody);
            JSONObject jobj = new JSONObject(reqBody);
            String authToken = jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            JSONObject verifiedReceiptObj =  jobj.getJSONObject(CommonJSONKey.APPLE_VERIFIED_RECEIPT_KEY);
            applePaymentUtil.processPaymentWithVerifiedReceipt(verifiedReceiptObj, userVO);

            // We move to normal email service just for welcoming email
            // Remove this in the future
//            userManager.addMarketingEmailContact(userVO, MarketingCampaign.PREMIUM_PACKAGE_CAMPAIGN);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }


        return OK_STATUS;
    }
    
    
    /*
      { "authToken":"0416f2af-2a58-401b-8a29-f19c90f2f084",
        "appleReceipt":"SSDFSDFDDFS"
      }
     */
    @POST
    @Path("/apple") 
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String processApplePayment(String reqBody) {

        try {
            JSONObject jobj = new JSONObject(reqBody);
            String authToken = jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            String receiptData = jobj.getString("appleReceipt");
            applePaymentUtil.processPayment(receiptData, userVO);

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        
        
        return OK_STATUS;
    }
    /*
     * {
		  "authToken":"{{userAuthToken}}",
		  "device":"ios", //ios or android
		  "packageCode":"1_PP_3M" //pricing package code user click
		}
     */
    @POST
    @Path("/paymentclick")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String insertPaymentClickRequest(String reqBody) {
    	try {
        	myLog.debug("insertPaymentClickRequest " + reqBody);
            JSONObject jobj = new JSONObject(reqBody);
            String authToken = jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
            String device = jobj.getString(CommonJSONKey.DEVICE_KEY);
            String packageCode = jobj.getString(CommonJSONKey.PACKAGE_CODE);
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            paymentManager.insertPaymentClickRequest(userVO, device, packageCode);

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }


        return OK_STATUS;
    }
    
    /*
    { "userid":["60-1112346789"],
    "month":12
    }
   */
	@POST
	@Path("/freepremium/{appid}/{appsecret}") 
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String freePremiumSubscription(@PathParam("appid") String applicationId,
          @PathParam("appsecret") String applicationSecret, String reqBody) {
		myLog.debug("Manual upgrade to premium:" + reqBody);
		try {
    	  	sysAdminUtil.grantClientAccess(applicationId, applicationSecret);
          	JSONObject jobj = new JSONObject(reqBody);
          	JSONArray useridArray = jobj.getJSONArray(CommonJSONKey.USER_ID_KEY);
          	int months = jobj.getInt(CommonJSONKey.MONTH);
          	for(int i=0;i<useridArray.length();i++) {
          		String userid = useridArray.getString(i);
          		UserVO userVO = userManager.getUserInfoByUserId(userid);
          		billingManager.freePremium(userVO, months);
          	}

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
      	}
      
      	return OK_STATUS;
	}

	@POST
    @Path("/verify/receipt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String verifyAppleReceipt(String reqBody) {
        // Dummy method
        // For testing purpose
        //
        return "{\"status\":0,\"receipt\":{\"app_item_id\":\"431208868\",\"product_id\":\"APPLE_12M_PP\"," +
                 "\"original_purchase_date_ms\":\"1363159592219\",\"original_purchase_date\":\"2013-03-13 07:26:32 Etc/GMT\"}}";
    }
    
}
