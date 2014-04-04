package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.User;
import repositories.ChallengesRepository;
import repositories.UserRepository;

public class ChallengeService {

    private final ChallengesRepository challengesRepository;

    public ChallengeService(ChallengesRepository challengesRepository) {
        this.challengesRepository = challengesRepository;
    }

    public Challenge createChallenge(User user) {
        return challengesRepository.createChallenge(user);
    }

    public ChallengeParticipation participateInChallenge(Challenge challenge, User user) {
        return new ChallengeParticipation(challenge, user);
    }
}
