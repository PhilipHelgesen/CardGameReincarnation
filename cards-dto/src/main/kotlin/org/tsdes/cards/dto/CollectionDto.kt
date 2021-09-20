package org.tsdes.cards.dto

class CollectionDto (
    var cards : MutableList<CardDto> = mutableListOf(),
    var prices : MutableMap<Rarity, Int> = mutableMapOf(),
    var millValues : MutableMap<Rarity, Int> = mutableMapOf(),
    var rarityProbabilities : MutableMap<Rarity, Double> = mutableMapOf()
        )