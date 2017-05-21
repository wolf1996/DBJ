package DBPackage.models.interfaces;

import DBPackage.views.*;
import java.util.List;
/**
 * Created by ksg on 20.05.17.
 */
public interface Forum {
    void create(String username, String slug, String title);
    ForumView getBySlug(String slug);
    List<ThreadView> getAllThreads(String slug, Integer limit, String since, Boolean desc);
    List<UserView> getAllUsers(String slug, Integer limit, String since, Boolean desc);
    Integer countForums();
    void clear();
}
