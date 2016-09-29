package com.secqme.domain.model.event;

import com.secqme.domain.model.UserVO;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "savedEvents")
@NamedQueries({
    @NamedQuery(name = SavedEventVO.QUERY_FIND_EVENTS_BY_USERID,
    query = "SELECT o "
    + "FROM SavedEventVO o "
    + "WHERE o.userVO.userid = :userid")

})
public class SavedEventVO implements Serializable {
	public static final String QUERY_FIND_EVENTS_BY_USERID = "savedEvents.findEventsByUserid";
	public static final String EVENT_ID_KEY = "id";
	public static final String EVENT_NAME_KEY = "eventName";
	public static final String EVENT_USERID_KEY = "userid";
	public static final String EVENT_DURATION_KEY = "eventDuration";
	public static final String EVENT_DEFAULT_KEY = "defaultEvent";
	public static final String EVENT_DESCRIPTION_KEY = "optionalDescription";

    private static final Logger myLog = Logger.getLogger(SavedEventVO.class);

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "userid")
    private UserVO userVO;
    private String eventName;
    private int eventDuration;
    private int defaultEvent;
    private String optionalDescription;
    
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
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public int getEventDuration() {
		return eventDuration;
	}
	public void setEventDuration(int eventDuration) {
		this.eventDuration = eventDuration;
	}
	public int getDefaultEvent() {
		return defaultEvent;
	}
	public void setDefaultEvent(int defaultEvent) {
		this.defaultEvent = defaultEvent;
	}
	public String getOptionalDescription() {
		return optionalDescription;
	}
	public void setOptionalDescription(String optionalDescription) {
		this.optionalDescription = optionalDescription;
	}
	
	public JSONObject getJSONObject() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put(EVENT_ID_KEY, this.getId());
            jobj.put(EVENT_NAME_KEY, this.getEventName());
            jobj.put(EVENT_USERID_KEY, this.getUserVO().getUserid());
            jobj.put(EVENT_DURATION_KEY, this.getEventDuration());
            jobj.put(EVENT_DEFAULT_KEY, this.getDefaultEvent());
            jobj.put(EVENT_DESCRIPTION_KEY, this.getOptionalDescription());
        } catch (JSONException ex) {
            myLog.error("Failed to create JSONObject from: " + this, ex);
        }
        return jobj;
    }
}
