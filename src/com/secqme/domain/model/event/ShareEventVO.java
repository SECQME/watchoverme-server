package com.secqme.domain.model.event;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.secqme.domain.model.UserVO;

@Entity
@Table(name="shareEvent")
@NamedQueries({
        @NamedQuery(name = ShareEventVO.QUERY_FIND_BY_SHARE_TO_USER,
                query = "SELECT o "
                        + "FROM ShareEventVO o "
                        + "WHERE o.id = (SELECT MAX(t.id) FROM ShareEventVO t "
                        + "WHERE t.shareToUserVO.userid = :userid "
                        + "AND t.secqMeEventVO.id = :eventid)"),
        @NamedQuery(name = ShareEventVO.QUERY_FIND_BY_EVENT_ID,
	        query = "SELECT o "
	                + "FROM ShareEventVO o "
	                + "WHERE o.secqMeEventVO.id = :eventid "
	                + "ORDER BY o.shareToUserVO "),
        @NamedQuery(name = ShareEventVO.QUERY_FIND_BY_SHARE_TIME_AND_SHARE_TO_USER,
        query = "SELECT o "
                + "FROM ShareEventVO o "
                + "WHERE o.shareTime >= :sharetime "
                + "AND o.shareToUserVO.userid = :userid")
})
public class ShareEventVO implements Serializable {

    public static final String QUERY_FIND_BY_SHARE_TO_USER = "ShareEventVO.findByShareToUser";
    public static final String QUERY_FIND_BY_EVENT_ID = "ShareEventVO.findByEventId";
    public static final String QUERY_FIND_BY_SHARE_TIME_AND_SHARE_TO_USER = "ShareEventVO.findByShareTimeAndShareToUser";
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    @ManyToOne
    @JoinColumn(name = "eventid")
    private SecqMeEventVO secqMeEventVO;
    @ManyToOne
    @JoinColumn(name = "shareToUser")
    private UserVO shareToUserVO;
    @Temporal(TemporalType.TIMESTAMP)
    private Date shareTime = null;
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
	public SecqMeEventVO getSecqMeEventVO() {
		return secqMeEventVO;
	}
	public void setSecqMeEventVO(SecqMeEventVO secqMeEventVO) {
		this.secqMeEventVO = secqMeEventVO;
	}
	public UserVO getShareToUserVO() {
		return shareToUserVO;
	}
	public void setShareToUserVO(UserVO shareToUserVO) {
		this.shareToUserVO = shareToUserVO;
	}
	public Date getShareTime() {
		return shareTime;
	}
	public void setShareTime(Date shareTime) {
		this.shareTime = shareTime;
	}
    
}
