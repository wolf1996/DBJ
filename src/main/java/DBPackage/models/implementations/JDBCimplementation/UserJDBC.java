package DBPackage.models.implementations.JDBCimplementation;

import DBPackage.models.interfaces.User;
import DBPackage.views.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ksg on 20.05.17.
 */
@Service
public class UserJDBC extends BaseJDBC implements User {

    public static class UserQueries {
        public static final String createUserQuery = "INSERT INTO users (about, email, fullname, nickname) VALUES(?, ?, ?, ?)";

        public static final String findUserQuery = "SELECT * FROM users WHERE nickname = ? OR email = ?";

        public static final String findUserIdQuery ="SELECT id FROM users WHERE nickname = ?";

        public static final String updateUserVoteQuery="UPDATE users SET thread_id = ?, voice = ? WHERE nickname = ?";

        public static final String countUsersQuery = "SELECT COUNT(*) FROM users";

        public static final String clearTableQuery= "DELETE FROM users";
    }


    public UserJDBC(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void create(final String about, final String email, final String fullname, final String nickname) {
        getJdbcTemplate().update(UserQueries.createUserQuery, about, email, fullname, nickname);
    }

    @Override
    public void update(final String about, final String email, final String fullname, final String nickname) {
        final StringBuilder sql = new StringBuilder("UPDATE users SET");
        final List<Object> args = new ArrayList<>();
        if (about != null) {
            sql.append(" about = ?,");
            args.add(about);
        }
        if (email != null) {
            sql.append(" email = ?,");
            args.add(email);
        }
        if (fullname != null) {
            sql.append(" fullname = ?,");
            args.add(fullname);
        }
        if (!args.isEmpty()) {
            sql.delete(sql.length() - 1, sql.length());
            sql.append(" WHERE nickname = ?");
            args.add(nickname);
            getJdbcTemplate().update(sql.toString(), args.toArray());
        }
    }

    @Override
    public UserView findSingleByNickOrMail(final String nickname, final String email) {
        return getJdbcTemplate().queryForObject(UserQueries.findUserQuery, new Object[]{nickname, email}, readUser);
    }

    @Override
    public List<UserView> findManyByNickOrMail(final String nickname, final String email) {
        return getJdbcTemplate().query(UserQueries.findUserQuery, new Object[]{nickname, email}, readUser);
    }

    @Override
    public Integer count() {
        return getJdbcTemplate().queryForObject(UserQueries.countUsersQuery, Integer.class);
    }

    @Override
    public void clear() {
        getJdbcTemplate().execute(UserQueries.clearTableQuery);
    }
}
