package DBPackage.views;

import DBPackage.models.*;
import DBPackage.models.ForumModel;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ksg on 11.03.17.
 */

public class PostDetailsView {
    private UserModel author;
    private ForumModel forum;
    private PostModel post;
    private ThreadModel thread;

    @JsonCreator
    public PostDetailsView(
            @JsonProperty("author") final UserModel author,
            @JsonProperty("forum") final ForumModel forum,
            @JsonProperty("post") final PostModel post,
            @JsonProperty("thread") final ThreadModel thread
    ) {
        this.author = author;
        this.forum = forum;
        this.post = post;
        this.thread = thread;
    }

    public PostDetailsView(PostDetailsView other) {
        this.author = other.author;
        this.forum = other.forum;
        this.post = other.post;
        this.thread = other.thread;
    }

    public final UserModel getAuthor() {
        return this.author;
    }

    public void setAuthor(final UserModel author) {
        this.author = author;
    }

    public final ForumModel getForum() {
        return this.forum;
    }

    public void setForum(ForumModel forum) {
        this.forum = forum;
    }

    public final PostModel getPost() {
        return this.post;
    }

    public void setPost(PostModel post) {
        this.post = post;
    }

    public final ThreadModel getThread() {
        return this.thread;
    }

    public void setThread(ThreadModel thread) {
        this.thread = thread;
    }
}