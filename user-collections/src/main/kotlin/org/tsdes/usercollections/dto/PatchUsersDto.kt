package org.tsdes.usercollections.dto

import io.swagger.annotations.ApiModelProperty

enum class Command {
    BUY_CARD, MILL_CARD, OPEN_PACK
}

class PatchUsersDto (

    @get:ApiModelProperty("Command to execute on a user's collection")
    var command: Command? = null,

    @get:ApiModelProperty("Optional card id, if a the command requires one")
    var cardId: String? = null
        )