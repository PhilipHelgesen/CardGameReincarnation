package org.tsdes.scores

import io.restassured.RestAssured
import io.restassured.RestAssured.*
import io.restassured.common.mapper.TypeRef
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.tsdes.advanced.rest.dto.PageDto
import org.tsdes.scores.db.UserStatsRepository
import org.tsdes.scores.db.UserStatsService
import javax.annotation.PostConstruct

@ActiveProfiles("FakeData", "test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [(Application::class)],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
internal class RestApiTest @Autowired constructor(private val service: UserStatsService, private val repository: UserStatsRepository) {

    @LocalServerPort
    protected var port = 0

    @PostConstruct
    fun init() {
        baseURI = "http://localhost"
        RestAssured.port = port
        enableLoggingOfRequestAndResponseIfValidationFails()
    }

    val page: Int = 10

    @Test
    fun testInit(){
        assertTrue(repository.count() > 10)
    }

    @Test
    fun testGetPage() {

        given().accept(ContentType.JSON)
            .get("/api/scores")
            .then()
            .statusCode(200)
            .body("data.list.size()", CoreMatchers.equalTo(page))
    }

    @Test
    fun testAllPages() {

        val read = mutableSetOf<String>()

        var page = given().accept(ContentType.JSON)
            .get("/api/scores")
            .then()
            .statusCode(200)
            .body("data.list.size()", CoreMatchers.equalTo(page))
            .extract().body().jsonPath().getObject("data", object : TypeRef<PageDto<Map<String, Any>>>() {})
        read.addAll(page.list.map { it["userId"].toString() })

        checkOrder(page)

        while (page.next != null) {
            page = given().accept(ContentType.JSON)
                .get(page.next)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getObject("data", object : TypeRef<PageDto<Map<String, Any>>>() {})
            read.addAll(page.list.map { it["userId"].toString() })
            checkOrder(page)
        }
        assertEquals(repository.count().toInt(), read.size)
    }

    private fun checkOrder(page: PageDto<Map<String, Any>>) {
        for (i in 0 until page.list.size - 1) {
            val aScore = page.list[i]["score"].toString().toInt()
            val bScore = page.list[i + 1]["score"].toString().toInt()
            val aid = page.list[i]["userId"].toString()
            val bid = page.list[i + 1]["userId"].toString()
            assertTrue(aScore >= bScore)
            if (aScore == bScore) {
                assertTrue(aid > bid)
            }
        }
    }
}