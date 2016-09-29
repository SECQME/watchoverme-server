package com.secqme.manager.referral;

import com.secqme.CoreException;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.referral.ReferralClickType;
import com.secqme.domain.model.referral.ReferralInfoVO;
import com.secqme.domain.model.referral.ReferralLogVO;
import com.secqme.domain.model.referral.ReferralProgramVO;

/**
 * User: James Khoo
 * Date: 2/20/14
 * Time: 11:57 AM
 */
public interface ReferralManager {

    public void initReferralProgram(); // To initialize all ReferralProgram
    public ReferralProgramVO getReferralProgramByUser(UserVO userVO);
    public ReferralInfoVO getReferralInfoVO(UserVO userVO);
    public String getReferralURL(UserVO userVO);
    public ReferralLogVO insertReferralLogs(String refURL, ReferralClickType referralClickType,
                                            String requestIP,
                                            UserVO refUserVO,
                                            String userAgent, String acceptLanguage,
                                            String sessionID, String fingerPrint) throws CoreException;

    public ReferralLogVO findReferralLogMatchWithFingerPrint(ReferralClickType clickType,
                                                             String requestIP, String fingerPrint,
                                                             Integer intervalInMinutes);

    public void rewardUser(UserVO orgUserVO, UserVO refUserVO) throws CoreException;
}
