package repositories;

import domain.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import play.db.jpa.JPA;

import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.List;

public class ChallengesRepository {

    public Challenge createChallenge(Challenge challenge) {
        JPA.em().persist(challenge);
        return challenge;
    }

    public boolean isChallengeWithGivenNameExistsForUser(String challengeName, String creatorUsername) {
        Query challengesWithNameOfCreatorNrQuery = JPA.em().createQuery("SELECT count(c) " +
                                                        "FROM Challenge c " +
                                                        "WHERE LOWER(c.challengeName) = LOWER(:challengeName) " +
                                                        "AND LOWER(c.creator.username) = LOWER(:creatorUsername)");
        challengesWithNameOfCreatorNrQuery.setParameter("challengeName", challengeName);
        challengesWithNameOfCreatorNrQuery.setParameter("creatorUsername", creatorUsername);
        Long challengesWithNameOfCreatorNr = (Long) challengesWithNameOfCreatorNrQuery.getSingleResult();
        return challengesWithNameOfCreatorNr > 0;
    }

    public ChallengeParticipation persistChallengeParticipation(ChallengeParticipation challengeParticipation) {
        JPA.em().persist(challengeParticipation);
        return challengeParticipation;
    }

    //Method should be handled based on database and concurrency integrity
    public boolean deleteChallengeParticipation(Challenge challenge, User user) {
        Query challengeParticipationQuery = JPA.em().createQuery("SELECT c FROM ChallengeParticipation c " +
                                                                 "WHERE LOWER(c.participator.username) = LOWER(:username)" +
                                                                 "AND LOWER(c.challenge.challengeName) = LOWER(:challengeName)");
        challengeParticipationQuery.setParameter("username", user.getUsername());
        challengeParticipationQuery.setParameter("challengeName", challenge.getChallengeName());
        ChallengeParticipation challengeParticipation = (ChallengeParticipation) challengeParticipationQuery.getSingleResult();
        JPA.em().remove(challengeParticipation);
        return true;
    }

    //todo check sql generated by hibernate -> if there is not unnecessary join with Challenge added
    public boolean isUserParticipatingInChallenge(Challenge challenge, String participatorUsername) {
        Query usernameUsersParticipatingNrQuery = JPA.em().createQuery("SELECT count(p) " +
                                                                    "FROM ChallengeParticipation p " +
                                                                    "WHERE p.challenge = :challenge " +
                                                                    "AND LOWER(p.participator.username) = LOWER(:participatorUsername)");
        usernameUsersParticipatingNrQuery.setParameter("challenge", challenge);
        usernameUsersParticipatingNrQuery.setParameter("participatorUsername", participatorUsername);
        Long usernameUsersParticipatingNr = (Long) usernameUsersParticipatingNrQuery.getSingleResult();
        return usernameUsersParticipatingNr > 0;
    }

    public boolean isUserRespondedToChallenge(Challenge challenge, String participatorUsername) {
        Query usernameUsersParticipatingNrQuery = JPA.em().createQuery("SELECT count(r) " +
                "FROM ChallengeResponse r " +
                "WHERE r.challengeParticipation.challenge = :challenge " +
                "AND LOWER(r.challengeParticipation.participator.username) = LOWER(:participatorUsername)");
        usernameUsersParticipatingNrQuery.setParameter("challenge", challenge);
        usernameUsersParticipatingNrQuery.setParameter("participatorUsername", participatorUsername);
        Long usernameUsersParticipatingNr = (Long) usernameUsersParticipatingNrQuery.getSingleResult();
        return usernameUsersParticipatingNr > 0;
    }

    public ChallengeResponse addChallengeResponse(ChallengeResponse challengeResponse) {
        JPA.em().persist(challengeResponse);
        return challengeResponse;
    }

    /**
     * THIS METHOD THROWS EXCEPTION WHEN NO CHALLENGE PARTICIPATION EXISTS FOR GIVEN ARGUMENTS.
     *
     * Must be used only after succeeded test for user's participation in challenge.
     *
     * No exception is thrown if there is more than one challenge participation for user. The first found result will be returned.
     * However this is invalid state of the system and is logged with error level.
     *
     * @param challenge challenge to get one of participations for
     * @param participatorUsername username of the participator in challenge
     * @return challengeParticipation for given challenge of given participator
     * @throws java.lang.IllegalStateException if there is no challengeParticipation for given challenge of given participator
     */
    public ChallengeParticipation getChallengeParticipation(Challenge challenge, String participatorUsername) {
        Query getChallengeParticipationQuery = JPA.em().createQuery("SELECT p " +
                                                                    "FROM ChallengeParticipation p " +
                                                                    "WHERE p.challenge = :challenge " +
                                                                    "AND LOWER(p.participator.username) = LOWER(:participatorUsername)");
        getChallengeParticipationQuery.setParameter("challenge", challenge);
        getChallengeParticipationQuery.setParameter("participatorUsername", participatorUsername);

        List<ChallengeParticipation> challengeParticipations = getChallengeParticipationQuery.getResultList();

        assertThatChallengeParticipationIsFound(challenge, participatorUsername, challengeParticipations);

        if(challengeParticipations.size() > 1) {
            //TODO when logger will be added then invalid system state should be logged with error level
        }

        return challengeParticipations.get(0);
    }

