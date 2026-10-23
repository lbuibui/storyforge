package com.storyforge.nativerewrite.ui

/**
 * Material 3 Expressive look built only from material3 1.4.0 public API.
 * The five shape stops are the ones [androidx.compose.material3.Shapes] exposes.
 * Larger expressive stops and the expressive motion scheme ship in material3 1.5.0.
 */
object ExpressiveShapeScale {
    const val EXTRA_SMALL_DP = 8
    const val SMALL_DP = 12
    const val MEDIUM_DP = 20
    const val LARGE_DP = 28
    const val EXTRA_LARGE_DP = 36

    fun isIncreasing(): Boolean {
        val stops = listOf(EXTRA_SMALL_DP, SMALL_DP, MEDIUM_DP, LARGE_DP, EXTRA_LARGE_DP)
        return stops.zipWithNext().all { (left, right) -> left < right }
    }
}

object ExpressiveMotion {
    /** Spring damping used until MotionScheme.expressive() is stable. */
    const val DAMPING = 0.72f
}
