package DBPackage.views

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ksg on 10.03.17.
 */

class UserView(@param:JsonProperty("about") var about: String?,
               @param:JsonProperty("email") var email: String?,
               @param:JsonProperty("fullname") var fullname: String?,
               @param:JsonProperty("nickname") var nickname: String?)