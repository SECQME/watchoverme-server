package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.UserDAO;
import com.secqme.domain.model.UserVO;
import javafx.print.Collation;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author james
 */
public class UserJPADAO extends BaseJPADAO<UserVO, String> implements UserDAO {

    private static Logger myLog = Logger.getLogger(UserJPADAO.class);

    public UserJPADAO() {
        super(UserVO.class);
    }

    @Override
    public UserVO findByMobileNumberAndMobileCountry(String mobileCountryISO, String mobileNumber) {
        UserVO userVO = null;
        JPAParameter parameter = new JPAParameter()
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("mobileCountryISO", mobileCountryISO);
        List<UserVO> userList = executeQueryWithResultList(UserVO.QUERY_FIND_BY_MOBILE_COUNTRY_NUMER, parameter);
        if(userList != null && userList.size() > 0) {
            userVO = userList.get(0);
        }
        return userVO;

        // TODO: The mobileCountry-mobileNo must be unique and then we can use executeQueryWithSingleResult
        // return executeQueryWithSingleResult(UserVO.QUERY_FIND_BY_MOBILE_COUNTRY_NUMER, parameter);
    }

    @Override
    public UserVO findByActivationCode(String actCode) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("activateCode", actCode);
        return executeQueryWithSingleResult(UserVO.QUERY_FIND_BY_ACTIVATIONCODE, parameter);
    }

    @Override
    public UserVO findByEmail(String email) {
        UserVO userVO = null;

        JPAParameter parameter = new JPAParameter().setParameter("email", email);
        List<UserVO> userList = executeQueryWithResultList(UserVO.QUERY_FIND_BY_EMAIL, parameter);
        if(userList != null && userList.size() > 0) {
            userVO = userList.get(0);
        }

        return userVO;

        // TODO: The email must be unique and then we can use executeQueryWithSingleResult
        // return executeQueryWithSingleResult(UserVO.QUERY_FIND_BY_EMAIL, parameter);
    }

    @Override
    public UserVO findByUserId(String userid) {
        JPAParameter param = new JPAParameter().setParameter("userid", userid);
        return executeQueryWithSingleResult(UserVO.QUERY_FIND_BY_USER_ID, param);
    }

    @Override
    public UserVO findByPasswordResetToken(String passwordResetToken) {
        JPAParameter param = new JPAParameter().setParameter("passwordResetToken", passwordResetToken);
        return executeQueryWithSingleResult(UserVO.QUERY_FIND_BY_PASSWORD_RESET_TOKEN, param);
    }

    @Override
    public UserVO findByEmailVerificationToken(String emailVerificationToken) {
        JPAParameter param = new JPAParameter().setParameter("emailVerificationToken", emailVerificationToken);
        return executeQueryWithSingleResult(UserVO.QUERY_FIND_BY_EMAIL_VERIFICATION_TOKEN, param);
    }


    @Override
    public List<UserVO> findAllUsers() {
    	return executeQueryWithResultList(UserVO.QUERY_FIND_ALL_USERS);
    }

    @Override
    public List<UserVO> findActiveUserByDeviceAndTimezone(String device, String timeZone, Date updatedDate) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("device", "%"+device+"%")
                .setParameter("timeZone", "%"+timeZone+"%")
                .setParameter("updatedDate", updatedDate);

        return executeQueryWithResultList(UserVO.QUERY_FIND_ACTIVE_USER_BY_DEVICE_AND_TIMEZONE,parameter);
    }
}
