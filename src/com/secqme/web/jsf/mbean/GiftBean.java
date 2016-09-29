package com.secqme.web.jsf.mbean;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.sms.SmsCountryVO;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import com.secqme.manager.UserManager;
import com.secqme.manager.payment.PaymentManager;
import com.secqme.util.ServletUtil;
import com.secqme.util.notification.sms.SMSManager;
import com.secqme.util.payment.PaypalGW;
import com.secqme.web.jsf.util.NavigationRules;

/**
 * Specific bean to allows user to reset password
 *
 * @author jameskhoo
 */
public class GiftBean implements Serializable {

    private static final Logger myLog = Logger.getLogger(GiftBean.class);

    private String name;
    private String mobileCountry;
    private String mobileNumber;
    private String email;
    private String recipientName;
    private String recipientMobileCountry;
    private String recipientMobileCallingCode;
    private String recipientMobileNumber;
    private String recipientEmail;
    private String message;
    private boolean subscribe = true;
    private Map<String,Object> countryMap;
    private String errorMessage = "";
    
    private UserManager userManager;
    private SMSManager smsManager = null;
    private PaymentManager paymentManager;
    private UserVO userVO = null;
    
    private List<SmsCountryVO> smsCountryVOList;
    public GiftBean() {
        // Empty constructor
    }

