package DBPackage.services;

import DBPackage.models.PostModel;
import DBPackage.models.UserModel;
import DBPackage.models.UserModel;
import DBPackage.views.PostView;
import DBPackage.views.UserView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ksg on 10.03.17.
 */
@Service
public final class UserService {
    private final JdbcTemplate jdbcTemplate;

    public UserService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public final void insertUserIntoDb(final UserModel user) {
        final String sql = "INSERT INTO users (about, email, fullname, nickname) " +
                "VALUES(?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getAbout(), user.getEmail(), user.getFullname(),
                user.getNickname());
    }

    public final List<UserModel> getUserFromDbModel(final UserModel user) {
        return jdbcTemplate.query(
                "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)" +
                        " OR LOWER(email) = LOWER(?)",
                new Object[]{user.getNickname(), user.getEmail()},
                UserService::read);
    }

    public final List<UserView> getUserFromDb(final UserModel user) {
        final List<UserModel> model = getUserFromDbModel(user);
        List<UserView>view = new ArrayList<UserView>();
        for(UserModel i:model){
            view.add(new UserView(i));
        }
        return view;
    }


    public final void updateUserInfoFromDb(final UserModel user) {
        final StringBuilder sql = new StringBuilder("UPDATE users SET");
        final List<Object> args = new ArrayList<>();

        if (user.getAbout() != null && !user.getAbout().isEmpty()) {
            sql.append(" about = ?,");
            args.add(user.getAbout());
        }

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            sql.append(" email = ?,");
            args.add(user.getEmail());
        }

        if (user.getFullname() != null && !user.getFullname().isEmpty()) {
            sql.append(" fullname = ?,");
            args.add(user.getFullname());
        }

        if (args.isEmpty()) {
            return;
        }

        sql.delete(sql.length() - 1, sql.length());
        sql.append(" WHERE LOWER(nickname) = LOWER(?)");
        args.add(user.getNickname());
        jdbcTemplate.update(sql.toString(), args.toArray());
    }


    public static UserModel read(ResultSet rs, int rowNum) throws SQLException {
        return new UserModel(
                rs.getString("about"),
                rs.getString("email"),
                rs.getString("fullname"),
                rs.getString("nickname")
        );
    }
}