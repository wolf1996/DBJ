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
    static class UserSQL {
        static final String insertUserSQL = "INSERT INTO users (about, email, fullname, nickname) VALUES(?, ?, ?, ?)";

        static final String getUserSQL = "SELECT * FROM users WHERE nickname = ? OR email = ?";

        static final String getUserIdSQL = "SELECT id FROM users WHERE nickname = ?";

        static final String countUsersSQL = "SELECT COUNT(*) FROM users";

        static final String clearTableSQL = "DELETE FROM users";
    }

    public UserJDBC(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void insertUser(final String about, final String email, final String fullname, final String nickname) {
        getJdbcTemplate().update(UserSQL.insertUserSQL, about, email, fullname, nickname);
    }

    @Override
    public void updateUser(final String about, final String email, final String fullname, final String nickname) {
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
    public UserView getUser(final String nickname, final String email) {
        return getJdbcTemplate().queryForObject(UserSQL.getUserSQL, new Object[]{nickname, email}, BaseJDBC::readUser);
    }

    @Override
    public List<UserView> getUsers(final String nickname, final String email) {
        return getJdbcTemplate().query(UserSQL.getUserSQL, new Object[]{nickname, email}, BaseJDBC::readUser);
    }

    @Override
    public Integer countUsers() {
        return getJdbcTemplate().queryForObject(UserSQL.countUsersSQL, Integer.class);
    }

    @Override
    public void clearUsersTable() {
        getJdbcTemplate().execute(UserSQL.clearTableSQL);
    }
}
