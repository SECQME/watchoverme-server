package com.secqme.domain.model;

/**
 *
 * @author james
 */
public enum ContactInvitationStatus {
    /***
     * The contact was invited to watch over a user, but they haven't accepted/rejected yet.
     */
    INVITED,

    /***
     * The contact accepted the invitation.
     */
    ACCEPTED,

    /***
     *  The contact rejected the invitation.
     */
    REJECTED,

    /***
     * The contact is the user herself.
     */
	SELFADDED;
}
