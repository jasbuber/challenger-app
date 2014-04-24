package repositories;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.User;
import play.db.jpa.JPA;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;

public class ChallengeFilter {

    private final CriteriaBuilder builder;

    private final CriteriaQuery<Challenge> criteriaQuery;

    private final Root<Challenge> root;

    private final EntityManager entityManager;

    private final int limit;

    public ChallengeFilter(int limit){

        this.entityManager = JPA.em();
        this.builder = entityManager.getCriteriaBuilder();
        this.criteriaQuery = builder.createQuery(Challenge.class);
        this.root = criteriaQuery.from(Challenge.class);
        this.criteriaQuery.select(root);
        this.limit = limit;
    }


    public CriteriaQuery<Challenge> getCriteriaQuery() {
        return criteriaQuery;
    }

    public CriteriaBuilder getBuilder() {
        return builder;
    }

    public Query getQuery(){
        return JPA.em().createQuery(this.criteriaQuery).setMaxResults(this.limit);
    }

    public Root<Challenge> getRoot() {
        return root;
    }

    public void orderDescBy(String fieldName){
        this.criteriaQuery.orderBy(this.builder.desc(this.root.get(fieldName)));
    }

    public Predicate excludeUser(User u){
        Expression<String> creator = root.get("creator");
        return builder.notEqual(creator, u);
    }

    public Predicate excludeChallengesThatUserParticipatesIn(User u){

        Subquery<ChallengeParticipation> subquery = this.criteriaQuery.subquery(ChallengeParticipation.class);
        Root subqueryRoot = subquery.from(ChallengeParticipation.class);
        subquery.select(subqueryRoot.get("challenge").get("id"));
        subquery.where(builder.equal(subqueryRoot.get("participator"), u));

        return builder.not(builder.in(this.root.get("id")).value(subquery));
    }

}
