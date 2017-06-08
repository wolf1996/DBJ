package DBPackage.models.implementations.JDBCimplementation;

import DBPackage.models.interfaces.Thread;
import DBPackage.views.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ksg on 20.05.17.
 */
@Service
public class ThreadJDBC extends BaseJDBC implements Thread {
    static class ThreadSQL {
        static final String getForumIdSQL = "SELECT forums.id FROM forums " +
                "JOIN threads ON (threads.forum_id = forums.id) " +
                "WHERE threads.id = ?";

        static final String getThreadIdSQL = "SELECT id FROM threads WHERE slug = ?";

        static final String insertThreadTimedSQL = "INSERT INTO threads (user_id, created, forum_id, message, slug, title) " +
                "  VALUES((SELECT id FROM users WHERE nickname = ?), ?, (SELECT id FROM forums WHERE slug = ?), ?, ?, ?) RETURNING id";

        static final String insertThreadNonTimedSQL = "INSERT INTO threads (user_id, forum_id, message, slug, title) " +
                "  VALUES((SELECT id FROM users WHERE nickname = ?), (SELECT id FROM forums WHERE slug = ?), ?, ?, ?) RETURNING id";

        static final String updatePostsCountSQL = "UPDATE forums SET posts = posts + ? WHERE forums.id = ?";


        static String getThreadSQL(final String slug_or_id) {
            return "SELECT u.nickname, t.created, f.slug AS f_slug, t.id, t.message, t.slug AS t_slug, t.title, t.votes " +
                    "FROM threads t " +
                    "  JOIN users u ON (t.user_id = u.id)" +
                    "  JOIN forums f ON (t.forum_id = f.id) " +
                    "  WHERE " + (slug_or_id.matches("\\d+") ? "t.id = ?" : "t.slug = ?");
        }

        static final String countThreadsSQL = "SELECT COUNT(*) FROM threads";

        static final String clearTableSQL = "DELETE FROM threads";
    }

    public ThreadJDBC(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public ThreadView insertThread(final String author, final String created, final String forum,
                             final String message, final String slug, final String title) {
        final Integer threadId;
        if (created != null) {
            threadId = getJdbcTemplate().queryForObject(ThreadSQL.insertThreadTimedSQL,
                    new Object[]{author, created, forum, message, slug, title}, Integer.class);
        } else {
            threadId = getJdbcTemplate().queryForObject(ThreadSQL.insertThreadNonTimedSQL,
                    new Object[]{author, forum, message, slug, title}, Integer.class);
        }
        getJdbcTemplate().update(ForumJDBC.ForumSQL.updateThreadsCountSQL, forum);
        return getJdbcTemplate().queryForObject(ThreadSQL.getThreadSQL(threadId.toString()),
                new Object[]{threadId}, BaseJDBC::readThread);
    }

    @Override
    public void updateThread(final String message, final String title, final String slug_or_id) {
        final StringBuilder sql = new StringBuilder("UPDATE threads SET");
        final List<Object> args = new ArrayList<>();
        if (message != null) {
            sql.append(" message = ?,");
            args.add(message);
        }
        if (title != null) {
            sql.append(" title = ?,");
            args.add(title);
        }
        if (!args.isEmpty()) {
            sql.delete(sql.length() - 1, sql.length());
            sql.append(slug_or_id.matches("\\d+") ? " WHERE id = ?" : " WHERE slug = ?");
            args.add(slug_or_id);
            getJdbcTemplate().update(sql.toString(), args.toArray());
        }
    }

    @Override
    public ThreadView getThread(final String slug_or_id) {
        return getJdbcTemplate().queryForObject(ThreadSQL.getThreadSQL(slug_or_id), new Object[]{slug_or_id}, BaseJDBC::readThread);
    }

    @Override
    public ThreadView updateVotes(final VoteView view, final String slug_or_id) {
        final Integer userId = getJdbcTemplate().queryForObject(UserJDBC.UserSQL.getUserIdSQL, Integer.class, view.getNickname());
        final Integer threadId = slug_or_id.matches("\\d+") ? Integer.valueOf(slug_or_id) :
                getJdbcTemplate().queryForObject(ThreadSQL.getThreadIdSQL, Integer.class, slug_or_id);
        final StringBuilder query = new StringBuilder("SELECT update_or_insert_votes(");
        query.append(userId.toString()).append(", ").append(threadId.toString())
                .append(", ").append(view.getVoice()).append(")");
        getJdbcTemplate().execute(query.toString());
        return getJdbcTemplate().queryForObject(ThreadSQL.getThreadSQL(slug_or_id), new Object[]{slug_or_id}, BaseJDBC::readThread);
    }

    @Override
    public Integer countThreads() {
        return getJdbcTemplate().queryForObject(ThreadSQL.countThreadsSQL, Integer.class);
    }

    @Override
    public void clearThreadsTable() {
        getJdbcTemplate().execute(ThreadSQL.clearTableSQL);
    }
}
