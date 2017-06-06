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
    static class PostSQL {
        static String insertPostSQL = "INSERT INTO posts (user_id, created, forum_id, id, message, parent, thread_id, path, root_id) " +
                "VALUES((SELECT id FROM users WHERE nickname = ?), ?, ?, ?, ?, ?, ?, array_append(?, ?), ?)";

        static String getPostSQL = "SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id " +
                "FROM posts p" +
                "  JOIN users u ON (u.id = p.user_id)" +
                "  JOIN forums f ON (f.id = p.forum_id) " +
                "WHERE p.id = ?";

        static String getFlatSortedPostsSQL(final String slug_or_id, final Boolean desc) {
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

        static String getTreeSortedPostsSQL(final String slug_or_id, final Boolean desc) {
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

        static String getParentTreeSortedPostsSQL(final String slug_or_id, final Boolean desc) {
            return "SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id " +
                    "FROM posts p" +
                    "  JOIN users u ON (u.id = p.user_id)" +
                    "  JOIN forums f ON (f.id = p.forum_id) " +
                    "WHERE p.root_id IN (" +
                    "  SELECT id" +
                    "  FROM posts" +
                    "  WHERE thread_id = " +
                    (slug_or_id.matches("\\d+") ?
                            "?" : "(SELECT threads.id FROM threads WHERE threads.slug = ?)"
                    ) +
                    "  AND parent = 0 " +
                    "  ORDER BY id " + (desc ? "DESC" : "ASC") +
                    "  LIMIT ? OFFSET ?) " +
                    "ORDER BY path " + (desc ? "DESC" : "ASC");
        }

        static String countPostsSQL = "SELECT COUNT(*) FROM posts";

        static String clearTableSQL = "DELETE FROM posts";
    }


    public PostJDBC(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void insertPostsPack(final List<PostView> posts, final String slug_or_id) {
        final Integer threadId = slug_or_id.matches("\\d+") ? Integer.valueOf(slug_or_id) :
                getJdbcTemplate().queryForObject(ThreadJDBC.ThreadSQL.getThreadIdSQL, Integer.class, slug_or_id);
        final Integer forumId = getJdbcTemplate().queryForObject(ThreadJDBC.ThreadSQL.getForumIdSQL, Integer.class, threadId);
        final Timestamp created = new Timestamp(System.currentTimeMillis());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try (Connection connection = getJdbcTemplate().getDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(PostSQL.insertPostSQL, Statement.NO_GENERATED_KEYS);
            for (PostView post : posts) {
                final Integer postId = getJdbcTemplate().queryForObject("SELECT nextval('posts_id_seq')", Integer.class);
                preparedStatement.setString(1, post.getAuthor());
                preparedStatement.setTimestamp(2, created);
                preparedStatement.setInt(3, forumId);
                preparedStatement.setInt(4, postId);
                preparedStatement.setString(5, post.getMessage());
                preparedStatement.setInt(6, post.getParent());
                preparedStatement.setInt(7, threadId);
                preparedStatement.setInt(9, postId);
                if (post.getParent() == 0) {
                    preparedStatement.setArray(8, null);
                    preparedStatement.setInt(10, postId);
                } else {
                    final Array path = getJdbcTemplate().queryForObject("SELECT path FROM posts WHERE id = ?", Array.class, post.getParent());
                    preparedStatement.setArray(8, path);
                    preparedStatement.setInt(10, ((Integer[]) path.getArray())[0]);
                }
                preparedStatement.addBatch();
                post.setCreated(dateFormat.format(created));
                post.setId(postId);
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
        } catch (SQLException ex) {
            throw new DataRetrievalFailureException(null);
        }
        getJdbcTemplate().update(ThreadJDBC.ThreadSQL.updatePostsCountSQL, posts.size(), forumId);
    }

    @Override
    public PostView updatePost(final String message, final Integer id) {
        final PostView post = getPostById(id);
        final StringBuilder sql = new StringBuilder("UPDATE posts SET message = ?");
        if (!message.equals(post.getMessage())) {
            sql.append(", is_edited = TRUE");
            post.setEdited(true);
            post.setMessage(message);
        }
        sql.append(" WHERE id = ?");
        getJdbcTemplate().update(sql.toString(), message, id);
        return post;
    }

    @Override
    public final PostView getPostById(final Integer id) {
        return getJdbcTemplate().queryForObject(PostSQL.getPostSQL, new Object[]{id}, BaseJDBC::readPost);
    }

    @Override
    public PostDetailsView getPostDetailed(final Integer id, final String[] related) {
        final PostView post = getPostById(id);
        UserView user = null;
        ForumView forum = null;
        ThreadView thread = null;
        if (related != null) {
            for (String relation : related) {
                switch (relation) {
                    case "user":
                        user = getJdbcTemplate().queryForObject(UserJDBC.UserSQL.getUserSQL,
                                new Object[]{post.getAuthor(), null}, BaseJDBC::readUser);
                        break;
                    case "forum":
                        forum = getJdbcTemplate().queryForObject(ForumJDBC.ForumSQL.getForumSQL,
                                new Object[]{post.getForum()}, BaseJDBC::readForum);
                        break;
                    case "thread":
                        thread = getJdbcTemplate().queryForObject(ThreadJDBC.ThreadSQL.getThreadSQL(String.valueOf(post.getThread())),
                                new Object[]{post.getThread()}, BaseJDBC::readThread);
                }
            }
        }
        return new PostDetailsView(user, forum, post, thread);
    }

    @Override
    public List<PostView> sortPosts(final Integer limit, final Integer offset, final String sort,
                                    final Boolean desc, final String slug_or_id) {
        switch (sort) {
            case "flat":
                return getJdbcTemplate().query(PostSQL.getFlatSortedPostsSQL(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, BaseJDBC::readPost);
            case "tree":
                return getJdbcTemplate().query(PostSQL.getTreeSortedPostsSQL(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, BaseJDBC::readPost);
            case "parent_tree":
                return getJdbcTemplate().query(PostSQL.getParentTreeSortedPostsSQL(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, BaseJDBC::readPost);
            default:
                throw new NullPointerException();
        }
    }

    @Override
    public Integer countPosts() {
        return getJdbcTemplate().queryForObject(PostSQL.countPostsSQL, Integer.class);
    }

    @Override
    public void clearPostsTable() {
        getJdbcTemplate().execute(PostSQL.clearTableSQL);
    }
}
