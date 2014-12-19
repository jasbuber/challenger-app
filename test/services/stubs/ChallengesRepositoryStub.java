package services.stubs;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import domain.*;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.lang.reflect.Field;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.lowerCase;

@NotThreadSafe
public class ChallengesRepositoryStub extends ChallengesRepository {

    private final Map<Long, Challenge> challenges = new HashMap<Long, Challenge>();
    private final Map<Challenge, List<ChallengeParticipation>> challengeParticipations = new HashMap<Challenge, List<ChallengeParticipation>>();
    private final Map<Long, ChallengeResponse> challengeResponses = new HashMap<Long, ChallengeResponse>();

    private long challengeIdGenerator = 0L;

    @Override
    public Challenge createChallenge(Challenge challenge) {
        long challengeId = challengeIdGenerator++;
        setIdOfChallenge(challengeId, challenge);
        challenges.put(challengeId, challenge);
        return challenge;
    }

    //TODO is this functionality should be in repository? this test seems to say sth else
    @Override
    public boolean isChallengeWithGivenNameExistsForUser(final String challengeName, final String creatorUsername) {
        Map<Long, Challenge> challengesForUser = Maps.filterValues(challenges, new Predicate<Challenge>() {
            @Override
            public boolean apply(Challenge input) {
                return input.getChallengeName().equals(lowerCase(challengeName))
                        && input.getCreator().getUsername().equals(lowerCase(creatorUsername));
            }
        });

        return challengesForUser.size() > 0;
    }

    //TODO should challenge participation have its own repository? two different states in one repository? there is sth wrong
    @Override
    public ChallengeParticipation persistChallengeParticipation(ChallengeParticipation challengeParticipation) {
        List<ChallengeParticipation> challengeParticipationsToUpdate = challengeParticipations.get(challengeParticipation.getChallenge());
        if(challengeParticipationsToUpdate == null) {
            challengeParticipationsToUpdate = new ArrayList<ChallengeParticipation>();
        }
        challengeParticipationsToUpdate.add(challengeParticipation);
        challengeParticipations.put(challengeParticipation.getChallenge(), challengeParticipationsToUpdate);
        return challengeParticipation;
    }

    @Override
    public boolean deleteChallengeParticipation(Challenge challenge, User user) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isUserParticipatingInChallenge(Challenge challenge, final String participatorUsername) {
        List<ChallengeParticipation> participationsOfChallenge = challengeParticipations.get(challenge);
        if(participationsOfChallenge == null) {
            participationsOfChallenge = new ArrayList<ChallengeParticipation>();
        }

        Collection<ChallengeParticipation> challengeParticipationsOfUser = Collections2.filter(participationsOfChallenge, new Predicate<ChallengeParticipation>() {
            @Override
            public boolean apply(@Nullable ChallengeParticipation input) {
                return input.getParticipator().getUsername().equals(lowerCase(participatorUsername));
            }
        });

        return challengeParticipationsOfUser.size() > 0;
    }

    @Override
    public boolean isUserRespondedToChallenge(Challenge challenge, String participatorUsername) {
        throw new NotImplementedException();
    }

    @Override
    public ChallengeResponse addChallengeResponse(ChallengeResponse challengeResponse) {
        challengeResponses.put(challengeResponse.getId(), challengeResponse);
        return challengeResponse;
    }

    @Override
    public ChallengeParticipation getChallengeParticipation(Challenge challenge, String participatorUsername) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isNotEvaluatedChallengeResponseExistsFor(ChallengeParticipation challengeParticipation) {
        throw new NotImplementedException();
    }

    @Override
    public Challenge getChallenge(long id) {
        return challenges.get(id);
    }

    @Override
    public List<Challenge> findChallenges(ChallengeFilter challengeFilter) {
        throw new NotImplementedException();
    }

    @Override
    public ChallengeResponse updateChallengeResponse(ChallengeResponse challengeResponse) {
        challengeResponses.put(challengeResponse.getId(), challengeResponse);
        return challengeResponse;
    }

    //TODO why the list is returned and not set, may two same participators be returned?
    @Override
    public List<User> getAllParticipatorsOf(Challenge challenge) {
        List<ChallengeParticipation> participationsForChallenge = challengeParticipations.get(challenge);
        if(participationsForChallenge == null) {
            participationsForChallenge = Collections.emptyList();
        }

        return Lists.transform(participationsForChallenge, new Function<ChallengeParticipation, User>() {
            @Nullable
            @Override
            public User apply(ChallengeParticipation input) {
                return input.getParticipator();
            }
        });
    }

