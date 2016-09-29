package com.secqme.domain.dao;

import java.util.Date;
import java.util.List;

import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;

public interface ContactDAO extends BaseDAO<ContactVO, Long>{

    public List<ContactVO> findInvitedContactByEmailOrMobileNumber(String emailAddress, String mobileCountryIso, String mobileNo);
	public List<ContactVO> findInvitedContactByMobileNumber(String iso, String mobileNo);
    public List<ContactVO> findInvitedContactByEmailAddress(String emailAddress);
	public List<ContactVO> findByEmailOrMobileNumber(String emailAddress, String mobileCountryIso, String mobileNo);
    public List<ContactVO> findContactsNeedToBeReminded(Date beforeDate);
	public List<ContactVO> findAllContacts();
	public ContactVO findByContactToken(String contactToken);
}
