# --- !Ups
CREATE TABLE CHALLENGES(ID SERIAL PRIMARY KEY, NAME VARCHAR(1000) NOT NULL, CATEGORY VARCHAR(30) NOT NULL, CREATOR INTEGER NOT NULL, CREATION_DATE DATETIME NOT NULL, VIDEO_ID VARCHAR(200), VISIBILITY BOOLEAN, ACTIVE BOOLEAN, ENDING_DATE DATETIME, DIFFICULTY TINYINT, CONSTRAINT NAME_CREATOR_UNIQUE UNIQUE(NAME, CREATOR), RATING FLOAT, PARTICIPATORS_RATED INTEGER);

CREATE TABLE CHALLENGE_PARTICIPATIONS(ID SERIAL PRIMARY KEY, PARTICIPATOR INTEGER NOT NULL, CHALLENGE INTEGER NOT NULL, JOINED DATETIME NOT NULL, ENDING_DATE DATETIME, IS_CHALLENGE_RATED CHAR(1), IS_RESPONSE_SUBMITTED CHAR(1));

CREATE TABLE CHALLENGE_RESPONSES(ID SERIAL PRIMARY KEY, CHALLENGE_PARTICIPATION INTEGER NOT NULL, ACCEPTANCE CHAR(1), VIDEO_RESPONSE_URL VARCHAR(200), MESSAGE VARCHAR(300), SUBMITTED DATETIME NOT NULL);

CREATE TABLE USERS(ID SERIAL PRIMARY KEY, USERNAME VARCHAR(200) UNIQUE NOT NULL, PROFILE_PICTURE_URL VARCHAR(300), JOINED DATETIME NOT NULL, FIRST_NAME VARCHAR(60), LAST_NAME VARCHAR(80), FULL_NAME VARCHAR(200), CREATION_POINTS INTEGER DEFAULT 0, PARTICIPATION_POINTS INTEGER DEFAULT 0, OTHER_POINTS INTEGER DEFAULT 0);

CREATE TABLE NOTIFICATIONS(ID SERIAL PRIMARY KEY, IS_READ CHAR(1) NOT NULL, USER_ID INTEGER NOT NULL, CREATION_TIMESTAMP TIMESTAMP NOT NULL, MESSAGE VARCHAR(250), SHORT_MESSAGE VARCHAR(40), TYPE VARCHAR(25), RELEVANT_OBJECT_ID VARCHAR (100));

CREATE TABLE COMMENTS(ID SERIAL PRIMARY KEY, AUTHOR_ID INTEGER NOT NULL, CREATION_TIMESTAMP TIMESTAMP NOT NULL, MESSAGE VARCHAR(250), RELEVANT_OBJECT_ID INTEGER NOT NULL);


# --- !Downs
DROP TABLE CHALLENGES;

DROP TABLE CHALLENGE_PARTICIPATIONS;

DROP TABLE CHALLENGE_RESPONSES;

DROP TABLE NOTIFICATIONS;

DROP TABLE USERS;

DROP TABLE COMMENTS;