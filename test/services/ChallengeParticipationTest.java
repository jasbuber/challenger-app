package services;

import domain.Challenge;
import domain.ChallengeCategory;
import domain.ChallengeParticipation;
import domain.User;
import org.junit.Test;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import java.lang.reflect.Field;
import java.util.*;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ChallengeParticipationTest {

    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final UsersRepository usersRepository = new UserRepositoryStub();
    private final ChallengeNotificationsService challengeNotificationService = mock(ChallengeNotificationsService.class);

    private final static String SOME_VIDEO_ID = "videoId";
    private final Boolean VISIBILITY_PRIVATE = false;

    private final static ChallengeCategory SOME_CATEGORY = ChallengeCategory.ALL;

    private final ChallengeService challengeService = createChallengeService();

    private final String challengeName = "challengeName";
    private final String userId = "1123123";
    private final String user = "username";

    private final Challenge challenge = createChallenge(user);

    private ChallengeServiceWithoutTransactionMgmt createChallengeService() {
        return new ChallengeServiceWithoutTransactionMgmt(challengesRepository, usersRepository, challengeNotificationService);
    }


    @Test
    public void shouldUserParticipationBeTrueIfUserIsAlreadyParticipatingInChallenge() throws Exception {
        //when
        participateInChallenge(user);
        boolean userParticipatingInChallenge = challengeService.isUserParticipatingInChallenge(challenge, user);

        //
        assertTrue(userParticipatingInChallenge);
    }

    @Test
    public void shouldUserParticipationBeFalseIfUserIsNotParticipatingInChallengeYet() throws Exception {
        //when
        boolean userParticipatingInChallenge = challengeService.isUserParticipatingInChallenge(challenge, user);

        //
        assertFalse(userParticipatingInChallenge);
    }

    @Test
    public void shouldCreateChallengeParticipationForUserAndChallenge() throws Exception {
        //when
        ChallengeParticipation challengeParticipation =
                challengeService.participateInChallenge(challenge, userId, user);

        //then
        assertTrue(challengeParticipation != null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenTryingToParticipateAgainInSameChallenge() throws Exception {
        //when
        participateInChallenge(user);
        participateInChallenge(user);

        //then throw exception
    }

    @Test
    public void shouldNotNotifyUntilPopularityFactorIsAchieved() throws Exception {
        //when
        participateInChallenge("participator");

        //then
        verify(challengeNotificationService, never()).notifyAboutNewChallengeParticipation(challenge, userId, "participator", Collections.<User>emptyList());
    }

    @Test
    public void shouldNotifyParticipatorForWhomPopularityFactorIsAchieved() throws Exception {
        //when
        achieveOneFromPopularityFactor(challenge);
        participateInChallenge("participatorOfFactorAchievement");

        //then
        verify(challengeNotificationService).notifyAboutNewChallengeParticipation(challenge, userId, "participatorOfFactorAchievement", Collections.<User>emptyList());
    }

    @Test
    public void shouldNotifyOnlyParticipatorForWhomPopularityFactorIsAchieved() throws Exception {
        //when
        achieveOneFromPopularityFactor(challenge);
        participateInChallenge("participatorOfFactorAchievement");
        participateInChallenge("participatorAfterAchievement");

        //then
        verify(challengeNotificationService).notifyAboutNewChallengeParticipation(challenge, userId, "participatorOfFactorAchievement", Collections.<User>emptyList());
    }

    private void participateInChallenge(String username) {
        challengeService.participateInChallenge(challenge, userId, username);
    }

    private void achieveOneFromPopularityFactor(Challenge challenge) {
        for (int i = 0; i < ChallengeService.POPULARITY_INDICATOR - 1; i++) {
            challengeService.participateInChallenge(challenge, userId, "participator" + i);
        }
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

    private class ChallengesRepositoryStub extends ChallengesRepository {

        private Set<ChallengeNameAndCreatorTuple> challengesWithCreators = new HashSet<ChallengeNameAndCreatorTuple>();
        private Map<Challenge, Set<String>> challengeParticipators = new HashMap<Challenge, Set<String>>();

        @Override
        public Challenge createChallenge(Challenge challengeWithoutId) {
            Challenge challenge = setIdFieldOf(challengeWithoutId);
            challengesWithCreators.add(new ChallengeNameAndCreatorTuple(challenge.getChallengeName(), challenge.getCreator().getUsername()));
            return challenge;
        }

        private Challenge setIdFieldOf(Challenge challenge) {
            Field idField = null;
            try {
                idField = Challenge.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(challenge, Long.valueOf(1L));
                return challenge;
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Exception was thrown during setting id field of challenge: " + challenge, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Exception was thrown during setting id field of challenge: " + challenge, e);
            }
        }

        @Override
        public ChallengeParticipation persistChallengeParticipation(ChallengeParticipation challengeParticipation) {
            Set<String> users = challengeParticipators.get(challengeParticipation.getChallenge());
            if (users == null) {
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

        @Override
        public List<User> getAllParticipatorsOf(Challenge challenge) {
            return Collections.<User>emptyList();
        }

        @Override
        public long getNrOfParticipationsOf(Challenge challenge) {
            return challengeParticipators.get(challenge).size();
        }

        @Override
        public Challenge getChallenge(long id) {
            return challenge;
        }

        private class ChallengeNameAndCreatorTuple {
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
