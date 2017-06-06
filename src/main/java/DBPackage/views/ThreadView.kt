package DBPackage.views

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ksg on 10.03.17.
 */
class ThreadView(@param:JsonProperty("author") var author: String?,
                 @param:JsonProperty("created") var created: String?,
                 @param:JsonProperty("forum") var forum: String?,
                 @param:JsonProperty("id") var id: Int?,
                 @param:JsonProperty("message") var message: String?,
                 @param:JsonProperty("slug") var slug: String?,
                 @param:JsonProperty("title") var title: String?,
                 @param:JsonProperty("votes") var votes: Int?)