    public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getMobileNumber() {
		return mobileNumber;
	}


	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}


	public String getRecipientName() {
		return recipientName;
	}


	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}


	public String getRecipientMobileNumber() {
		return recipientMobileNumber;
	}


	public void setRecipientMobileNumber(String recipientMobileNumber) {
		this.recipientMobileNumber = recipientMobileNumber;
	}

	public String getRecipientMobileCallingCode() {
		return recipientMobileCallingCode;
	}

	public void setRecipientMobileCallingCode(String recipientMobileCallingCode) {
		this.recipientMobileCallingCode = recipientMobileCallingCode;
	}

	public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public String getMobileCountry() {
		return mobileCountry;
	}

	public void setMobileCountry(String mobileCountry) {
		this.mobileCountry = mobileCountry;
	}

	public String getRecipientMobileCountry() {
		return recipientMobileCountry;
	}

	public void setRecipientMobileCountry(String recipientMobileCountry) {
		this.recipientMobileCountry = recipientMobileCountry;
	}

	public String getRecipientEmail() {
		return recipientEmail;
	}

	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSubscribe() {
		return subscribe;
	}

	public void setSubscribe(boolean subscribe) {
		this.subscribe = subscribe;
	}

	public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }

    public Map<String, Object> getCountryMap() {
		return countryMap;
	}

	public void setCountryMap(Map<String, Object> countryMap) {
		this.countryMap = countryMap;
	}

	public SMSManager getSmsManager() {
		return smsManager;
	}

	public void setSmsManager(SMSManager smsManager) {
		this.smsManager = smsManager;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public PaymentManager getPaymentManager() {
		return paymentManager;
	}

	public void setPaymentManager(PaymentManager paymentManager) {
		this.paymentManager = paymentManager;
	}

	public List<SmsCountryVO> getSmsCountryVOList() {
		if(smsCountryVOList == null) {
			smsCountryVOList = smsManager.getSupportedSMSCountryList();
		}
		return smsCountryVOList;
	}

	public void setSmsCountryVOList(List<SmsCountryVO> smsCountryVOList) {
		this.smsCountryVOList = smsCountryVOList;
	}

	public void initGiftBean() {
    	myLog.debug("initGiftBean ");
    	SmsCountryVO countryVOUS = null;
    	SmsCountryVO countryVOAU = null;
    	SmsCountryVO countryVOUK = null;
    	SmsCountryVO countryVONZ = null;
    	SmsCountryVO countryVOMY = null;
    	Map<String,Object> otherCountry = new LinkedHashMap<String,Object>();
    	if(countryMap == null) {
    		countryMap = new LinkedHashMap<String,Object>();
    		if(getSmsCountryVOList()!= null && getSmsCountryVOList().size() >  0) {
                for(SmsCountryVO smsCountry : getSmsCountryVOList()) {
                	//insert top 5 country first
                	if("US".equalsIgnoreCase(smsCountry.getCountryVO().getIso())) {
                		countryVOUS = smsCountry;
                	} else if("AU".equalsIgnoreCase(smsCountry.getCountryVO().getIso())) {
                		countryVOAU = smsCountry;
                	} else if("UK".equalsIgnoreCase(smsCountry.getCountryVO().getIso())) {
                		countryVOUK = smsCountry;
                	} else if("NZ".equalsIgnoreCase(smsCountry.getCountryVO().getIso())) {
                		countryVONZ = smsCountry;
                	} else if("MY".equalsIgnoreCase(smsCountry.getCountryVO().getIso())) {
                		countryVOMY = smsCountry;
                	} else {
                		otherCountry.put(smsCountry.getCountryVO().getCountryName() 
                    			+ " - +" + smsCountry.getCountryVO().getCallingCode(), smsCountry.getCountryVO().getIso());
                	}
                }
                if(countryVOUS != null){
                	countryMap.put(countryVOUS.getCountryVO().getCountryName() 
                			+ " - +" + countryVOUS.getCountryVO().getCallingCode(), countryVOUS.getCountryVO().getIso());
                }
                if(countryVOAU != null){
                	countryMap.put(countryVOAU.getCountryVO().getCountryName() 
                			+ " - +" + countryVOAU.getCountryVO().getCallingCode(), countryVOAU.getCountryVO().getIso());
                }
                if(countryVOUK != null){
                	countryMap.put(countryVOUK.getCountryVO().getCountryName() 
                			+ " - +" + countryVOUK.getCountryVO().getCallingCode(), countryVOUK.getCountryVO().getIso());
                }
                if(countryVONZ != null){
                	countryMap.put(countryVONZ.getCountryVO().getCountryName() 
                			+ " - +" + countryVONZ.getCountryVO().getCallingCode(), countryVONZ.getCountryVO().getIso());
                }
                if(countryVOMY != null){
                	countryMap.put(countryVOMY.getCountryVO().getCountryName() 
                			+ " - +" + countryVOMY.getCountryVO().getCallingCode(), countryVOMY.getCountryVO().getIso());
                }
                countryMap.putAll(otherCountry);
            }
    	}
    }
    
    public String purchaseValidation() {
    	myLog.debug("purchaseValidation " + recipientMobileCountry + recipientMobileNumber);
        String naviPath = NavigationRules.PURCHASE_VALIDATION_FAILED.getNaviPath();
        
        try {
        	
        	userManager.validateUserMobileNumber(recipientMobileCountry, recipientMobileNumber, null);
            userVO = userManager.getUserByMobileNumber(recipientMobileCountry, recipientMobileNumber);
            if(userVO != null) {
            	if(userVO.getUserCurrentSubscription() != null) { //user is on a subscription
                	throw new CoreException(ErrorType.USER_ALREADY_HAS_SUBSCRIPTION_ERROR, null, recipientName);
                } 
            } else { //check if user buy twice for an unregistered number
            	List<GiftPaymentLogVO> giftPackageList = paymentManager.getGiftPackageByMobileCountryAndMobileNumber(
            			recipientMobileCountry, recipientMobileNumber);
            	myLog.debug("purchaseValidation not a user, gift found:" + giftPackageList); 
            	if(giftPackageList != null && giftPackageList.size() > 0) {
            		throw new CoreException(ErrorType.USER_ALREADY_HAS_SUBSCRIPTION_ERROR, null, recipientName);
            	}
            }
            myLog.debug("purchaseValidation for ->" + userVO);
           
           
            
    		FacesContext fc = FacesContext.getCurrentInstance();
    		ExternalContext ec = fc.getExternalContext();
    		HttpServletRequest request = (HttpServletRequest) ec.getRequest();
    		String url = ServletUtil.getBaseServerPath(request) + "/payment/gift/PAYPAL?pricepkg=AR_1Y_SMS"
    				+ "&" + PaypalGW.GIFT_NAME_KEY + "=" + URLEncoder.encode(name, "UTF-8")
    				+ "&" + PaypalGW.GIFT_MOBILE_COUNTRY_KEY + "=" + URLEncoder.encode(mobileCountry, "UTF-8")
    				+ "&" + PaypalGW.GIFT_MOBILE_NUMBER_KEY + "=" + URLEncoder.encode(mobileNumber, "UTF-8")
    				+ "&" + PaypalGW.GIFT_EMAIL + "=" + URLEncoder.encode(email, "UTF-8")
    				+ "&" + PaypalGW.GIFT_MESSAGE + "=" + URLEncoder.encode(message, "UTF-8")
    				+ "&" + PaypalGW.GIFT_RECIPIENT_NAME_KEY + "=" + URLEncoder.encode(recipientName, "UTF-8")
    				+ "&" + PaypalGW.GIFT_RECIPIENT_MOBILE_COUNTRY_KEY + "=" + URLEncoder.encode(recipientMobileCountry, "UTF-8")
    				+ "&" + PaypalGW.GIFT_RECIPIENT_MOBILE_NUMBER_KEY + "=" + URLEncoder.encode(recipientMobileNumber, "UTF-8")
    				+ "&" + PaypalGW.GIFT_RECIPIENT_EMAIL_KEY + "=" + URLEncoder.encode(recipientEmail, "UTF-8")
    				+ "&" + PaypalGW.GIFT_SUBSCRIBE + "=" + subscribe;
    		myLog.debug("url " + url);
    		ec.redirect(url);
    		for(SmsCountryVO smsCountryVO : getSmsCountryVOList()) {
    			if(smsCountryVO.getCountryVO().getIso().equalsIgnoreCase(recipientMobileCountry)){
    				recipientMobileCallingCode  = smsCountryVO.getCountryVO().getCallingCode();
    				break;
    			}
    		}
    		naviPath = NavigationRules.PURCHASE_VALIDATION_SUCCESS.getNaviPath();
    		errorMessage = "";
            myLog.debug("naviPath " + naviPath);
        } catch (CoreException ex) {
            myLog.error(ex.getMessage(), ex);
            if(ex.getErrorType() == ErrorType.USER_MOBILE_NUMBER_INVALID) {
            	errorMessage = new CoreException(ErrorType.USER_GIFT_MOBILE_NUMBER_INVALID, null, recipientName).getLocalizedMessage();
            } else {
            	errorMessage = ex.getLocalizedMessage();
            }
            
        } catch (UnsupportedEncodingException ex) {
            myLog.error(ex.getMessage(), ex);
            errorMessage = ex.getLocalizedMessage();
		} catch (IOException ex) {
            myLog.error(ex.getMessage(), ex);
			errorMessage = "Please contact support";
		}
        if(errorMessage.length() > 0) {
        	error();
        }
        return naviPath;
    }

    public void error() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", errorMessage));
    }

}
