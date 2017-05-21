package DBPackage.models.interfaces;

import DBPackage.views.*;
import java.util.List;

/**
 * Created by ksg on 20.05.17.
 */
public interface Post {
    void create(List<PostView> posts, String slug_or_id);
    PostView update(String message, Integer id);
    PostView getById(Integer id);
    PostDetailsView detailsView(Integer id, String[] related);
    List<PostView> sort(Integer limit, Integer offset, String sort, Boolean desc, String slug_or_id);
    Integer countPost();
    void clear();
}
