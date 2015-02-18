package services;

import com.restfb.*;
import com.restfb.batch.BatchRequest;
import com.restfb.batch.BatchResponse;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;
import com.restfb.types.Video;
import domain.ChallengeResponse;
import domain.FacebookUser;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Eventually to be merged with FacebookNotificationService
 */
public class FacebookService {

    private static Logger logger = LoggerFactory.getLogger(FacebookService.class);

    private final DefaultFacebookClient client;

    public FacebookService(String token, String secret) {
        this.client = new DefaultFacebookClient(token, secret, Version.VERSION_2_1);
    }

    public DefaultFacebookClient getClient() {
        return client;
    }

    public static String generateAccessToken(String code, String applicationId, String secret, String redirectUrl) {
        logger.debug("Generating access token for code {}", code);
        try {
            WebRequestor wr = new DefaultWebRequestor();
            WebRequestor.Response accessTokenResponse = wr.executeGet(
                    "https://graph.facebook.com/oauth/access_token?client_id=" + applicationId + "&redirect_uri=" + redirectUrl
                            + "&client_secret=" + secret + "&code=" + code);
            return DefaultFacebookClient.AccessToken.fromQueryString(accessTokenResponse.getBody()).getAccessToken();
        } catch (IOException e) {
            logger.error("Exception has been thrown during generating access token", e);
        }

        return null;
    }

    public FacebookUser getFacebookUser() {
        return this.client.fetchObject("me", FacebookUser.class, Parameter.with("fields", "id, first_name, last_name, name"));
    }

    public String getProfilePictureUrl() {
        JsonObject photo = this.client.fetchObject("me/picture", JsonObject.class, Parameter.with("redirect", "0"), Parameter.with("width", "150"), Parameter.with("height", "150"));
        return photo.getJsonObject("data").getString("url");
    }

    public String publishAVideo(String challengeName, InputStream videoPath, String fileName) {
        logger.info("Publishing video to fb with name {} for challenge {}", fileName, challengeName);

        return publishVideoToFb(challengeName, videoPath, fileName, "{'value': 'EVERYONE'}");
    }

    public String publishAPrivateVideo(String challengeName, InputStream videoPath, String fileName) {
        logger.info("Publishing video to fb with name {} for challenge {}", fileName, challengeName);
        
        return publishVideoToFb(challengeName, videoPath, fileName, "{'value': 'ALL_FRIENDS'}");
    }

    private String publishVideoToFb(String challengeName, InputStream videoPath, String fileName, String privacyParam) {
        StopWatch videoPublishingTime = new StopWatch();
        videoPublishingTime.start();

        FacebookType publishedVideo = this.client.publish("me/videos", FacebookType.class,
                BinaryAttachment.with(fileName, videoPath),
                Parameter.with("message", challengeName),
                Parameter.with("privacy", privacyParam));
        
        videoPublishingTime.stop();
        
        String videoId = publishedVideo.getId();
        
        logger.info("Video with name {} for challenge {} has been published to fb with id {} in time of {} ms",
                fileName, challengeName, videoId, videoPublishingTime.getTime());

        return videoId;
    }

    public Video getVideo(String videoId) {
        logger.debug("Getting video {} from fb", videoId);
        Video video = this.client.fetchObject(videoId, Video.class, Parameter.with("fields", "source, picture"));

        return video;
    }

    public List<ChallengeResponse> getThumbnailsForResponses(List<ChallengeResponse> responses) {
        logger.trace("Getting thumbnails for responses");
        List<BatchRequest> requests = new ArrayList<BatchRequest>();

        JsonMapper jsonMapper = new DefaultJsonMapper();

        for (Iterator<ChallengeResponse> i = responses.iterator(); i.hasNext(); ) {
            BatchRequest request = new BatchRequest.BatchRequestBuilder(i.next().getVideoResponseUrl()).body(Parameter.with("fields", "picture")).build();
            requests.add(request);

        }
        List<BatchResponse> batchResponses = this.client.executeBatch(requests);

        for (int i = 0; i < responses.size(); i++) {

            Video video = jsonMapper.toJavaObject(batchResponses.get(i).getBody(), Video.class);
            responses.get(i).setThumbnailUrl(video.getPicture());
        }

        return responses;

    }

    public List<FacebookUser> getFacebookUsers(List<String> userIds) {
        logger.trace("Getting facebook users for given ids");
        
        List<BatchRequest> requests = new ArrayList<BatchRequest>();

        JsonMapper jsonMapper = new DefaultJsonMapper();

        for (String friendId : userIds) {
            BatchRequest request = new BatchRequest.BatchRequestBuilder(friendId).body(Parameter.with("fields", "id, name")).build();
            requests.add(request);

            BatchRequest pictureRequest = new BatchRequest.BatchRequestBuilder(friendId + "/picture?width=150&height=150").body().build();
            requests.add(pictureRequest);
        }

        List<BatchResponse> batchResponses = this.client.executeBatch(requests);

        List<FacebookUser> users = new ArrayList<FacebookUser>(batchResponses.size());

        for (int i = 0; i < batchResponses.size(); i += 2) {

            if (batchResponses.get(i).getBody() != null) {

                FacebookUser user = jsonMapper.toJavaObject(batchResponses.get(i).getBody(), FacebookUser.class);
                user.setPicture(batchResponses.get(i + 1).getHeaders().get(3).getValue());
                users.add(user);
            }
        }

        return users;

    }

}
