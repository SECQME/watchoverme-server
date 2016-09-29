package com.secqme.domain.model.promotion;

import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.payment.PricingPackageVO;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * User: James Khoo
 * Date: 12/9/13
 * Time: 1:50 PM
 */
@Entity
@Table(name = "promotions")
@NamedQueries({
        @NamedQuery(name = PromotionVO.QUERY_FIND_ALL,
                query = "SELECT o FROM PromotionVO o ")
})
public class PromotionVO implements Serializable {

//    # "promoteTitle":"GET WATCH OVER FOR ONLY RM 0.10 A DAY!"
//            # "promoteHeader":"To celebrate 2014, Watch Over Me will be available at a 50% discount for Malaysian at only RM38 a year (USD 11.99)"
//            # "promoteBullets":["Let the app watch over you via..","List as many emergency contacts as you.."]
//            # "promoteCallToActionText:"GET IT NOW USD 11.99/RM 38 a year"]

    private static final Logger myLog = Logger.getLogger(PromotionVO.class);

    public static final String QUERY_FIND_ALL = "promotionVO.findAll";
    public static final String PROMOTOE_CODE_KEY = "promoteCode";
    public static final String PROMOTE_TITLE_KEY = "promoteTitle";
    public static final String PROMOTE_HEADER_KEY = "promoteHeader";
    public static final String PROMOTE_BULLETS_KEY = "promoteBullets";
    public static final String PROMOTE_CALL_TO_ACTION_KEY = "promoteCallToActionText";
    public static final String FREEMIUM_TITLE_KEY = "freemiumTitle";
    public static final String FREEMIUM_HEADER_KEY = "freemiumHeader";
    public static final String FREEMIUM_COMPARE_TABLE_KEY = "freemiumCompareTable";
    public static final String FREEMIUM_CALL_TO_ACTION_KEY = "freemiumCallToActionText";
    public static final String PRICING_PACKAGE = "pricingPackages";
    @Id
    private String promoteCode;
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    private String promoteCountryList;
    private String defaultSignUpPackage;
    @Column(name = "pricingPackageList")
    private String pricingPackageListString;
    private String promoteTitle;
    private String promoteHeader;
    private String promoteBullets;
    private String promoteCallToAction;
    private String freemiumHeader;
    private String freemiumTitle;
    private String freemiumCompareTable;
    private String freemiumCallToAction;
    @Transient
    private List<PricingPackageVO> applePricingPkgList;
    @Transient
    private List<PricingPackageVO> androidPricingPkgList;
    @Transient
    private BillingPkgVO freemiumBillPkgVO;

    public PromotionVO() {
        // Empty Constructor
    }

    public String getPromoteCode() {
        return promoteCode;
    }

