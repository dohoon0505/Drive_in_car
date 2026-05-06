package com.driveincar.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.driveincar.ui.login.LoginScreen
import com.driveincar.ui.map.CourseDetailScreen
import com.driveincar.ui.map.MapScreen
import com.driveincar.ui.profile.ProfileSetupScreen
import com.driveincar.ui.race.RaceScreen
import com.driveincar.ui.ranking.RankingScreen
import com.driveincar.ui.result.ResultScreen
import com.driveincar.ui.splash.SplashScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: RootDestination,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen()
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onSignedIn = { needsProfile ->
                    val target = if (needsProfile) Routes.PROFILE_SETUP else Routes.MAP
                    navController.navigate(target) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(
                onCompleted = {
                    navController.navigate(Routes.MAP) {
                        popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAP) {
            MapScreen(
                onCourseSelected = { courseId ->
                    navController.navigate(Routes.courseDetail(courseId))
                },
                onSignedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAP) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Routes.COURSE_DETAIL,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { entry ->
            val courseId = entry.arguments?.getString("courseId").orEmpty()
            CourseDetailScreen(
                courseId = courseId,
                onJoinRace = { navController.navigate(Routes.race(courseId)) },
                onViewRanking = { navController.navigate(Routes.ranking(courseId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.RACE,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { entry ->
            val courseId = entry.arguments?.getString("courseId").orEmpty()
            RaceScreen(
                courseId = courseId,
                onFinished = { time, avg, flagged, pb ->
                    navController.navigate(Routes.result(courseId, time, avg, flagged, pb)) {
                        popUpTo(Routes.MAP)
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            Routes.RESULT,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("timeMs") { type = NavType.LongType },
                navArgument("averageKmh") { type = NavType.StringType },
                navArgument("flagged") { type = NavType.BoolType },
                navArgument("personalBest") { type = NavType.BoolType },
            )
        ) { entry ->
            val courseId = entry.arguments?.getString("courseId").orEmpty()
            val timeMs = entry.arguments?.getLong("timeMs") ?: 0L
            val averageKmh = entry.arguments?.getString("averageKmh")?.toDoubleOrNull() ?: 0.0
            val flagged = entry.arguments?.getBoolean("flagged") ?: false
            val pb = entry.arguments?.getBoolean("personalBest") ?: false
            ResultScreen(
                courseId = courseId,
                timeMs = timeMs,
                averageKmh = averageKmh,
                flagged = flagged,
                personalBest = pb,
                onViewRanking = { navController.navigate(Routes.ranking(courseId)) },
                onRetry = { navController.navigate(Routes.race(courseId)) },
                onBackToMap = {
                    navController.navigate(Routes.MAP) {
                        popUpTo(Routes.MAP) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Routes.RANKING,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { entry ->
            val courseId = entry.arguments?.getString("courseId").orEmpty()
            RankingScreen(
                courseId = courseId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
