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
        final Integer postsCount = post.countPost();
        final Integer threadsCount = thread.count();
        final Integer usersCount = user.count();
        return ResponseEntity.status(HttpStatus.OK).body(new StatusView(forumsCount, postsCount, threadsCount, usersCount));
    }

    @RequestMapping("/clear")
    public final ResponseEntity<Object> clearService() {
        post.clear();
        thread.clear();
        forum.clear();
        user.clear();
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}