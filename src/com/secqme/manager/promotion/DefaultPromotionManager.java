package com.secqme.manager.promotion;

import com.secqme.CoreException;
import com.secqme.domain.dao.BillingPkgDAO;
import com.secqme.domain.dao.PricingPackageDAO;
import com.secqme.domain.dao.PromotionDAO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.payment.PricingPackageVO;
import com.secqme.domain.model.promotion.PromotionVO;
import com.secqme.util.cache.CacheUtil;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * User: James Khoo
 * Date: 12/9/13
 * Time: 4:02 PM
 */
public class DefaultPromotionManager implements PromotionManager {

    private static final String PROMOTION_BY_COUNTRY_HASH_KEY = "promotionCountryHashMap";
    private static final String PROMOTION_PRICING_PACKAGE_HASH_KEY = "pricingPackages";
    private static final String PROMOTION_BILLING_PACKAGE_HASH_KEY = "billingPackages";
    private static final String PROMOTION_FREEMIUM_PKG_HASH_KEY = "freePackages";
    private static final org.apache.log4j.Logger myLog = org.apache.log4j.Logger.getLogger(DefaultPromotionManager.class);
    private PromotionDAO promotionDAO;
    private PricingPackageDAO pricingPackageDAO;
    private BillingPkgDAO billPackageDAO;
    private CacheUtil cacheUtil;
//    private HashMap<String, PromotionVO> promotionByCountryHashMap;

    public DefaultPromotionManager(PromotionDAO promoteDAO, PricingPackageDAO pricePkgDAO,
                                   BillingPkgDAO billPackageDAO,
                                   CacheUtil cacheUtil) {
        this.promotionDAO = promoteDAO;
        this.pricingPackageDAO = pricePkgDAO;
        this.billPackageDAO = billPackageDAO;
        this.cacheUtil = cacheUtil;
    }

    public void initPromotion() {
        getPromotionByCountryHashMap();
    }

    public BillingPkgVO getFreemiumBillingPkg(String promoteCode) {
        return getFreemiumBillingPackageHashMap().get(promoteCode);
    }

    public PromotionVO getUserCurrentPromotion(UserVO userVO) throws CoreException {
        PromotionVO promotionVO = null;
        if (userVO.getCountryVO() != null) {
            myLog.debug("Attempt to get Promotion for user:" + userVO.getUserid() + ",->Country:" + userVO.getCountryVO().getIso());
            promotionVO = getPromotionByCountryHashMap().get(userVO.getCountryVO().getIso());
        }

        return (promotionVO != null &&
                promotionVO.getStartDate().getTime() <= new Date().getTime() &&
                promotionVO.getEndDate().getTime() >= new Date().getTime()) ?
                promotionVO : null;
    }

    private List<PricingPackageVO> initPricingPkgListForPromotion(JSONArray priceListArray) throws JSONException {
        List<PricingPackageVO> priceList = new ArrayList<PricingPackageVO>();
        for (int i = 0; i < priceListArray.length(); i++) {
            if (getPricingPkgHashMap().get(priceListArray.get(i)) != null) {
                priceList.add(getPricingPkgHashMap().get(priceListArray.get(i)));
            }
        }
        return priceList;
    }

    private HashMap<String, BillingPkgVO> getFreemiumBillingPackageHashMap() {
        HashMap<String, BillingPkgVO> freemiumBillingPackageHashMap =
                (HashMap<String, BillingPkgVO>) cacheUtil.getCachedObject(CacheUtil.PROMOTION_KEY + PROMOTION_FREEMIUM_PKG_HASH_KEY);
        if (freemiumBillingPackageHashMap == null) {
            init();
            freemiumBillingPackageHashMap = (HashMap<String, BillingPkgVO>)
                    cacheUtil.getCachedObject(CacheUtil.PROMOTION_KEY + PROMOTION_FREEMIUM_PKG_HASH_KEY);

        }

        return freemiumBillingPackageHashMap;
    }

    private HashMap<String, BillingPkgVO> getBillingPackageHashMap() {
        HashMap<String, BillingPkgVO> billingPkgVOHashMap =
                (HashMap<String, BillingPkgVO>) cacheUtil.getCachedObject(CacheUtil.PROMOTION_KEY + PROMOTION_BILLING_PACKAGE_HASH_KEY);
        if (billingPkgVOHashMap == null) {
            billingPkgVOHashMap = new HashMap<String, BillingPkgVO>();
            List<BillingPkgVO> billingPkgVOList = billPackageDAO.findAllBillingPackage();
            if (billingPkgVOList != null) {
                for (BillingPkgVO billingPkgVO : billingPkgVOList) {
                    billingPkgVOHashMap.put(billingPkgVO.getPkgName(), billingPkgVO);
                }
                cacheUtil.storeObjectIntoCache(CacheUtil.PROMOTION_KEY + PROMOTION_BILLING_PACKAGE_HASH_KEY, billingPkgVOHashMap);
            }
        }
        return billingPkgVOHashMap;
    }

