package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.User;
import repositories.ChallengesRepository;

public class ChallengeService {

    private final ChallengesRepository challengesRepository;
    private final User user;

    public ChallengeService(ChallengesRepository challengesRepository, User user) {
        this.challengesRepository = challengesRepository;
        this.user = user;
    }

    public Challenge createChallenge() {
        return challengesRepository.createChallenge(user);
    }

    public ChallengeParticipation participateInChallenge(Challenge challenge) {
        return new ChallengeParticipation(challenge, user);
    }
}
