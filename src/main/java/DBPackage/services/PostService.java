package DBPackage.services;

import DBPackage.models.*;
import DBPackage.models.ForumModel;
import DBPackage.models.ServiceModel;
import DBPackage.models.UserModel;
import DBPackage.views.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.ArrayList;



/**
 * Created by ksg on 10.03.17.
 */
@Service
public class PostService {

    private final JdbcTemplate jdbcTemplate;

    public PostService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public final List<PostModel> getPostFromDbModel(final Integer id) {
        final String sql = "SELECT * FROM posts WHERE id = ?";
        return jdbcTemplate.query(
                "SELECT * FROM posts WHERE id = ?",
                new Object[]{id},
                PostService::read);
    }

    public final List<PostView> getPostFromDb(final Integer id) {
        List<PostModel> model = getPostFromDbModel(id);
        List<PostView> view = new ArrayList<PostView>();
        for(PostModel i: model){
            view.add(new PostView(i));
        }
        return view;
    }

    public final PostDetailsView getDetailedPostFromDb(final PostModel post, String[] related) {
        UserModel user = null;
        ForumModel forum = null;
        ThreadModel thread = null;

        if (related != null) {

            for (String relation : related) {

                if (Objects.equals(relation, "user")) {
                    UserService userService = new UserService(jdbcTemplate);
                    List<UserModel> users = userService.getUserFromDbModel(new UserModel(null, null, null, post.getAuthor()));

                    if (!users.isEmpty()) {
                        user = users.get(0);
                    }
                }

                if (relation.equals("forum")) {
                    ForumService forumService = new ForumService(jdbcTemplate);
                    List<ForumView> forums = forumService.getForumInfo(post.getForum());

                    if (!forums.isEmpty()) {
                        forum = new ForumModel(forums.get(0));
                    }

                    forum.setThreads(jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM threads WHERE LOWER(forum) = LOWER(?)",
                            Integer.class,
                            forum.getSlug()
                    ));
                }

                if (relation.equals("thread")) {
                    ThreadService forumService = new ThreadService(jdbcTemplate);
                    List<ThreadModel> threads = forumService.getThreadInfoByIdModel(post.getThread());

                    if (!threads.isEmpty()) {
                        thread = threads.get(0);
                    }
                }
            }
        }

        return new PostDetailsView(user, forum, post, thread);
    }

    public final List<PostView> updatePostInDb(final PostView post, final Integer id) {
        final StringBuilder sql = new StringBuilder("UPDATE posts SET \"message\" = ?");
        List<PostView> posts = getPostFromDb(id);

        if (posts.isEmpty()) {
            return posts;
        }

        if (!Objects.equals(post.getMessage(), posts.get(0).getMessage())) {
            sql.append(", isEdited = TRUE");
        }

        sql.append(" WHERE id = ?");
        jdbcTemplate.update(sql.toString(), post.getMessage(), id);

        return getPostFromDb(id);
    }

    public static PostModel read(ResultSet rs, int rowNum) throws SQLException {
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));

        return new PostModel(
                rs.getString("author"),
                dateFormat.format(timestamp),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getBoolean("isEdited"),
                rs.getString("message"),
                rs.getInt("parent"),
                rs.getInt("thread")
        );
    }
}
