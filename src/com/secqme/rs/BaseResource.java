package com.secqme.rs;

import com.secqme.CoreException;
import com.secqme.domain.model.ContactInvitationStatus;
import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.EventStatusType;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingCycleVO;
import com.secqme.domain.model.billing.BillingPkgType;
import com.secqme.domain.model.event.*;
import com.secqme.domain.model.safezone.SafeZoneVO;
import com.secqme.domain.model.util.SafetyLevelVO;
import com.secqme.manager.*;
import com.secqme.manager.billing.BillingManager;
import com.secqme.manager.crime.CrimeManager;
import com.secqme.manager.notification.EmailLogManager;
import com.secqme.manager.payment.PaymentManager;
import com.secqme.manager.payment.UserSubscriptionInfoManager;
import com.secqme.manager.promocode.PromoCodeManager;
import com.secqme.manager.promotion.PromotionManager;
import com.secqme.manager.referral.ReferralManager;
import com.secqme.manager.safezone.SafeZoneManager;
import com.secqme.rs.v2.CommonJSONKey;
import com.secqme.sns.FacebookUtil;
import com.secqme.util.LocationUtil;
import com.secqme.util.notification.sms.SMSManager;
import com.secqme.util.notification.sns.FacebookSNSService;
import com.secqme.util.notification.sns.SNSManager;
import com.secqme.util.payment.PaymentUtil;
import com.secqme.util.validator.MobileNumberUtil;
import com.secqme.util.spring.BeanType;
import com.secqme.util.spring.DefaultSpringUtil;
import com.secqme.util.spring.SpringUtil;
import com.secqme.util.system.SysAdminUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.*;

/**
 * User: James Khoo
 * Date: 7/22/13
 * Time: 3:13 PM
 */
public class BaseResource {

    private final static Logger myLog = Logger.getLogger(BaseResource.class);
	private static final long ONE_DAY_TIME = 24 * 60 * 60 * 1000; //one day
    protected final static SpringUtil beanUtil = DefaultSpringUtil.getInstance();
    protected final static String OK_STATUS = "{\"status\":\"ok\"}";
    protected final static String NOT_OK_RESULT = "{\"status\":\"notok\"}";

    protected static BillingManager billingManager = null;
    protected static ContactManager contactManager = null;
    protected static CrimeManager crimeManager = null;
    protected static EmailLogManager emailLogManager = null;
    protected static EventManager eventManager = null;
    protected static PaymentManager paymentManager = null;
    protected static PromotionManager promotionManager = null;
    protected static PromoCodeManager promoCodeManager = null;
    protected static QuickEventManager quickEventManager = null;
    protected static ReferralManager referralManager = null;
    protected static SMSManager smsManager = null;
    protected static SNSManager snsManager = null;
    protected static SafeZoneManager safeZoneManager = null;
    protected static TrackingManager trackingManager = null;
    protected static UserManager userManager = null;
    protected static UserSubscriptionInfoManager userSubscriptionInfoManager = null;

    protected static FacebookUtil fbUtil = null;
    protected static PaymentUtil applePaymentUtil = null;
    protected static PaymentUtil googlePaymentUtil = null;
    protected static SysAdminUtil sysAdminUtil = null;
    protected static LocationUtil locationUtil = null;
    protected static MobileNumberUtil mobileNumberUtil = null;

    protected static FacebookSNSService fbService = null;

