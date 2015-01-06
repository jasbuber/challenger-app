package services;

import domain.Challenge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VideoUploadingStrategy {


    private final FacebookService facebookService;

    public VideoUploadingStrategy(FacebookService facebookService) {
        this.facebookService = facebookService;
    }

    public String uploadVideo(Challenge challenge, String filename, File resourceFile) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(resourceFile);
            if (isChallengePrivate(challenge)) {
                return facebookService.publishAPrivateVideo(challenge.getChallengeName(), stream, filename);
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
