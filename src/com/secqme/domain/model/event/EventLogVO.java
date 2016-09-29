
package com.secqme.domain.model.event;

import com.secqme.domain.model.EventStatusType;
import com.secqme.util.MediaFileType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author james
 */
@Entity
@Table(name="eventLog")
@NamedQueries({
        @NamedQuery(name = EventLogVO.QUERY_FIND_BY_EVENTID,
                query = "SELECT o "
                        + "FROM EventLogVO o "
                        + "WHERE o.secqMeEventVO.id = :eventId "),
        @NamedQuery(name = EventLogVO.QUERY_FIND_BY_EVENTID_AND_MEDIA_TYPE,
                query = "SELECT o "
                        + "FROM EventLogVO o "
                        + "WHERE o.secqMeEventVO.id = :eventId "
                        + "AND o.mediaType = :mediaType")
})
public class EventLogVO implements Serializable {

    public static final String QUERY_FIND_BY_EVENTID = "EventLogVO.findByEventId";
    public static final String QUERY_FIND_BY_EVENTID_AND_MEDIA_TYPE = "EventLogVO.findByEventIDMediaType";

    public static final String EVENT_STATUS_KEY = "eventStatus";
    public static final String EVENT_LOG_TIME_KEY = "eventLogTime";
    public static final String EVENT_LOG_MESSAGE_KEY = "eventLogMessage";
    public static final String EVENT_MEDIA_FILE_URL = "eventMediaFileURL";
    public static final String EVENT_MEDIA_TYPE = "eventMediaType";
    public static final String EVENT_LOG_LATITUDE_KEY = "latitude";
    public static final String EVENT_LOG_LONGITUDE_KEY = "longitude";
    public static final String EVENT_LOG_ACCURACY_KEY = "accuracy";


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "secqMeEventId")
    private SecqMeEventVO secqMeEventVO;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;

    @Enumerated(EnumType.STRING)
    private EventStatusType status;

    private String logMessage;
    private String mediaFileURL;

    @Enumerated(EnumType.STRING)
    private MediaFileType mediaType;

    private Double latitude;
    private Double longitude;
    private Double accuracy;
    

    public EventLogVO() {
        // Empty Constructor
    }

    public Date getEventTime() {
        return eventTime == null ? null : new Date(eventTime.getTime());
    }

    public void setEventTime(Date evtTime) {
        this.eventTime = evtTime == null ? null : new Date(evtTime.getTime());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public SecqMeEventVO getSecqMeEventVO() {
        return secqMeEventVO;
    }

    public void setSecqMeEventVO(SecqMeEventVO secqMeEventVO) {
        this.secqMeEventVO = secqMeEventVO;
    }

    public String getMediaFileURL() {
        return mediaFileURL;
    }

    public void setMediaFileURL(String mediaFileURL) {
        this.mediaFileURL = mediaFileURL;
    }

    public MediaFileType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaFileType mediaType) {
        this.mediaType = mediaType;
    }

    public EventStatusType getStatus() {
        return status;
    }

    public void setStatus(EventStatusType status) {
        this.status = status;
    }


    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jobj = new JSONObject();
        jobj.put(EVENT_STATUS_KEY, this.getStatus())
                .put(EVENT_LOG_TIME_KEY, this.getEventTime().getTime());

        if(this.getLogMessage() != null) {
            jobj.put(EVENT_LOG_MESSAGE_KEY, this.getLogMessage());
        }

        if(this.getMediaFileURL() != null) {
            jobj.put(EVENT_MEDIA_FILE_URL, this.getMediaFileURL());
        }

        if(this.getMediaType() != null) {
            jobj.put(EVENT_MEDIA_TYPE, this.getMediaType().toString());
        }

        if(this.getLatitude() != null) {
            jobj.put(EVENT_LOG_LATITUDE_KEY, this.getLatitude())
                    .put(EVENT_LOG_LONGITUDE_KEY, this.getLongitude())
                    .put(EVENT_LOG_ACCURACY_KEY, this.getAccuracy());
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
        final EventLogVO otherVO = (EventLogVO) obj;
        return new EqualsBuilder()
                .append(this.id, otherVO.id)
                .append(this.getEventTime(), otherVO.getEventTime())
                .append(this.getStatus(), otherVO.getStatus())
                .append((this.secqMeEventVO), otherVO.secqMeEventVO)
                .isEquals();
    }


    @Override
    public int hashCode() {
        return new HashCodeBuilder(7,17)
                .append(this.id)
                .append(this.secqMeEventVO)
                .append(this.eventTime)
                .append(this.logMessage)
                .append(this.getStatus())
                .toHashCode();
    }



    


}
