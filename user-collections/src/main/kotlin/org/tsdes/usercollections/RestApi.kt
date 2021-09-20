package org.tsdes.usercollections

import io.swagger.annotations.Api
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.tsdes.cards.dto.UserDto
import org.tsdes.usercollections.db.UserService

@Api(value = "/api/user-collection", description = "API for the user-collections")
@RequestMapping(
    "/api/user-collections",
    produces = [(MediaType.APPLICATION_JSON_VALUE)]
)
@RestController
class RestApi (private val userService: UserService){

    @GetMapping(path = ["/{userId}"])
    fun getUserInfo(@PathVariable("userId") userId: String) : ResponseEntity<UserDto>
    {
        val user = userService.findByIdEager(userId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.status(200).body(DtoConverter.transform(user))
    }

    @PutMapping(path = ["/{userId}"])
    fun createUser(@PathVariable("userId") userId: String): ResponseEntity<Void> {

        val ok = userService.registerNewUser(userId)
        return if (ok) ResponseEntity.status(201).build()
        else ResponseEntity.status(400).build()
    }
}