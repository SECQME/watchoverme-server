package com.secqme.domain.model.notification.email;

/**
 * Created by edward on 08/05/2015.
 */
public enum EmailStatus {
    INVALID,
    REJECTED,
    HARD_BOUNCED,
    SOFT_BOUNCED,
    MARKED_AS_SPAM,
    UNSUBSCRIBED,
    DEFERRED,
    QUEUED,
    SENT,
    OPENED,
    CLICKED
}