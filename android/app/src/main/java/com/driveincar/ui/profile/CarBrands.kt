package com.driveincar.ui.profile

import androidx.compose.ui.graphics.Color

/** APEX Lines 디자인의 시그니처 8개 메이커 + 각 액센트 색. */
data class CarBrand(
    val name: String,
    val country: String,
    val color: Color,
    val slug: String,
    val models: List<String>,
)

object CarBrands {
    val all: List<CarBrand> = listOf(
        CarBrand(
            "BMW", "DEU", Color(0xFF1C69D4), "BMW",
            listOf("M3 Competition", "M2", "X5 M50i", "M4 CSL", "420i Coupe"),
        ),
        CarBrand(
            "현대", "KOR", Color(0xFF002C5F), "HYU",
            listOf("아이오닉 5 N", "아반떼 N", "쏘나타 N Line", "아이오닉 6"),
        ),
        CarBrand(
            "Porsche", "DEU", Color(0xFFD5001C), "POR",
            listOf("911 GT3", "911 Carrera S", "Cayman GT4", "Taycan GTS"),
        ),
        CarBrand(
            "Tesla", "USA", Color(0xFFE31937), "TSL",
            listOf("Model 3 Performance", "Model S Plaid", "Model Y", "Roadster"),
        ),
        CarBrand(
            "Mercedes-Benz", "DEU", Color(0xFF00ADEF), "MBZ",
            listOf("AMG GT", "AMG C63", "AMG E63 S", "AMG SL"),
        ),
        CarBrand(
            "Audi", "DEU", Color(0xFFBB0A30), "AUD",
            listOf("RS5", "RS6 Avant", "R8 V10", "S4"),
        ),
        CarBrand(
            "기아", "KOR", Color(0xFF05141F), "KIA",
            listOf("EV6 GT", "Stinger GT", "K5 GT", "EV9 GT"),
        ),
        CarBrand(
            "Genesis", "KOR", Color(0xFF9F8345), "GEN",
            listOf("G70 Shooting Brake", "G80 Sport", "GV70 3.5T", "X Concept"),
        ),
    )

    fun byName(name: String): CarBrand? = all.firstOrNull { it.name == name }
}
