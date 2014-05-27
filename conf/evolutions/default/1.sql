# --- !Ups
CREATE TABLE CHALLENGES(ID SERIAL PRIMARY KEY, NAME VARCHAR(1000) UNIQUE NOT NULL, CATEGORY VARCHAR(30) NOT NULL, CREATOR INTEGER NOT NULL, CREATION_DATE DATETIME NOT NULL, VIDEO_DESCRIPTION_URL VARCHAR(200), VISIBILITY BOOLEAN);

CREATE TABLE CHALLENGE_PARTICIPATIONS(ID SERIAL PRIMARY KEY, PARTICIPATOR INTEGER NOT NULL, CHALLENGE INTEGER NOT NULL);

CREATE TABLE CHALLENGE_RESPONSES(ID SERIAL PRIMARY KEY, CHALLENGE_PARTICIPATION INTEGER NOT NULL, ACCEPTANCE CHAR(1));

CREATE TABLE USERS(ID SERIAL PRIMARY KEY, USERNAME VARCHAR(200) UNIQUE NOT NULL, PROFILE_PICTURE_URL VARCHAR(300), JOINED DATETIME NOT NULL);

CREATE TABLE NOTIFICATIONS(ID SERIAL PRIMARY KEY, IS_READ CHAR(1), USER_ID INTEGER NOT NULL, CREATION_TIMESTAMP TIMESTAMP NOT NULL);

# --- !Downs
DROP TABLE CHALLENGES;

DROP TABLE CHALLENGE_PARTICIPATIONS;

DROP TABLE CHALLENGE_RESPONSES;

DROP TABLE NOTIFICATIONS;

DROP TABLE USERS;