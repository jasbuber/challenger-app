package services;

import domain.Challenge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class VideoUploadingStrategy {


    private final FacebookService facebookService;

    private final List<String> participants;

    public VideoUploadingStrategy(FacebookService facebookService, List<String> participants) {

        this.facebookService = facebookService;

        this.participants = participants;

    }

    public String uploadVideo(Challenge challenge, String filename, File resourceFile) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(resourceFile);
            if (isChallengePrivate(challenge)) {
                return facebookService.publishAPrivateVideo(challenge.getChallengeName(), stream, filename, participants);
            } else {
                return facebookService.publishAVideo(challenge.getChallengeName(), stream, filename);
            }
        } catch (IOException e) {
            throw new UploadVideoFileException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new UploadVideoFileException(e);
                }
            }
        }
    }

    private boolean isChallengePrivate(Challenge challenge) {
            return !challenge.getVisibility();
        }
}
