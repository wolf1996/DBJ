package DBPackage.views

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ksg on 10.03.17.
 */
class VoteView(@param:JsonProperty("nickname") var nickname: String?,
               @param:JsonProperty("voice") var voice: Int?)
