package com.secqme.manager.billing;

import com.secqme.CoreException;
import com.secqme.domain.model.billing.BillingCycleVO;
import com.secqme.domain.model.billing.BillingLogType;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.referral.ReferralProgramVO;
import org.codehaus.jettison.json.JSONException;

/**
 *
 * @author james
 */
public interface BillingManager{

    public void verifyUserBillingPackage(UserVO userVO) throws CoreException;
    public void activateFreeMiumPackage(UserVO userVO, Boolean provisionBillCycle) throws CoreException;
    public void assignMarketAndBillingPackage(UserVO userVO);
    public void provisionBillingCycleForNewUser(BillingPkgVO billPkgVO, UserVO userVO) throws CoreException;
    public void chargeSMSCreditUsed(SecqMeEventVO eventVO, int smsCreditUsed) throws CoreException;
    public void chargeEventRegistration(SecqMeEventVO eventVO) throws CoreException;
    public void authorizeTransaction(SecqMeEventVO eventVO) throws CoreException;
    public int  getAvailableSMSCredit(UserVO userVO) throws CoreException;
    public BillingCycleVO getUserLatestBillCycleVO(UserVO userVO);
    public void assignFreeMiumBillingPackage(UserVO userVO);
    public void activateTrialPackageForUser(UserVO userVO) throws CoreException;
    public void provisionBillingCycle(BillingPkgVO billPkgVO, UserVO userVO,   Integer numberOfMonths, Boolean provisionForTrial) throws CoreException;
	public int getAvailableSMSCredit(SecqMeEventVO secqMeEvent) throws CoreException;
    public void executeReferralRewards(UserVO orgUserVO, UserVO refUserVO, ReferralProgramVO referralProgramVO) throws CoreException;
    public BillingPkgVO getDefaultTrialPackage(UserVO userVO);
    public String getTrialPackageMarketingTextJSON(UserVO userVO) throws CoreException, JSONException;
    public String getPremiumMarketTextConfigJSON(UserVO userVO) throws CoreException, JSONException;
    public void addSMSCreditToUser(UserVO userVO, int smsCredit) throws CoreException;
    // convert trial user to lite and give free SMS credits to the user
    public void checkTrialBillingPackage(UserVO userVO) throws CoreException;
    // free upgrade to premium 
    public void freePremium(UserVO userVO, int months) throws CoreException;
    public void rewardUser(UserVO userVO, BillingPkgVO billingPkgVO, Integer extendedDays, Integer smsAddition, BillingLogType logType) throws CoreException;
}