    public BaseResource () {
        billingManager = (BillingManager) beanUtil.getBean(BeanType.billingManager);
        crimeManager = (CrimeManager) beanUtil.getBean(BeanType.crimeManager);
        contactManager = (ContactManager) beanUtil.getBean(BeanType.CONTACT_MANAGER);
        emailLogManager = (EmailLogManager) beanUtil.getBean(BeanType.EMAIL_LOG_MANAGER);
        eventManager = (EventManager) beanUtil.getBean(BeanType.eventManager);
        paymentManager = (PaymentManager) beanUtil.getBean(BeanType.paymentManager);
        promotionManager = (PromotionManager) beanUtil.getBean(BeanType.promotionManager);
        promoCodeManager = (PromoCodeManager) beanUtil.getBean(BeanType.PROMO_CODE_MANAGER);
        quickEventManager  = (QuickEventManager) beanUtil.getBean(BeanType.quickEventManager);
        referralManager = (ReferralManager) beanUtil.getBean(BeanType.referralManager);
        safeZoneManager = (SafeZoneManager) beanUtil.getBean(BeanType.safeZoneManager);
        smsManager = (SMSManager) beanUtil.getBean(BeanType.smsManager);
        snsManager = (SNSManager) beanUtil.getBean(BeanType.SNS_MANAGER);
        trackingManager = (TrackingManager) beanUtil.getBean(BeanType.trackingManager);
        userManager = (UserManager) beanUtil.getBean(BeanType.userManager);
        userSubscriptionInfoManager = (UserSubscriptionInfoManager) beanUtil.getBean(BeanType.userSubscriptionInfoManager);

        applePaymentUtil = (PaymentUtil) beanUtil.getBean(BeanType.applePaymentUtil);
        fbUtil = (FacebookUtil) beanUtil.getBean(BeanType.faceBookUtil);
        googlePaymentUtil = (PaymentUtil) beanUtil.getBean(BeanType.googlePaymentUtil);
        locationUtil = (LocationUtil) beanUtil.getBean(BeanType.locationUtil);
        mobileNumberUtil = (MobileNumberUtil) beanUtil.getBean(BeanType.mobileNumberUtil);
        sysAdminUtil = (SysAdminUtil) beanUtil.getBean(BeanType.sysAdminUtil);

        fbService = (FacebookSNSService) beanUtil.getBean(BeanType.FACEBOOK_SNS_SERVICE);
    }

    protected JSONArray parseUserContactListAsJSONArray(UserVO userVO) throws JSONException, CoreException {
        JSONArray contactArray = new JSONArray();
        List<ContactVO> userContactList = userVO.getContactList();
        Integer maxContactAllow = userVO.getPackageVO().getMaxContactAllow();
        int index = 0;
        if (userContactList != null && userContactList.size() > 0) {
            Collections.sort(userContactList);
            for (ContactVO contactVO : userContactList) {
                JSONObject contactObj = new JSONObject();
                contactObj.put(ContactVO.CONTACT_ID_KEY, contactVO.getId());
                if(contactVO.getCountryVO() != null) {
                    contactObj.put(CommonJSONKey.MOBILE_COUNTRY_KEY, contactVO.getCountryVO().getIso())
                            .put(CommonJSONKey.USER_NAME_KEY, contactVO.getNickName())
                            .put(CommonJSONKey.MOBILE_COUNTRY_CODE_KEY, contactVO.getCountryVO().getCallingCode())
                            .put(CommonJSONKey.MOBILE_NO_KEY, contactVO.getMobileNo());
                }
                contactObj.put(CommonJSONKey.EMAIL_ADDR_KEY, contactVO.getEmailAddress());
                contactObj.put(CommonJSONKey.RELATIONSHIP_KEY, contactVO.getRelationship());

                // Check whether contact is a registered user and has latest app
                // Note: Network contact feature was removed due security/privacy issues.
                if (contactVO.getContactUserVO() == null || contactVO.getStatus() == ContactInvitationStatus.SELFADDED) {
                	contactObj.put(CommonJSONKey.NETWORK_CONTACT_KEY, false);
                } else {
                	if (contactVO.getContactUserVO().getClientVersion() < 6) {
                        contactObj.put(CommonJSONKey.NETWORK_CONTACT_KEY, false);
                	} else {
                        contactObj.put(CommonJSONKey.NETWORK_CONTACT_KEY, true);
                	}
                }
                contactArray.put(contactObj);

                index++;
                if(index >= maxContactAllow) {
                    break;
                }
            }
        }
        return contactArray;
    }

