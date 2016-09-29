package com.secqme.manager.payment;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.secqme.CoreException;
import com.secqme.domain.dao.GooglePaymentLogDAO;
import com.secqme.domain.dao.SubscriptionInfoDAO;
import com.secqme.domain.dao.SubscriptionInfoLogDAO;
import com.secqme.domain.model.payment.GooglePaymentLogVO;
import com.secqme.domain.model.payment.UserSubscriptionInfoLogVO;
import com.secqme.domain.model.payment.UserSubscriptionInfoVO;
import com.secqme.manager.BaseManager;
import com.secqme.manager.billing.BillingManager;
import com.secqme.util.notification.NotificationEngine;
import com.secqme.util.payment.DefaultGooglePaymentUtil;

public class DefaultUserSubscriptionInfoManager extends BaseManager implements UserSubscriptionInfoManager {

	private static final Logger myLog = Logger.getLogger(DefaultUserSubscriptionInfoManager.class);
	private static final String GOOGLE_PAYMENT_GATEWAY = "GOOGLE";
	private static final String PAYPAL_PAYMENT_GATEWAY = "PAYPAL";
	private static final long DAY_IN_MILLISECONDS = 86400000L;
	
	private SubscriptionInfoDAO subscriptionInfoDAO = null;
	private SubscriptionInfoLogDAO subscriptionInfoLogDAO = null;
	private GooglePaymentLogDAO googlePaymentLogDAO = null;
	private NotificationEngine notificationEngine = null;
	private DefaultGooglePaymentUtil googlePaymentUtil = null;
	private BillingManager billingManager = null;
	public DefaultUserSubscriptionInfoManager(SubscriptionInfoDAO subscriptionInfoDAO,
			SubscriptionInfoLogDAO subscriptionInfoLogDAO,
			GooglePaymentLogDAO googlePaymentLogDAO,
			NotificationEngine notificationEngine,
			DefaultGooglePaymentUtil googlePaymentUtil,
			BillingManager billingManager) {
		this.subscriptionInfoDAO = subscriptionInfoDAO;
		this.subscriptionInfoLogDAO = subscriptionInfoLogDAO;
		this.googlePaymentLogDAO = googlePaymentLogDAO;
		this.notificationEngine = notificationEngine;
		this.googlePaymentUtil = googlePaymentUtil;
		this.billingManager = billingManager;
	}
	
