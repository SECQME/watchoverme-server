package com.secqme.rs;

import com.secqme.domain.model.UserVO;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/payment")
public class PaymentResource extends BaseResource {


    public PaymentResource() {
    }
    
    
    /*
      { "authToken":"0416f2af-2a58-401b-8a29-f19c90f2f084",
        "appleReceipt":"SSDFSDFDDFS"
        "market":"GW"
      }
      
     */
    @POST
    @Path("/apple") 
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String processApplePayment(String reqBody) {
        String result = OK_STATUS;
        try {
            JSONObject jobj = new JSONObject(reqBody);
            String authToken = jobj.getString("authToken");
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            String market = jobj.getString("market");
            String receiptData = jobj.getString("appleReceipt");
            applePaymentUtil.processPayment(receiptData, userVO);

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        
        
        return result;
    }
    
    @POST
    @Path("/verify/receipt")
    @Consumes("appllication/json")
    @Produces(MediaType.APPLICATION_JSON)
    public String verifyAppleReceipt(String reqBody) {
        // Dummy method
        // For testing purpose
        //
        return "{\"status\":0,\"receipt\":{\"app_item_id\":\"431208868\",\"product_id\":\"APPLE_12M_PP\"," +
                 "\"original_purchase_date_ms\":\"1363159592219\",\"original_purchase_date\":\"2013-03-13 07:26:32 Etc/GMT\"}}";
                
                
    }
}
