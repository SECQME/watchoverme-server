package com.secqme.domain.model.billing;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author coolboykl
 */
@Entity
@Table(name="marketBillingPackage")
@IdClass(MarketBillingPkgID.class)
public class MarketBillingPkgVO implements Serializable {
    
    @Id
    @Column(name = "market", insertable = false, updatable = false)
    private String market;
    @Id
    @Column(name = "pkgName", insertable = false, updatable = false)
    private String pkgName;
    
    @ManyToOne
    @JoinColumn(name = "market")
    private MarketVO marketVO;
    
    @ManyToOne
    @JoinColumn(name = "pkgName")
    private BillingPkgVO billingPkgVO;
    
    private Boolean defaultPkg;
    private String currencyCode;
    private Boolean defaultFreemium;
    private Boolean defaultTrialPackage;
    private Boolean defaultPremium;
    
    public MarketBillingPkgVO() {
        // Empty Constructor
    }

    public BillingPkgVO getBillingPkgVO() {
        return billingPkgVO;
    }

    public void setBillingPkgVO(BillingPkgVO billingPkgVO) {
        this.billingPkgVO = billingPkgVO;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Boolean isDefaultPkg() {
        return defaultPkg;
    }

    public void setDefaultPkg(Boolean defaultPkg) {
        this.defaultPkg = defaultPkg;
    }


    public MarketVO getMarketVO() {
        return marketVO;
    }

    public void setMarketVO(MarketVO marketVO) {
        this.marketVO = marketVO;
    }

    public Boolean isDefaultFreemium() {
        return defaultFreemium;
    }

    public void setDefaultFreemium(Boolean defaultFreemium) {
        this.defaultFreemium = defaultFreemium;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public Boolean isDefaultTrialPackage() {
        return defaultTrialPackage;
    }

    public void setDefaultTrialPackage(Boolean defaultTrialPackage) {
        this.defaultTrialPackage = defaultTrialPackage;
    }

    public Boolean isDefaultPremium() {
		return defaultPremium;
	}

	public void setDefaultPremium(Boolean defaultPremium) {
		this.defaultPremium = defaultPremium;
	}

	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MarketBillingPkgVO other = (MarketBillingPkgVO) obj;
        if (this.marketVO != other.marketVO && (this.marketVO == null || !this.marketVO.equals(other.marketVO))) {
            return false;
        }
        if (this.billingPkgVO != other.billingPkgVO && (this.billingPkgVO == null || !this.billingPkgVO.equals(other.billingPkgVO))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.marketVO != null ? this.marketVO.hashCode() : 0);
        hash = 97 * hash + (this.billingPkgVO != null ? this.billingPkgVO.hashCode() : 0);
        return hash;
    }


  
    
    
    
}