    private void assertThatChallengeParticipationIsFound(Challenge challenge, String participatorUsername, List<ChallengeParticipation> challengeParticipations) {
        if(challengeParticipations.isEmpty()) {
            throw new IllegalStateException("Participator: " + participatorUsername + " is not participating in challenge " + challenge.getChallengeName());
        }
    }

    public boolean isNotEvaluatedChallengeResponseExistsFor(ChallengeParticipation challengeParticipation) {
        Query notEvaluatedChallengeResponsesForParticipationNr = JPA.em().createQuery("SELECT count(r) " +
                                                                                   "FROM ChallengeResponse r " +
                                                                                   "WHERE r.challengeParticipation = :challengeParticipation " +
                                                                                   "AND r.isAccepted IS NULL");

        notEvaluatedChallengeResponsesForParticipationNr.setParameter("challengeParticipation", challengeParticipation);
        Long notEvaluatedChallengeResponsesNr = (Long) notEvaluatedChallengeResponsesForParticipationNr.getSingleResult();
        return notEvaluatedChallengeResponsesNr > 0;
    }

    public Challenge getChallenge(long id){ return JPA.em().find(Challenge.class, id); }

    public List<Challenge> findChallenges(ChallengeFilter challengeFilter){
        return challengeFilter.getQuery().getResultList();
    }

    public ChallengeResponse updateChallengeResponse(ChallengeResponse challengeResponse) {
        return JPA.em().merge(challengeResponse);
    }

    public List<User> getAllParticipatorsOf(Challenge challenge) {
        Query participators = JPA.em().createQuery("SELECT p.participator " +
                                                  "FROM ChallengeParticipation p " +
                                                  "WHERE p.challenge = :challenge");
        participators.setParameter("challenge", challenge);
        return participators.getResultList();
    }

    public Long countCreatedChallengesForUser(String username){
        Query challengesCreatedByUserQuery = JPA.em().createQuery("SELECT count(c) FROM Challenge c " +
                "WHERE LOWER(c.creator.username) = LOWER(:username)");
        challengesCreatedByUserQuery.setParameter("username", username);
        return (Long) challengesCreatedByUserQuery.getSingleResult();
    }

    public Long countCompletedChallenges(String participatorUsername) {
        Query completedChallengesQuery = JPA.em().createQuery("SELECT count(r) " +
                "FROM ChallengeResponse r " +
                "INNER JOIN r.challengeParticipation p " +
                "WHERE r.isAccepted = 'Y'" +
                "AND LOWER(p.participator.username) = LOWER(:participatorUsername)");
        completedChallengesQuery.setParameter("participatorUsername", participatorUsername);
        return (Long) completedChallengesQuery.getSingleResult();
    }

    public List getChallengesWithParticipantsNrForUser(String creatorUsername) {
        Query completedChallengesQuery = JPA.em().createQuery("SELECT c.challengeName as name, c.creationDate, count(p), c.id " +
                "FROM ChallengeParticipation p " +
                "RIGHT OUTER JOIN p.challenge c " +
                "WHERE c.active = true " +
                "AND LOWER(c.creator.username) = LOWER(:creatorUsername) " +
                "GROUP BY c.challengeName " +
                "ORDER BY c.creationDate DESC");
        completedChallengesQuery.setParameter("creatorUsername", creatorUsername);
        return completedChallengesQuery.getResultList();
    }

    public List getLatestChallengesWithParticipantsNrForUser(String creatorUsername) {
        Query completedChallengesQuery = JPA.em().createQuery("SELECT c.challengeName as name, c.creationDate, count(p), c.id " +
                "FROM ChallengeParticipation p " +
                "RIGHT OUTER JOIN p.challenge c " +
                "WHERE c.active = true " +
                "AND LOWER(c.creator.username) = LOWER(:creatorUsername) " +
                "GROUP BY c.challengeName " +
                "ORDER BY c.creationDate DESC");
        completedChallengesQuery.setParameter("creatorUsername", creatorUsername);
        completedChallengesQuery.setMaxResults(3);
        return completedChallengesQuery.getResultList();
    }

