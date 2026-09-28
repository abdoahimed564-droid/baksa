package com.example.data.model

data class Player(
    val id: String,
    val name: String,
    val emoji: String = "😎",
    val colorHex: Long = 0xFF7C3AED,
    var score: Int = 0,
    var bluffsSucceeded: Int = 0,
    var bluffsCaught: Int = 0
)

enum class GameMode(val titleAr: String, val subtitleAr: String, val icon: String) {
    BLUFF_MASTER(
        "البكّاس ضد الكل",
        "لاعب واحد في كل جولة يكون البكّاس، وعليه أن يقنع البقية بهبدته!",
        "🎭"
    ),
    ALL_BLUFF(
        "تحدي الهبد الجماعي",
        "السؤال يظهر للجميع، وكل لاعب يهبد إجابته ونرى من الأكثر إقناعاً!",
        "👥"
    ),
    QUICK_PLAY(
        "جلسة سريعة",
        "أسئلة عشوائية مستمرة بدون حساب نقاط، فقط للضحك والمتعة في القعدة!",
        "⚡"
    )
}

data class GameSettings(
    val mode: GameMode = GameMode.BLUFF_MASTER,
    val roundTimerSeconds: Int = 45, // 0 for unlimited
    val totalRounds: Int = 10, // 0 for unlimited
    val selectedCategories: Set<QuestionCategory> = setOf(QuestionCategory.ALL),
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)
