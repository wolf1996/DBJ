package DBPackage.models.implementations.JDBCimplementation;

import DBPackage.models.interfaces.Post;
import DBPackage.views.*;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by ksg on 20.05.17.
 */
@Service
public class PostJDBC extends BaseJDBC implements Post {
    private static class PostQueries {
        public static String createPostsQuery="INSERT INTO posts (user_id, created, forum_id, id, message, parent, thread_id, path) VALUES(" +
                    "(SELECT id FROM users WHERE nickname = ?), ?, ?, ?, ?, ?, ?, " +
                    "array_append((SELECT path FROM posts WHERE id = ?), ?))";

        public static String getPostQuery="SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id " +
                    "FROM posts p" +
                    "  JOIN users u ON (u.id = p.user_id)" +
                    "  JOIN forums f ON (f.id = p.forum_id) " +
                    "WHERE p.id = ?";

        public static String postsFlatSortQuery(final String slug_or_id, final Boolean desc) {
            return "SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id " +
                    "FROM posts p" +
                    "  JOIN users u ON (u.id = p.user_id)" +
                    "  JOIN forums f ON (f.id = p.forum_id) " +
                    "WHERE p.thread_id = " +
                    (slug_or_id.matches("\\d+")
                            ? "?" : "(SELECT threads.id FROM threads WHERE threads.slug = ?)") +
                    " ORDER BY p.created " + (desc ? "DESC" : "ASC") + ", p.id " + (desc ? "DESC" : "ASC") +
                    " LIMIT ? OFFSET ?";
        }

        public static String postsTreeSortQuery(final String slug_or_id, final Boolean desc) {
            return "SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id " +
                    "FROM posts p" +
                    "  JOIN users u ON (u.id = p.user_id)" +
                    "  JOIN forums f ON (f.id = p.forum_id) " +
                    "WHERE p.thread_id = " +
                    (slug_or_id.matches("\\d+") ?
                            "?" : "(SELECT threads.id FROM threads WHERE threads.slug = ?)"
                    ) +
                    " ORDER BY path " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?";
        }

        public static String postsParentTreeSortQuery(final String slug_or_id, final Boolean desc) {
            return "WITH RECURSIVE some_posts AS (" +
                    "    SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id, p.path" +
                    "    FROM posts p" +
                    "      JOIN users u ON (u.id = p.user_id)" +
                    "      JOIN forums f ON (f.id = p.forum_id) " +
                    "    WHERE p.thread_id = " +
                    (slug_or_id.matches("\\d+") ?
                            "?" : "(SELECT threads.id FROM threads WHERE threads.slug = ?)"
                    ) +
                    "), tree AS (" +
                    "    (" +
                    "      SELECT *" +
                    "      FROM some_posts" +
                    "        WHERE parent = 0" +
                    "      ORDER BY id " + (desc ? "DESC" : "ASC") +
                    "      LIMIT ? OFFSET ?" +
                    "    )" +
                    "    UNION ALL" +
                    "    (" +
                    "      SELECT" +
                    "        some_posts.*" +
                    "      FROM tree" +
                    "        JOIN some_posts ON some_posts.parent = tree.id" +
                    "      WHERE some_posts.path && tree.path" +
                    "    )" +
                    ")" +
                    "SELECT * FROM tree ORDER BY path " + (desc ? "DESC" : "ASC");
        }

        public static String countPostsQuery = "SELECT COUNT(*) FROM posts";

        public static String clearTableQuery= "DELETE FROM posts";
    }



    public PostJDBC(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void create(final List<PostView> posts, final String slug_or_id) {
        final Integer threadId = slug_or_id.matches("\\d+") ? Integer.valueOf(slug_or_id) :
                getJdbcTemplate().queryForObject(ThreadJDBC.ThreadQueries.getThreadId, Integer.class, slug_or_id);
        final Integer forumId = getJdbcTemplate().queryForObject(ThreadJDBC.ThreadQueries.getForumIdQuery, Integer.class, threadId);
        final Timestamp created = new Timestamp(System.currentTimeMillis());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try (Connection connection = getJdbcTemplate().getDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(PostJDBC.PostQueries.createPostsQuery, Statement.NO_GENERATED_KEYS);

            for (PostView post : posts) {
                final Integer postId = getJdbcTemplate().queryForObject("SELECT nextval('posts_id_seq')", Integer.class);
                preparedStatement.setString(1, post.getAuthor());
                preparedStatement.setTimestamp(2, created);
                preparedStatement.setInt(3, forumId);
                preparedStatement.setInt(4, postId);
                preparedStatement.setString(5, post.getMessage());
                preparedStatement.setInt(6, post.getParent());
                preparedStatement.setInt(7, threadId);
                preparedStatement.setInt(8, post.getParent());
                preparedStatement.setInt(9, postId);
                preparedStatement.addBatch();
                post.setCreated(dateFormat.format(created));
                post.setId(postId);
            }

            preparedStatement.executeBatch();
            preparedStatement.close();
        } catch (SQLException ex) {
            throw new DataRetrievalFailureException(null);
        }
        getJdbcTemplate().update(ThreadJDBC.ThreadQueries.updateForumsPostsCount, posts.size(), forumId);
    }

    @Override
    public PostView update(final String message, final Integer id) {
        final PostView post = getById(id);
        final StringBuilder sql = new StringBuilder("UPDATE posts SET message = ?");
        if (!message.equals(post.getMessage())) {
            sql.append(", is_edited = TRUE");
            post.setIsEdited(true);
            post.setMessage(message);
        }
        sql.append(" WHERE id = ?");
        getJdbcTemplate().update(sql.toString(), message, id);
        return post;
    }

    @Override
    public final PostView getById(final Integer id) {
        return getJdbcTemplate().queryForObject(PostJDBC.PostQueries.getPostQuery, new Object[]{id}, readPost);
    }

    @Override
    public PostDetailsView detailsView(final Integer id, final String[] related) {
        final PostView post = getById(id);
        UserView user = null;
        ForumView forum = null;
        ThreadView thread = null;
        if (related != null) {
            for (String relation : related) {
                switch (relation) {
                    case "user":
                        user = getJdbcTemplate().queryForObject(UserJDBC.UserQueries.findUserQuery,
                                new Object[]{post.getAuthor(), null}, readUser);
                        break;
                    case "forum":
                        forum = getJdbcTemplate().queryForObject(ForumJDBC.ForumQueries.getForumQuery,
                                new Object[]{post.getForum()}, readForum);
                        break;
                    case "thread":
                        thread = getJdbcTemplate().queryForObject(ThreadJDBC.ThreadQueries.getThreadQuery(String.valueOf(post.getThread())),
                                new Object[]{post.getThread()}, readThread);
                }
            }
        }
        return new PostDetailsView(user, forum, post, thread);
    }

    @Override
    public List<PostView> sort(final Integer limit, final Integer offset, final String sort,
                               final Boolean desc, final String slug_or_id) {
        switch (sort) {
            case "flat":
                return getJdbcTemplate().query(PostQueries.postsFlatSortQuery(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, readPost);
            case "tree":
                return getJdbcTemplate().query(PostQueries.postsTreeSortQuery(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, readPost);
            case "parent_tree":
                return getJdbcTemplate().query(PostQueries.postsParentTreeSortQuery(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, readPost);
            default:
                throw new NullPointerException();
        }
    }

    @Override
    public Integer countPost() {
        return getJdbcTemplate().queryForObject(PostQueries.countPostsQuery, Integer.class);
    }

    @Override
    public void clear() {
        getJdbcTemplate().execute(PostQueries.clearTableQuery);
    }
}
