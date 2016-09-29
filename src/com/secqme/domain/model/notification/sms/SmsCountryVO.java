package com.secqme.domain.model.notification.sms;

import com.secqme.domain.model.CountryVO;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Is represent a GW VO with
 * @author coolboykl
 */
@Entity
@Table(name="smsgwCountry")
public class SmsCountryVO implements Serializable, Comparable {
    
    public final static String COUNTRY_KEY = "country";
    public final static String COUNTRY_NAME_KEY = "countryName";
    public final static String COUNTRY_TEL_CODE_KEY = "countryCode";

    private static final Logger myLog = Logger.getLogger(SmsCountryVO.class);

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "gwName")
    private SmsGateWayVO smsGateWayVO;
    private Integer smsCredit;
    private String description;
    private String smsPreText;
    private String smsPostText;

    @OneToOne
    @JoinColumn(name="country")
    private CountryVO countryVO;

    public SmsCountryVO() {
        // Empty Constructor
    }

    public SmsGateWayVO getSmsGateWayVO() {
        return smsGateWayVO;
    }

    public void setSmsGateWayVO(SmsGateWayVO smsGateWayVO) {
        this.smsGateWayVO = smsGateWayVO;
    }


    public String getDescription() {
        return description;
    }

    public CountryVO getCountryVO() {
        return countryVO;
    }

    public void setCountryVO(CountryVO countryVO) {
        this.countryVO = countryVO;
    }

    public String getSmsPostText() {
        return smsPostText;
    }

    public void setSmsPostText(String smsPostText) {
        this.smsPostText = smsPostText;
    }

    public String getSmsPreText() {
        return smsPreText;
    }

    public void setSmsPreText(String smsPreText) {
        this.smsPreText = smsPreText;
    }

    public JSONObject toJSONObject() {
        JSONObject countryObj = new JSONObject();
        if(countryVO != null) {
            try {
                countryObj.put(COUNTRY_KEY, countryVO.getIso());
                countryObj.put(COUNTRY_NAME_KEY, countryVO.getCountryName());
                countryObj.put(COUNTRY_TEL_CODE_KEY, countryVO.getCallingCode());
            } catch (JSONException ex) {
                myLog.error("Failed to create JSONObject from: " + this, ex);
            }
        }
        return countryObj;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSmsCredit() {
        return smsCredit;
    }

    public void setSmsCredit(Integer smsCredit) {
        this.smsCredit = smsCredit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SmsCountryVO other = (SmsCountryVO) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if (this.smsGateWayVO != other.smsGateWayVO && (this.smsGateWayVO == null || !this.smsGateWayVO.equals(other.smsGateWayVO))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 83 * hash + (this.smsGateWayVO != null ? this.smsGateWayVO.hashCode() : 0);
        return hash;
    }

    public int compareTo(Object t) {
        if (t ==null) {
            return 1;
        }

        SmsCountryVO otherSmsCountryVO = (SmsCountryVO) t;

        return this.getCountryVO().compareTo(otherSmsCountryVO.getCountryVO());
    }
}
