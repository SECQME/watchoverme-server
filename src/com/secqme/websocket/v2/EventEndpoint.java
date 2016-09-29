/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.secqme.websocket.v2;

import com.secqme.domain.model.event.FullEventInfoVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.rs.BaseResource;
import org.apache.log4j.Logger;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/ws/v2/events/{trackingPin}")
public class EventEndpoint extends BaseResource {

    private static final Logger myLog = Logger.getLogger(EventEndpoint.class);

    @OnMessage
    public void requestEventTracking(@PathParam("trackingPin") String trackingPin, String message, Session session) {
        myLog.debug("requestEventTracking: " + trackingPin);
        try {
            if (session.isOpen()) {
                SecqMeEventVO eventVO = eventManager.getEventByTrackingPin(trackingPin);
                FullEventInfoVO eventInfoVO = eventManager.getFullEventInfoOfContact(eventVO.getId());
                session.getBasicRemote().sendText(eventInfoVO.toJSON().toString());
            }
        } catch (IOException ex) {
            myLog.error("Tracking event web socket error: " + trackingPin, ex);
            try {
                session.close();
            } catch (IOException ex1) {
                // Ignore
            }
        }
    }
}
