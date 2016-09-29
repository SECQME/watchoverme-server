package com.secqme.util.io;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.secqme.domain.dao.EventLogDAO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.crime.CrimeReportVO;
import com.secqme.domain.model.event.EventLogVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.util.Base64;
import com.secqme.util.MediaFileType;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward on 25/06/2015.
 */
public class S3MediaFileManager implements MediaFileManager {
    private static final Logger myLog  = Logger.getLogger(S3MediaFileManager.class);

    private final String PROFILE_PIC_PREFIX = "user_";
    private final String CRIME_REPORT_PREFIX = "report_";

    private AWSCredentials awsCredentials;
    private AmazonS3Client s3;

    private String crimeReportBucketName;
    private String profilePicBucketName;
    private String eventAudioBucketName;
    private String eventImageBucketName;
    private String eventVideoBucketName;

    private EventLogDAO eventLogDAO;

    public S3MediaFileManager(String accessKeyId, String secretKey, EventLogDAO eventLogDAO, String profilePicBucketName, String eventAudioBucketName, String eventImageBucketName, String eventVideoBucketName, String crimeReportBucketName) {
        this.awsCredentials = new BasicAWSCredentials(accessKeyId, secretKey);
        this.s3 = new AmazonS3Client(this.awsCredentials);

        this.eventLogDAO = eventLogDAO;

        this.profilePicBucketName = profilePicBucketName;
        this.eventAudioBucketName = eventAudioBucketName;
        this.eventImageBucketName = eventImageBucketName;
        this.eventVideoBucketName = eventVideoBucketName;
        this.crimeReportBucketName = crimeReportBucketName;
    }

    @Override
    public String saveCrimeReportMediaFile(CrimeReportVO crimeReportVO, MediaFileType fileType, String base64EncodedFile) {
        String key = CRIME_REPORT_PREFIX + crimeReportVO.getUserVO().getId() + "_" + crimeReportVO.getId() + "_" + crimeReportVO.getReportDate().getTime() + "." + fileType.getFileExt();

        try {
            myLog.debug("Convert crime report picture " + key + " from Base64 encoded format.");
            byte[] imageBytes = Base64.decode(base64EncodedFile);

            myLog.debug("Saving crime report picture " + key + " with length " + imageBytes.length + " bytes to AWS S3.");

            String url = saveToS3(crimeReportBucketName, key, imageBytes);
            crimeReportVO.setCrimePictureURL(url);
            return url;
        } catch (IOException ex) {
            myLog.error(ex.getMessage(), ex);
        }

        return null;
    }

    @Override
    public String saveEventMediaFile(EventLogVO eventLogVO, MediaFileType fileType, String base64EncodedFile) {
        try {
            byte[] data = Base64.decode(base64EncodedFile);
            return saveEventMediaFile(eventLogVO, fileType, data);
        } catch (IOException ex) {
            myLog.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public String saveEventMediaFile(EventLogVO eventLogVO, MediaFileType fileType, InputStream uploadedFile) {
        try {
            byte[] data = IOUtils.toByteArray(uploadedFile);
            return saveEventMediaFile(eventLogVO, fileType, data);
        } catch (IOException ex) {
            myLog.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public String saveEventMediaFile(EventLogVO eventLogVO, MediaFileType fileType, byte[] data) {
        long eventId = eventLogVO.getSecqMeEventVO().getId();
        long userId = eventLogVO.getSecqMeEventVO().getUserVO().getId();
        long eventLogId = eventLogVO.getId();
        long timestamp = eventLogVO.getEventTime().getTime();

        String bucketName = null;
        if (MediaFileType.AUDIO.equals(fileType)) {
            bucketName = this.eventAudioBucketName;
        } else if (MediaFileType.PICTURE.equals(fileType)) {
            bucketName = this.eventImageBucketName;
        } else if (MediaFileType.VIDEO.equals(fileType)) {
            bucketName = this.eventVideoBucketName;
        } else {
            throw new IllegalArgumentException("Unknown file type: " + fileType);
        }

        String key = userId + "_" + eventId + "_" + eventLogId + "_" + timestamp + "." + fileType.getFileExt();

        myLog.debug("Saving event media file " + key + " with length " + data.length + " bytes to AWS S3.");
        String url = saveToS3(bucketName, key, data);

        eventLogVO.setMediaFileURL(url);
        eventLogVO.setMediaType(fileType);

        return url;
    }

    @Override
    public String saveUserProfilePicture(UserVO userVO, MediaFileType fileType, String base64EncodedFile) {
        String key = PROFILE_PIC_PREFIX + userVO.getId() + "." + fileType.getFileExt();

        try {
            myLog.debug("Convert user profile picture " + userVO.getUserid() + " from Base64 encoded format.");
            byte[] imageBytes = Base64.decode(base64EncodedFile);

            myLog.debug("Saving profile picture " + userVO.getUserid() + " with length " + imageBytes.length + " bytes to AWS S3.");

            String url = saveToS3(profilePicBucketName, key, imageBytes);
            userVO.setProfilePictureURL(url);
            return url;
        } catch (IOException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        
        return null;
    }

    private String saveToS3(String bucketName, String key, byte[] data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(data.length);

        s3.putObject(bucketName, key, inputStream, metadata);
        return s3.getResourceUrl(bucketName, key);
    }

    @Override
    public List<String> populateEventMediaUrl(SecqMeEventVO eventVO, MediaFileType mediaFileType) {
        List<EventLogVO> eventLogVOs = eventLogDAO.findByEventIdAndMediaType(eventVO.getId(), mediaFileType);

        List<String> urls = new ArrayList<>();
        for (EventLogVO eventLogVO : eventLogVOs) {
            urls.add(eventLogVO.getMediaFileURL());
        }

        return urls;
    }
}