    //return contact that is running event in the pass 24 hour
    protected JSONArray parseRunningEventContactListAsJSONArray(UserVO userVO) throws JSONException, CoreException {
    	List<ContactVO> contactAddedByOtherUserList = userManager.getContactAddedByOtherUserList(userVO);
        JSONArray contactArray = new JSONArray();
        HashMap<String, ContactVO> contactNoEventHashMap = new HashMap<>();
        myLog.debug("parseRunningEventContactListAsJSONArray1 " + contactAddedByOtherUserList);
        if (contactAddedByOtherUserList != null && contactAddedByOtherUserList.size() > 0) {
        	myLog.debug("parseRunningEventContactListAsJSONArray2 " + contactAddedByOtherUserList.size());
            for (ContactVO contactVO : contactAddedByOtherUserList) {
            	JSONObject contactObj = new JSONObject();
            	SecqMeEventVO eventVO = eventManager.getUserLatestEvent(contactVO.getUserVO().getUserid());
            	contactNoEventHashMap.put(contactVO.getUserVO().getUserid(), contactVO);
            	if (eventVO == null) {
            		myLog.debug("parseRunningEventContactListAsJSONArray3 " + eventVO);
            	} else {
            		myLog.debug("parseRunningEventContactListAsJSONArray3 " + (System.currentTimeMillis() - eventVO.getStartTime().getTime()));
            	}
            	
            	//check if event is within 24 hours
                if (eventVO != null && (System.currentTimeMillis() - eventVO.getStartTime().getTime() < ONE_DAY_TIME 
                		|| eventVO.isEventRunning())) {
                	//check if this event is share to user
                	ShareEventVO shareEventVO = eventManager.getShareEventVO(userVO.getUserid(), eventVO.getId());
                	myLog.debug("parseRunningEventContactListAsJSONArray3 " + eventVO.getEventType() + eventVO.isEnableShare()
                			+ "*" + shareEventVO);
//                	if((eventVO.getEventType() == EventType.NORMAL && eventVO.isEnableShare())
//                			|| eventVO.getEventType() == EventType.EMERGENCY) {
                	if(shareEventVO != null
                			|| eventVO.getEventType() == EventType.EMERGENCY) {
                		boolean isEventExpiredOrEmergency = false;
                		FullEventInfoVO fullEventInfoVO = eventManager.getFullEventInfo(contactVO.getUserVO(), eventVO.getId());
                        contactObj.put(CommonJSONKey.EVENT_ID_KEY, eventVO.getId());
                        contactObj.put(CommonJSONKey.EVENT_START_TIME_KEY, eventVO.getStartTime().getTime());
                        contactObj.put(ContactVO.CONTACT_ID_KEY, contactVO.getId());
                        UserVO contactUserVO = contactVO.getUserVO();

                        if (contactUserVO.getMobileCountry() != null) {
                            contactObj.put(CommonJSONKey.MOBILE_COUNTRY_KEY, contactUserVO.getMobileCountry().getIso())
                                    .put(CommonJSONKey.MOBILE_COUNTRY_CODE_KEY, contactUserVO.getMobileCountry().getIso())
                                    .put(CommonJSONKey.MOBILE_NO_KEY, contactUserVO.getMobileNo());
                        }

                        contactObj.put(CommonJSONKey.USER_NAME_KEY, contactUserVO.getNickName());
                        contactObj.put(CommonJSONKey.EMAIL_ADDR_KEY, contactUserVO.getEmailAddress());
                        contactObj.put(CommonJSONKey.RELATIONSHIP_KEY, contactVO.getRelationship());
                        
                        if (fullEventInfoVO.getEventLogVOList() != null && fullEventInfoVO.getEventLogVOList().size() > 0) {
                            JSONArray eventLogArray = new JSONArray();
                            for (EventLogVO eventLog : fullEventInfoVO.getEventLogVOList()) {
                            	if(EventStatusType.NOTIFY == eventLog.getStatus()) { //event expired/emergency event
                            		isEventExpiredOrEmergency = true;
                            	}
                                eventLogArray.put(eventLog.toJSON());
                            }
                            contactObj.put(FullEventInfoVO.EVENT_LOG_LIST_KEY, eventLogArray);
                        }
                        if (EventStatusType.END == eventVO.getStatus()
                                || EventStatusType.SAFE_NOTIFY == eventVO.getStatus()) {
                        	contactObj.put(CommonJSONKey.CONFIRM_SAFETY, true);
                        } else {
                        	contactObj.put(CommonJSONKey.CONFIRM_SAFETY, false);
                        }
                        //if event expired, treat it as emergency event
//                        if (isEventExpiredOrEmergency) {
//                        	contactObj.put(CommonJSONKey.EVENT_TYPE_KEY, EventType.EMERGENCY);
//                        } else {
//                        	contactObj.put(CommonJSONKey.EVENT_TYPE_KEY, eventVO.getEventType());
//                        }
                        contactObj.put(CommonJSONKey.EVENT_TYPE_KEY, eventVO.getEventType());
                        contactArray.put(contactObj);
                        contactNoEventHashMap.remove(contactVO.getUserVO().getUserid());
                	}
                }
                
                
                
            }
        }
        
        // event that was shared to user might not be the latest event
        List<ShareEventVO> shareEventVOList = eventManager.getEventShareToUserWithinADay(userVO);
        if(shareEventVOList != null && shareEventVOList.size() > 0){
        	Long eventId = 0l;
        	for(ShareEventVO shareEventVO : shareEventVOList) {
        		if(eventId == shareEventVO.getSecqMeEventVO().getId()) {
        			//avoid same event that share multiple times 
        			continue;
        		} else {
        			eventId = shareEventVO.getSecqMeEventVO().getId();
        		}
        		if(contactNoEventHashMap.get(shareEventVO.getUserVO().getUserid()) != null ) { 
        			myLog.debug("contactNoEventHashMap " + shareEventVO.getId());
        			//no event from this user yet, put to contactArray
        			SecqMeEventVO eventVO = shareEventVO.getSecqMeEventVO();
        			ContactVO contactVO = (ContactVO) contactNoEventHashMap.get(shareEventVO.getUserVO().getUserid());
        			FullEventInfoVO fullEventInfoVO = eventManager.getFullEventInfo(shareEventVO.getUserVO(), 
        					shareEventVO.getSecqMeEventVO().getId());
        			JSONObject contactObj = new JSONObject();
                    contactObj.put(CommonJSONKey.EVENT_ID_KEY, eventVO.getId());
                    contactObj.put(CommonJSONKey.EVENT_START_TIME_KEY, eventVO.getStartTime().getTime());
                    contactObj.put(ContactVO.CONTACT_ID_KEY, contactVO.getId());

                    UserVO contactUserVO = contactVO.getUserVO();
                    contactObj.put(CommonJSONKey.USER_NAME_KEY, contactUserVO.getNickName());
                    contactObj.put(CommonJSONKey.EMAIL_ADDR_KEY, contactUserVO.getEmailAddress());
                    contactObj.put(CommonJSONKey.RELATIONSHIP_KEY, contactVO.getRelationship());

                    if (contactUserVO.getMobileCountry() != null) {
                        contactObj.put(CommonJSONKey.MOBILE_COUNTRY_KEY, contactUserVO.getMobileCountry().getIso())
                                .put(CommonJSONKey.MOBILE_COUNTRY_CODE_KEY, contactUserVO.getMobileCountry().getIso())
                                .put(CommonJSONKey.MOBILE_NO_KEY, contactUserVO.getMobileNo());
                    }

                    if (fullEventInfoVO.getEventLogVOList() != null && fullEventInfoVO.getEventLogVOList().size() > 0) {
                        JSONArray eventLogArray = new JSONArray();
                        for (EventLogVO eventLog : fullEventInfoVO.getEventLogVOList()) {
                            eventLogArray.put(eventLog.toJSON());
                        }
                        contactObj.put(FullEventInfoVO.EVENT_LOG_LIST_KEY, eventLogArray);
                    }
                    if (EventStatusType.END == eventVO.getStatus()
                            || EventStatusType.SAFE_NOTIFY == eventVO.getStatus()) {
                    	contactObj.put(CommonJSONKey.CONFIRM_SAFETY, true);
                    } else {
                    	contactObj.put(CommonJSONKey.CONFIRM_SAFETY, false);
                    }
                    contactObj.put(CommonJSONKey.EVENT_TYPE_KEY, eventVO.getEventType());
                    contactArray.put(contactObj);
        		}
        	}
        }
        return contactArray;
    }
    
