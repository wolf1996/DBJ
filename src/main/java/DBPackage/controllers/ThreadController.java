package DBPackage.controllers;

import DBPackage.views.PostView;
import DBPackage.views.PostsMarkerView;
import DBPackage.views.ThreadView;
import DBPackage.views.VoteView;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Created by ksg on 10.03.17.
 */
@RestController
@RequestMapping(value = "/api/thread/{slug}")
public class ThreadController extends BaseController{

    @RequestMapping(value = "/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final CompletableFuture<ResponseEntity<List<PostView>>> createPosts(
            @RequestBody final List<PostView> posts,
            @PathVariable(value = "slug") final String slug
    ) {
        final CompletableFuture<ResponseEntity<List<PostView>>> ent = CompletableFuture.
                supplyAsync
                        (() -> {
                            try {
                                ThreadView thread = this.thread.getByIdOrSlug(slug);
                                if (posts.isEmpty()) {
                                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                                }
                                for (PostView post : posts) {
                                    if (post.getParent() != 0) {
                                        try {
                                            PostView parent = this.post.getById(post.getParent());
                                            post.setForum(thread.getForum());
                                            post.setThread(thread.getId());
                                            if (!thread.getId().equals(parent.getThread())) {
                                                return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
                                            }
                                        } catch (EmptyResultDataAccessException ex) {
                                            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
                                        }
                                    }
                                    post.setForum(thread.getForum());
                                    post.setThread(thread.getId());
                                }
                                post.create(posts, slug);
                            } catch (DuplicateKeyException ex) {
                                return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
                            } catch (DataAccessException ex) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                            }
                            return ResponseEntity.status(HttpStatus.CREATED).body(posts);
                        });
        return ent;
    }


    @RequestMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadView> viewThread(
            @PathVariable(value = "slug") final String slug
    ) {
        final ThreadView thread;
        try {
            thread = this.thread.getByIdOrSlug(slug);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }


    @RequestMapping(value = "/details",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadView> updateThread(
            @RequestBody ThreadView thread,
            @PathVariable(value = "slug") final String slug
    ) {
        try {
            this.thread.update(thread.getMessage(), thread.getTitle(), slug);
            thread = this.thread.getByIdOrSlug(slug);
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(this.thread.getByIdOrSlug(slug));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }


    @RequestMapping(value = "/vote",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadView> voteForThread(
            @RequestBody final VoteView vote,
            @PathVariable("slug") final String slug
    ) {
        final ThreadView thread;
        try {
            thread = this.thread.updateVotes(vote, slug);
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(this.thread.getByIdOrSlug(slug));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    private static Integer markerValue = 0;

    @RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<PostsMarkerView> viewThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "marker", required = false) String marker,
            @RequestParam(value = "sort", required = false, defaultValue = "flat") final String sort,
            @RequestParam(value = "desc", required = false, defaultValue = "false") final Boolean desc,
            @PathVariable("slug") final String slug
    ) {
        if (marker == null) {
            marker = "0";
        }
        final List<PostView> posts = this.post.sort(limit, Integer.parseInt(marker), sort, desc, slug);
        if (posts.isEmpty() && marker.equals("0")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new PostsMarkerView(
                !posts.isEmpty() ? String.valueOf(Integer.parseInt(marker) + limit) : marker, posts));
    }
}
