package com.secqme.domain.model;

/**
 *
 * @author james
 */
public enum EventStatusType {
    NEW,        // Receive 
    CONFIRM,    // Send to Owner to ask for confirmation
    EXTENDED,	// khlow20120605 Event extended by owner
    EMERGENCY,  // Watach over me event, user  trigger emergency
    EMERGENCY_VIDEO, // Merging Event with Video
    EMERGENCY_AUDIO, // Merging Event with Audio
    END,        // Event ended by owner
    REJECTED,   // System can't process the Event, due to insuffienct funds
    NOTIFY,     // Event send to owner emergency contact for notification
    JOURNEY,    //
    SAFE_NOTIFY; //

}
