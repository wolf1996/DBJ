package DBPackage.services;

import DBPackage.models.PostModel;
import DBPackage.models.ThreadModel;
import DBPackage.models.VoteModel;
import DBPackage.models.ServiceModel;
import DBPackage.views.PostView;
import DBPackage.views.ThreadView;
import DBPackage.views.VoteView;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * Created by ksg on 10.03.17.
 */

@Service
public class ThreadService {

    private final JdbcTemplate jdbcTemplate;

    public ThreadService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    private static Integer id = null;
    private static Boolean isNumber = null;

    private final StringBuilder[] makeRequests(final String slug) {
        final StringBuilder insertRequest = new StringBuilder(
                "INSERT INTO posts (author, created, forum, \"message\", thread, parent) ");
        final StringBuilder getRequest = new StringBuilder("SELECT * FROM posts WHERE thread =");
        final StringBuilder updateRequest = new StringBuilder("UPDATE forums SET posts = posts + ?" +
                " WHERE forums.slug = (SELECT threads.forum FROM threads WHERE");
        final StringBuilder checkRequest = new StringBuilder("SELECT posts.id FROM posts WHERE " +
                "posts.thread = ");

        try {
            id = Integer.valueOf(slug);
            isNumber = Boolean.TRUE;
            insertRequest.append("VALUES(?, ?, (SELECT forum FROM threads WHERE id = ?), ?, ?, ?)");
            getRequest.append(" ?");
            updateRequest.append(" threads.id = ?)");
            checkRequest.append(" ?");

        } catch (NumberFormatException ex) {
            isNumber = Boolean.FALSE;
            insertRequest.append("VALUES(?, ?, (SELECT forum FROM threads WHERE LOWER(slug) = LOWER(?)), ?," +
                    "(SELECT id FROM threads WHERE LOWER(slug) = LOWER(?)), ?)");
            getRequest.append(" (SELECT id FROM threads WHERE LOWER(slug) = LOWER(?))");
            updateRequest.append(" threads.slug = ?)");
            checkRequest.append(" (SELECT threads.id FROM threads WHERE LOWER(threads.slug) = LOWER(?))");
        }

        getRequest.append(" ORDER BY posts.id");

        return new StringBuilder[]{insertRequest, getRequest, updateRequest, checkRequest};
    }

    public final List<PostModel> insertPostsIntoDbModel(final List<PostModel> posts, final String slug) {
        final StringBuilder[] requests = makeRequests(slug);

        for (PostModel post : posts) {

            if (post.getParent() != 0) {
                final List<Integer> dBPosts = jdbcTemplate.queryForList(
                        requests[3].toString(),
                        Integer.class,
                        isNumber ? id : slug
                );

                if (!dBPosts.contains(post.getParent())) {
                    throw new DuplicateKeyException(null);
                }
            }

            if (post.getCreated() == null) {
                post.setCreated(LocalDateTime.now().toString());
            }

            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(post.getCreated(), DateTimeFormatter.ISO_DATE_TIME));

            if (!post.getCreated().endsWith("Z")) {
                timestamp = Timestamp.from(timestamp.toInstant().plusSeconds(-10800));
            }

            jdbcTemplate.update(requests[0].toString(), post.getAuthor(), timestamp, isNumber ? id : slug,
                    post.getMessage(), isNumber ? id : slug, post.getParent());
        }

        jdbcTemplate.update(requests[2].toString(), posts.size(), isNumber ? id : slug);

        final List<PostModel> dbPosts = jdbcTemplate.query(requests[1].toString(),
                isNumber ? new Object[]{id} : new Object[]{slug}, PostService::read);
        final Integer beginIndex = dbPosts.size() - posts.size();
        final Integer endIndex = dbPosts.size();

