package services;

import domain.FacebookUser;
import domain.User;
import play.libs.F;
import repositories.UsersRepository;

public class UserService extends TransactionalBase {

    private final UsersRepository usersRepository;

    public UserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public User createNewOrGetExistingUser(final FacebookUser user, final String profilePictureUrl) {
        return createNewOrGetExistingUser(user.getId(), user.getFirstName(), user.getLastName(), profilePictureUrl);
    }

    public User createNewOrGetExistingUser(final String username, final String firstName, final String lastName, final String profilePictureUrl) {
        return withTransaction(new F.Function0<User>() {
            @Override
            public User apply() throws Throwable {

                if (usersRepository.isUserExist(username)) {
                    return usersRepository.getUser(username);
                }

                return usersRepository.createUser(username, firstName, lastName, profilePictureUrl);
            }
        });
    }

    /**
     * Gets user from repository. User is assumed to exist in storage. Exception is thrown otherwise
     * <p>
     * CAUTION! Method should not be used if user existence is not certain
     *
     * @param username username of user to be fetched from storage
     * @return user with given username
     * @throws IllegalStateException if user is not in storage or there is more than one user with username
     *                               RuntimeException for any other exception
     */
    public User getExistingUser(final String userId) {
        return withReadOnlyTransaction(new F.Function0<User>() {
            @Override
            public User apply() throws Throwable {
                return usersRepository.getUser(userId);
            }
        });
    }
}
