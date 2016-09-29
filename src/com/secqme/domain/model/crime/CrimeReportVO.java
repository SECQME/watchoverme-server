package com.secqme.domain.model.crime;

import com.secqme.domain.model.UserVO;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * User: James Khoo
 * Date: 2/18/14
 * Time: 5:11 PM
 */
@Entity
@Table(name="crimeReport")
@NamedQueries({
        @NamedQuery(name=CrimeReportVO.QUERY_FIND_BY_USER_ID,
        query = "SELECT o " +
                "FROM CrimeReportVO o " +
                "WHERE o.userVO.userid = :userid"),

        @NamedQuery(name=CrimeReportVO.QUERY_FIND_BY_EVENT_ID,
        query = "SELECT o " +
                "FROM CrimeReportVO  o " +
                "WHERE o.eventId = :eventId")
})
public class CrimeReportVO implements Serializable {
    public static final String QUERY_FIND_BY_USER_ID = "CrimeReportVO.findByUserID";
    public static final String QUERY_FIND_BY_EVENT_ID = "CrimeReportVO.findByEventId";


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reportDate;

    private String timeRange; // 7AM-12PM, 12AM-4PM, 4AM-8PM
    private String timeZone;
    private Long eventId;
    @Enumerated(EnumType.STRING)
    private CrimeType crimeType;

    private String note;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private String crimePictureURL;
    private String crimeVideoURL;

    public CrimeReportVO() {
        // Empty Constructor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public CrimeType getCrimeType() {
        return crimeType;
    }

    public void setCrimeType(CrimeType crimeType) {
        this.crimeType = crimeType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public String getCrimePictureURL() {
        return crimePictureURL;
    }

    public void setCrimePictureURL(String crimePictureURL) {
        this.crimePictureURL = crimePictureURL;
    }

    public String getCrimeVideoURL() {
        return crimeVideoURL;
    }

    public void setCrimeVideoURL(String crimeVideoURL) {
        this.crimeVideoURL = crimeVideoURL;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CrimeReportVO that = (CrimeReportVO) o;

        if (accuracy != null ? !accuracy.equals(that.accuracy) : that.accuracy != null) return false;
        if (crimePictureURL != null ? !crimePictureURL.equals(that.crimePictureURL) : that.crimePictureURL != null)
            return false;
        if (crimeType != that.crimeType) return false;
        if (crimeVideoURL != null ? !crimeVideoURL.equals(that.crimeVideoURL) : that.crimeVideoURL != null)
            return false;
        if (!id.equals(that.id)) return false;
        if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null) return false;
        if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null) return false;
        if (note != null ? !note.equals(that.note) : that.note != null) return false;
        if (reportDate != null ? !reportDate.equals(that.reportDate) : that.reportDate != null) return false;
        if (!userVO.equals(that.userVO)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + userVO.hashCode();
        result = 31 * result + (reportDate != null ? reportDate.hashCode() : 0);
        result = 31 * result + (crimeType != null ? crimeType.hashCode() : 0);
        result = 31 * result + (note != null ? note.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (accuracy != null ? accuracy.hashCode() : 0);
        result = 31 * result + (crimePictureURL != null ? crimePictureURL.hashCode() : 0);
        result = 31 * result + (crimeVideoURL != null ? crimeVideoURL.hashCode() : 0);
        return result;
    }
}
