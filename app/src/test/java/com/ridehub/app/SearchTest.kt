package com.ridehub.app
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
class SearchTest {
 @Test fun cancellationAndTypeChangeAreIndependent() = runBlocking {
  val m = SearchModel(Dispatchers.Unconfined)
  m.search("Uber"); m.search("Ola"); m.search("Rapido")
  m.cancel("Uber"); m.type("Rapido", "Bike")
  delay(1800)
  assertEquals(SearchPhase.Cancelled, m.quotes.value[0].phase)
  assertEquals(SearchPhase.Ready, m.quotes.value[1].phase)
  assertEquals(SearchPhase.Idle, m.quotes.value[2].phase)
  assertEquals("Bike", m.quotes.value[2].type)
 }
 @Test fun retrySupersedesOldSearch() = runBlocking {
  val m = SearchModel(Dispatchers.Unconfined)
  m.search("Uber"); m.type("Uber", "Premium"); m.search("Uber")
  delay(1100)
  assertEquals(310, m.quotes.value[0].fare)
  assertEquals(SearchPhase.Ready, m.quotes.value[0].phase)
 }
}
