package com.ridehub.app

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchTest {
    private fun SearchModel.quote(provider: String) = quotes.value.first { it.provider == provider }

    @Test fun cancellationAndTypeChangeAreIndependent() = runTest {
        val model = SearchModel(StandardTestDispatcher(testScheduler))
        model.search("Uber"); model.search("Ola"); model.search("Rapido")
        runCurrent()
        model.cancel("Uber"); model.type("Rapido", "Bike")
        advanceUntilIdle()
        assertEquals(SearchPhase.Cancelled, model.quote("Uber").phase)
        assertEquals(SearchPhase.Ready, model.quote("Ola").phase)
        assertEquals(SearchPhase.Idle, model.quote("Rapido").phase)
        assertEquals("Bike", model.quote("Rapido").type)
        assertEquals(0, model.quote("Rapido").fare)
    }

    @Test fun retrySupersedesAnAlreadyRunningSearch() = runTest {
        val model = SearchModel(StandardTestDispatcher(testScheduler))
        model.search("Uber")
        runCurrent()
        advanceTimeBy(500)
        model.type("Uber", "Premium"); model.search("Uber")
        runCurrent()
        // At the original request's completion time, the newer request is still pending.
        advanceTimeBy(400); runCurrent()
        assertEquals(SearchPhase.Searching, model.quote("Uber").phase)
        assertEquals(0, model.quote("Uber").fare)
        advanceUntilIdle()
        assertEquals(310, model.quote("Uber").fare)
        assertEquals(SearchPhase.Ready, model.quote("Uber").phase)
    }

    @Test fun providersCompleteIndependently() = runTest {
        val model = SearchModel(StandardTestDispatcher(testScheduler))
        model.search("Uber"); model.search("Ola"); model.search("Rapido")
        runCurrent()
        advanceTimeBy(900); runCurrent()
        assertEquals(SearchPhase.Ready, model.quote("Uber").phase)
        assertEquals(SearchPhase.Searching, model.quote("Ola").phase)
        assertEquals(SearchPhase.Searching, model.quote("Rapido").phase)
        advanceUntilIdle()
        assertTrue(model.quotes.value.all { it.phase == SearchPhase.Ready })
    }

    @Test fun cancellingJustBeforeCompletionKeepsCancelledState() = runTest {
        val model = SearchModel(StandardTestDispatcher(testScheduler))
        model.search("Uber")
        runCurrent()
        advanceTimeBy(899)
        model.cancel("Uber")
        advanceUntilIdle()
        assertEquals(SearchPhase.Cancelled, model.quote("Uber").phase)
        assertEquals(0, model.quote("Uber").fare)
    }

    @Test fun repeatedRetryPublishesOnlyLatestRideType() = runTest {
        val model = SearchModel(StandardTestDispatcher(testScheduler))
        model.search("Uber")
        runCurrent(); advanceTimeBy(100)
        model.type("Uber", "Bike"); model.search("Uber")
        runCurrent(); advanceTimeBy(100)
        model.type("Uber", "Premium"); model.search("Uber")
        advanceUntilIdle()
        assertEquals("Premium", model.quote("Uber").type)
        assertEquals(310, model.quote("Uber").fare)
        assertEquals(SearchPhase.Ready, model.quote("Uber").phase)
        assertEquals(SearchPhase.Idle, model.quote("Ola").phase)
    }
}
