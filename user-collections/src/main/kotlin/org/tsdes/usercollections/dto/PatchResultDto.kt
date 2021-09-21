package org.tsdes.usercollections.dto

import io.swagger.annotations.ApiModelProperty

class PatchResultDto(
    @get:ApiModelProperty("If a card pack was opened, spoecify which cards where in it")
    var cardIdsInOpenedPack: MutableList<String> = mutableListOf()
)