    private HashMap<String, PromotionVO> getPromotionByCountryHashMap() {
        HashMap<String, PromotionVO> promotionVOHashMap =
                (HashMap<String, PromotionVO>) cacheUtil.getCachedObject(CacheUtil.PROMOTION_KEY + PROMOTION_BY_COUNTRY_HASH_KEY);

        if (promotionVOHashMap == null) {
            init();
            promotionVOHashMap = (HashMap<String, PromotionVO>) cacheUtil.getCachedObject(CacheUtil.PROMOTION_KEY + PROMOTION_BY_COUNTRY_HASH_KEY);

        }
        return promotionVOHashMap;
    }

    private void init() {

        HashMap<String, PromotionVO> promotionVOHashMap = new HashMap<String, PromotionVO>();
        HashMap<String, BillingPkgVO> freemiumBillingPackageHashMap = new HashMap<String, BillingPkgVO>();
        try {
            List<PromotionVO> promotionVOList = promotionDAO.findAll();
            if (promotionVOList != null && promotionVOList.size() > 0) {
                for (PromotionVO promotionVO : promotionVOList) {
                    JSONObject pricingPkgListObj = new JSONObject(promotionVO.getPricingPackageListString());
                    if (pricingPkgListObj.has("APPLE")) {
                        promotionVO.setApplePricingPkgList(initPricingPkgListForPromotion(pricingPkgListObj.getJSONArray("APPLE")));
                    }

                    if (pricingPkgListObj.has("ANDROID")) {
                        promotionVO.setAndroidPricingPkgList(initPricingPkgListForPromotion(pricingPkgListObj.getJSONArray("ANDROID")));
                    }

                    if (promotionVO.getDefaultSignUpPackage() != null) {
                        BillingPkgVO freeBillPkgVO = getBillingPackageHashMap().get(promotionVO.getDefaultSignUpPackage());
                        promotionVO.setFreemiumBillPkgVO(freeBillPkgVO);
                        freemiumBillingPackageHashMap.put(promotionVO.getPromoteCode(), freeBillPkgVO);
                    }

                    JSONArray countryArray = new JSONArray(promotionVO.getPromoteCountryList());
                    for (int i = 0; i < countryArray.length(); i++) {
                        promotionVOHashMap.put(countryArray.getString(i), promotionVO);
                        myLog.debug("Init " + countryArray.getString(i) + ", promotion, promoteCode:" + promotionVO.getPromoteCode());
                    }
                }
                cacheUtil.storeObjectIntoCache(CacheUtil.PROMOTION_KEY + PROMOTION_BY_COUNTRY_HASH_KEY, promotionVOHashMap);
                cacheUtil.storeObjectIntoCache(CacheUtil.PROMOTION_KEY + PROMOTION_FREEMIUM_PKG_HASH_KEY, freemiumBillingPackageHashMap);
            }
        } catch (JSONException ex) {
            myLog.trace("Error", ex);
        }
    }

    private HashMap<String, PricingPackageVO> getPricingPkgHashMap() {
        HashMap<String, PricingPackageVO> pricingPkgHashMap =
                (HashMap<String, PricingPackageVO>) cacheUtil.getCachedObject(CacheUtil.PROMOTION_KEY + PROMOTION_PRICING_PACKAGE_HASH_KEY);

        if (pricingPkgHashMap == null) {
            pricingPkgHashMap = new HashMap<String, PricingPackageVO>();
            List<PricingPackageVO> pricingPkgList = pricingPackageDAO.findAllActivePackageWithPromotion(PricingPackageVO.DEFAULT_MARKET_CODE);
            if (pricingPkgList != null && pricingPkgList.size() > 0) {
                for (PricingPackageVO pricingPackageVO : pricingPkgList) {
                    pricingPkgHashMap.put(pricingPackageVO.getPkgCode(), pricingPackageVO);

                }
                cacheUtil.storeObjectIntoCache(CacheUtil.PROMOTION_KEY + PROMOTION_PRICING_PACKAGE_HASH_KEY, pricingPkgHashMap);
                myLog.debug("Total " + pricingPkgList.size() + " initialized");
            }
        }
        return pricingPkgHashMap;
    }


}
