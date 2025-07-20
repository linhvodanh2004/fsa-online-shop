package fsa.project.online_shop.services;

import fsa.project.online_shop.models.User;

public interface UserService {
    public User getUserByUsername(String username);

    public User handleSaveUser(User user);

    public boolean checkUsernameExists(String username);

    public boolean checkEmailExists(String email);
    public User getUserByEmail(String email);
}