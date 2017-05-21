package DBPackage.views;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by ksg on 11.03.17.
 */

public class PostsMarkerView {
    private List<PostView> posts;
    private String marker;

    @JsonCreator
    public PostsMarkerView(
            @JsonProperty("marker") final String marker,
            @JsonProperty("posts") final List<PostView> posts
    ) {
        this.marker = marker == null ? "some marker" : marker;
        this.posts = posts;
    }

    public PostsMarkerView(
            PostsMarkerView other
    ) {
        this.marker = other.marker;
        this.posts = other.posts;
    }

    public final List<PostView> getPosts() {
        return this.posts;
    }

    public void setPosts(final List<PostView> posts) {
        this.posts = posts;
    }

    public final String getMarker() {
        return this.marker;
    }

    public void setMarker(final String marker) {
        this.marker = marker;
    }
}
