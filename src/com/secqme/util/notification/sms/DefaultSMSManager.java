package com.secqme.util.notification.sms;

import com.secqme.domain.dao.SMSGWDAO;
import com.secqme.domain.dao.SMSLogDAO;
import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.notification.sms.*;
import com.secqme.util.TextUtil;
import com.secqme.util.ar.ARTemplateEngine;
import com.secqme.util.cache.CacheUtil;
import com.secqme.util.validator.MobileNumberUtil;
import com.secqme.util.rest.RestUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.commons.lang3.text.WordUtils;

import java.util.*;

/**
 *
 * @author jameskhoo
 */
public class DefaultSMSManager implements SMSManager {
    private static Logger myLog = Logger.getLogger(DefaultSMSManager.class);

    private SMSGWDAO smsGWDAO = null;
    private SMSLogDAO smsLogDAO = null;

    private RestUtil restUtl = null;
    private TextUtil textUtil = null;
    private CacheUtil cacheUtil = null;
    private MobileNumberUtil mobileNumberUtil = null;

    private ARTemplateEngine arTemplateEngine;

    public DefaultSMSManager(SMSGWDAO gwDAO, SMSLogDAO logDAO,
            RestUtil restUtil, TextUtil txtUtil, CacheUtil cacheUtil,
            MobileNumberUtil mobileNumberUtil, ARTemplateEngine arTemplateEngine) {
        smsGWDAO = gwDAO;
        smsLogDAO = logDAO;
        this.restUtl = restUtil;
        this.textUtil = txtUtil;
        this.cacheUtil = cacheUtil;
        this.mobileNumberUtil = mobileNumberUtil;
        this.arTemplateEngine = arTemplateEngine;
        initAllSMSGateways();
    }

    public int getSMSCreditForCountry(String countryISO) {
        if (getSMSCountryHashMap().get(countryISO) == null) {
            return -1;
        }
        return getSMSCountryHashMap().get(countryISO).getSmsCredit();
    }

    public CountryVO getCountry(String countryISO) {
        return getSMSCountryHashMap().get(countryISO) != null
                ? getSMSCountryHashMap().get(countryISO).getCountryVO() : null;
    }

    public void refreshSetting() {
        initAllSMSGateways();
    }

    public int sendSMS(SMSVO smsVO, OnBeforeSMSLogSavedListener onBeforeSMSLogSavedListener, OnAfterSMSLogSavedListener onAfterSMSLogSavedListener) {
        List<ContentBasedSMSVO> contentBasedSMSVOs = null;
        if (smsVO instanceof TemplateBasedSMSVO) {
            TemplateBasedSMSVO templateBasedSMSVO = (TemplateBasedSMSVO) smsVO;
            contentBasedSMSVOs = ContentBasedSMSVO.fromTemplateBasedSMSVO(templateBasedSMSVO, arTemplateEngine);
        } else if (smsVO instanceof ContentBasedSMSVO) {
            ContentBasedSMSVO contentBasedSMSVO = (ContentBasedSMSVO) smsVO;
            contentBasedSMSVOs = ContentBasedSMSVO.separateToListContentBasedSMSVO(contentBasedSMSVO);
        }

        int credit = 0;
        for (ContentBasedSMSVO contentBasedSMSVO : contentBasedSMSVOs) {
            try {
                credit += sendContentBasedSMSToSingleRecipient(contentBasedSMSVO, onBeforeSMSLogSavedListener, onAfterSMSLogSavedListener);
            } catch (SMSSendException ex) {
                myLog.error("Problem while sending an sms", ex);
            }
        }

        return credit;
    }

    private int sendContentBasedSMSToSingleRecipient(ContentBasedSMSVO smsVO, OnBeforeSMSLogSavedListener onBeforeSMSLogSavedListener, OnAfterSMSLogSavedListener onAfterSMSLogSavedListener) throws SMSSendException {
        myLog.debug("Sending an SMS: " + smsVO);

        String smsBody = smsVO.getBody();
        SMSRecipientVO recipientVO = smsVO.getRecipients().get(0);

        String countryISO = recipientVO.getCountryVO().getIso();
        String mobileNumber = recipientVO.getMobileNumber();

        SmsCountryVO smsCountryVO = getSMSCountryHashMap().get(countryISO);
        if (smsCountryVO == null) {
            throw new SMSSendException("No SMS gateway found for this country: " + countryISO);
        }

        String smsText = smsBody.trim();
        if (StringUtils.isNotEmpty(smsCountryVO.getSmsPreText())) {
            smsText = smsCountryVO.getSmsPreText() + smsText;
        }
        if (StringUtils.isNotEmpty(smsCountryVO.getSmsPostText())) {
            smsText = smsText + " " + smsCountryVO.getSmsPostText();
        }

        String countryCallingCode = smsCountryVO.getCountryVO().getCallingCode();

        String gwName = smsCountryVO.getSmsGateWayVO().getName();
        SMSService smsService = getSMSServiceHashMap().get(gwName);

        myLog.debug("Sending SMS to: " + countryISO + "-" + mobileNumber + ", via: " + gwName + " message: " + smsText);
        SMSLogVO smsLogVO = smsService.sendSMS(countryCallingCode, mobileNumber, smsText);
        int smsCredit = smsCountryVO.getSmsCredit();

        insertSMSLog(smsLogVO, smsVO, recipientVO, gwName, smsCredit, onBeforeSMSLogSavedListener, onAfterSMSLogSavedListener);

        if(!smsLogVO.isSendOut()) {
            throw new SMSSendException("Problem on sending the SMS to: " + countryCallingCode + mobileNumber);
        }

        return smsCredit;
    }

