package DBPackage.models.interfaces;

import DBPackage.views.*;

import java.util.List;

/**
 * Created by ksg on 20.05.17.
 */
public interface User {
    void create(String about, String email, String fullname, String nickname);
    void update(String about, String email, String fullname, String nickname);
    UserView findSingleByNickOrMail(String nickname, String email);
    List<UserView> findManyByNickOrMail(String nickname, String email);
    Integer count();
    void clear();
}
