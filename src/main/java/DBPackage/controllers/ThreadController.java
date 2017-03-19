package DBPackage.controllers;

import DBPackage.models.*;
import DBPackage.models.ServiceModel;
import DBPackage.views.PostsMarkerView;
import DBPackage.models.VoteModel;
import DBPackage.services.ThreadService;
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
/**
 * Created by ksg on 10.03.17.
 */
@RestController
@RequestMapping(value = "/api/thread/{slug}")
public class ThreadController {
    private final ThreadService service;

    public ThreadController(final ThreadService service) {
        this.service = service;
    }

    @RequestMapping(value = "/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<PostModel>> createPosts(
            @RequestBody final List<PostModel> posts,
            @PathVariable(value = "slug") final String slug
    ) {
        final List<PostModel> dbPosts;

        try {

            if (posts.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

            dbPosts = service.insertPostsIntoDbModel(posts, slug);

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dbPosts, HttpStatus.CREATED);
    }


    @RequestMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> viewThread(
            @PathVariable(value = "slug") final String slug
    ) {
        final List<ThreadModel> threads;

        try {
            threads = service.getThreadInfoModel(slug);

            if (threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(threads.get(0), HttpStatus.OK);
    }


    @RequestMapping(value = "/details",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> updateThread(
            @RequestBody final ThreadModel thread,
            @PathVariable(value = "slug") final String slug
    ) {
        final List<ThreadModel> threads;

        try {
            service.updateThreadInfoFromDb(thread, slug);
            threads = service.getThreadInfoModel(slug);

            if (threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(service.getThreadInfoModel(slug).get(0), HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(threads.get(0), HttpStatus.OK);
    }


    @RequestMapping(value = "/vote",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> voteForThread(
            @RequestBody final VoteModel vote,
            @PathVariable("slug") final String slug
    ) {
        final List<ThreadModel> threads;

        try {
            threads = service.updateVotesModel(vote, slug);

            if (threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(service.getThreadInfoModel(slug).get(0), HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(threads.get(0), HttpStatus.OK);
    }

    private static Integer markerValue = 0;

    @RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<PostsMarkerView> viewThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "marker", required = false) final String marker,
            @RequestParam(value = "sort", required = false, defaultValue = "flat") final String sort,
            @RequestParam(value = "desc", required = false) final Boolean desc,
            @PathVariable("slug") final String slug
    ) {
        final List<PostModel> posts = service.getPostsSortedModel(sort, desc, slug);

        if (posts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        markerValue += marker != null && !Objects.equals(sort, "parent_tree") ? limit : 0;

        if (Objects.equals(sort, "parent_tree")) {

            if (markerValue >= posts.size() && marker != null) {
                markerValue = 0;

                return new ResponseEntity<>(new PostsMarkerView(marker, new ArrayList<>()), HttpStatus.OK);

            } else if (markerValue == posts.size()) {
                markerValue = 0;
            }

            Integer zeroCount = 0, counter = 0;

            for (PostModel post : posts.subList(markerValue, posts.size())) {

                if (zeroCount.equals(limit) && desc == Boolean.TRUE) {
                    break;

                } else if (zeroCount.equals(limit + 1) && (desc == Boolean.FALSE || desc == null)) {
                    --counter;
                    break;
                }

                zeroCount += post.getParent().equals(0) ? 1 : 0;
                ++counter;
            }

            return new ResponseEntity<>(new PostsMarkerView(marker,
                    posts.subList(markerValue, markerValue += counter)), HttpStatus.OK);
        }

        if (markerValue > posts.size()) {
            markerValue = 0;

            return new ResponseEntity<>(new PostsMarkerView(marker, new ArrayList<>()), HttpStatus.OK);
        }

        return new ResponseEntity<>(new PostsMarkerView(marker, posts.subList(markerValue,
                limit + markerValue > posts.size() ? posts.size() : limit + markerValue)), HttpStatus.OK);
    }
}
