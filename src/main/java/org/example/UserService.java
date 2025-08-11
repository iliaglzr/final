package org.example;

import DAO.UserDAO;
import org.example.User;

public class UserService {
    private  final UserDAO userDao = new UserDAO();

    public  boolean register(String name, String email, String password) {
        boolean b = false;
        if (userDao.FindByUsername(email) != null) {
            throw new RuntimeException("User with this email already exists");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        userDao.saveUser(user);
         b = true;
        return b;
    }

    public  User login(String email, String password) {
        User user = userDao.FindByUsername(email);
        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password");
        }
        return user;
    }
}