    public List getChallengeParticipationsWithParticipantsNrForUser(String participatorUsername) {
        Query completedChallengesQuery = JPA.em().createQuery("SELECT c.challengeName as name, c.creationDate, count(p), c.id, p.endingDate, p.joined, p.isResponseSubmitted " +
                "FROM ChallengeParticipation p " +
                "JOIN p.challenge c " +
                "WHERE c.active = true " +
                "AND LOWER(p.participator.username) = LOWER(:participatorUsername) " +
                "GROUP BY c.challengeName " +
                "ORDER BY p.endingDate ASC");
        completedChallengesQuery.setParameter("participatorUsername", participatorUsername);
        return completedChallengesQuery.getResultList();
    }

    public List getLastestParticipationsWithParticipantsNrForUser(String participatorUsername) {
        Query completedChallengesQuery = JPA.em().createQuery("SELECT c.challengeName as name, c.creationDate, count(p), c.id " +
                "FROM ChallengeParticipation p " +
                "JOIN p.challenge c " +
                "WHERE c.active = true " +
                "AND LOWER(p.participator.username) = LOWER(:participatorUsername) " +
                "GROUP BY c.challengeName " +
                "ORDER BY p.joined DESC");
        completedChallengesQuery.setParameter("participatorUsername", participatorUsername);
        completedChallengesQuery.setMaxResults(3);
        return completedChallengesQuery.getResultList();
    }

    public Challenge closeChallenge(long id){
        Challenge challenge = getChallenge(id);
        if( challenge.isActive() ){
            challenge.setInactive();
            return JPA.em().merge(challenge);
        }
        return challenge;
    }

    public List<ChallengeResponse> getResponsesForChallenge(long challengeId) {
        Query completedChallengesQuery = JPA.em().createQuery("SELECT r " +
                "FROM ChallengeResponse r " +
                "WHERE LOWER(r.challengeParticipation.challenge.id) = LOWER(:challengeId)" +
                "ORDER BY r.submitted DESC");
        completedChallengesQuery.setParameter("challengeId", challengeId);
        return completedChallengesQuery.getResultList();
    }

    public Long getResponsesNrForChallenge(long challengeId) {
        Query completedChallengesQuery = JPA.em().createQuery("SELECT count(r) " +
                "FROM ChallengeResponse r " +
                "WHERE LOWER(r.challengeParticipation.challenge.id) = LOWER(:challengeId)");
        completedChallengesQuery.setParameter("challengeId", challengeId);
        return (Long) completedChallengesQuery.getSingleResult();
    }

    public ChallengeResponse getChallengeResponse(long id){ return JPA.em().find(ChallengeResponse.class, id); }

    public List<ChallengeResponse> getChallengeParticipationsForUser(String creatorUsername) {
        Query challengeParticipationsQuery = JPA.em().createQuery("SELECT p, r " +
                "FROM ChallengeResponse r " +
                "RIGHT OUTER JOIN r.challengeParticipation p " +
                "WHERE p.challenge.active = true " +
                "AND LOWER(p.participator.username) = LOWER(:creatorUsername) " +
                "ORDER BY p.endingDate ASC");
        challengeParticipationsQuery.setParameter("creatorUsername", creatorUsername);
        return challengeParticipationsQuery.getResultList();
    }

    public List<Challenge> getCompletedChallenges(String username) {
        Query completedChallengesQuery = JPA.em().createQuery("SELECT DISTINCT c " +
                "FROM Challenge c " +
                "WHERE c.active = false AND LOWER(c.creator.username) = LOWER(:username) OR c IN " +
                "(SELECT p.challenge FROM ChallengeParticipation p WHERE LOWER(p.participator.username) = LOWER(:username) AND p.challenge.active = false)"
        );
        completedChallengesQuery.setParameter("username", username);
        return completedChallengesQuery.getResultList();
    }

    public List<ChallengeParticipation> getParticipantsForChallenge(long challengeId){

        Query participantsForChallengeQuery = JPA.em().createQuery("SELECT p " +
                        "FROM ChallengeParticipation p " +
                        "WHERE p.challenge.id = :challengeId");

        participantsForChallengeQuery.setParameter("challengeId", challengeId);
        return participantsForChallengeQuery.getResultList();
    }

