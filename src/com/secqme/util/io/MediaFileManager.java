package com.secqme.util.io;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.crime.CrimeReportVO;
import com.secqme.domain.model.event.EventLogVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.util.MediaFileType;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jameskhoo
 */
public interface MediaFileManager {
    public String saveCrimeReportMediaFile(CrimeReportVO crimeReportVO, MediaFileType fileType, String base64EncodedFile);
    public String saveEventMediaFile(EventLogVO eventLogVO, MediaFileType fileType, String base64EncodedFile);
    public String saveEventMediaFile(EventLogVO eventLogVO, MediaFileType fileType, InputStream uploadedFile);
    public String saveEventMediaFile(EventLogVO eventLogVO, MediaFileType fileType, byte[] data);
    public String saveUserProfilePicture(UserVO userVO, MediaFileType fileType, String base64EncodedFile);
    public List<String> populateEventMediaUrl(final SecqMeEventVO eventVO, final MediaFileType mediaFileType);
}
