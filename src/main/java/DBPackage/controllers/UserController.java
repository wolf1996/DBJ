package DBPackage.controllers;

import DBPackage.views.UserView;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Created by ksg on 10.03.17.
 */

@RestController
@RequestMapping(value = "/api/user/{nickname}")
public final class UserController extends BaseController {
    @RequestMapping(value = "/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<Object> createUser(
            @RequestBody UserView user,
            @PathVariable(value = "nickname") String nickname
    ) {
        try {
            this.user.insertUser(user.getAbout(), user.getEmail(), user.getFullname(), nickname);
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(this.user.getUsers(nickname, user.getEmail()));
        }
        user.setNickname(nickname);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @RequestMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<UserView> viewProfile(
            @PathVariable(value = "nickname") String nickname
    ) {
        final UserView user = this.user.getUser(nickname, null);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @RequestMapping(value = "/profile",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<UserView> modifyProfile(
            @RequestBody UserView user,
            @PathVariable(value = "nickname") String nickname
    ) {
        try {
            this.user.updateUser(user.getAbout(), user.getEmail(), user.getFullname(), nickname);
            user = this.user.getUser(nickname, user.getEmail());
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