    @Override
    public Long countCreatedChallengesForUser(String username) {
        throw new NotImplementedException();
    }

    @Override
    public Long countCompletedChallenges(String participatorUsername) {
        throw new NotImplementedException();
    }

    @Override
    public List getChallengesWithParticipantsNrForUser(String creatorUsername, int offsetIndex) {
        throw new NotImplementedException();
    }

    @Override
    public List getLatestChallengesWithParticipantsNrForUser(String creatorUsername) {
        throw new NotImplementedException();
    }

    @Override
    public List getChallengeParticipationsWithParticipantsNrForUser(String participatorUsername, int offsetIndex) {
        throw new NotImplementedException();
    }

    @Override
    public List getLastestParticipationsWithParticipantsNrForUser(String participatorUsername) {
        throw new NotImplementedException();
    }

    @Override
    public Challenge closeChallenge(long id) {
        throw new NotImplementedException();
    }

    @Override
    public List<ChallengeResponse> getResponsesForChallenge(long challengeId) {
        throw new NotImplementedException();
    }

    @Override
    public Long getResponsesNrForChallenge(long challengeId) {
        throw new NotImplementedException();
    }

    @Override
    public ChallengeResponse getChallengeResponse(long id) {
        throw new NotImplementedException();
    }

    @Override
    public List<ChallengeResponse> getChallengeParticipationsForUser(String creatorUsername) {
        throw new NotImplementedException();
    }

    @Override
    public List<Challenge> getCompletedChallenges(String username) {
        throw new NotImplementedException();
    }

    @Override
    public List<ChallengeParticipation> getParticipantsForChallenge(long challengeId, int offsetIndex) {
        throw new NotImplementedException();
    }

    @Override
    public List<ChallengeParticipation> getLatestParticipantsForChallenge(long challengeId) {
        throw new NotImplementedException();
    }

    @Override
    public long getParticipantsNrForChallenge(long challengeId) {
        throw new NotImplementedException();
    }

    @Override
    public Long getCreatedChallengesNrForUser(String username) {
        throw new NotImplementedException();
    }

    @Override
    public Long getJoinedChallengesNrForUser(String username) {
        throw new NotImplementedException();
    }

    @Override
    public Long getCompletedChallengesNrForUser(String username) {
        throw new NotImplementedException();
    }

    @Override
    public long getNrOfParticipationsOf(Challenge challenge) {
        return getAllParticipatorsOf(challenge).size();
    }

    @Override
    public boolean isUserCreatedAChallenge(Long id, String creatorUsername) {
        throw new NotImplementedException();
    }

    @Override
    public Challenge updateChallenge(Challenge challenge) {
        challenges.put(challenge.getId(), challenge);
        return challenge;
    }

    @Override
    public Long getResponsesNrForUser(String username) {
        throw new NotImplementedException();
    }

    @Override
    public Long getAcceptedResponsesNrForUser(String username) {
        throw new NotImplementedException();
    }

    @Override
    public ChallengeParticipation updateChallengeParticipation(ChallengeParticipation challengeParticipation) {
        throw new NotImplementedException();
    }

    @Override
    public List<Challenge> getTopRatedChallenges() {
        throw new NotImplementedException();
    }

    @Override
    public List<Challenge> getTrendingChallenges() {
        throw new NotImplementedException();
    }

    @Override
    public List getMostPopularChallenges() {
        throw new NotImplementedException();
    }

    @Override
    public List<Comment> getCommentsForChallenge(long challengeId, int offsetIndex) {
        throw new NotImplementedException();
    }

    @Override
    public Comment createComment(Comment comment) {
        throw new NotImplementedException();
    }

    @Override
    public Long getChallengesNrForUser(String username) {
        throw new NotImplementedException();
    }

    @Override
    public Long getChallengeParticipationsNrForUser(String username) {
        throw new NotImplementedException();
    }

    @Override
    public Long getCommentsNrForChallenge(long challengeId) {
        throw new NotImplementedException();
    }


    private Challenge setIdOfChallenge(long id, Challenge challenge)  {
        try {
            Field challengeIdField = Challenge.class.getDeclaredField("id");
            challengeIdField.setAccessible(true);
            challengeIdField.set(challenge, id);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Assert.fail();
        }
        return challenge;
    }
}
