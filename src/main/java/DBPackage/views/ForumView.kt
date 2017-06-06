package DBPackage.views

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ksg on 10.03.17.
 */
class ForumView (@param:JsonProperty("posts") var posts: Int?,
                 @param:JsonProperty("slug") var slug: String?,
                 @param:JsonProperty("threads") var threads: Int?,
                 @param:JsonProperty("title") var title: String?,
                 @param:JsonProperty("user") var user: String?)
