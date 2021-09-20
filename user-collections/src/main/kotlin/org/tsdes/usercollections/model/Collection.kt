package org.tsdes.usercollections.model

import org.tsdes.cards.dto.CollectionDto
import org.tsdes.cards.dto.Rarity
import kotlin.math.abs

class Collection(
    val cards: List<Card>,
    val prices: Map<Rarity, Int>,
    val millValues: Map<Rarity, Int>,
    val rarityProbabilities: Map<Rarity, Double>
) {

    constructor(dto: CollectionDto) : this(
        dto.cards.map { Card(it) },
        dto.prices.toMap(),
        dto.millValues.toMap(),
        dto.rarityProbabilities.toMap()
    )

    val cardsByRarity: Map<Rarity, List<Card>> = cards.groupBy { it.rarity }

    init {
        if (cards.isEmpty()) {
            throw IllegalArgumentException("No cards")
        }
        Rarity.values().forEach {
            requireNotNull(prices[it])
            requireNotNull(millValues[it])
            requireNotNull(rarityProbabilities[it])
        }

        val p = rarityProbabilities.values.sum()
        if (abs(1 - p) > 0.000001) {
            throw IllegalArgumentException("Invalid probability sum: $p")
        }

    }

}