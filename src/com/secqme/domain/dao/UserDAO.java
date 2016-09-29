package com.secqme.domain.dao;

import java.util.Date;
import java.util.List;

import com.secqme.domain.model.UserVO;

/**
 *
 * @author james
 */
public interface UserDAO extends BaseDAO<UserVO, String> {
    public UserVO findByMobileNumberAndMobileCountry(String mobileCountryISO, String mobileNumber);
    public UserVO findByActivationCode(String actCode);
    public UserVO findByEmail(String email);
    public UserVO findByUserId(String userid);
    public UserVO findByPasswordResetToken(String passwordResetToken);
    public UserVO findByEmailVerificationToken(String emailVerificationToken);
    public List<UserVO> findAllUsers();
    public List<UserVO> findActiveUserByDeviceAndTimezone(String device, String timeZone, Date updatedDate);
}
