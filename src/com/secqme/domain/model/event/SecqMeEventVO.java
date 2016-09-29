package com.secqme.domain.model.event;

import com.secqme.domain.model.EventStatusType;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingLogVO;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author james
 */
@Entity
@Table(name = "secqMeEvent")
@NamedQueries({

    @NamedQuery(name=SecqMeEventVO.QUERY_GET_ALL_USER_EVENTS,
    query = "SELECT o "
    + "FROM SecqMeEventVO  o "
    + "WHERE o.userVO.userid = :userid "
    + "ORDER BY o.id desc"),

    @NamedQuery(name = SecqMeEventVO.QUERY_FIND_EVENT_ENDTIME_INBETWEEN,
    query = "SELECT o "
    + "FROM SecqMeEventVO o "
    + "WHERE o.endTime >= :startTime "
    + "AND o.endTime <= :endTime "
    + "AND o.status = :status "),
            
    @NamedQuery(name = SecqMeEventVO.QUERY_FIND_EVENT_LESS_THEN_ENDTIME,
    query = "SELECT o "
    + "FROM SecqMeEventVO o "
    + "WHERE o.endTime <= :endTime "
    + "AND o.status = :status "),

   @NamedQuery(name = SecqMeEventVO.QUERY_FIND_USER_EVENT_BETWEEN_DATES,
    query = "SELECT o "
    + "FROM SecqMeEventVO o "
    + "WHERE o.userVO.userid = :userid "
    + "AND o.startTime >= :startTime "
    + "AND o.startTime <= :endTime "    
    + "ORDER BY o.startTime desc"),

    @NamedQuery(name = SecqMeEventVO.QUERY_FIND_USER_LATEST_EVENT,
    query = "SELECT o "
    + "FROM SecqMeEventVO o "
    + "WHERE o.id = ( SELECT MAX(t.id) FROM SecqMeEventVO t WHERE t.userVO.userid = :userid)"),

    @NamedQuery(name = SecqMeEventVO.QUERY_FIND_BY_TRACKING_PIN,
    query = "SELECT o "
    + "FROM SecqMeEventVO o "
    + "WHERE o.id = ( SELECT MAX(t.id) FROM SecqMeEventVO t WHERE t.trackingPin = :trackingPin)"),

    // Ensure only one require is return
    @NamedQuery(name = SecqMeEventVO.QUERY_FIND_NEW_CONFIRM_EVENT_BY_USER,
    query = "SELECT o "
    + "FROM SecqMeEventVO o "
    + "WHERE o.userVO.userid = :userid "
    + " AND ( o.status = :newStatus "
    + " OR o.status = :confirmStatus )"),

    @NamedQuery(name= SecqMeEventVO.QUERY_COUNT_EVENT,
    query = "SELECT count(o.id)  " +
            "FROM SecqMeEventVO o " +
            "WHERE o.userVO.userid = :userid ")

})
public class SecqMeEventVO implements Serializable {

    public static final String QUERY_FIND_EVENT_ENDTIME_INBETWEEN = "secQme.findInBetween_EndTime";
    public static final String QUERY_FIND_NEW_CONFIRM_EVENT_BY_USER = "secQme.findNewConfirmEventByUser";
    public static final String QUERY_FIND_EVENT_LESS_THEN_ENDTIME = "secQme.findEventLessThenEndTime";
    public static final String QUERY_FIND_USER_LATEST_EVENT = "secQme.findUserLatestEvent";
    public static final String QUERY_FIND_BY_TRACKING_PIN = "secQme.findByTrackingPin";
    public static final String QUERY_FIND_USER_EVENT_BETWEEN_DATES = "secQme.eventBetweenDate";
    public static final String QUERY_GET_ALL_USER_EVENTS = "SecqMeEventVO.findAllEvent";
    public static final String QUERY_COUNT_EVENT = "secQmeEvent.countEvent" ;
    public static final String EVENT_ID_KEY = "eventId";
    public static final String EVENT_MSG_KEY = "eventMsg";
    public static final String EVENT_START_TIME_KEY = "eventStartTime";
    public static final String EVENT_END_TIME_KEY = "eventEndTime";
    public static final String EVENT_TYPE_KEY = "eventType";
    public static final String EVENT_STATUS_KEY = "eventStatus";
    public static final String EVENT_TRACKING_PIN_KEY = "eventTrackingPin";
    public static final String EVENT_ENABLE_GPS_KEY = "eventEnableGPS";
    public static final String EVENT_RUNNING_KEY = "eventRunning";
    public static final String EVENT_ENABLE_SAFETY_NOTIFY_KEY = "eventSafetyNotify";
    public static final String USER_CONFIRM_SAFETY_KEY = "confirmSafety";
    public static final String EVENT_DURATION_KEY = "eventDuration";

