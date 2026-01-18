package com.rmtm.lifelog.core.model

enum class Mood(val value: Int, val label: String, val emoji: String) {
    HAPPY(5, "기쁨", "😊"),
    EXCITED(4, "흥분", "🤩"),
    CALM(3, "평온", "😐"),
    ANNOYED(2, "짜증", "😤"),
    SAD(1, "슬픔", "😢"),
    ANGRY(0, "화남", "😡"),
    DEPRESSED(-1, "우울", "😔");

    companion object {
        fun fromValue(value: Int): Mood = entries.find { it.value == value } ?: CALM
    }
}
