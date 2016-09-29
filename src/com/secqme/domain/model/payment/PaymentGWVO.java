package com.secqme.domain.model.payment;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author coolboykl
 */
@Entity
@Table(name="paymentGW")
@NamedQueries({
    @NamedQuery(name = PaymentGWVO.QUERY_FIND_ALL,
    query = "SELECT o FROM PaymentGWVO o ")
})
public class PaymentGWVO implements Serializable {

    private static Logger myLog = Logger.getLogger(PaymentGWVO.class);

    public final static String QUERY_FIND_ALL = "PaymentGWVO.findAll";
    
    @Id
    private String gwName;
    
    @Column(name="gwDescription")
    private String description;
    
    @Column(name="additionalConfig")
    private String configString;
    
    private String implClass;
    private Boolean defaultGW;
    public PaymentGWVO() {
        // 
    }

    public Boolean getDefaultGW() {
        return defaultGW;
    }

    public void setDefaultGW(Boolean defaultGW) {
        this.defaultGW = defaultGW;
    }
    
    
    
    public JSONObject getConfig() {
        JSONObject jsonObject = null;
        if(configString != null) {
            try {
                jsonObject = new JSONObject(configString);
            } catch (JSONException ex) {
                myLog.error("Failed to serialize " + this.getClass().getName());
            }
        } 
        return jsonObject;
    }
    
    public void updateConfig(JSONObject jobj) {
       configString = (jobj == null ? null : jobj.toString());
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
    }

    public String getImplClass() {
        return implClass;
    }

    public void setImplClass(String implClass) {
        this.implClass = implClass;
    }

    
//    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PaymentGWVO other = (PaymentGWVO) obj;
        if ((this.gwName == null) ? (other.gwName != null) : !this.gwName.equals(other.gwName)) {
            return false;
        }
        if ((this.configString == null) ? (other.configString != null) : !this.configString.equals(other.configString)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.gwName != null ? this.gwName.hashCode() : 0);
        hash = 89 * hash + (this.configString != null ? this.configString.hashCode() : 0);
        return hash;
    }
    
    
    
    
    
}
