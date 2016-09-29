package com.secqme.util;


import com.secqme.CoreException;
import com.secqme.domain.model.event.SecqMeEventVO;

/**
 * User: James Khoo
 * Date: 3/12/14
 * Time: 6:42 PM
 */
public interface ShortURLUpdateService {
    public void updateShortURL(Object valueObject, String longURL) throws CoreException;
}