	@Override
	public void processSubscriptionInfo() {
		myLog.debug("processSubscriptionInfo");
		List<UserSubscriptionInfoVO> subscriptionInfoList = subscriptionInfoDAO.findAllSubscriptionInfo();
		for(UserSubscriptionInfoVO userSubscriptionInfoVO : subscriptionInfoList) {
			//get nextpayment date if not null
			Date paymentDate = null;
			if(userSubscriptionInfoVO.getNextPaymentDate() != null) {
				paymentDate = userSubscriptionInfoVO.getNextPaymentDate();
			} else {
				paymentDate = userSubscriptionInfoVO.getEffectiveDate();
			}
			int numberOfMonths = userSubscriptionInfoVO.getPricePkgVO().getQuantity();
			Date endDate = null;
			//For Google - nextPaymentDate indicate the new effective date after user renew subscription. 
			//For paypal - nextPaymentDate indicate the next date that user will be charged.
			if(userSubscriptionInfoVO.getNextPaymentDate() != null && 
					PAYPAL_PAYMENT_GATEWAY.equalsIgnoreCase(userSubscriptionInfoVO.getGwName())) {
				endDate = paymentDate;
			} else {
				endDate = DateUtils.addMonths(paymentDate, numberOfMonths);
			}
			
			long currentTime = System.currentTimeMillis();
			if(GOOGLE_PAYMENT_GATEWAY.equalsIgnoreCase(userSubscriptionInfoVO.getGwName())){
				//manual check for sub date
				//refresh token
				//TODO - only refresh token every hour
				myLog.debug("google payment " + userSubscriptionInfoVO.getUserid());
				GooglePaymentLogVO googlePaymentLogVO  = googlePaymentLogDAO.getUserLatestLogVO(
						userSubscriptionInfoVO.getUserid());
				if(googlePaymentLogVO == null) {
					//TODO - log not created
					myLog.debug("google payment missing log for " + userSubscriptionInfoVO.getUserid());
				} else {
					if(googlePaymentLogVO.getExpiryDate() == null || 
							googlePaymentLogVO.getExpiryDate().getTime() - System.currentTimeMillis() < DAY_IN_MILLISECONDS * 2) {
						try {
							String result = googlePaymentUtil.verifyGooglePayment(googlePaymentLogVO.getPurchaseToken(),
									userSubscriptionInfoVO.getPricePkgVO().getPkgCode());
							if(result != null) {
								JSONObject resultObject = new JSONObject(result);
								if(resultObject.has("validUntilTimestampMsec")) {
									long expiryTime = resultObject.getLong("validUntilTimestampMsec");
									boolean autoRenewing = resultObject.getBoolean("autoRenewing");
									if(googlePaymentLogVO.getExpiryDate() == null) { //receipt not verified when user purchase
										//change to verified and update the expiry date
										googlePaymentLogVO.setStatus(DefaultGooglePaymentUtil.RECEPIT_VERIFY_STATUS);
										googlePaymentLogVO.setExpiryDate(new Date(expiryTime));
										googlePaymentLogDAO.update(googlePaymentLogVO);
									} else if(googlePaymentLogVO.getExpiryDate().getTime() < expiryTime) {
										//subscription renewed
										GooglePaymentLogVO newGooglePaymentLogVO = new GooglePaymentLogVO(googlePaymentLogVO);
										newGooglePaymentLogVO.setReceiptDate(new Date());
										newGooglePaymentLogVO.setExpiryDate(new Date(expiryTime));
										googlePaymentLogDAO.create(newGooglePaymentLogVO);
										userSubscriptionInfoVO.setNextPaymentDate(new Date());
										subscriptionInfoDAO.update(userSubscriptionInfoVO);
										billingManager.provisionBillingCycle(userSubscriptionInfoVO.getPricePkgVO().getBillPkgVO(), 
												userSubscriptionInfoVO.getUserVO(), userSubscriptionInfoVO.getPricePkgVO().getQuantity(), false);
									} else if(expiryTime > System.currentTimeMillis() && !autoRenewing) {
										//expired and cancel subscription
										UserSubscriptionInfoLogVO userSubscriptionInfoLogVO= new UserSubscriptionInfoLogVO(userSubscriptionInfoVO);
										subscriptionInfoLogDAO.create(userSubscriptionInfoLogVO);
										subscriptionInfoDAO.delete(userSubscriptionInfoVO);
									} else {
										//do nothing - wait for google play to renew
									}
								} else { //cant find the transaction, move to log
									UserSubscriptionInfoLogVO userSubscriptionInfoLogVO= new UserSubscriptionInfoLogVO(userSubscriptionInfoVO);
									subscriptionInfoLogDAO.create(userSubscriptionInfoLogVO);
									subscriptionInfoDAO.delete(userSubscriptionInfoVO);
								}
							}
						} catch (JSONException ex) {
                            myLog.trace("Error", ex);
						} catch (CoreException ex) {
                            myLog.trace("Error", ex);
                        }
					} else {
						myLog.debug("google payment not in expire range " + userSubscriptionInfoVO.getUserid());
					}
					
				}
				
	            
			} else if(endDate.getTime() < currentTime) {
				//subscription ended and not renew
				myLog.debug("enddate < now " + userSubscriptionInfoVO.getUserid());
				UserSubscriptionInfoLogVO userSubscriptionInfoLogVO= new UserSubscriptionInfoLogVO(userSubscriptionInfoVO);
				subscriptionInfoLogDAO.create(userSubscriptionInfoLogVO);
				subscriptionInfoDAO.delete(userSubscriptionInfoVO);
			} else {
				//check if it is not renewable, and send reminder to renew 3 days before
				
				myLog.debug("enddate > now " + userSubscriptionInfoVO.getUserid() + " " + DateUtils.addDays(endDate, -3)
						+ " autorenew:" + userSubscriptionInfoVO.getPricePkgVO().isAutoRenew());
				if(!userSubscriptionInfoVO.getPricePkgVO().isAutoRenew()) {
					if(DateUtils.addDays(endDate, -3).getTime() <= currentTime && 
							DateUtils.addDays(endDate, -2).getTime() > currentTime) {
						myLog.debug("send reminder to " + userSubscriptionInfoVO.getUserid());
						notificationEngine.sendNonRecurringBillEndingReminder(userSubscriptionInfoVO.getUserVO(), 
								userSubscriptionInfoVO.getPricePkgVO().getPkgDesc(), endDate);
						
					}
				}
				
				
			}
		}
	}
	
}
