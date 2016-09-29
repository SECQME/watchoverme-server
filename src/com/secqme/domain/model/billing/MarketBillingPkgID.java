package com.secqme.domain.model.billing;

import java.io.Serializable;

/**
 *
 * @author coolboykl
 */
public class MarketBillingPkgID implements Serializable {
    
    private String market;
    private String pkgName;
    
    public MarketBillingPkgID() {
        // Empty Constructor
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MarketBillingPkgID other = (MarketBillingPkgID) obj;
        if ((this.market == null) ? (other.market != null) : !this.market.equals(other.market)) {
            return false;
        }
        if ((this.pkgName == null) ? (other.pkgName != null) : !this.pkgName.equals(other.pkgName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.market != null ? this.market.hashCode() : 0);
        hash = 17 * hash + (this.pkgName != null ? this.pkgName.hashCode() : 0);
        return hash;
    }
    
    
    
}
