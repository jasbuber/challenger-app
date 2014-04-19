package repositories;

import domain.Challenge;
import play.db.jpa.JPA;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

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
}
