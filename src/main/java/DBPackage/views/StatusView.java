package DBPackage.views;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ksg on 20.05.17.
 */
public class StatusView {
    private Integer forum;
    private Integer post;
    private Integer thread;
    private Integer user;

    public StatusView(@JsonProperty("forum") final Integer forum,
                      @JsonProperty("post") final Integer post,
                      @JsonProperty("thread") final Integer thread,
                      @JsonProperty("user") final Integer user) {
        this.forum = forum;
        this.post = post;
        this.thread = thread;
        this.user = user;
    }

    public final Integer getForum() {
        return this.forum;
    }

    public void setForum(final Integer forum) {
        this.forum = forum;
    }

    public final Integer getPost() {
        return this.post;
    }

    public void setPost(final Integer post) {
        this.post = post;
    }

    public final Integer getThread() {
        return this.thread;
    }

    public void setThread(final Integer thread) {
        this.thread = thread;
    }

    public final Integer getUser() {
        return this.user;
    }

    public void setUser(final Integer user) {
        this.user = user;
    }
}