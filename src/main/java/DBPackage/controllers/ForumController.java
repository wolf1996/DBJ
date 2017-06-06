package DBPackage.controllers;

import DBPackage.views.ForumView;
import DBPackage.views.ThreadView;
import DBPackage.views.UserView;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Created by ksg on 10.03.17.
 */

@RestController
@RequestMapping(value = "api/forum")
public final class ForumController extends BaseController {
    @RequestMapping(value = "/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ForumView> createForum(
            @RequestBody final ForumView forum
    ) {
        try {
            this.forum.insertForum(forum.getUser(), forum.getSlug(), forum.getTitle());
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(this.forum.getForum(forum.getSlug()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.forum.getForum(forum.getSlug()));
    }

    @RequestMapping(value = "/{slug}/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadView> createSlug(
            @RequestBody ThreadView thread,
            @PathVariable(value = "slug") final String slug
    ) {
        final String threadSlug = thread.getSlug();
        try {
            thread = this.thread.insertThread(thread.getAuthor(), thread.getCreated(), slug,
                    thread.getMessage(), thread.getSlug(), thread.getTitle());
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(this.thread.getThread(threadSlug));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(thread);
    }

    @RequestMapping(value = "/{slug}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ForumView> viewForum(
            @PathVariable("slug") final String slug
    ) {
        final ForumView forum = this.forum.getForum(slug);
        return ResponseEntity.status(HttpStatus.OK).body(forum);
    }


    @RequestMapping(value = "/{slug}/threads", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<ThreadView>> viewThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "since", required = false) final String since,
            @RequestParam(value = "desc", required = false) final Boolean desc,
            @PathVariable("slug") final String slug
    ) {
        this.forum.getForum(slug);
        return ResponseEntity.status(HttpStatus.OK).body(this.forum.getForumThreads(slug, limit, since, desc));
    }

    @RequestMapping(value = "/{slug}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<UserView>> viewUsers(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "since", required = false) final String since,
            @RequestParam(value = "desc", required = false) final Boolean desc,
            @PathVariable("slug") final String slug
    ) {
        this.forum.getForum(slug);
        return ResponseEntity.status(HttpStatus.OK).body(this.forum.getForumUsers(slug, limit, since, desc));
    }
}