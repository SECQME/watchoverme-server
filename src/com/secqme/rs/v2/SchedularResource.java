package com.secqme.rs.v2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.secqme.rs.BaseResource;

@Path("/v2/schedular")
public class SchedularResource extends BaseResource {

    private final static Logger myLog = Logger.getLogger(SchedularResource.class);

    public SchedularResource() {
    }


    //to run schedular manually
    @GET
    @Path("/{appid}/{appsecret}/{schdule}")
    @Produces(MediaType.APPLICATION_JSON)
    public String runSchedulerManually(@PathParam("appid") String applicationId,
                                       @PathParam("appsecret") String applicationSecret,
                                       @PathParam("schedule") int schedule) {
        String result = "";

        sysAdminUtil.grantClientAccess(applicationId, applicationSecret);
        switch(schedule) {
        case 0:
            userSubscriptionInfoManager.processSubscriptionInfo();
            break;
        }

        return result;
    }


}
