package ir.sharif.xo.ads

import android.app.Activity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AdMediationTest {
    @Test
    fun parsesOnlySupportedProviderValues() {
        assertEquals(AdProvider.TAPSELL, AdProvider.parse("TAPSELL"))
        assertEquals(AdProvider.ADIVERY, AdProvider.parse("adivery"))
        assertEquals(AdProvider.MIXED, AdProvider.parse("unsupported"))
    }

    @Test
    fun placeholderPlacementsAreNotConfigured() {
        assertEquals(false, AdPlacements.isConfigured(AdPlacements.TAPSELL_INTERSTITIAL))
        assertEquals(false, AdPlacements.isConfigured(AdPlacements.TAPSELL_NATIVE))
        assertEquals(true, AdPlacements.isConfigured(AdPlacements.ADIVERY_INTERSTITIAL))
    }

    @Test
    fun mixedStrategyFallsBackWhenTapsellFails() = runBlocking {
        val calls = mutableListOf<String>()
        val tapsell = fakeStrategy("tapsell", AdResult.Failed(FailureReason.NO_FILL), calls)
        val adivery = fakeStrategy("adivery", AdResult.Shown, calls)
        val strategy = MixedAdStrategy(tapsell, adivery)

        val result = strategy.loadAndShowAd(Activity(), AdRequest("game_over", "t", "a"))

        assertEquals(AdResult.Shown, result)
        assertEquals(listOf("tapsell", "adivery"), calls)
    }

    @Test
    fun mixedStrategyDoesNotCallFallbackAfterSuccess() = runBlocking {
        val calls = mutableListOf<String>()
        val tapsell = fakeStrategy("tapsell", AdResult.Shown, calls)
        val adivery = fakeStrategy("adivery", AdResult.Shown, calls)

        val result = MixedAdStrategy(tapsell, adivery)
            .loadAndShowAd(Activity(), AdRequest("pause", "t", "a"))

        assertEquals(AdResult.Shown, result)
        assertEquals(listOf("tapsell"), calls)
    }

    private fun fakeStrategy(
        name: String,
        result: AdResult,
        calls: MutableList<String>
    ) = object : AdStrategy {
        override suspend fun loadAndShowAd(activity: Activity, request: AdRequest): AdResult {
            calls += name
            return result
        }

        override fun close() = Unit
    }
}