        return dbPosts.subList(beginIndex, endIndex);
    }
    public final List<PostView> insertPostsIntoDb(final List<PostView> posts, final String slug) {
        List<PostModel>posts_models = new ArrayList<PostModel>();
        for(PostView i: posts){
            posts_models.add(new PostModel(i));
        }
        final List<PostModel> model = insertPostsIntoDbModel(posts_models,slug);
        List<PostView>view = new ArrayList<PostView>();
        for(PostModel i:model){
            view.add(new PostView(i));
        }
        return view;
    }

    public final List<ThreadModel> getThreadInfoModel(final String slug) {
        final StringBuilder sql = new StringBuilder("SELECT * FROM threads WHERE ");
        final Integer id;

        try {
            id  = Integer.valueOf(slug);

        } catch (NumberFormatException ex) {
            return jdbcTemplate.query(sql.append("LOWER(slug) = LOWER(?)").toString(),
                    new Object[]{slug}, ThreadService::read);
        }

        return jdbcTemplate.query(sql.append("id = ?").toString(),
                new Object[]{id}, ThreadService::read);
    }

    public final List<ThreadView> getThreadInfo(final String slug) {
        List<ThreadModel> model = getThreadInfoModel(slug);
        List<ThreadView> view = new ArrayList<ThreadView>();
        for(ThreadModel i:model){
            view.add(new ThreadView(i));
        }
        return view;
    }

    public final void updateThreadInfoFromDb(final ThreadModel thread, final String slug) {
        final StringBuilder sql = new StringBuilder("UPDATE threads SET");
        final List<Object> args = new ArrayList<>();

        if (thread.getMessage() != null && !thread.getMessage().isEmpty()) {
            sql.append(" message = ?,");
            args.add(thread.getMessage());
        }

        if (thread.getTitle() != null && !thread.getTitle().isEmpty()) {
            sql.append(" title = ?,");
            args.add(thread.getTitle());
        }

        if (args.isEmpty()) {
            return;
        }

        sql.delete(sql.length() - 1, sql.length());
        final Integer id;

        try {
            id = Integer.valueOf(slug);
            sql.append(" WHERE id = ?");
            args.add(id);

        } catch (NumberFormatException ex) {
            sql.append(" WHERE LOWER(slug) = LOWER(?)");
            args.add(slug);
        }

        jdbcTemplate.update(sql.toString(), args.toArray());
    }


    private void getUserVotes(final VoteModel vote) {
        final List<VoteModel> usersList = jdbcTemplate.query("SELECT * FROM uservotes " +
                        "WHERE LOWER(nickname) = LOWER(?)",
                new Object[]{vote.getNickname()}, (rs, rowNum) ->
                        new VoteModel(rs.getString("nickname"), rs.getInt("voice")));
        final Map<String, Integer> usersMap = new LinkedHashMap<>();

        for (VoteModel user : usersList) {
            usersMap.put(user.getNickname(), user.getVoice());
        }

        if (usersMap.containsKey(vote.getNickname())) {

            if (usersMap.get(vote.getNickname()) < 0 && vote.getVoice() < 0) {
                vote.setVoice(0);

            } else if (usersMap.get(vote.getNickname()) < 0 && vote.getVoice() > 0) {
                vote.setVoice(2);

            } else if (usersMap.get(vote.getNickname()) > 0 && vote.getVoice() < 0) {
                vote.setVoice(-2);

            } else {
                vote.setVoice(0);
            }

            jdbcTemplate.update("UPDATE uservotes SET voice = voice + ? " +
                    "WHERE LOWER(nickname) = LOWER(?)", vote.getVoice(), vote.getNickname());

        } else {
            jdbcTemplate.update("INSERT INTO uservotes (nickname, voice) VALUES(?, ?)",
                    vote.getNickname(), vote.getVoice());
        }
    }

    public final List<ThreadView> updateVotes(final VoteView vote, final String slug){
        final List<ThreadModel> model = updateVotesModel(new VoteModel(vote),slug);
        List<ThreadView>view = new ArrayList<ThreadView>();
        for(ThreadModel i:model){
            view.add(new ThreadView(i));
        }
        return view;
    }

    public final List<ThreadModel> updateVotesModel(final VoteModel vote, final String slug) {
        final StringBuilder sql = new StringBuilder("UPDATE threads SET votes = votes + ? WHERE ");
        final List<Object> args = new ArrayList<>();
        final Integer id;
        getUserVotes(vote);
        args.add(vote.getVoice());

        try {
            id = Integer.valueOf(slug);

        } catch (NumberFormatException ex) {
            args.add(slug);
            jdbcTemplate.update(sql.append("LOWER(slug) = LOWER(?)").toString(), args.toArray());

            return jdbcTemplate.query("SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)",
                    new Object[]{slug}, ThreadService::read);
        }

        args.add(id);
        jdbcTemplate.update(sql.append("id = ?").toString(), args.toArray());

        return jdbcTemplate.query("SELECT * FROM threads WHERE id = ?",
                new Object[]{id}, ThreadService::read);
    }


    public final List<ThreadModel> getThreadInfoByIdModel(final Integer id) {
        return jdbcTemplate.query(
                "SELECT * FROM threads WHERE id = ?",
                new Object[]{id},
                ThreadService::read
        );
    }


    public final List<ThreadView> getThreadInfoById(final Integer id) {
        final List<ThreadModel> model = getThreadInfoByIdModel(id);
        List<ThreadView>view = new ArrayList<ThreadView>();
        for(ThreadModel i:model){
            view.add(new ThreadView(i));
        }
        return view;
    }

    public final List<PostView> getPostsSorted(
            final String sort,
            final Boolean desc,
            final String slug
    ){
        final List<PostModel> model = getPostsSortedModel(sort, desc, slug);
        List<PostView>view = new ArrayList<PostView>();
        for(PostModel i:model){
            view.add(new PostView(i));
        }
        return view;
    }

    public final List<PostModel> getPostsSortedModel(
            final String sort,
            final Boolean desc,
            final String slug
    ) {
        final String recurseTemplate = " tree AS (SELECT *, array[id] AS path FROM some_threads WHERE parent = 0 " +
                "UNION SELECT st.*, tree.path || st.id AS path FROM tree JOIN some_threads st ON st.parent = tree.id) " +
                "SELECT * FROM tree ORDER BY path";
        final StringBuilder sql = new StringBuilder();
        Integer id = null;
        Boolean isNumber = false;

        try {
            final String sqlTemplate = "SELECT * FROM posts WHERE posts.thread = " +
                    "(SELECT threads.id FROM threads WHERE threads.id = ?)";
            id = Integer.valueOf(slug);
            isNumber = Boolean.TRUE;

            if (Objects.equals(sort, "flat")) {
                sql.append(sqlTemplate + " ORDER BY posts.created");

            } else {
                sql.append("WITH RECURSIVE some_threads AS (" + sqlTemplate + "), " + recurseTemplate);
            }

        } catch (NumberFormatException ex) {
            final String sqlTemplate = "SELECT * FROM posts WHERE posts.thread = " +
                    "(SELECT threads.id FROM threads WHERE LOWER(threads.slug) = LOWER(?))";

            if (Objects.equals(sort, "flat")) {
                sql.append(sqlTemplate + " ORDER BY posts.created");

            } else {
                sql.append("WITH RECURSIVE some_threads AS (" + sqlTemplate + "), " + recurseTemplate);
            }
        }

        if (desc == Boolean.TRUE) {
            sql.append(" DESC");
        }

        return jdbcTemplate.query(
                sql.toString(),
                isNumber ? new Object[]{id} : new Object[]{slug},
                PostService::read
        );
    }


    public static ThreadModel read(ResultSet rs, int rowNum) throws SQLException {
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));

        return new ThreadModel(
                rs.getString("author"),
                dateFormat.format(timestamp),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getString("message"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getInt("votes")
        );
    }
}
