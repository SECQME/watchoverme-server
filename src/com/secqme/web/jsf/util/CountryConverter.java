package com.secqme.web.jsf.util;

import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.notification.sms.SmsCountryVO;
import com.secqme.util.notification.sms.SMSManager;
import com.secqme.util.spring.BeanType;
import com.secqme.util.spring.DefaultSpringUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author coolboykl
 */
public class CountryConverter implements Converter {
    private final static Logger myLog = Logger.getLogger(CountryConverter.class);

    private SMSManager smsManager = null;
    private HashMap<String, CountryVO> smsCountryHashMap = null;

    public CountryConverter() {
        smsManager = (SMSManager) DefaultSpringUtil.getInstance().getBean(BeanType.smsManager);
        smsCountryHashMap = new HashMap<String, CountryVO>();
        if (smsManager.getSupportedSMSCountryList() != null) {
            for (SmsCountryVO smsCountryVO : smsManager.getSupportedSMSCountryList()) {
                smsCountryHashMap.put(smsCountryVO.getCountryVO().getIso(), smsCountryVO.getCountryVO());
            }
        }
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String newValue) {
        CountryVO countryVO = null;
        try {
            countryVO = smsCountryHashMap.get(newValue);
        } catch (Throwable ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return countryVO;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = "";
        try {
            if (value != null) {
                CountryVO countryVO = (CountryVO) value;
                val = countryVO.getIso();
            }
        } catch (Throwable ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return val;
    }
}
