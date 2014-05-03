package repositories;

import domain.Challenge;
import domain.User;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;

import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class UsersRepository {

    public User createUser(String username) {
        User user = new User(username);
        JPA.em().persist(user);
        return user;
    }

    public User createUser(String username, String profilePictureUrl) {
        User user = new User(username, profilePictureUrl);
        JPA.em().persist(user);
        return user;
    }

    public boolean isUserExist(String username) {
        Query userWithUsernameNrQuery = JPA.em().createQuery("SELECT count(u) FROM User u WHERE LOWER(u.username) = LOWER(:username)");
        userWithUsernameNrQuery.setParameter("username", username);
        Long userWithUserNameNr = (Long) userWithUsernameNrQuery.getSingleResult();
        return userWithUserNameNr > 0;
    }

    /**
     * Gets user from repository. User is assumed to exist in storage. Exception is thrown otherwise
     * <p/>
     * CAUTION! Method should not be used if user existence is not certain
     *
     * @param username username of user to be fetched from storage
     * @return user with given username
     * @throws IllegalStateException if user is not in storage or there is more than one user with username
     *                               RuntimeException for any other exception
     */
    public User getUser(String username) {
        Query userByUsernameQuery = JPA.em().createQuery("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)");
        userByUsernameQuery.setParameter("username", username);
        return executeGetUserByUsernameQuery(userByUsernameQuery, username);
    }

    private User executeGetUserByUsernameQuery(Query userByUsernameQuery, String username) {
        try {
            return (User) userByUsernameQuery.getSingleResult();
        } catch (NoResultException noResultExc) {
            throw new IllegalStateException("No user with username " + username + " found in storage", noResultExc);
        } catch (NonUniqueResultException nonUniqueExc) {
            throw new IllegalStateException("More than one user with username " + username + " found in storage", nonUniqueExc);
        }
    }

    public List<User> getParticipatorsFor(Challenge challenge) {
        return new ArrayList<User>();
    }
}
