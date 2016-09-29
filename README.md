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

## Build and Run Project

This project using [Apache Ant](http://ant.apache.org/) (v1.9.4 and above) and [Apache Ivy](http://ant.apache.org/ivy/) (v2.4.0 and above). Please install the build tools.

Preparation before building first time:

```
mkdir -p ./lib
mkdir -p ./dist
ant init resolve
```

You also need to import the database into MySQL server.

To build and create `*.war` file:

```
ant clean war
```

The `*.war` file will be in `dist/mainportal.war`. You can deploy this file by putting into Tomcat's `webapps` directory.

## Initial Configuration
You will need to edit two files, `local.properties` and  `conf/SystemConfig.properties` first and add in your relevant settings and keys.

## Setup Build Environment

Each environment may have different setup (for example database connection and Elastic Beanstalk environment).

To allow multiple environment in this project, please edit `/local.properties`.
To create more environments, duplicate the file and rename it after your environment.

You can add new build environment by creating a new `/${build.env}.properties`.

To build, please run `ant` with an argument `-Dbuild.env={your-environment-name}`. If you don't provide the argument, Ant will use `local` environment.

E.g.:

    ant clean war                         ## local env
    ant clean war -Dbuild.env=production  ## production env

FYI: All `./conf/**/*.xml` files will be process in Ant Replace Token filter. So, any string within `@` and `@` will be replaced by corresponding properties in `./${build.env}.properties`.
