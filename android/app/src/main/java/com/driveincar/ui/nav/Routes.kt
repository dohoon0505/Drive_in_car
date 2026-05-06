package com.driveincar.ui.nav

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val PROFILE_SETUP = "profile_setup"
    const val MAP = "map"
    const val COURSE_DETAIL = "course_detail/{courseId}"
    const val RACE = "race/{courseId}"
    const val RESULT = "result/{courseId}/{timeMs}/{averageKmh}/{flagged}/{personalBest}"
    const val RANKING = "ranking/{courseId}"

    fun courseDetail(courseId: String) = "course_detail/$courseId"
    fun race(courseId: String) = "race/$courseId"
    fun result(courseId: String, timeMs: Long, averageKmh: Double, flagged: Boolean, personalBest: Boolean) =
        "result/$courseId/$timeMs/$averageKmh/$flagged/$personalBest"
    fun ranking(courseId: String) = "ranking/$courseId"
}

enum class RootDestination(val route: String) {
    Splash(Routes.SPLASH),
    Login(Routes.LOGIN),
    ProfileSetup(Routes.PROFILE_SETUP),
    Map(Routes.MAP),
}