    public Long getCreatedChallengesNrForUser(String username) {
        Query createdChallengesQuery = JPA.em().createQuery("SELECT count(c) " +
                "FROM Challenge c " +
                "WHERE LOWER(c.creator.username) = LOWER(:username)");
        createdChallengesQuery.setParameter("username", username);
        return (Long) createdChallengesQuery.getSingleResult();
    }

    public Long getJoinedChallengesNrForUser(String username) {
        Query joinedChallengesQuery = JPA.em().createQuery("SELECT count(p) " +
                "FROM ChallengeParticipation p " +
                "WHERE LOWER(p.participator.username) = LOWER(:username)");
        joinedChallengesQuery.setParameter("username", username);
        return (Long) joinedChallengesQuery.getSingleResult();
    }

    public Long getCompletedChallengesNrForUser(String username) {
        Query completedChallengesQuery = JPA.em().createQuery("SELECT count(r) " +
                "FROM ChallengeResponse r " +
                "WHERE LOWER(r.challengeParticipation.participator.username) = LOWER(:username) " +
                "AND r.isAccepted = 'Y' " +
                "AND r.challengeParticipation.challenge.active = false");
        completedChallengesQuery.setParameter("username", username);
        return (Long) completedChallengesQuery.getSingleResult();
    }

    public long getNrOfParticipationsOf(Challenge challenge) {
        Query nrOfParticipationsOfChallengeQuery = JPA.em().createQuery("SELECT COUNT(p) " +
                "FROM ChallengeParticipation p " +
                "WHERE p.challenge = :challenge");

        nrOfParticipationsOfChallengeQuery.setParameter("challenge", challenge);
        return (Long) nrOfParticipationsOfChallengeQuery.getSingleResult();
    }

    public boolean isUserCreatedAChallenge(Long id, String creatorUsername) {

        Challenge challenge = getChallenge(id);

        if(challenge.getCreator().getUsername().compareTo(creatorUsername) == 0){
            return true;
        }else{
            return false;
        }

    }

    public Challenge updateChallenge(Challenge challenge) {
        return JPA.em().merge(challenge);
    }

    public Long getResponsesNrForUser(String username) {
        Query completedChallengesQuery = JPA.em().createQuery("SELECT count(r) " +
                "FROM ChallengeResponse r " +
                "WHERE LOWER(r.challengeParticipation.participator.username) = LOWER(:username)");
        completedChallengesQuery.setParameter("username", username);
        return (Long) completedChallengesQuery.getSingleResult();
    }

    public Long getAcceptedResponsesNrForUser(String username) {
        Query completedChallengesQuery = JPA.em().createQuery("SELECT count(r) " +
                "FROM ChallengeResponse r " +
                "WHERE LOWER(r.challengeParticipation.participator.username) = LOWER(:username) " +
                "AND r.isAccepted = 'Y'");
        completedChallengesQuery.setParameter("username", username);
        return (Long) completedChallengesQuery.getSingleResult();
    }

    public ChallengeParticipation updateChallengeParticipation(ChallengeParticipation challengeParticipation) {
        return JPA.em().merge(challengeParticipation);
    }

    public List<Challenge> getTopRatedChallenges(){
        Query topChallengesQuery = JPA.em().createQuery("SELECT c FROM Challenge c ORDER BY c.rating DESC");
        topChallengesQuery.setMaxResults(6);
        return topChallengesQuery.getResultList();
    }

    public List<Challenge> getTrendingChallenges(){

        DateTimeZone timeZone = DateTimeZone.forID( "Europe/Paris" );
        DateTime dateTime = new DateTime( new java.util.Date(), timeZone );
        DateTime trendingDate = dateTime.minusDays(14);

        Query trendingChallengesQuery = JPA.em().createQuery("SELECT c " +
                "FROM Challenge c " +
                "WHERE c.creationDate > :trendingDate " +
                "ORDER BY c.rating DESC");
        trendingChallengesQuery.setParameter("trendingDate", trendingDate.toDate(), TemporalType.DATE);
        trendingChallengesQuery.setMaxResults(6);
        return trendingChallengesQuery.getResultList();
    }

    public List getMostPopularChallenges() {
        Query mostPopularChallenges = JPA.em().createQuery("SELECT c.id, c.challengeName as name, count(p) " +
                "FROM ChallengeParticipation p " +
                "RIGHT OUTER JOIN p.challenge c " +
                "WHERE c.active = true " +
                "GROUP BY c.challengeName " +
                "ORDER BY count(p) DESC");
        mostPopularChallenges.setMaxResults(6);
        return mostPopularChallenges.getResultList();
    }
}
