package DBPackage.services;

import DBPackage.models.ServiceModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by ksg on 10.03.17.
 */
@Service
public class ServiceService {
    private final JdbcTemplate jdbcTemplate;

    public ServiceService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final ResponseEntity<Object> serverStatus() {
        final Integer forumsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM forums", Integer.class);
        final Integer postsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts", Integer.class);
        final Integer threadsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM threads", Integer.class);
        final Integer usersCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);

        return new ResponseEntity<>(new ServiceModel(forumsCount, postsCount, threadsCount, usersCount), HttpStatus.OK);
    }

    public final ResponseEntity<Object> clearService() {
        jdbcTemplate.execute("DELETE FROM uservotes");
        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM threads");
        jdbcTemplate.execute("DELETE FROM forums");
        jdbcTemplate.execute("DELETE FROM users");

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
