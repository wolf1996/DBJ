package DBPackage.views

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ksg on 20.05.17.
 */
class StatusView(@param:JsonProperty("forum") var forum: Int?,
                 @param:JsonProperty("post") var post: Int?,
                 @param:JsonProperty("thread") var thread: Int?,
                 @param:JsonProperty("user") var user: Int?)