    protected JSONArray parseUserSafeZoneListAsJSONArray(UserVO userVO) throws JSONException {
        List<SafeZoneVO> safeZoneList = userVO.getUserCreatedSafeZoneList();
        JSONArray jarray = new JSONArray();
        if (safeZoneList != null && safeZoneList.size() > 0) {
            for (SafeZoneVO safeZoneVO : safeZoneList) {
                jarray.put(safeZoneVO.toJSON());
            }
        }
        return jarray;
    }

    protected JSONArray populateContactBeingAddedList(List<ContactVO> contactList) throws JSONException {
        JSONArray contactArray = new JSONArray();
        if (contactList != null && contactList.size() > 0) {
            Collections.sort(contactList);
            for (ContactVO contactVO : contactList) {
            	if (contactVO.getStatus() != ContactInvitationStatus.ACCEPTED) { //accepted mean already exist
	                JSONObject contactObj = new JSONObject();
	                contactObj.put(ContactVO.CONTACT_ID_KEY, contactVO.getId());
	                if(contactVO.getCountryVO() != null) {
	                    contactObj.put(CommonJSONKey.MOBILE_COUNTRY_KEY, contactVO.getCountryVO().getIso())
	                            .put(CommonJSONKey.USER_NAME_KEY, contactVO.getNickName())
	                            .put(CommonJSONKey.MOBILE_COUNTRY_CODE_KEY, contactVO.getCountryVO().getCallingCode())
	                            .put(CommonJSONKey.MOBILE_NO_KEY, contactVO.getMobileNo());
	                }
	                contactObj.put(CommonJSONKey.EMAIL_ADDR_KEY, contactVO.getEmailAddress());
	                contactObj.put(CommonJSONKey.RELATIONSHIP_KEY, contactVO.getRelationship());
	                contactArray.put(contactObj);
            	}
            }
        }
        return contactArray;
    }

