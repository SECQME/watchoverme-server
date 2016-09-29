package com.secqme.domain.model.referral;

import com.secqme.domain.model.UserVO;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * User: James Khoo
 * Date: 3/15/14
 * Time: 10:35 PM
 */
@Entity
@Table(name="referralLogs")
@NamedQueries({
        @NamedQuery(name = ReferralLogVO.QUERY_FIND_BY_HTML_PARAMETERS,
        query = "SELECT o " +
                "FROM ReferralLogVO o " +
                "WHERE o.clickType = :clickType " +
                "AND o.clickTime >= :startTime " +
                "AND o.clickTime <= :endTime " +
                "AND o.requestIP = :requestIP " +
                "ORDER BY o.clickTime asc "
        ),
        @NamedQuery(name = ReferralLogVO.QUERY_FIND_BY_REF_USER_ID,
        query = "SELECT o " +
                "FROM ReferralLogVO o " +
                "WHERE o.refUserVO.userid = :userid"
        )
})
public class ReferralLogVO implements Serializable {

    public static final String QUERY_FIND_BY_HTML_PARAMETERS = "referralLogVO.htmlParameters";
    public static final String QUERY_FIND_BY_REF_USER_ID = "referralLogVO.findByRefUserID";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String referralURL;
    @Temporal(TemporalType.TIMESTAMP)
    private Date clickTime;

    @Enumerated(EnumType.STRING)
    private ReferralClickType clickType;

    @ManyToOne
    @JoinColumn(name="refuserid")
    private UserVO refUserVO;

    private String requestIP;
    private String userAgent;
    private String acceptLanguage;
    private String sessionID;
    private String fingerPrint;

    public ReferralLogVO() {
        // Empty Constructor
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferralURL() {
        return referralURL;
    }

    public void setReferralURL(String referralURL) {
        this.referralURL = referralURL;
    }

    public Date getClickTime() {
        return clickTime;
    }

    public void setClickTime(Date clickTime) {
        this.clickTime = clickTime;
    }

    public ReferralClickType getClickType() {
        return clickType;
    }

    public void setClickType(ReferralClickType clickType) {
        this.clickType = clickType;
    }

    public String getRequestIP() {
        return requestIP;
    }

    public void setRequestIP(String requestIP) {
        this.requestIP = requestIP;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    public UserVO getRefUserVO() {
        return refUserVO;
    }

    public void setRefUserVO(UserVO refUserVO) {
        this.refUserVO = refUserVO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferralLogVO that = (ReferralLogVO) o;

        if (acceptLanguage != null ? !acceptLanguage.equals(that.acceptLanguage) : that.acceptLanguage != null)
            return false;
        if (!clickType.equals(that.clickType)) return false;
        if (fingerPrint != null ? !fingerPrint.equals(that.fingerPrint) : that.fingerPrint != null) return false;
        if (!id.equals(that.id)) return false;
        if (!referralURL.equals(that.referralURL)) return false;
        if (requestIP != null ? !requestIP.equals(that.requestIP) : that.requestIP != null) return false;
        if (sessionID != null ? !sessionID.equals(that.sessionID) : that.sessionID != null) return false;
        if (userAgent != null ? !userAgent.equals(that.userAgent) : that.userAgent != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + referralURL.hashCode();
        result = 31 * result + clickType.hashCode();
        result = 31 * result + (requestIP != null ? requestIP.hashCode() : 0);
        result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0);
        result = 31 * result + (acceptLanguage != null ? acceptLanguage.hashCode() : 0);
        result = 31 * result + (sessionID != null ? sessionID.hashCode() : 0);
        result = 31 * result + (fingerPrint != null ? fingerPrint.hashCode() : 0);
        return result;
    }
}