    public static final String CONFIRM_SAFETY_TIME_KEY = "confirmSafetyTime";

    private static final Logger myLog = Logger.getLogger(SecqMeEventVO.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;



    private String message;
    @Enumerated(EnumType.STRING)
    @Column(name = "eventType")
    private EventType eventType;
    @Enumerated(EnumType.STRING)
    private EventStatusType status;
    private String trackingPin; // Each Event Registration will come with each own 4-6 random character as tracking Pin;
    private Boolean enableGPS = false;
    private Boolean enableAudio = false;
    private Boolean enablePicture = false;
    private Boolean enableSafetyNotify = false;
    private Boolean enableVideo = false;
    private Boolean archived = false;

    private Long extendedDuration; //khlow20120606
    private String eventTimeZone; // jk20130120  to capture user's TimeZone
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date confirmSafetyTime = null;

    @OneToMany(mappedBy = "secqMeEventVO",
    fetch = FetchType.EAGER,
    cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<BillingLogVO> billingLogVOList;

    @Temporal(TemporalType.TIMESTAMP)
    private Date emergencyTriggerAt;

    private Double emergencyLatitude;
    private Double emergencyLongitude;
    private Double emergencyAccuracy;
    private String trackingURL;
    private boolean enableShare;
    public SecqMeEventVO() {
        // empty constructor
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getStartTime() {
        return this.startTime == null ? null : new Date(this.startTime.getTime());
    }

    public void setStartTime(Date newTime) {
        this.startTime = newTime == null ? null : new Date(newTime.getTime());
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public Boolean getEnableVideo() {
        return enableVideo;
    }

    public void setEnableVideo(Boolean enableVideo) {
        this.enableVideo = enableVideo;
    }
    
    public Long getEventDurationInSeconds() {
        Long duration = 0l;
        if (endTime != null) {
            duration = (endTime.getTime() - startTime.getTime())/ (1000);
        }
        return duration;
    }
    
    public Long getEventDurationInMinutes() {
        Long duration = 0l;
        if (endTime != null) {
            duration = (endTime.getTime() - startTime.getTime())/ (1000 * 60);
        }
        return duration;
    }

    public Boolean getEnableAudio() {
        return enableAudio;
    }

    public void setEnableAudio(Boolean enableAudio) {
        this.enableAudio = enableAudio;
    }

    public Boolean getEnableSafetyNotify() {
        return enableSafetyNotify;
    }

    public void setEnableSafetyNotify(Boolean enableSafetyNotify) {
        this.enableSafetyNotify = enableSafetyNotify;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }

    public EventStatusType getStatus() {
        return status;
    }
    
    public Boolean isEventRunning() {
        return !(EventStatusType.END == status);
    }

    public void setStatus(EventStatusType status) {
        this.status = status;
    }

    public String getTrackingURL() {
        return trackingURL;
    }

    public void setTrackingURL(String trackingURL) {
        this.trackingURL = trackingURL;
    }

    public Date getEndTime() {
        return this.endTime == null ? null : new Date(this.endTime.getTime());
    }

    public List<BillingLogVO> getBillingLogVOList() {
        return billingLogVOList;
    }

    public void setBillingLogVOList(List<BillingLogVO> billingLogVOList) {
        this.billingLogVOList = billingLogVOList;
    }

    public int getTotalEventCreditUsed() {
        int totalCreditUsed = 0;
        if(this.billingLogVOList != null && billingLogVOList.size() > 0) {
            for(BillingLogVO billingLog : billingLogVOList) {
                totalCreditUsed += billingLog.getEventRegCreditUsed();
            }
        }
        return totalCreditUsed;
    }

    public String getEventTimeZone() {
        return eventTimeZone;
    }

    public void setEventTimeZone(String eventTimeZone) {
        this.eventTimeZone = eventTimeZone;
    }
    
    public int getTotalSMSCreditUsed() {
        int totalSMSCredit = 0;
        if(this.billingLogVOList != null && billingLogVOList.size() > 0) {
            for(BillingLogVO billingLog : billingLogVOList) {
                totalSMSCredit += billingLog.getSmsCreditUsed();
            }
        }
        return totalSMSCredit;
    }

    public Date getConfirmSafetyTime() {
        return confirmSafetyTime;
    }

    public void setConfirmSafetyTime(Date confirmSafetyTime) {
        this.confirmSafetyTime = confirmSafetyTime;
    }
    
    
    // TODO
    // Give a method to return total SMS Credit Used;

    public void setEndTime(Date newTime) {
        this.endTime = newTime == null ? null : new Date(newTime.getTime());
    }

    public String getTrackingPin() {
        return trackingPin;
    }

    public void setTrackingPin(String trackingPin) {
        this.trackingPin = trackingPin;
    }

    public Boolean getEnableGPS() {
        return enableGPS;
    }

    public void setEnableGPS(Boolean enableGPS) {
        this.enableGPS = enableGPS;
    }

    public Boolean getEnablePicture() {
        return enablePicture;
    }

    public void setEnablePicture(Boolean enablePicture) {
        this.enablePicture = enablePicture;
    }

    public Date getEmergencyTriggerAt() {
        return emergencyTriggerAt;
    }

    public void setEmergencyTriggerAt(Date emergencyTriggerAt) {
        this.emergencyTriggerAt = emergencyTriggerAt;
    }

    public Double getEmergencyLatitude() {
        return emergencyLatitude;
    }

    public void setEmergencyLatitude(Double emergencyLatitude) {
        this.emergencyLatitude = emergencyLatitude;
    }

    public Double getEmergencyLongitude() {
        return emergencyLongitude;
    }

    public void setEmergencyLongitude(Double emergencyLongitude) {
        this.emergencyLongitude = emergencyLongitude;
    }

    public Double getEmergencyAccuracy() {
        return emergencyAccuracy;
    }

    public void setEmergencyAccuracy(Double emergencyAccuracy) {
        this.emergencyAccuracy = emergencyAccuracy;
    }

    public JSONObject getJSON() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put(EVENT_ID_KEY, this.getId());
            jobj.put(EVENT_MSG_KEY, this.getMessage());
            jobj.put(EVENT_START_TIME_KEY, this.getStartTime());
            jobj.put(EVENT_END_TIME_KEY, this.getEndTime());
            jobj.put(EVENT_TYPE_KEY, this.getEventType());
            jobj.put(EVENT_STATUS_KEY, this.getStatus());
            jobj.put(EVENT_ENABLE_GPS_KEY, this.getEnableGPS());
            jobj.put(EVENT_TRACKING_PIN_KEY, this.getTrackingPin());
            jobj.put(EVENT_RUNNING_KEY, this.isEventRunning());
            jobj.put(EVENT_ENABLE_SAFETY_NOTIFY_KEY, this.getEnableSafetyNotify());
        } catch (JSONException ex) {
            myLog.error("Failed to create JSONObject from: " + this, ex);
        }
        return jobj;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SecqMeEventVO otherVO = (SecqMeEventVO) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(this.id, otherVO.id).isEquals();

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 15).append(this.id).toHashCode();
    }

    @Override
    public String toString() {
        StringBuffer userStrBuffer = new StringBuffer();
        userStrBuffer.append(new ToStringBuilder(this).append(this.id).append(this.userVO.getUserid()).append(this.startTime).append(this.endTime).append(this.eventType).append(this.status).append(this.getMessage()).toString());

        return userStrBuffer.toString();
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

	public Long getExtendedDuration() {
		return extendedDuration;
	}

	public void setExtendedDuration(Long extendedDuration) {
		this.extendedDuration = extendedDuration;
	}

	public boolean isEnableShare() {
		return enableShare;
	}

	public void setEnableShare(boolean enableShare) {
		this.enableShare = enableShare;
	}
}