    protected JSONArray populateSafetyLevelList(UserVO userVO) throws JSONException {
    	List<ContactVO> contactList = userVO.getContactList();
    	if(contactList != null && contactList.size() > 0) {
    		
    	}
    	List<SafeZoneVO> safeZoneList = userVO.getUserCreatedSafeZoneList();
        JSONArray jarray = new JSONArray();
        if (safeZoneList != null && safeZoneList.size() > 0) {
            for (SafeZoneVO safeZoneVO : safeZoneList) {
                jarray.put(safeZoneVO.toJSON());
            }
        }
        return jarray;
    }
    
    // get safety level details
    protected void populateUserSafetyLevelList(UserVO userVO, JSONObject resultObj) throws JSONException {
//    	myLog.debug("populateUserSafetyLevelList0 " + userVO + ":" + resultObj);
    	//add all to map
    	HashMap<String, SafetyLevelVO> safetyLevelMap = new HashMap<String, SafetyLevelVO>();
    	safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_THREE_CONTACTS, 
    			new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_THREE_CONTACTS, 3, 0, userVO.getLangCode()));
    	safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_THREE_NETWORKS, 
    			new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_THREE_NETWORKS, 3, 0, userVO.getLangCode()));
    	safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_ADD_PLACE, 
    			new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_ADD_PLACE, 3, 0, userVO.getLangCode()));
    	safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_SMS_CREDIT, 
    			new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_SMS_CREDIT, 3, 0, userVO.getLangCode()));
        safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_MOBILE_NO,
                new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_MOBILE_NO, 1, 0, userVO.getLangCode()));
    	
    	//event
