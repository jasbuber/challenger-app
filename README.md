# Description

This is a web application that allows people to create video challenges that others can join and try to complete. A person, creating a challenge,
needs to upload a video description so that other people understand what is it all about. After completing a challenge, a video response can be uploaded
by a participant, showing that a challenge was completed. Challenge creator can accept or reject such response, granting points or deleting response if it failed.

Since this application is tightly coupled with facebook, challenges may have different visibilities, being public or strictly for a chosen group of friends. Videos are being
uploaded to users facebook accounts, using facebook api.

## Prerequisites

[sbt](http://www.scala-sbt.org/download.html)

### Getting Started

Start the app on localhost:9000.

```
sbt run
```

In order for the app to work, facebook application needs to be created and properly configured. Application url has to be provided
in Application.java

```
FacebookClient.AccessToken token = FacebookService.generateAccessToken(code, "facebook_app_url");
```

Application will use in-memory database. For setting a different database, please read the play tutorial.

https://www.playframework.com/documentation/2.2.x/ScalaDatabase
