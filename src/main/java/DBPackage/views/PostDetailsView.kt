package DBPackage.views

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ksg on 11.03.17.
 */
class PostDetailsView (@param:JsonProperty("author") var author: UserView?,
                       @param:JsonProperty("forum") var forum: ForumView?,
                       @param:JsonProperty("post") var post: PostView?,
                       @param:JsonProperty("thread") var thread: ThreadView?)