//    	SecqMeEventVO eventVO = eventManager.getUserLatestEvent(userVO.getUserid());
//    	if (eventVO != null) {
//    		Date currentDate = new Date();
//    		Date eventDate = DateUtils.addDays(eventVO.getStartTime(), 14);
//    		if(eventDate.getTime() > currentDate.getTime()) {
//    			safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_RUN_EVENT, 
//    	    			new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_RUN_EVENT, 1, true, userVO.getLangCode()));
//    		}
//    	}
    	//contact
    	if(userVO.getContactList() != null && userVO.getContactList().size() > 0) {
    		if(userVO.getContactList().size() < 3) {
    			safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_THREE_CONTACTS, 
    					new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_THREE_CONTACTS, 3, userVO.getContactList().size(), userVO.getLangCode()));
    		} else {
    			safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_THREE_CONTACTS, 
    					new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_THREE_CONTACTS, 3, 3, userVO.getLangCode()));
    		}
			
			int totalNetworkContact = 0;
			for(ContactVO contactVO : userVO.getContactList()) {
				if(contactVO.getContactUserVO() == null || contactVO.getStatus() == ContactInvitationStatus.SELFADDED) {
                	//do nothing
                } else {
                	if(contactVO.getContactUserVO().getClientVersion() >= 5) {
                		totalNetworkContact++;
                	} else {
                		//do nothing
                	}
                }
			}
			if(totalNetworkContact >= 3) {
				safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_THREE_NETWORKS, 
    					new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_THREE_NETWORKS, 3, 3, userVO.getLangCode()));
			} else {
				safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_THREE_NETWORKS, 
    					new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_THREE_NETWORKS, 3, totalNetworkContact, userVO.getLangCode()));
			}
    	}
    	
    	// safe zone
    	if(userVO.getUserCreatedSafeZoneList() != null && userVO.getUserCreatedSafeZoneList().size() > 0) {
//    		for (SafeZoneVO safeZoneVO : userVO.getUserCreatedSafeZoneList()) {
//    			if(SafeZoneVO.SAFE_ZONE_HOME.equalsIgnoreCase(safeZoneVO.getZoneName())) {
//    				safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_ADD_HOME, 
//	    					new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_ADD_HOME, 1, true, userVO.getLangCode()));
//    			} else if (SafeZoneVO.SAFE_ZONE_WORK.equalsIgnoreCase(safeZoneVO.getZoneName())) {
//    				safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_ADD_WORK, 
//	    					new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_ADD_WORK, 1, true, userVO.getLangCode()));
//    			}
//    		}
    		safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_ADD_PLACE, 
					new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_ADD_PLACE, 3, 3, userVO.getLangCode()));
    	} else {
    		safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_ADD_PLACE, 
					new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_ADD_PLACE, 3, 0, userVO.getLangCode()));
    	}
    	
    	// sms credit or premium
    	BillingCycleVO billCycleVO = billingManager.getUserLatestBillCycleVO(userVO);
        if (billCycleVO != null) {
        	if(BillingPkgType.PREMIUM == billCycleVO.getBillingPkgVO().getPkgType()) {
        		safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_SMS_CREDIT, 
    					new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_SMS_CREDIT, 3, 3, userVO.getLangCode()));
        	} 
