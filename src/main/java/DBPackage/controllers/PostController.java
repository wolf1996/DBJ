package DBPackage.controllers;

/**
 * Created by ksg on 10.03.17.
 */

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
public class PostController extends BaseController {
    @RequestMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<PostDetailsView> viewForum(
            @RequestParam(value = "related", required = false) String[] related,
            @PathVariable("id") final Integer id
    ) {
        final PostDetailsView post = this.post.getPostDetailed(id, related);
        return ResponseEntity.status(HttpStatus.OK).body(post);
    }

    @RequestMapping(value = "/details",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<PostView> viewForum(
            @RequestBody PostView post,
            @PathVariable("id") final Integer id
    ) {
        post = post.getMessage() != null ? this.post.updatePost(post.getMessage(), id) : this.post.getPostById(id);
        return ResponseEntity.status(HttpStatus.OK).body(post);
    }
}