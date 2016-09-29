package com.secqme.util.notification.sms;

import com.secqme.domain.model.notification.sms.SmsCountryVO;

import java.util.Comparator;

/**
 * Created by Edmund on 2/26/15.
 */
public class SMSCountryListSort implements Comparator<SmsCountryVO> {

    @Override
    public int compare(SmsCountryVO o1, SmsCountryVO o2) {
        return o1.getCountryVO().getCountryName().compareTo(o2.getCountryVO().getCountryName());
    }
}
