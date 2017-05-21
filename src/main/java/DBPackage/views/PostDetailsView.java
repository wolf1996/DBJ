package DBPackage.views;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ksg on 11.03.17.
 */

public class PostDetailsView {
    private UserView author;
    private ForumView forum;
    private PostView post;
    private ThreadView thread;

    @JsonCreator
    public PostDetailsView(
            @JsonProperty("author") final UserView author,
            @JsonProperty("forum") final ForumView forum,
            @JsonProperty("post") final PostView post,
            @JsonProperty("thread") final ThreadView thread
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

    public final UserView getAuthor() {
        return this.author;
    }

    public void setAuthor(final UserView author) {
        this.author = author;
    }

    public final ForumView getForum() {
        return this.forum;
    }

    public void setForum(ForumView forum) {
        this.forum = forum;
    }

    public final PostView getPost() {
        return this.post;
    }

    public void setPost(PostView post) {
        this.post = post;
    }

    public final ThreadView getThread() {
        return this.thread;
    }

    public void setThread(ThreadView thread) {
        this.thread = thread;
    }
}