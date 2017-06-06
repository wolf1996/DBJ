package DBPackage.controllers;

import DBPackage.models.interfaces.Forum;
import DBPackage.models.interfaces.Post;
import DBPackage.models.interfaces.User;
import DBPackage.models.interfaces.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ksg on 20.05.17.
 */

@RestController
public class BaseController {
    @Autowired
    protected Forum forum;
    @Autowired
    protected User user;
    @Autowired
    protected Thread thread;
    @Autowired
    protected Post post;

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public void handleDataAccessException(DataAccessException e) {

    }
}
