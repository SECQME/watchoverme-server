package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.ContactDAO;
import com.secqme.domain.model.ContactInvitationStatus;
import com.secqme.domain.model.ContactVO;

import java.util.Date;
import java.util.List;

public class ContactJPADAO extends BaseJPADAO<ContactVO, Long> implements ContactDAO {
    public ContactJPADAO() {
        super(ContactVO.class);
    }

    @Override
    public List<ContactVO> findInvitedContactByEmailOrMobileNumber(String emailAddress, String mobileCountryIso, String mobileNo) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("emailAddress", emailAddress)
                .setParameter("iso", mobileCountryIso)
                .setParameter("mobileNo", mobileNo)
                .setParameter("status1", ContactInvitationStatus.INVITED)
                .setParameter("status2", ContactInvitationStatus.ACCEPTED);
        return executeQueryWithResultList(ContactVO.QUERY_FIND_BY_ACCEPTED_CONTACT_EMAIL_OR_MOBILE_NO, parameter);
    }

    @Override
    public List<ContactVO> findByEmailOrMobileNumber(String emailAddress, String mobileCountryIso, String mobileNo) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("emailAddress", emailAddress)
                .setParameter("iso", mobileCountryIso)
                .setParameter("mobileNo", mobileNo);
        return executeQueryWithResultList(ContactVO.QUERY_FIND_BY_CONTACT_EMAIL_OR_MOBILE_NO, parameter);
    }

    @Override
    public List<ContactVO> findInvitedContactByMobileNumber(String iso, String mobileNo) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("iso", iso)
                .setParameter("mobileNo", mobileNo)
                .setParameter("status1", ContactInvitationStatus.INVITED)
                .setParameter("status2", ContactInvitationStatus.ACCEPTED);
        return executeQueryWithResultList(ContactVO.QUERY_FIND_BY_CONTACT_MOBILE_NO, parameter);
    }

    @Override
    public List<ContactVO> findInvitedContactByEmailAddress(String emailAddress) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("emailAddress", emailAddress)
                .setParameter("status1", ContactInvitationStatus.INVITED)
                .setParameter("status2", ContactInvitationStatus.ACCEPTED);
        return executeQueryWithResultList(ContactVO.QUERY_FIND_BY_CONTACT_EMAIL_ADDRESS, parameter);
    }

    @Override
    public List<ContactVO> findContactsNeedToBeReminded(Date beforeDate) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("beforeDate", beforeDate);
        return executeQueryWithResultList(ContactVO.QUERY_FIND_CONTACT_NEED_TO_BE_REMINDED, parameter);
    }

    @Override
    public List<ContactVO> findAllContacts() {
        return executeQueryWithResultList(ContactVO.QUERY_FIND_ALL_CONTACT);
    }

    @Override
    public ContactVO findByContactToken(String contactToken) {
        JPAParameter param = new JPAParameter().setParameter("rejectCode", contactToken);
        List<ContactVO> contacts = executeQueryWithResultList(ContactVO.QUERY_FIND_BY_CONTACT_TOKEN, param);
        if (contacts != null && contacts.size() > 0) {
            return contacts.get(0);
        } else {
            return null;
        }
    }
}
