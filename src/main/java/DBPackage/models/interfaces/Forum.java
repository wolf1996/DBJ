package DBPackage.models.interfaces;

import DBPackage.views.*;
import java.util.List;
/**
 * Created by ksg on 20.05.17.
 */
public interface Forum {
    void insertForum(String username, String slug, String title);
    ForumView getForum(String slug);
    List<ThreadView> getForumThreads(String slug, Integer limit, String since, Boolean desc);
    List<UserView> getForumUsers(String slug, Integer limit, String since, Boolean desc);
    Integer countForums();
    void clearForumsTable();
}
