package org.tsdes.usercollections

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.tsdes.usercollections.db.UserRepository
import org.tsdes.usercollections.db.UserService
import javax.annotation.PostConstruct

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class RestApiTest @Autowired constructor(
    private val userService: UserService,
    private val userRepository: UserRepository){
    @LocalServerPort
    protected var port = 0
    @PostConstruct
    fun init(){
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
    fun testGetUser(){
        val id = "Test"
        userService.registerNewUser(id)

        given().get("/$id").then().statusCode(200)
    }

    @Test
    fun testCreateUser(){
        val id = "Test"

        given().put("/$id").then().statusCode(201)

        assertTrue(userRepository.existsById(id))
    }
}