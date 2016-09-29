package com.secqme.domain.model.payment;

import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.billing.MarketVO;
import com.secqme.domain.model.promotion.PromotionVO;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author coolboykl
 */
@Entity
@Table(name = "pricingPackages")
@NamedQueries({
    @NamedQuery(name = PricingPackageVO.QUERY_FIND_ACTIVE_PACKAGE_WITHOUT_PROMOTION_BY_MARKET,
    query = "SELECT p "
    + "FROM PricingPackageVO p "
    + "WHERE p.marketVO.name = :marketCode "
    + "AND p.effectiveDate <= :todayDate "
    + "AND p.active = :active "
    + "AND p.promoteCode IS NULL "
    + "ORDER BY p.quantity"),
    @NamedQuery(name = PricingPackageVO.QUERY_FIND_ALL_ACTIVE_PACKAGE_WITHPROMTOTION,
    query = "SELECT p "
    + "FROM PricingPackageVO p "
    + "WHERE p.effectiveDate <= :todayDate "
    + "AND p.active = :active "
    + "AND p.marketVO.name = :marketCode "
    + "AND p.promoteCode IS NOT NULL "
    + "ORDER BY p.quantity")
})
public class PricingPackageVO implements Serializable {
    private static Logger myLog = Logger.getLogger(PricingPackageVO.class);

    public static final String DEFAULT_MARKET_CODE =  "default";

    public final static String QUERY_FIND_ACTIVE_PACKAGE_WITHOUT_PROMOTION_BY_MARKET =
            "pricingPackageVO.findActivePkgByMarket";
    public final static String QUERY_FIND_ALL_ACTIVE_PACKAGE_WITHPROMTOTION = "pricingPkgVO.findAllActivePkgWithPromotion";
    public static final String PACKAGE_CODE_KEY = "pkgCode";
    public static final String PACKAGE_NAME_KEY = "pkgName";
    public static final String AUTO_RENEW_KEY = "autoRenew";
    public static final String PRICE_KEY = "price";
    public static final String APPLE_PRICE_KEY = "applePrice";
    public static final String QUANTITY_KEY = "quantity";
    public static final String PCK_DESC_KEY = "pkgDesc";
    public static final String PCK_DESC_HTML_KEY = "pkgDescHtml";
    public static final String CURRENCY_CODE_KEY = "currencyCode";
    public static final String APPLE_PRODUCT_CODE = "appleProductCode";
    public static final String ADDITIONAL_CONFIG_KEY = "additionalConfig";
    @Id
    private String pkgCode;
    @ManyToOne
    @JoinColumn(name = "market")
    private MarketVO marketVO;
    @ManyToOne
    @JoinColumn(name = "pkgName")
    private BillingPkgVO billPkgVO;
    private Double price;
    private Boolean autoRenew;
    private Integer quantity;
    private String additionalConfig;
    private String pkgDesc;
    private String pricePkgName;
    private String pkgDescHTML;
    private String currencyCode;
    private Double applePrice;
    @Temporal(TemporalType.TIMESTAMP)
    private Date effectiveDate;
    private Boolean active;
    private String appleProductCode;
    private String promoteCode;
    