    public void setPromoteCode(String promoteCode) {
        this.promoteCode = promoteCode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getPromoteCountryList() {
        return promoteCountryList;
    }

    public void setPromoteCountryList(String promoteCountryList) {
        this.promoteCountryList = promoteCountryList;
    }

    public String getDefaultSignUpPackage() {
        return defaultSignUpPackage;
    }

    public void setDefaultSignUpPackage(String defaultSignUpPackage) {
        this.defaultSignUpPackage = defaultSignUpPackage;
    }

    public String getPromoteTitle() {
        return promoteTitle;
    }

    public void setPromoteTitle(String promoteTitle) {
        this.promoteTitle = promoteTitle;
    }

    public String getPromoteHeader() {
        return promoteHeader;
    }

    public void setPromoteHeader(String promoteHeader) {
        this.promoteHeader = promoteHeader;
    }

    public String getPromoteBullets() {
        return promoteBullets;
    }

    public void setPromoteBullets(String promoteBullets) {
        this.promoteBullets = promoteBullets;
    }

    public String getPromoteCallToAction() {
        return promoteCallToAction;
    }

    public void setPromoteCallToAction(String promoteCallToAction) {
        this.promoteCallToAction = promoteCallToAction;
    }

    public String getFreemiumHeader() {
        return freemiumHeader;
    }

    public void setFreemiumHeader(String freemiumHeader) {
        this.freemiumHeader = freemiumHeader;
    }

    public String getFreemiumTitle() {
        return freemiumTitle;
    }

    public void setFreemiumTitle(String freemiumTitle) {
        this.freemiumTitle = freemiumTitle;
    }

    public String getFreemiumCompareTable() {
        return freemiumCompareTable;
    }

    public void setFreemiumCompareTable(String freemiumCompareTable) {
        this.freemiumCompareTable = freemiumCompareTable;
    }

    public String getFreemiumCallToAction() {
        return freemiumCallToAction;
    }

    public void setFreemiumCallToAction(String freemiumCallToAction) {
        this.freemiumCallToAction = freemiumCallToAction;
    }

    public List<PricingPackageVO> getApplePricingPkgList() {
        return applePricingPkgList;
    }

    public void setApplePricingPkgList(List<PricingPackageVO> applePricingPkgList) {
        this.applePricingPkgList = applePricingPkgList;
    }

    public List<PricingPackageVO> getAndroidPricingPkgList() {
        return androidPricingPkgList;
    }

    public void setAndroidPricingPkgList(List<PricingPackageVO> androidPricingPkgList) {
        this.androidPricingPkgList = androidPricingPkgList;
    }

    public String getPricingPackageListString() {
        return pricingPackageListString;
    }

    public void setPricingPackageListString(String pricingPackageListString) {
        this.pricingPackageListString = pricingPackageListString;
    }

    public BillingPkgVO getFreemiumBillPkgVO() {
        return freemiumBillPkgVO;
    }

    public void setFreemiumBillPkgVO(BillingPkgVO freemiumBillPkgVO) {
        this.freemiumBillPkgVO = freemiumBillPkgVO;
    }

    public JSONObject toJSONObject() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put(PROMOTOE_CODE_KEY, this.getPromoteCode());
            jobj.put(PROMOTE_TITLE_KEY, this.getPromoteTitle());
            jobj.put(PROMOTE_HEADER_KEY, this.getPromoteHeader());
            jobj.put(PROMOTE_BULLETS_KEY, this.getPromoteBullets());
            jobj.put(PROMOTE_CALL_TO_ACTION_KEY, this.getPromoteCallToAction());
            jobj.put(FREEMIUM_TITLE_KEY, this.getFreemiumTitle());
            jobj.put(FREEMIUM_HEADER_KEY, this.getFreemiumHeader());
            jobj.put(FREEMIUM_COMPARE_TABLE_KEY, this.getFreemiumCompareTable());
            jobj.put(FREEMIUM_CALL_TO_ACTION_KEY, this.getFreemiumCallToAction());
            if (this.getApplePricingPkgList() != null || this.getAndroidPricingPkgList() != null) {
                JSONObject priceObj = new JSONObject();
                if (this.getApplePricingPkgList() != null) {
                    priceObj.put("APPLE", preparePricingArray(this.getApplePricingPkgList()));
                }

                if(this.getAndroidPricingPkgList() != null) {
                    priceObj.put("ANDROID", preparePricingArray(this.getAndroidPricingPkgList()));
                }

                jobj.put("pricingPackages", priceObj);
            }
        } catch (JSONException ex) {
            myLog.error("Failed to create JSONObject from: " + this, ex);
        }


        return jobj;
    }

    private JSONArray preparePricingArray(List<PricingPackageVO> pricePkgList) throws JSONException {
        JSONArray priceArray = new JSONArray();
        for (PricingPackageVO pricingPackageVO : pricePkgList) {
            priceArray.put(pricingPackageVO.toJSONObject());
        }
        return priceArray;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PromotionVO that = (PromotionVO) o;

        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) return false;
        if (promoteCode != null ? !promoteCode.equals(that.promoteCode) : that.promoteCode != null) return false;
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = promoteCode != null ? promoteCode.hashCode() : 0;
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("promoteCode", promoteCode)
                .append("startDate", startDate)
                .append("endDate", endDate)
                .append("promoteCountryList", promoteCountryList)
                .append("defaultSignUpPackage", defaultSignUpPackage)
                .append("promoteTitle", promoteTitle)
                .append("promoteHeader", promoteHeader)
                .append("promoteBullets", promoteBullets)
                .append("promoteCallToAction", promoteCallToAction)
                .toString();
    }
}