    public List<SmsCountryVO> getSupportedSMSCountryList() {
        if (cacheUtil.getCachedObject(CacheUtil.SMS_COUNTRY_LIST_KEY) == null) {
            initAllSMSGateways();
        }
        return (List<SmsCountryVO>) cacheUtil.getCachedObject(CacheUtil.SMS_COUNTRY_LIST_KEY);
    }

    private HashMap<String, SMSService> getSMSServiceHashMap() {
        if (cacheUtil.getCachedObject(CacheUtil.SMS_SERVICE_HASH_MAP_KEY) == null) {
            initAllSMSGateways();
        }
        return (HashMap<String, SMSService>) cacheUtil.getCachedObject(CacheUtil.SMS_SERVICE_HASH_MAP_KEY);
    }

    private HashMap<String, SmsCountryVO> getSMSCountryHashMap() {
        if (cacheUtil.getCachedObject(CacheUtil.SMS_COUNTRY_HASH_MAP_KEY) == null) {
            initAllSMSGateways();
        }
        return (HashMap<String, SmsCountryVO>) cacheUtil.getCachedObject(CacheUtil.SMS_COUNTRY_HASH_MAP_KEY);
    }

    public void initAllSMSGateways() {
        List<SmsGateWayVO> smsGWList = smsGWDAO.findAll();
        if (smsGWList != null && smsGWList.size() > 0) {
            myLog.debug("Initializing total of " + smsGWList.size() + " SMS GW");
            HashMap<String, SMSService> smsServiceHashMap = new HashMap<String, SMSService>();
            HashMap<String, SmsCountryVO> smsCountryHashMap = new HashMap<String, SmsCountryVO>();
            List<SmsCountryVO> smsCountryList = new ArrayList<SmsCountryVO>();

            SMSService smsServ = null;
            for (SmsGateWayVO smsGW : smsGWList) {
                try {
                    myLog.debug("Configuring " + smsGW.getName() + " SMS GW");
                    smsServ = (SMSService) Class.forName(smsGW.getImplClass()).newInstance();
                    smsServ.init(smsGW, restUtl, textUtil, mobileNumberUtil);
                    smsServiceHashMap.put(smsGW.getName(), smsServ);

                    // For each country we supported, we needs to init the
                    // country
                    if (smsGW.getCountryVOList() != null && smsGW.getCountryVOList().size() > 0) {
                        for (SmsCountryVO smsCountry : smsGW.getCountryVOList()) {
                            smsCountryHashMap.put(smsCountry.getCountryVO().getIso(), smsCountry);
                            String countryName = smsCountry.getCountryVO().getCountryName();
                            smsCountry.getCountryVO().setCountryName(WordUtils.capitalizeFully(countryName));
                            smsCountry.getCountryVO().setCountryName(smsCountry.getCountryVO().getCountryName());
                            smsCountryList.add(smsCountry);
                        }
                        myLog.debug("Initialized total of " + smsGW.getCountryVOList().size() +
                                " Countries for->" + smsGW.getName() + " Gateway");
                    }


                } catch (InstantiationException ex) {
                    myLog.error("Problem of Instantiate " + smsGW.getName()
                            + ", implClass:" + smsGW.getImplClass() + " error->" + ex.getMessage(), ex);
                } catch (IllegalAccessException ex) {
                    myLog.error("IllegalAccessException " + smsGW.getName()
                            + ", implClass:" + smsGW.getImplClass() + " error->" + ex.getMessage(), ex);
                } catch (ClassNotFoundException ex) {
                    myLog.error("Class Not Found for " + smsGW.getName()
                            + ", implClass:" + smsGW.getImplClass() + " error->" + ex.getMessage(), ex);
                }

            }
            // Sort smsCountryList
            Collections.sort(smsCountryList, new SMSCountryListSort());
            cacheUtil.storeObjectIntoCache(CacheUtil.SMS_COUNTRY_HASH_MAP_KEY, smsCountryHashMap);
            cacheUtil.storeObjectIntoCache(CacheUtil.SMS_COUNTRY_LIST_KEY, smsCountryList);
            cacheUtil.storeObjectIntoCache(CacheUtil.SMS_SERVICE_HASH_MAP_KEY, smsServiceHashMap);
        }
    }

    private void insertSMSLog(SMSLogVO log, SMSVO smsVO, SMSRecipientVO smsRecipientVO, String gatewayName, int smsCredit, OnBeforeSMSLogSavedListener onBeforeSMSLogSavedListener, OnAfterSMSLogSavedListener onAfterSMSLogSavedListener) {
        log.setGwName(gatewayName);
        log.setCredit(smsCredit);
        log.setMessageType(smsVO.getMessageType());
        if (onBeforeSMSLogSavedListener != null) {
            onBeforeSMSLogSavedListener.onBeforeSMSLogSaved(smsVO, smsRecipientVO, log);
        }
        smsLogDAO.create(log);
        if (onAfterSMSLogSavedListener != null) {
            onAfterSMSLogSavedListener.onAfterSMSLogSaved(smsVO, smsRecipientVO, log);
        }
    }

}
