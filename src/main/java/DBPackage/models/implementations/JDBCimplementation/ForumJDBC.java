package DBPackage.models.implementations.JDBCimplementation;

import DBPackage.models.interfaces.Forum;
import DBPackage.views.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ksg on 20.05.17.
 */

@Service
public class ForumJDBC extends BaseJDBC implements Forum{
    static class ForumQueries{
        static final String createForumQuery = "INSERT INTO forums (user_id, slug, title)"+
                " VALUES((SELECT id FROM users WHERE nickname = ?), ?, ?)";
        static final String getForumQuery = "SELECT f.posts, f.slug, f.threads, f.title, u.nickname " +
                "FROM forums f " +
                "  JOIN users u ON (f.user_id = u.id)" +
                "  WHERE f.slug = ?";
        static final String updateThreadsCountQuery = "UPDATE forums SET threads = threads + 1 WHERE slug = ?";
        static final String  getThreadsByForumQuery = "SELECT u.nickname, t.created, f.slug as f_slug, t.id, t.message, t.slug as t_slug, t.title, t.votes " +
                "FROM threads t " +
                "  JOIN users u ON (t.user_id = u.id)" +
                "  JOIN forums f ON (t.forum_id = f.id) " +
                "  WHERE f.slug = ?";
        static final String getUsersByForumQuery = "SELECT u.about, u.email, u.fullname, u.nickname " +
                "FROM users u " +
                "WHERE u.id IN (" +
                "  SELECT user_id " +
                "  FROM forum_users " +
                "  WHERE forum_id = ?" +
                ")";
        static final String countForumsQuery = "SELECT COUNT(*) FROM forums";
        static final String clearTableQuery = "DELETE FROM forums";

    }
    public ForumJDBC(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void create(String username, String slug, String title) {
        getJdbcTemplate().update(ForumQueries.createForumQuery, username, slug, title);
    }

    @Override
    public ForumView getBySlug(String slug) {
        return getJdbcTemplate().queryForObject(ForumQueries.getForumQuery, new Object[]{slug}, readForum);
    }

    @Override
    public List<ThreadView> getAllThreads(String slug, Integer limit, String since, Boolean desc) {
        final StringBuilder sql = new StringBuilder(ForumQueries.getThreadsByForumQuery);
        final List<Object> args = new ArrayList<>();
        args.add(slug);
        if (since != null) {
            sql.append(" AND t.created ");
            sql.append(desc == Boolean.TRUE ? "<= ?" : ">= ?");
            args.add(since);
        }
        sql.append(" ORDER BY t.created");
        sql.append(desc == Boolean.TRUE ? " DESC" : "");
        sql.append(" LIMIT ?");
        args.add(limit);
        return getJdbcTemplate().query(sql.toString(), args.toArray(new Object[args.size()]), readThread);
    }

    @Override
    public List<UserView> getAllUsers(String slug, Integer limit, String since, Boolean desc) {
        final Integer forumId = getJdbcTemplate().queryForObject("SELECT id FROM forums WHERE slug = ?", Integer.class, slug);
        final StringBuilder sql = new StringBuilder(ForumQueries.getUsersByForumQuery);
        final List<Object> args = new ArrayList<>();
        args.add(forumId);
        if (since != null) {
            sql.append(" AND u.nickname ");
            sql.append(desc == Boolean.TRUE ? "< ?" : "> ?");
            args.add(since);
        }
        sql.append(" ORDER BY u.nickname COLLATE ucs_basic");
        sql.append(desc == Boolean.TRUE ? " DESC" : "");
        sql.append(" LIMIT ?");
        args.add(limit);
        return getJdbcTemplate().query(sql.toString(), args.toArray(), readUser);
    }

    @Override
    public Integer countForums() {
        return getJdbcTemplate().queryForObject(ForumQueries.countForumsQuery, Integer.class);
    }

    @Override
    public void clear() {
        getJdbcTemplate().execute(ForumQueries.clearTableQuery);
    }
}
