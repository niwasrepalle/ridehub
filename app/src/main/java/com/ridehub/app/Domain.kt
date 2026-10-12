package com.ridehub.app

import java.net.URI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel

enum class SearchPhase { Idle, Searching, Ready, Cancelled, Failed }
data class Quote(val provider: String, val type: String = "Car", val phase: SearchPhase = SearchPhase.Idle, val fare: Int = 0, val generation: Int = 0)
fun permittedRapidoLink(value: String): Boolean = try {
 val u = URI(value)
 u.scheme == "https" && u.host == "trk.rapido.bike" && u.port == -1 && u.userInfo == null && u.query == null && u.fragment == null && Regex("/RAPIDO/[a-fA-F0-9]{24}").matches(u.path)
} catch (_: Exception) { false }
class SearchModel(private val dispatcher: CoroutineDispatcher = Dispatchers.Main) : ViewModel() {
 val quotes = MutableStateFlow(listOf("Uber", "Ola", "Rapido").map { Quote(it) })
 private val scope = CoroutineScope(SupervisorJob() + dispatcher)
 private val jobs = mutableMapOf<String, Job>()
 fun update(p: String, transform: (Quote) -> Quote) { quotes.value = quotes.value.map { if (it.provider == p) transform(it) else it } }
 fun cancel(p: String) { jobs.remove(p)?.cancel(); update(p) { it.copy(phase = SearchPhase.Cancelled, generation = it.generation + 1) } }
 fun type(p: String, t: String) { cancel(p); update(p) { it.copy(type = t, phase = SearchPhase.Idle, fare = 0) } }
 fun search(p: String) {
  cancel(p)
  val q = quotes.value.first { it.provider == p }
  update(p) { it.copy(phase = SearchPhase.Searching) }
  jobs[p] = scope.launch { try { delay(900L + quotes.value.indexOfFirst { it.provider == p } * 700L)
   update(p) { if (it.generation == q.generation) it.copy(phase = SearchPhase.Ready, fare = mapOf("Bike" to 75, "Auto" to 120, "Car" to 190, "Premium" to 310).getValue(it.type) + listOf("Uber", "Ola", "Rapido").indexOf(p) * 13) else it }
  } catch (_: CancellationException) { } catch (_: Exception) { update(p) { it.copy(phase = SearchPhase.Failed) } } }
 }
 override fun onCleared() { scope.cancel() }
}
data class LiveSession(val token: String, val expiresAt: Long)
interface LiveTrackingGateway {
 suspend fun createSession(): LiveSession
 suspend fun publish(session: LiveSession, latitude: Double, longitude: Double, timestamp: Long)
 suspend fun revoke(session: LiveSession)
}
// Deliberately no backend implementation: never manufacture a working live link.
