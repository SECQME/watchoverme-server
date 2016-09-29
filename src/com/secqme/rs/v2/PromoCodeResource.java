package com.secqme.rs.v2;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.promocode.PromoCodeLogVO;
import com.secqme.domain.model.promocode.PromoCodeRewardVO;
import com.secqme.domain.model.promocode.PromoCodeVO;
import com.secqme.rs.BaseResource;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by edward on 28/07/2015.
 */
@Path("/v2/promocode")
public class PromoCodeResource extends BaseResource {

    private Logger myLog = Logger.getLogger(PromoCodeResource.class);

    @GET
    @Path("/{authToken}/{promoCode}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String validatePromoCode(@PathParam("authToken") String authToken, @PathParam("promoCode") String promoCode) {
        myLog.debug("Check promo code based authToken: " + authToken + ", promoCode: " + promoCode);

        UserVO userVO = userManager.getUserByActivationCode(authToken);
        PromoCodeVO promoCodeVO = promoCodeManager.validatePromoCode(userVO, promoCode);
        return preparePromoCodeJSON(promoCodeVO).toString();
    }

    @POST
    @Path("/apply")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String applyPromoCode(String reqBody) {
        myLog.debug("Apply promo code: " + reqBody);
        try {
            JSONObject jobj = new JSONObject(reqBody);
            String authToken = jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
            String promoCode = jobj.getString(CommonJSONKey.PROMO_CODE_KEY);

            UserVO userVO = userManager.getUserByActivationCode(authToken);
            PromoCodeVO promoCodeVO = promoCodeManager.applyPromoCode(userVO, promoCode);
            return preparePromoCodeJSON(promoCodeVO).toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
    }

    private JSONObject preparePromoCodeJSON(PromoCodeVO promoCodeVO) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(CommonJSONKey.PROMO_CODE_KEY, promoCodeVO.getCode());
            jsonObject.put("type", promoCodeVO.getType());
            jsonObject.put("config", promoCodeVO.getConfig());

            PromoCodeRewardVO reward = promoCodeVO.getUserReward();
            JSONObject jsonReward = new JSONObject();
            jsonReward.put("name", reward.getRewardName());
            jsonReward.put("forBillingPackage", reward.getForBillingPackageVO() == null ? null : reward.getForBillingPackageVO().getPkgName());
            jsonReward.put("rewardBillingPackage", reward.getRewardBillingPackageVO() == null ? null :  reward.getRewardBillingPackageVO().getPkgName());
            jsonReward.put("rewardBillingCycleDays", reward.getRewardBillingCycleDays());
            jsonReward.put("rewardSMS", reward.getRewardSMS());
            jsonObject.put("reward", jsonReward);
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }

        return jsonObject;
    }
}
