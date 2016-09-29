package com.secqme.manager;

import com.secqme.CoreException;
import com.secqme.domain.model.event.SavedEventVO;
import com.secqme.domain.model.UserVO;

import java.util.List;

/**
 * User: James Khoo
 * Date: 11/18/13
 * Time: 11:34 AM
 */
public interface QuickEventManager {
    public void createNewSavedEvent(UserVO userVO, String eventName,
                                    Integer eventDuration, String optionalDescription) throws CoreException;
    public void updateUserSavedEvent(UserVO userVO, Long quickEventId, String newEventName,
                                     Integer newEventDuration, String optionalDescription) throws CoreException;
    public void deleteUserSavedEvent(UserVO userVO, Long quickEventId) throws CoreException;
    public List<SavedEventVO> getUserSavedEventList(UserVO userVO) throws CoreException;
    public void populateSavedEventListForNewUser(UserVO userVO);
    public void refreshSetting();
}
