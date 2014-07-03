package services;

import domain.Challenge;
import domain.ChallengeCategory;
import domain.ChallengeParticipation;
import domain.User;
import org.junit.Test;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import java.util.*;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ChallengeParticipationTest {

    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final UsersRepository usersRepository = new UserRepositoryStub();
    private final ChallengeNotificationsService challengeNotificationService = mock(ChallengeNotificationsService.class);

    private final static String SOME_VIDEO_ID = "videoId";
    private final Boolean VISIBILITY_PRIVATE = false;

    private final static ChallengeCategory SOME_CATEGORY = ChallengeCategory.ALL;

    private final ChallengeService challengeService = createChallengeService();

    private final String challengeName = "challengeName";

    private ChallengeServiceWithoutTransactionMgmt createChallengeService() {
            return new ChallengeServiceWithoutTransactionMgmt(challengesRepository, usersRepository, challengeNotificationService);
        }

    @Test
    public void shouldUserParticipationBeTrueIfUserIsAlreadyParticipatingInChallenge() throws Exception {
        //given
        String user = "username";
        Challenge challenge = createChallenge(user);

        //when
        challengeService.participateInChallenge(challenge, user);
        boolean userParticipatingInChallenge = challengeService.isUserParticipatingInChallenge(challenge, user);

        //
        assertTrue(userParticipatingInChallenge);
    }

    @Test
    public void shouldUserParticipationBeFalseIfUserIsNotParticipatingInChallengeYet() throws Exception {
        //given
        String user = "username";
        Challenge challenge = createChallenge(user);

        //when
        boolean userParticipatingInChallenge = challengeService.isUserParticipatingInChallenge(challenge, user);

        //
        assertFalse(userParticipatingInChallenge);
    }

    @Test
    public void shouldCreateChallengeParticipationForUserAndChallenge() throws Exception {
        //given
        String user = "username";
        Challenge challenge = createChallenge(user);

        //when
        ChallengeParticipation challengeParticipation =
                challengeService.participateInChallenge(challenge, user);

        //then
        assertTrue(challengeParticipation != null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenTryingToParticipateAgainInSameChallenge() throws Exception {
        //given
        String user = "username";
        Challenge challenge = createChallenge(user);

        //when
        challengeService.participateInChallenge(challenge, user);
        challengeService.participateInChallenge(challenge, user);

        //then throw exception
    }

    private Challenge createChallenge(String user) {
        return challengeService.createChallenge(user, challengeName, SOME_CATEGORY, SOME_VIDEO_ID, VISIBILITY_PRIVATE);
    }


    private static class UserRepositoryStub extends UsersRepository {

        @Override
        public User getUser(String username) {
            return new User(username);
        }
    }

    private static class ChallengesRepositoryStub extends ChallengesRepository {

        private Set<ChallengeNameAndCreatorTuple> challengesWithCreators = new HashSet<ChallengeNameAndCreatorTuple>();
        private Map<Challenge, Set<String>> challengeParticipators = new HashMap<Challenge, Set<String>>();

        @Override
        public Challenge createChallenge(Challenge challenge) {
            challengesWithCreators.add(new ChallengeNameAndCreatorTuple(challenge.getChallengeName(), challenge.getCreator().getUsername()));
            return challenge;
        }

        @Override
        public ChallengeParticipation persistChallengeParticipation(ChallengeParticipation challengeParticipation) {
            Set<String> users = challengeParticipators.get(challengeParticipation.getChallenge());
            if(users == null) {
                users = new HashSet<String>();
            }
            users.add(challengeParticipation.getParticipator().getUsername());
            challengeParticipators.put(challengeParticipation.getChallenge(), users);
            return challengeParticipation;
        }

        @Override
        public boolean isUserParticipatingInChallenge(Challenge challenge, String participatorUsername) {
            Set<String> users = challengeParticipators.get(challenge);
            return users != null && users.contains(participatorUsername);
        }

        @Override
        public boolean isChallengeWithGivenNameExistsForUser(String challengeName, String creatorUsername) {
            return challengesWithCreators.contains(new ChallengeNameAndCreatorTuple(challengeName, creatorUsername));
        }

        private static class ChallengeNameAndCreatorTuple {
            private final String challengeName;
            private final String challengeCreatorUsername;

            private ChallengeNameAndCreatorTuple(String challengeName, String challengeCreatorUsername) {
                this.challengeName = challengeName;
                this.challengeCreatorUsername = challengeCreatorUsername;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                ChallengeNameAndCreatorTuple that = (ChallengeNameAndCreatorTuple) o;

                if (!challengeCreatorUsername.equals(that.challengeCreatorUsername)) return false;
                if (!challengeName.equals(that.challengeName)) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = challengeName.hashCode();
                result = 31 * result + challengeCreatorUsername.hashCode();
                return result;
            }
        }
    }
}
