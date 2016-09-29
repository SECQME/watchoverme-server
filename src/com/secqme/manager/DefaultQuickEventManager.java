package com.secqme.manager;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.dao.QuickEventTemplateDAO;
import com.secqme.domain.model.event.QuickEventTemplateVO;
import com.secqme.domain.model.event.SavedEventVO;
import com.secqme.domain.model.UserVO;
import com.secqme.util.cache.CacheUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: James Khoo
 * Date: 11/18/13
 * Time: 11:36 AM
 */
public class DefaultQuickEventManager extends BaseManager implements QuickEventManager  {
    private static final Logger myLog = Logger.getLogger(DefaultQuickEventManager.class);
    private QuickEventTemplateDAO quickEventTemplateDAO = null;

    public DefaultQuickEventManager(QuickEventTemplateDAO eventTemplateDAO) {
        this.quickEventTemplateDAO = eventTemplateDAO;
    }

    public void createNewSavedEvent(UserVO userVO, String eventName,
                                    Integer eventDuration, String optionalDescription) throws CoreException {
        SavedEventVO eventVO = new SavedEventVO();
        eventVO.setUserVO(userVO);
        eventVO.setEventName(eventName);
        eventVO.setEventDuration(eventDuration);
        eventVO.setOptionalDescription(optionalDescription);
        getSavedEventsDAO().create(eventVO);
        userVO.getSavedEventVOList().add(eventVO);
        getUserDAO().update(userVO);
    }

    public void updateUserSavedEvent(UserVO userVO, Long quickEventId, String newEventName,
                                     Integer newEventDuration, String optionalDescription) throws CoreException {

        SavedEventVO SavedEventVO = getQuickEvent(userVO, quickEventId);
        if(SavedEventVO == null) {
            throw new CoreException(ErrorType.QUICK_EVENT_NOT_FOUND_ERROR, userVO.getLangCode(), quickEventId, userVO.getUserid());
        }

        if(newEventName != null) {
            SavedEventVO.setEventName(newEventName);
        }

        if(newEventDuration != null) {
            SavedEventVO.setEventDuration(newEventDuration);
        }

        if(optionalDescription != null) {
            SavedEventVO.setOptionalDescription(optionalDescription);
        }

        getUserDAO().update(userVO);
    }

    public void deleteUserSavedEvent(UserVO userVO, Long quickEventId) throws CoreException {
        SavedEventVO SavedEventVO = getQuickEvent(userVO, quickEventId);
        if (SavedEventVO != null) {
            userVO.getSavedEventVOList().remove(SavedEventVO);
            getUserDAO().update(userVO);
        } else {
            throw new CoreException(ErrorType.QUICK_EVENT_NOT_FOUND_ERROR, userVO.getLangCode(), quickEventId, userVO.getUserid());
        }
    }

    public List<SavedEventVO> getUserSavedEventList(UserVO userVO) throws CoreException {
        return userVO.getSavedEventVOList();
    }

    public void populateSavedEventListForNewUser(UserVO userVO) {
        myLog.debug("Populating Quick Events For user->" + userVO.getUserid() + ", language->" + userVO.getLangCode());
        List<SavedEventVO> SavedEventVOList = new ArrayList<SavedEventVO>();
        List<QuickEventTemplateVO> eventTemplateVOs = getQuickEventTemplateHashMap().get(userVO.getLangCode());

        if (eventTemplateVOs != null) {
            for (QuickEventTemplateVO eventTemplateVO : eventTemplateVOs) {
                SavedEventVO SavedEventVO = new SavedEventVO();
                SavedEventVO.setUserVO(userVO);
                SavedEventVO.setEventName(eventTemplateVO.getEventName());
                SavedEventVO.setEventDuration(eventTemplateVO.getEventDuration());
                SavedEventVO.setOptionalDescription(eventTemplateVO.getOptionalDescription());

                SavedEventVOList.add(SavedEventVO);
            }
            userVO.setSavedEventVOList(SavedEventVOList);
            getUserDAO().update(userVO);
        }
    }


    public void refreshSetting() {
        HashMap<String, List<QuickEventTemplateVO>> quickEventTemplateHashMap =
                new HashMap<String, List<QuickEventTemplateVO>>();

        List<QuickEventTemplateVO> quickEventTemplateVOList = quickEventTemplateDAO.findAll();
        if (quickEventTemplateVOList != null && quickEventTemplateVOList.size() > 0) {
            for (QuickEventTemplateVO eventTemplateVO : quickEventTemplateVOList) {
                if (!quickEventTemplateHashMap.containsKey(eventTemplateVO.getLangCode())) {
                    quickEventTemplateHashMap.put(eventTemplateVO.getLangCode(), new ArrayList<QuickEventTemplateVO>());
                }
                quickEventTemplateHashMap.get(eventTemplateVO.getLangCode()).add(eventTemplateVO);
            }

            getCacheUtil().storeObjectIntoCache(CacheUtil.QUICK_EVENT_TEMPLATE_LIST_KEY, quickEventTemplateHashMap);
            myLog.debug("Total of : " + quickEventTemplateVOList.size() + " QuickEvent Templates stored to cache");
        }

    }


    private SavedEventVO getQuickEvent(UserVO userVO, Long quickEventId) {
        SavedEventVO  aQuickEvent = null;

        if (userVO.getSavedEventVOList() != null && userVO.getSavedEventVOList().size() > 0) {
            for (SavedEventVO quickEvent : userVO.getSavedEventVOList()) {
                if (quickEvent.getId().equals(quickEventId)) {
                    aQuickEvent = quickEvent;
                    break;
                }
            }
        }
        return aQuickEvent;
    }

    private HashMap<String, List<QuickEventTemplateVO>> getQuickEventTemplateHashMap() {
        if (getCacheUtil().getCachedObject(CacheUtil.QUICK_EVENT_TEMPLATE_LIST_KEY) == null) {
            refreshSetting();
        }

        return (HashMap<String, List<QuickEventTemplateVO>>)
                getCacheUtil().getCachedObject(CacheUtil.QUICK_EVENT_TEMPLATE_LIST_KEY);
    }
}
