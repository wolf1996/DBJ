package DBPackage.services;

import DBPackage.models.ForumModel;
import DBPackage.models.ServiceModel;
import DBPackage.models.ThreadModel;
import DBPackage.models.UserModel;
import DBPackage.views.ForumView;
import DBPackage.views.ThreadView;
import DBPackage.views.UserView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ksg on 10.03.17.
 */

@Service
final public class ForumService {

    private final JdbcTemplate jdbcTemplate;

    public ForumService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public final int insertForumIntoDb(final ForumView forum) {
        return jdbcTemplate.update("INSERT INTO forums (slug, title, \"user\") VALUES(?, ?, " +
                        "(SELECT nickname FROM users WHERE LOWER(nickname) = LOWER(?)))",
                forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public final List<ThreadView> insertThreadIntoDb(final ThreadView thread) {
        // todo normilize time
        if (thread.getCreated() == null) {
            thread.setCreated(LocalDateTime.now().toString());
        }

        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME));

        if (!thread.getCreated().endsWith("Z")) {
            timestamp = Timestamp.from(timestamp.toInstant().plusSeconds(-10800));
        }

        final String sql = "INSERT INTO threads (author, created, forum, \"message\", " +
                "slug, title) VALUES(?, ?, " +
                "(SELECT slug FROM forums WHERE LOWER(slug) = LOWER(?)), " +
                "?, ?, ?)";

        jdbcTemplate.update(sql, thread.getAuthor(), timestamp, thread.getForum(),
                thread.getMessage(), thread.getSlug(), thread.getTitle()
        );

        jdbcTemplate.update("UPDATE forums SET threads = threads + 1 WHERE LOWER(slug) = LOWER(?)",
                thread.getForum());

        List<ThreadModel> model = jdbcTemplate.query("SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)",
                                            new Object[]{thread.getSlug()}, ThreadService::read);
        List<ThreadView> view = new ArrayList<ThreadView>();
        for(ThreadModel i:model){
            view.add(new ThreadView(i));
        }
        return view;
    }

    public final List<ForumModel> getForumInfoModel(final String slug) {
        return jdbcTemplate.query("SELECT * FROM forums WHERE LOWER(slug) = LOWER(?)",
                new Object[]{slug}, ForumService::read
        );
    }

    public final List<ForumView> getForumInfo(final String slug) {
        List<ForumModel> model = getForumInfoModel(slug);
        List<ForumView> view = new ArrayList<ForumView>();
        for(ForumModel i: model){
            view.add(new ForumView(i));
        }
        return view;
    }

    public final List<ThreadModel> getThreadInfoModel(final String slug) {
        return jdbcTemplate.query("SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)",
                new Object[]{slug}, ThreadService::read);
    }


    public final List<ThreadView> getThreadInfo(final String slug) {
        List<ThreadModel> model = getThreadInfoModel(slug);
        List<ThreadView> view = new ArrayList<ThreadView>();
        for(ThreadModel i: model){
            view.add(new ThreadView(i));
        }
        return view;
    }

    public final List<ThreadView> getThreadsInfo(
            final String slug,
            final Integer limit,
            final String since,
            final Boolean desc
    ) {
        StringBuilder sql = new StringBuilder("SELECT * FROM threads WHERE LOWER(forum) = LOWER(?)");
        final List<Object> args = new ArrayList<>();
        args.add(slug);

        if (since != null) {
            sql.append(" AND created ");

            if (desc == Boolean.TRUE) {
                sql.append("<= ?");

            } else {
                sql.append(">= ?");
            }

            args.add(Timestamp.valueOf(LocalDateTime.parse(since, DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        }

        sql.append(" ORDER BY created");

        if (desc == Boolean.TRUE) {
            sql.append(" DESC");
        }

        sql.append(" LIMIT ?");
        args.add(limit);

        List<ThreadModel> model = jdbcTemplate.query( sql.toString(), args.toArray(new Object[args.size()]),
                                                                                        ThreadService::read);
        List<ThreadView> view = new ArrayList<ThreadView>();
        for(ThreadModel i: model){
            view.add(new ThreadView(i));
        }

        return view;
    }


    public final List<UserModel> getUsersInfoModel(
            final String slug,
            final Integer limit,
            final String since,
            final Boolean desc
    ) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE LOWER(users.nickname) IN " +
                "(SELECT LOWER(posts.author) FROM posts WHERE LOWER(posts.forum) = LOWER(?) " +
                "UNION " +
                "SELECT LOWER(threads.author) FROM threads WHERE LOWER(threads.forum) = LOWER(?))");
        final List<Object> args = new ArrayList<>();
        args.add(slug);
        args.add(slug);

        if (since != null) {
            sql.append(" AND LOWER(users.nickname) ");

            if (desc == Boolean.TRUE) {
                sql.append("< LOWER(?)");

            } else {
                sql.append("> LOWER(?)");
            }

            args.add(since);
        }

        sql.append(" ORDER BY LOWER(users.nickname) COLLATE ucs_basic");

        if (desc == Boolean.TRUE) {
            sql.append(" DESC");
        }

        sql.append(" LIMIT ?");
        args.add(limit);

         List<UserModel> model = jdbcTemplate.query(sql.toString(), args.toArray(new Object[args.size()]),
                                                                    UserService::read);
        return model;
    }

    public final List<UserView> getUsersInfo(
            final String slug,
            final Integer limit,
            final String since,
            final Boolean desc
    ) {
        List<UserModel> model = getUsersInfoModel(slug,limit,since,desc);
        List<UserView> view = new ArrayList<UserView>();
        for(UserModel i:model){
            view.add(new UserView(i));
        }
        return view;
    }


    public static ForumModel read(ResultSet rs, int rowNum) throws SQLException {
        return new ForumModel(
                rs.getInt("posts"),
                rs.getString("slug"),
                rs.getInt("threads"),
                rs.getString("title"),
                rs.getString("user")
        );
    }
}
