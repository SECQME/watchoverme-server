package com.secqme.domain.model.util;

import java.io.Serializable;

import com.secqme.util.spring.DefaultSpringUtil;

public class SafetyLevelVO  implements Serializable {

    public static final String SAFETY_LEVEL_THREE_CONTACTS = "safetyLevelThreeContacts";
    public static final String SAFETY_LEVEL_THREE_NETWORKS = "safetyLevelThreeNetworks";
    public static final String SAFETY_LEVEL_ADD_PLACE = "safetyLevelAddPlace";
    public static final String SAFETY_LEVEL_SMS_CREDIT = "safetyLevelSmsCredit";
    public static final String SAFETY_LEVEL_MOBILE_NO = "safetyLevelMobileNo";
    //ACTION
    public static final String ACTION_UPDATE_MOBILE_NO = "updateMobileNo";
    public static final String ACTION_ADD_CONTACT = "addContact";
    public static final String ACTION_NOTIFY_CONTACT = "notifyContact";
    public static final String ACTION_ADD_SAFE_ZONE_PLACE = "addSafeZonePlace";
    public static final String ACTION_RUN_EVENT = "runEvent";
    public static final String ACTION_UPGRADE_PREMIUM = "upgradePremium";
    public static final String ACTION_NONE = "none";
    
    private static final String DEFAULT_LANG_CODE = "en_US";
	private String name;
	private String description;
	private String action;
	private int weight;
	private int enable;
	
	public SafetyLevelVO(String name, int weight, int enable, String langCode) {
		this.name = name;
		if(langCode == null) {
			langCode = DEFAULT_LANG_CODE;
		}
		this.description = DefaultSpringUtil.getInstance().getMessage(name, langCode);
		this.action = assignAction(name);
		this.weight = weight;
		this.enable = enable;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public int getEnable() {
		return enable;
	}
	public void setEnable(int enable) {
		this.enable = enable;
	}

	private String assignAction(String name) {
		if(SAFETY_LEVEL_THREE_CONTACTS.equalsIgnoreCase(name)) {
			return ACTION_ADD_CONTACT;
		} else if(SAFETY_LEVEL_THREE_NETWORKS.equalsIgnoreCase(name)) {
			return ACTION_NOTIFY_CONTACT;
		} else if(SAFETY_LEVEL_ADD_PLACE.equalsIgnoreCase(name)) {
			return ACTION_ADD_SAFE_ZONE_PLACE;
		} else if(SAFETY_LEVEL_SMS_CREDIT.equalsIgnoreCase(name)) {
			return ACTION_UPGRADE_PREMIUM;
        } else if(SAFETY_LEVEL_MOBILE_NO.equalsIgnoreCase(name)) {
            return ACTION_UPDATE_MOBILE_NO;
		} else {
			return ACTION_NONE;
		}
	}
}
