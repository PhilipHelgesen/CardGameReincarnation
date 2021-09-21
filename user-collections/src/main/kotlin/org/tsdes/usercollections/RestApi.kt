package org.tsdes.usercollections

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.tsdes.cards.dto.UserDto
import org.tsdes.usercollections.db.UserService
import org.tsdes.usercollections.dto.Command
import org.tsdes.usercollections.dto.PatchResultDto
import org.tsdes.usercollections.dto.PatchUsersDto

@Api(value = "/api/user-collection", description = "API for the user-collections")
@RequestMapping(
    "/api/user-collections",
    produces = [(MediaType.APPLICATION_JSON_VALUE)]
)
@RestController
class RestApi(private val userService: UserService) {
    @ApiOperation("get a user with a certain id")
    @GetMapping("/{userId}")
    fun getUserInfo(@PathVariable("userId") userId: String): ResponseEntity<UserDto> {
        val user = userService.findByIdEager(userId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.status(200).body(DtoConverter.transform(user))
    }

    @ApiOperation("create a user with an id")
    @PutMapping("/{userId}")
    fun createUser(@PathVariable("userId") userId: String): ResponseEntity<Void> {

        val ok = userService.registerNewUser(userId)
        return if (ok) ResponseEntity.status(201).build()
        else ResponseEntity.status(400).build()
    }

    @ApiOperation("Execute a command on a user's collection, like for example buying/milling cards")
    @PatchMapping("/{userId}", consumes = [(MediaType.APPLICATION_JSON_VALUE)])
    fun patchUser(
        @PathVariable("userId") userId: String,
        @RequestBody dto: PatchUsersDto
    ): ResponseEntity<PatchResultDto> {
        return when (dto.command) {
            Command.BUY_CARD -> {
                val cardId = dto.cardId ?: return ResponseEntity.status(400).build()
                try {
                    userService.buyCard(userId, cardId)
                } catch (e: java.lang.IllegalArgumentException) {
                    return ResponseEntity.status(400).build()
                }
                ResponseEntity.status(200).body(PatchResultDto())
            }
            Command.MILL_CARD -> {
                val cardId = dto.cardId ?: return ResponseEntity.status(400).build()
                try {
                    userService.millCard(userId, cardId)
                } catch (e: java.lang.IllegalArgumentException) {
                    return ResponseEntity.status(400).build()
                }
                ResponseEntity.status(200).body(PatchResultDto())
            }
            Command.OPEN_PACK -> {
                val ids = try {
                    userService.openPack(userId)
                } catch (e: IllegalArgumentException) {
                    return ResponseEntity.status(400).build()
                }
                ResponseEntity.status(200).body(PatchResultDto().apply { cardIdsInOpenedPack.addAll(ids) })
            }
            else -> ResponseEntity.status(400).build()
        }
    }
}