    public PricingPackageVO() {
        // empty constructor
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getAdditionalConfig() {
        return additionalConfig;
    }

    public String getPkgDesc() {
        return pkgDesc;
    }

    public void setPkgDesc(String pkgDesc) {
        this.pkgDesc = pkgDesc;
    }

    public void setAdditionalConfig(String additionalConfig) {
        this.additionalConfig = additionalConfig;
    }

    public Boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public BillingPkgVO getBillPkgVO() {
        return billPkgVO;
    }

    public void setBillPkgVO(BillingPkgVO billPkgVO) {
        this.billPkgVO = billPkgVO;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getPkgCode() {
        return pkgCode;
    }

    public void setPkgCode(String pkgCode) {
        this.pkgCode = pkgCode;
    }

    public MarketVO getMarketVO() {
        return marketVO;
    }

    public void setMarketVO(MarketVO marketVO) {
        this.marketVO = marketVO;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getPkgDescHTML() {
        return pkgDescHTML;
    }

    public void setPkgDescHTML(String pkgDescHTML) {
        this.pkgDescHTML = pkgDescHTML;
    }

    public String getPricePkgName() {
        return pricePkgName;
    }

    public void setPricePkgName(String pricePkgName) {
        this.pricePkgName = pricePkgName;
    }

    public Double getApplePrice() {
        return applePrice;
    }

    public void setApplePrice(Double applePrice) {
        this.applePrice = applePrice;
    }

    public String getAppleProductCode() {
        return appleProductCode;
    }

    public void setAppleProductCode(String appleProductCode) {
        this.appleProductCode = appleProductCode;
    }

    public Boolean getAutoRenew() {
        return autoRenew;
    }

    public Boolean getActive() {
        return active;
    }

    public String getPromoteCode() {
        return promoteCode;
    }

    public void setPromoteCode(String promoteCode) {
        this.promoteCode = promoteCode;
    }

    public JSONObject toJSONObject() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put(PACKAGE_CODE_KEY, this.getPkgCode());
            jobj.put(PACKAGE_NAME_KEY, this.getPricePkgName());
            jobj.put(PCK_DESC_HTML_KEY, this.getPkgDescHTML());
            jobj.put(PCK_DESC_KEY, this.getPkgDesc());
            jobj.put(AUTO_RENEW_KEY, this.isAutoRenew());
            jobj.put(PRICE_KEY, this.getPrice());
            jobj.put(APPLE_PRICE_KEY, this.getApplePrice());
            jobj.put(CURRENCY_CODE_KEY, this.getCurrencyCode());
            jobj.put(ADDITIONAL_CONFIG_KEY, this.getAdditionalConfig());
            jobj.put(QUANTITY_KEY, this.getQuantity());
            jobj.put(APPLE_PRODUCT_CODE, this.getAppleProductCode());
            jobj.put(PromotionVO.PROMOTOE_CODE_KEY, this.getPromoteCode());
        } catch (JSONException ex) {
            myLog.error("Failed to create JSONObject from: " + this, ex);
        }

        return jobj;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PricingPackageVO other = (PricingPackageVO) obj;
        if (this.pkgCode != other.pkgCode && (this.pkgCode == null || !this.pkgCode.equals(other.pkgCode))) {
            return false;
        }
        if (this.marketVO != other.marketVO && (this.marketVO == null || !this.marketVO.equals(other.marketVO))) {
            return false;
        }
        if (this.billPkgVO != other.billPkgVO && (this.billPkgVO == null || !this.billPkgVO.equals(other.billPkgVO))) {
            return false;
        }
        if (this.price != other.price && (this.price == null || !this.price.equals(other.price))) {
            return false;
        }
        if (this.autoRenew != other.autoRenew && (this.autoRenew == null || !this.autoRenew.equals(other.autoRenew))) {
            return false;
        }
        if (this.quantity != other.quantity && (this.quantity == null || !this.quantity.equals(other.quantity))) {
            return false;
        }
        if ((this.additionalConfig == null) ? (other.additionalConfig != null) : !this.additionalConfig.equals(other.additionalConfig)) {
            return false;
        }
        if (this.effectiveDate != other.effectiveDate && (this.effectiveDate == null || !this.effectiveDate.equals(other.effectiveDate))) {
            return false;
        }
        if (this.active != other.active && (this.active == null || !this.active.equals(other.active))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.pkgCode != null ? this.pkgCode.hashCode() : 0);
        hash = 13 * hash + (this.marketVO != null ? this.marketVO.hashCode() : 0);
        hash = 13 * hash + (this.billPkgVO != null ? this.billPkgVO.hashCode() : 0);
        hash = 13 * hash + (this.price != null ? this.price.hashCode() : 0);
        hash = 13 * hash + (this.autoRenew != null ? this.autoRenew.hashCode() : 0);
        hash = 13 * hash + (this.quantity != null ? this.quantity.hashCode() : 0);
        hash = 13 * hash + (this.additionalConfig != null ? this.additionalConfig.hashCode() : 0);
        hash = 13 * hash + (this.effectiveDate != null ? this.effectiveDate.hashCode() : 0);
        hash = 13 * hash + (this.active != null ? this.active.hashCode() : 0);
        return hash;
    }
}
