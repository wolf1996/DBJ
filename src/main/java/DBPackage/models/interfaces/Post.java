package DBPackage.models.interfaces;

import DBPackage.views.*;
import java.util.List;

/**
 * Created by ksg on 20.05.17.
 */
public interface Post {
    void insertPostsPack(List<PostView> posts, String slug_or_id);
    PostView updatePost(String message, Integer id);
    PostView getPostById(Integer id);
    PostDetailsView getPostDetailed(Integer id, String[] related);
    List<PostView> sortPosts(Integer limit, Integer offset, String sort, Boolean desc, String slug_or_id);
    Integer countPosts();
    void clearPostsTable();
}
