package com.secqme.util.io;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.crime.CrimeReportVO;
import com.secqme.domain.model.event.EventLogVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.util.Base64;
import com.secqme.util.MediaFileType;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jameskhoo
 */
public class DefaultMediaFileManager implements MediaFileManager {
    public final static String EVENT_JOUNEY_PREFIX = "eventJourney";
    public final static String CRIME_REPORT_PREFIX = "crimePic";
    public final static String PROFILE_PIC_PREFIX = "profilePic";

    private String mediaRootFilePath = null;
    private String mediaURLBasePath = null;
    private Logger myLog  = Logger.getLogger(DefaultMediaFileManager.class);

    public DefaultMediaFileManager(String rootPath, String mediaURLBasePath) {
        mediaRootFilePath = rootPath;
        this.mediaURLBasePath = mediaURLBasePath;
    }

    public String saveEventJourneyMediaFile(EventLogVO eventLogVO, MediaFileType fileType,
                                            String encodedMediaFileStream) {
        String eventLogMediaFileName = this.getEventLogMediaFileName(eventLogVO.getSecqMeEventVO().getId(), eventLogVO.getId());
        this.saveMediaFile(eventLogMediaFileName, fileType, encodedMediaFileStream);
        return mediaURLBasePath + eventLogMediaFileName + "." + fileType.getFileExt();
    }

    public String saveCrimeReportMediaFile(CrimeReportVO crimeReportVO, MediaFileType fileType, String base64EncodedFile) {
        String userId = crimeReportVO.getUserVO().getUserid();
        Date reportDate = crimeReportVO.getReportDate();

        String fileName = CRIME_REPORT_PREFIX + userId.replace("@","__") + reportDate.getTime();
        this.saveMediaFile(fileName, fileType, base64EncodedFile);
        return mediaURLBasePath + fileName + "." + fileType.getFileExt();
    }

    @Override
    public String saveEventMediaFile(EventLogVO eventLogVO, MediaFileType fileType, String base64EncodedFile) {
        return null;
    }

    @Override
    public String saveEventMediaFile(EventLogVO eventLogVO, MediaFileType fileType, InputStream uploadedFile) {
        return null;
    }

    @Override
    public String saveEventMediaFile(EventLogVO eventLogVO, MediaFileType fileType, byte[] data) {
        return null;
    }

    public List<String> populateEventMediaUrl(final SecqMeEventVO eventVO, final MediaFileType mediaFileType) {
        List<String> mediaFileURLList = null;
        File mediaRootFileDir = new File(mediaRootFilePath);
        myLog.debug("Populating Media file, trackingPin->" + eventVO + " fileType->" + mediaFileType.getFileExt()
                + " mediaURLPrefix->" + mediaURLBasePath + ", mediaRootFileDir->" + mediaRootFileDir);
        FilenameFilter mediaFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String fileName) {
                return fileName.startsWith(eventVO.getTrackingPin()) && fileName.endsWith(mediaFileType.getFileExt());
            }
        };

        String matchFileArray[] = mediaRootFileDir.list(mediaFileFilter);
        if (matchFileArray != null && matchFileArray.length > 0) {
            mediaFileURLList = new ArrayList<String>();
            for (String fileName : matchFileArray) {
                mediaFileURLList.add(mediaURLBasePath + fileName);
            }
        }

        if (mediaFileURLList != null) {
            myLog.debug("Total " + mediaFileURLList.size() + " media file initialized" );
        }
        return mediaFileURLList;
    }

    public void saveMediaFile(String fileid, MediaFileType fileType, String encodedMediaFileStream) {

        try {
            myLog.debug("Saving" + fileType + " file to " + mediaRootFilePath + fileid + "."  + fileType.getFileExt() );
            Base64.decodeToFile(encodedMediaFileStream, mediaRootFilePath + fileid + "." + fileType.getFileExt());
        } catch (IOException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }

    public String saveEventMediaFileMultipart(InputStream uploadedInputStream, String fileid, MediaFileType fileType) {
        String filePath = mediaRootFilePath + fileid + "." + fileType.getFileExt();
        myLog.debug("Saving file " + filePath);
        try {
            OutputStream out;
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(filePath));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
            myLog.debug("Saving file complete" + filePath);
        } catch (IOException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return mediaURLBasePath + encodeFileName(fileid, fileType);
    }

    @Override
    public String saveUserProfilePicture(UserVO userVO, MediaFileType fileType, String base64EncodedFile) {
    	String fileName = PROFILE_PIC_PREFIX + userVO.getUserid().replace("@","__") + new Date().getTime();
        this.saveMediaFile(fileName, fileType, base64EncodedFile);
        return mediaURLBasePath + fileName + "." + fileType.getFileExt();
    }
    
    private String getEventLogMediaFileName(Long eventId, Long eventLogID) {
       return EVENT_JOUNEY_PREFIX + eventId + "_" + eventLogID;
    }

    private String encodeFileName(String fileName, MediaFileType fileType) {
        String returnFileName = null;
        try {
            returnFileName = URLEncoder.encode(fileName + "." + fileType.getFileExt(), "UTF-8");
        } catch (UnsupportedEncodingException ue) {

        }
        return returnFileName;
    }


}
