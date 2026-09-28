package com.example.data.model

enum class QuestionCategory(val titleAr: String, val icon: String, val descriptionAr: String) {
    ALL("الكل عشوائي", "🎲", "خلطة هبد من كل الأنواع"),
    HISTORY("تاريخ عجيب", "🏛️", "أحداث تاريخية وقرارات مجنونة للملوك"),
    INVENTIONS("اختراعات ومصادفات", "🧪", "أشياء تم اختراعها بطرق مضحكة وغريبة"),
    ANIMALS("عالم الحيوان", "🐾", "غرائب الكائنات وتصرفاتها المحيرة"),
    LAWS("عادات وقوانين مجنونة", "⚖️", "قوانين حقيقية تعتقد أنها مزحة"),
    FOOD_AND_OBJECTS("أكلات وأصول أشياء", "🍔", "أصل الأكلات والماركات واستخداماتها"),
    DILEMMAS("ألغاز وتساؤلات هبد", "🧠", "أسئلة ذكاء غير متوقعة وتفسيرات فلسفية"),
    CUSTOM("أسئلتي المضافة", "⭐", "أسئلة أضفتها أنت وأصحابك")
}

data class Question(
    val id: Int,
    val text: String,
    val category: QuestionCategory,
    val trueAnswer: String,
    val bluffHint: String,
    val isCustom: Boolean = false
)