//        	else if(billCycleVO.getSMSCreditBalance() == null || billCycleVO.getSMSCreditBalance() > 0) {
//        		safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_SMS_CREDIT, 
//    					new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_SMS_CREDIT, 3, 3, userVO.getLangCode()));
//        	}
        }

        if (userVO.getMobileCountry() != null && userVO.getMobileNo() != null) {
            safetyLevelMap.put(SafetyLevelVO.SAFETY_LEVEL_MOBILE_NO, new SafetyLevelVO(SafetyLevelVO.SAFETY_LEVEL_MOBILE_NO, 1, 1, userVO.getLangCode()));
        }
        
        DecimalFormat decim = new DecimalFormat("0.00");
        JSONObject safetyLevelObject = new JSONObject();
        JSONArray safetyLevelArray = new JSONArray();
        int enabledSafetyLevel = 0; 
        int totalSafetyWeight = 0;
        Iterator it = safetyLevelMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            SafetyLevelVO safetyLevelVO = (SafetyLevelVO) pairs.getValue();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(CommonJSONKey.SAFETY_LEVEL_NAME_KEY, safetyLevelVO.getName());
            jsonObject.put(CommonJSONKey.SAFETY_LEVEL_DESCRIPTION_KEY, safetyLevelVO.getDescription());
            jsonObject.put(CommonJSONKey.SAFETY_LEVEL_ACTION_KEY, safetyLevelVO.getAction());
            jsonObject.put(CommonJSONKey.SAFETY_LEVEL_WEIGHT_KEY, safetyLevelVO.getWeight());
            jsonObject.put(CommonJSONKey.SAFETY_LEVEL_ENABLE_KEY, safetyLevelVO.getEnable());
            safetyLevelArray.put(jsonObject);
            totalSafetyWeight += safetyLevelVO.getWeight();
            enabledSafetyLevel += safetyLevelVO.getEnable();
            it.remove();
        }
        
        double safetyLevelPercentage = (double) enabledSafetyLevel / (double) totalSafetyWeight * 100;
        safetyLevelObject.put(CommonJSONKey.SAFETY_LEVEL_PERCENTAGE_KEY, Double.valueOf(decim.format(safetyLevelPercentage)));
        safetyLevelObject.put(CommonJSONKey.SAFETY_LEVEL_LIST_KEY, safetyLevelArray);
        resultObj.put(CommonJSONKey.SAFETY_LEVEL_KEY, safetyLevelObject);
    }

    protected String getAuthorizationToken(HttpServletRequest request, JSONObject jsonObject) throws JSONException {
        if (jsonObject != null && jsonObject.has(CommonJSONKey.AUTH_TOKEN_KEY)) {
            myLog.debug("Using authorization token from JSON");
            return jsonObject.getString(CommonJSONKey.AUTH_TOKEN_KEY);
        }

        myLog.debug("Using authorization token from Header");
        return (String) request.getAttribute(CommonJSONKey.AUTH_TOKEN_KEY);
    }

    protected Double getClientVersion(HttpServletRequest request, JSONObject jsonObject) throws JSONException {
        if (jsonObject != null && jsonObject.has(CommonJSONKey.CLIENT_VERSION_KEY)) {
            myLog.debug("Using client version from JSON");
            return jsonObject.getDouble(CommonJSONKey.CLIENT_VERSION_KEY);
        }

        myLog.debug("Using client version from Header");
        return (Double) request.getAttribute(CommonJSONKey.CLIENT_VERSION_KEY);
    }

    protected String getClientOS(HttpServletRequest request, JSONObject jsonObject) throws JSONException {
        String device = null;

        if (jsonObject != null && jsonObject.has(CommonJSONKey.DEVICE_KEY)) {
            device = jsonObject.optString(CommonJSONKey.DEVICE_KEY);
        } else {
            device = (String) request.getAttribute(CommonJSONKey.CLIENT_OS_KEY);
        }

        String platformType;
        if (StringUtils.startsWithIgnoreCase(device, "Android")) {
            platformType = "Android";
        } else if (StringUtils.startsWithIgnoreCase(device, "iOS") || StringUtils.startsWithIgnoreCase(device, "iPhone")) {
            platformType = "iOS";
        } else {
            platformType = null;
        }

        return platformType;
    }
}
