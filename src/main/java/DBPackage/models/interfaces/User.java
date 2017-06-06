package DBPackage.models.interfaces;

import DBPackage.views.*;

import java.util.List;

/**
 * Created by ksg on 20.05.17.
 */
public interface User {
    void insertUser(String about, String email, String fullname, String nickname);
    void updateUser(String about, String email, String fullname, String nickname);
    UserView getUser(String nickname, String email);
    List<UserView> getUsers(String nickname, String email);
    Integer countUsers();
    void clearUsersTable();
}
