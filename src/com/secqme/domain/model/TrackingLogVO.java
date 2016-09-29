package com.secqme.domain.model;

import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.rs.v2.CommonJSONKey;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author jameskhoo
 */
@Entity
@Table(name = "trackingLog")
@NamedQueries({
    @NamedQuery(name = TrackingLogVO.QUERY_FIND_BY_SECQME_ID,
    query = "SELECT o "
    + "FROM TrackingLogVO o "
    + "WHERE o.secqMeEventVO.id = :secqMeid "
    + "ORDER BY o.id DESC")
})
public class TrackingLogVO implements Serializable {

    public static final String QUERY_FIND_BY_SECQME_ID = "trackingLogVO.findByTrackingId";
    public static final String EMERGENCY_FLAG = "emergency";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "secqMeEventId")
    private SecqMeEventVO secqMeEventVO;
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeReport;
    private Double latitude;
    @Column(name="longtitude")
    private Double longitude;
    private String geocode;
    private String otherInfo;
    private Double accuracy; // Accuracy in Meter;

    public TrackingLogVO() {
        this.timeReport = new Date();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLocString() {
        return "" + this.latitude + ", " + this.longitude;
    }

    public SecqMeEventVO getSecqMeEventVO() {
        return secqMeEventVO;
    }

    public void setSecqMeEventVO(SecqMeEventVO secqMeEventVO) {
        this.secqMeEventVO = secqMeEventVO;
    }

    public Date getTimeReport() {
        return timeReport == null ? null : new Date(timeReport.getTime());
    }

    public void setTimeReport(Date reportTime) {
        this.timeReport = reportTime == null ? null : new Date(reportTime.getTime());
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }


    public String getGeocode() {
        return geocode;
    }

    public void setGeocode(String geocode) {
        this.geocode = geocode;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherinfo) {
        this.otherInfo = otherinfo;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jobj = new JSONObject();
        jobj.put(CommonJSONKey.LATITUDE_KEY, this.getLatitude())
                .put(CommonJSONKey.LONGITUDE_KEY, this.getLongitude())
                .put(CommonJSONKey.TIME_STAMP_KEY, this.getTimeReport().getTime())
                .put(CommonJSONKey.LOCATION_ACCURACY_KEY, this.getAccuracy());
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
        final TrackingLogVO other = (TrackingLogVO) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if (this.secqMeEventVO != other.secqMeEventVO && (this.secqMeEventVO == null || !this.secqMeEventVO.equals(other.secqMeEventVO))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 71 * hash + (this.secqMeEventVO != null ? this.secqMeEventVO.hashCode() : 0);
        return hash;
    }
}
