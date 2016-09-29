package com.secqme.domain.model.payment;

import com.secqme.domain.model.notification.*;
import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 *
 * @author jameskhoo
 */
public class UserSubscriptionID implements Serializable {
    private String userid;
    private String pricePkgCode;
    private String gwName;

    public UserSubscriptionID() {
        // Empty Constructor
    }

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
    }

    public String getPricePkgCode() {
        return pricePkgCode;
    }

    public void setPricePkgCode(String pricePkgCode) {
        this.pricePkgCode = pricePkgCode;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }


    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
