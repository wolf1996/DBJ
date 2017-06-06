package DBPackage.controllers;

import DBPackage.views.StatusView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ksg on 10.03.17.
 */
@RestController
@RequestMapping("/api/service")
class ServiceController extends BaseController {
    @RequestMapping("/status")
    public final ResponseEntity<Object> serverStatus() {
        final Integer forumsCount = forum.countForums();
        final Integer postsCount = post.countPosts();
        final Integer threadsCount = thread.countThreads();
        final Integer usersCount = user.countUsers();
        return ResponseEntity.status(HttpStatus.OK).body(new StatusView(forumsCount, postsCount, threadsCount, usersCount));
    }

    @RequestMapping("/clear")
    public final ResponseEntity<Object> clearService() {
        post.clearPostsTable();
        thread.clearThreadsTable();
        forum.clearForumsTable();
        user.clearUsersTable();
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}