package services;

import com.restfb.*;
import com.restfb.batch.BatchHeader;
import com.restfb.batch.BatchRequest;
import com.restfb.batch.BatchResponse;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;
import com.restfb.types.Message;
import com.restfb.types.User;
import com.restfb.types.Video;
import domain.ChallengeResponse;
import domain.FacebookUser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Eventually to be merged with FacebookNotificationService
 */
public class FacebookService {

    private final static String secret = "a8d1db17c5add29872d79dd35bf793dc";

    private final static String applicationId = "471463259622297";

    private final static String appUrl = "https://apps.facebook.com/471463259622297";

    private final DefaultFacebookClient client;

    public FacebookService(String token){

        this.client = new DefaultFacebookClient(token, this.secret, Version.VERSION_2_1);

    }

    public DefaultFacebookClient getClient() {
        return client;
    }

    public static FacebookClient.AccessToken generateAccessToken(String code, String redirectUrl){
        try {
            WebRequestor wr = new DefaultWebRequestor();
            WebRequestor.Response accessTokenResponse = wr.executeGet(
                "https://graph.facebook.com/oauth/access_token?client_id=" + FacebookService.applicationId + "&redirect_uri=" + redirectUrl
                        + "&client_secret=" + FacebookService.secret + "&code=" + code);
            return DefaultFacebookClient.AccessToken.fromQueryString(accessTokenResponse.getBody());
        } catch (IOException e) {
        }

        return null;
    }

    public FacebookUser getFacebookUser() {
        return this.client.fetchObject("me", FacebookUser.class, Parameter.with("fields", "id, first_name, last_name, name"));
    }

    public String getProfilePictureUrl(){
        JsonObject photo = this.client.fetchObject("me/picture", JsonObject.class, Parameter.with("redirect","0"), Parameter.with("width","150"), Parameter.with("height","150"));
        return photo.getJsonObject("data").getString("url");
    }

    public String publishAVideo(String challengeName, InputStream videoPath,String  fileName){
            FacebookType video = this.client.publish("me/videos", FacebookType.class,
                    BinaryAttachment.with(fileName, videoPath),
                    Parameter.with("message", challengeName),
                    Parameter.with("privacy", "{'value': 'EVERYONE'}"));

        return video.getId();
    }

    public String publishAPrivateVideo(String challengeName, InputStream videoPath, String fileName){

        FacebookType video = this.client.publish("me/videos", FacebookType.class,
                BinaryAttachment.with(fileName, videoPath),
                Parameter.with("message", challengeName),
                Parameter.with("privacy", "{'value': 'ALL_FRIENDS'}"));

        return video.getId();
    }

    public Video getVideo(String videoId){

        Video video = this.client.fetchObject(videoId, Video.class, Parameter.with("fields", "source, picture"));

        return video;
    }

    public List<ChallengeResponse> getThumbnailsForResponses(List<ChallengeResponse> responses){

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

    public List<FacebookUser> getFacebookUsers(List<String> userIds){

        List<BatchRequest> requests = new ArrayList<BatchRequest>();

        JsonMapper jsonMapper = new DefaultJsonMapper();

        for ( String friendId : userIds) {
            BatchRequest request = new BatchRequest.BatchRequestBuilder(friendId).body(Parameter.with("fields", "id, name")).build();
            requests.add(request);

            BatchRequest pictureRequest = new BatchRequest.BatchRequestBuilder(friendId + "/picture?width=150&height=150").body().build();
            requests.add(pictureRequest);
        }

        List<BatchResponse> batchResponses = this.client.executeBatch(requests);

        List<FacebookUser> users = new ArrayList<FacebookUser>(batchResponses.size());

        for (int i = 0; i < batchResponses.size(); i +=2) {

            if(batchResponses.get(i).getBody() != null) {

                FacebookUser user = jsonMapper.toJavaObject(batchResponses.get(i).getBody(), FacebookUser.class);
                user.setPicture(batchResponses.get(i + 1).getHeaders().get(3).getValue());
                users.add(user);
            }
        }

        return users;

    }

}
