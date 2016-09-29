package com.secqme.util;

import com.secqme.util.schedular.ScheduleManager;
import com.secqme.util.spring.BeanType;
import com.secqme.util.spring.DefaultSpringUtil;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Web application lifecycle listener.
 * @author jameskhoo
 */
public class CoreContextListener implements ServletContextListener {

    private static Logger myLog = Logger.getLogger(CoreContextListener.class);
    private static ScheduleManager scheduleManager;

    public void contextInitialized(ServletContextEvent servletCtxEvent) {
        try {
            WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletCtxEvent.getServletContext());
            DefaultSpringUtil.getInstance().setApplicationContext(ctx);
            myLog.debug("Initializing all require resources..");
            myLog.debug("Initializing JPA Entity Factory");
            EntityManagerFactory emf = (EntityManagerFactory) DefaultSpringUtil.getInstance().getBean(BeanType.entityManagerFactory);
            emf.createEntityManager();

            myLog.debug("Init our Schedule Manager");
            scheduleManager = (ScheduleManager) DefaultSpringUtil.getInstance().getBean(BeanType.scheduleManager);
        } catch (Exception ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }

    public void contextDestroyed(ServletContextEvent arg0) {
        myLog.debug("Cleaning up all resources..");
        myLog.debug("Stopping all schedule job");
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                myLog.debug(String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
                myLog.error(String.format("Error deregistering driver %s", driver), e);
            }
        }
//        SchedulerFactoryBean factoryBean = (SchedulerFactoryBean) DefaultSpringUtil.getInstance().getBean(BeanType.scheduleFactory);
//        factoryBean.stop();
        
        myLog.debug("Closing JPA Entity Factory..");
        EntityManagerFactory emf = (EntityManagerFactory) DefaultSpringUtil.getInstance().getBean(BeanType.entityManagerFactory);
        emf.close();

        myLog.debug("secQ.Me JPA Entities Factory Closed");
        myLog.debug("Closing the scheduleManager");
        scheduleManager.shutdown();
        
    }
}