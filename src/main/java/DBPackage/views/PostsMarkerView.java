package DBPackage.views;

import DBPackage.models.PostModel;
import DBPackage.models.PostModel;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by ksg on 11.03.17.
 */

public class PostsMarkerView {
    private List<PostModel> posts;
    private String marker;

    @JsonCreator
    public PostsMarkerView(
            @JsonProperty("marker") final String marker,
            @JsonProperty("posts") final List<PostModel> posts
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

    public final List<PostModel> getPosts() {
        return this.posts;
    }

    public void setPosts(final List<PostModel> posts) {
        this.posts = posts;
    }

    public final String getMarker() {
        return this.marker;
    }

    public void setMarker(final String marker) {
        this.marker = marker;
    }
}
