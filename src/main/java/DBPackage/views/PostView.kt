package DBPackage.views

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ksg on 10.03.17.
 */
class PostView {
    var author: String? = null
    var created: String? = null
    var forum: String? = null
    var id: Int? = null
    @JsonProperty("isEdited")
    var isEdited: Boolean? = null
    var message: String? = null
    var parent: Int? = null
    var thread: Int? = null

    @JsonCreator
    constructor(
            @JsonProperty("author") author: String?,
            @JsonProperty("created") created: String?,
            @JsonProperty("forum") forum: String?,
            @JsonProperty("id") id: Int?,
            @JsonProperty("isEdited") isEdited: Boolean?,
            @JsonProperty("message") message: String?,
            @JsonProperty("parent") parent: Int?,
            @JsonProperty("thread") thread: Int?
    ) {
        this.author = author
        this.created = created
        this.forum = forum
        this.id = id
        this.isEdited = isEdited
        this.message = message
        this.parent = parent ?: 0
        this.thread = thread
    }
}