package com.driveincar.domain.model

data class User(
    val uid: String,
    val nickname: String,
    val carBrand: String,
    val carModel: String,
    val profileImageId: String,
) {
    val carDisplay: String get() = "$carBrand $carModel"
}
