package DBPackage.controllers;

import DBPackage.models.interfaces.Forum;
import DBPackage.models.interfaces.Post;
import DBPackage.models.interfaces.User;
import DBPackage.models.interfaces.Thread;
import org.springframework.beans.factory.annotation.Autowired;
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
}
