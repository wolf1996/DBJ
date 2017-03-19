package DBPackage.controllers;

/**
 * Created by ksg on 10.03.17.
 */

import DBPackage.models.PostModel;
import DBPackage.services.PostService;
import DBPackage.views.PostDetailsView;
import DBPackage.views.PostView;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post/{id}")
public class PostController {

    private final PostService service;

    public PostController(final PostService service) {
        this.service = service;
    }


    @RequestMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<PostDetailsView> viewForum(
            @RequestParam(value = "related", required = false) String[] related,
            @PathVariable("id") final Integer id
    ) {
        List<PostModel> posts;

        try {
            posts = service.getPostFromDbModel(id);


            if (posts.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(service.getDetailedPostFromDb(posts.get(0), related), HttpStatus.OK);
    }

    @RequestMapping(value = "/details",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<PostView> viewForum(
            @RequestBody final PostView post,
            @PathVariable("id") final Integer id
    ) {
        List<PostView> posts;

        try {
            if (post.getMessage() != null && !post.getMessage().isEmpty()) {
                posts = service.updatePostInDb(post, id);

            } else {
                posts = service.getPostFromDb(id);
            }

            if (posts.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(posts.get(0), HttpStatus.OK);
    }
}