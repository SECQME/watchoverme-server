package com.secqme.domain.model.notification.sms;

import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.ar.Parameterizable;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by edward on 15/05/2015.
 */
public class SMSRecipientVO implements Serializable, Parameterizable<String, Object> {

    private static Logger myLog = Logger.getLogger(SMSRecipientVO.class);

    private CountryVO countryVO;
    private String mobileNumber;
    private Map<String, Object> params;

    public SMSRecipientVO(CountryVO countryVO, String mobileNumber) {
        this(countryVO, mobileNumber, new HashMap<String, Object>());
    }

    public SMSRecipientVO(CountryVO countryVO, String mobileNumber, @NotNull Map<String, Object> params) {
        this.countryVO = countryVO;
        this.mobileNumber = mobileNumber;
        this.params = params;
    }

    public CountryVO getCountryVO() {
        return countryVO;
    }

    public void setCountryVO(CountryVO countryVO) {
        this.countryVO = countryVO;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public void clearParam() {
        params.clear();
    }

    @Override
    public void putParam(String key, Object value) {
        params.put(key, value);
    }

    @Override
    public Object removeParam(String key) {
        return params.remove(key);
    }


    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(writer, this);
        } catch (IOException e) {
            myLog.error("Failed to serialize " + this.getClass().getName());
        }
        return writer.toString();
    }
}
