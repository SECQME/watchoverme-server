# WOM-Server

WOM-Server is a server web API for Watch Over Me tracking app.

Tech stack:

* Apache Tomcat 7.0
* Java EE 1.7
    * Eclipse Java Persistence 2.0
    * Spring 2.5
    * EH Cache 2.4 (In-memory database for caching)
    * Java WS RS 2.0
* MySQL 5.6

## High-level View

```
   ┌──────────┐                         ┌──────────┐         ┌────────────────┐
 ┌───────────┐│     ┌───────────┐     ┌───────────┐│       ┌─────────────────┐│
 │ Filters   │┘ --> │ Resource  │ --> │ Manager   │┘ --->  │ Model/Util/etc. │┘
 └───────────┘      └───────────┘     └───────────┘        └─────────────────┘

```

1. When a request come to the server, it will be process by *filter* ([`com.secqme.filter.*`](https://github.com/SECQME/WOM-Server/tree/master/src/com/secqme/filter)).
2. The request will be processed by *resource endpoint* ([`com.secqme.rs.*`](https://github.com/SECQME/WOM-Server/tree/master/src/com/secqme/rs)).
3. Each endpoint will call some related *managers*.
4. And managers will use DAO/Model/Util/etc. that related to its functionality.

The structure is basically follow [JAX RS 2.0](http://www.vogella.com/tutorials/REST/article.html).

If you want to know better the code, start read from resource classes first.

## Versioning

| Version                       | Notes                                                  |
| ----------------------------- | ------------------------------------------------------ |
| v1 (no version number in url) | Support version mobile app v5 and below (deprecated).  |
| v2                            | Support version mobile app v6.                         |
| v2.1                          | Support version mobile app v6. Supplement for `v2`.    |
| v3                            | Postponed. TODO: Proper OAuth authentication.          |

## Notification System (Email/SMS/SNS/Push Notification)

WOM-Server Notification Engine (`com.secqme.util.notification.DefaultNotificationEngine`) diagram:

```
                                               ┌───────────────┐
                                           +-> │ Twilio Svc.   │
                                           |   └───────────────┘
                       ┌───────────────┐   |   ┌───────────────┐
                   +-> │ SMS Manager   │ --+-> │ Plivo Svc.    │
                   |   └───────────────┘       └───────────────┘
                   |                       
                   |   ┌───────────────┐       ┌───────────────┐
                   +-> │ SNS Manager   │ ----> │ Facebook Svc. │    
                   |   └───────────────┘       └───────────────┘
                   |
┌──────────────┐   |   ┌───────────────┐
│ Notification │   |   │ Email Svc.    │
│ Engine       │ --+-> │ (SparkPost)   │
└──────────────┘   |   └───────────────┘
                   |   
                   |   ┌───────────────┐
                   +-> │ Push Svc.     │
                   |   │ (Parse)       │
                   |   └───────────────┘
                   |   
                   |   ┌───────────────┐
                   +-> │ Marketing     │
                       │ Email Svc.    │
                       │ (GetResponse) │
                       └───────────────┘
             
```

Manager in here will choose which service/provider they will be used to send the notification. Ideally, all service should be wrapped in manager, but currently only SMS and SNS have their manager.

All notification will be sent via **background worker**. The background worker implementation uses [java.util.concurrent.ExecutorService](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html) and [java.util.concurrent.FutureTask](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/FutureTask.html).


### Email

There are 2 type email:

* Transactional: Signup/Event/Contact email. Handled by [SparkPost](https://sparkpost.com).
* Marketing/subscriber
  * Emergency contact funnel: To ask em/c to be our user. Handled by [Get Response](http://getresponse.com).
  * Other marketing email: Now handled by [Wombat](https://github.com/SECQME/wombat). Implementation in Java (`com.secqme.util.schedular.job.MarketingEmailBlastByDeviceAndTimezoneJob`) using Quartz was *deprecated*.

### SMS

SMS providers:

* **[Twilio](https://www.twilio.com/)**
* **[Plivo](https://www.plivo.com/)**
* OneWay (deprecated)
* Routo (deprecated)

You can changed the SMS provider data in database (`smsgwCountry` table) and restart the app.

### Push Notification

For push notification, we use [Parse](http://parse.com) but it will **shutdown on January 28. 2017**.

Push notification will be sent to emergency contacts (also WOM users) if a user have emergency/shared events. Each users will listen on their channel/topics (currently we use `authToken` as channel name). In case of emergency/shared event triggered, server will find her emergency contacts and send the push notification message to emergency contact's channel.

**TODO**: **Replace** push notification service. We can use [Parse self hosted](http://parse.com/migration) or [AWS SNS](https://aws.amazon.com/sns/).

### SNS (Social Networking Service)

The only working SNS provider we have is [Facebook](https://facebook.com). It will post a new feed in case of shared/emergency event.

The user Facebook auth token is stored in `userSnsConfigs` table.

## Notes for some packages/classes for API v2 and v2.1

* `com.secqme`
  * `domains`
    * `converter`: Java Persistence data type converter
      * `JsonMapConverter`: Convert JSON String (database) <-> HashMap (model)
      * `JSONObjectConverter`: Convert JSON String (database) <-> JSONObject (model)
    * `dao`: Data Access Object. Querying database.
    * `factory`
      * `notification`: Build recipient model for sending notification/email/sms
    * `model`: Data model.
      * `ar`: (abbr. Auto Responder) Email/SMS/SNS templating.
  * `filters`
    * `RollbarFilter`: Log request and catch exception. Send the exception to Rollbar.
    * `AuthorizationFilter`: Log user token and mobile version.