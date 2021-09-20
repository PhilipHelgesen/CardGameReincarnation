package org.tsdes.usercollections.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.tsdes.usercollections.CardService
import org.tsdes.usercollections.FakeData
import org.tsdes.usercollections.model.Collection

@Profile("UserServiceTest")
@Primary
@Service
class FakeCardService : CardService() {

    override fun fetchData() {
        val dto = FakeData.getCollectionDto()
        super.collection = Collection(dto)
    }
}

@ActiveProfiles("UserServiceTest", "test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
internal class UserServiceTest {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun initTest() {
        userRepository.deleteAll()
    }

    @Test
    fun testCreateUser() {
        val id = "Test"
        assertTrue(userService.registerNewUser(id))
        assertTrue(userRepository.existsById(id))
    }

    @Test
    fun testFailCreateUserTwice() {
        val id = "Test"
        assertTrue(userService.registerNewUser(id))
        assertFalse(userService.registerNewUser(id))
    }

    @Test
    fun testBuyCard() {
        val userId = "Test"
        val cardId = "c00"
        userService.registerNewUser(userId)
        userService.buyCard(userId, cardId)
        val user = userService.findByIdEager(userId)
        assertTrue(user!!.ownedCards.any { it.cardId == cardId })
    }

    @Test
    fun testBuyCardFailNotEnoughMoney() {
        val userId = "Test"
        val cardId = "c09"
        userService.registerNewUser(userId)
        val e = assertThrows(IllegalArgumentException::class.java) {
            userService.buyCard(userId, cardId)
        }
        assertTrue(e.message!!.contains("coin"), "Wrong error message: ${e.message}")
    }

    @Test
    fun testOpenPack() {
        val userId = "Test"
        userService.registerNewUser(userId)

        val before = userService.findByIdEager(userId)!!
        val totCards = before.ownedCards.sumBy { it.numberOfCopies }
        val totPacks = before.cardPacks
        assertTrue(totPacks > 0)

        assertEquals(UserService.CARDS_PER_PACK, userService.openPack(userId).size)

        val after = userService.findByIdEager(userId)!!
        assertEquals(totPacks - 1, after.cardPacks)
        assertEquals(totCards + UserService.CARDS_PER_PACK,
            after.ownedCards.sumBy { it.numberOfCopies })
    }

    @Test
    fun testOpenPackFail() {
        val userId = "Test"
        userService.registerNewUser(userId)

        val before = userService.findByIdEager(userId)!!
        val totPacks = before.cardPacks

        repeat(totPacks) {
            userService.openPack(userId)
        }

        assertEquals(0, userService.findByIdEager(userId)?.cardPacks)

        assertThrows(IllegalArgumentException::class.java) {
            userService.openPack(userId)
        }
    }

    @Test
    fun testMillCard() {
        val userId = "Test"
        userService.registerNewUser(userId)

        val before = userRepository.findById(userId).get()
        val coins = before.coins

        userService.openPack(userId)

        val between = userService.findByIdEager(userId)!!
        val n = between.ownedCards.sumBy { it.numberOfCopies }
        userService.millCard(userId, between.ownedCards[0].cardId!!)


        val after = userService.findByIdEager(userId)!!
        assertTrue(after.coins > coins)
        assertEquals(n - 1, after.ownedCards.sumBy { it.numberOfCopies })
    }
}