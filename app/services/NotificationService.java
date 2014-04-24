package services;

import domain.User;

import java.util.List;

public interface NotificationService {

    void notifyUser(User user);

    void notifyUsers(List<User> users);
}
