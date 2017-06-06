package DBPackage.views

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ksg on 11.03.17.
 */

class PostsMarkerView (@param:JsonProperty("marker") var marker: String?,
                       @param:JsonProperty("posts") var posts: List<PostView>?)
