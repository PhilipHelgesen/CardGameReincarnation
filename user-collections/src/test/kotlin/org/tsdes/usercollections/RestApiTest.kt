package org.tsdes.usercollections

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.tsdes.usercollections.db.UserRepository
import org.tsdes.usercollections.db.UserService
import org.tsdes.usercollections.dto.Command
import org.tsdes.usercollections.dto.PatchUsersDto
import org.tsdes.usercollections.model.Collection
import javax.annotation.PostConstruct
@Profile("RestAPITest")
@Primary
@Service
class FakeCardService: CardService() {
    override fun fetchData() {
        val dto = FakeData.getCollectionDto()
        super.collection = Collection(dto)
    }
}


@ActiveProfiles("RestAPITest","test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class RestApiTest @Autowired constructor(
    private val userService: UserService,
    private val userRepository: UserRepository
) {
    @LocalServerPort
    protected var port = 0

    @PostConstruct
    fun init() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.basePath = "/api/user-collections"
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @BeforeEach
    fun initTest() {
        userRepository.deleteAll()
    }

    @Test
    fun testGetUser() {
        val id = "Test"
        userService.registerNewUser(id)

        given().get("/$id").then().statusCode(200)
    }

    @Test
    fun testCreateUser() {
        val id = "Test"

        given().put("/$id").then().statusCode(201)

        assertTrue(userRepository.existsById(id))
    }

    @Test
    fun testBuyCard() {
        val userId = "foo"

        given().put("/$userId").then().statusCode(201)

        val cardId = "c00"

        given().contentType(ContentType.JSON)
            .body(PatchUsersDto(Command.BUY_CARD, cardId))
            .patch("/$userId")
            .then()
            .statusCode(200)
        val user = userService.findByIdEager(userId)!!
        assertTrue(user.ownedCards.any { it.cardId == cardId })
    }

    @Test
    fun testOpenPack() {
        val userId = "Test"

        given().put("/$userId").then().statusCode(201)

        val before = userService.findByIdEager(userId)!!
        val totPacks = before.cardPacks
        assertTrue(totPacks > 0)

        given().contentType(ContentType.JSON)
            .body(PatchUsersDto(Command.OPEN_PACK))
            .patch("/$userId")
            .then()
            .statusCode(200)

        val after = userService.findByIdEager(userId)!!

        assertEquals(totPacks - 1, after.cardPacks)
        assertEquals(
            before.ownedCards.sumBy { it.numberOfCopies } + UserService.CARDS_PER_PACK,
            after.ownedCards.sumBy { it.numberOfCopies }
        )
    }

    @Test
    fun testMillCard() {
        val userId = "Test"

        given().put("/$userId").then().statusCode(201)

        val before = userRepository.findById(userId).get()

        given().contentType(ContentType.JSON)
            .body(PatchUsersDto(Command.OPEN_PACK))
            .patch("/$userId")
            .then()
            .statusCode(200)

        val between = userService.findByIdEager(userId)!!

        val cardId = between.ownedCards[0].cardId!!
        given().contentType(ContentType.JSON)
            .body(PatchUsersDto(Command.MILL_CARD, cardId))
            .patch("/$userId")
            .then()
            .statusCode(200)

        val after = userService.findByIdEager(userId)!!
        assertTrue(after.coins > before.coins)
        assertEquals(
            between.ownedCards.sumBy { it.numberOfCopies } - 1,
            after.ownedCards.sumBy { it.numberOfCopies }
        )
    }
}