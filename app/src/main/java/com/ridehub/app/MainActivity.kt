package com.ridehub.app

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

private val Blue = Color(0xFF4285FF)
private val Red = Color(0xFFFF4958)
class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent {
  MaterialTheme(colorScheme = darkColorScheme(primary = Blue, background = Color(0xFF080A0E), surface = Color(0xFF14171D))) { RideHub() }
 } }
 @Composable fun RideHub(model: SearchModel = viewModel()) {
  val prefs = remember { getSharedPreferences("ridehub", MODE_PRIVATE) }
  var screen by remember { mutableStateOf(if (prefs.getBoolean("onboarded", false)) "Explore" else "Welcome") }
  var splash by remember { mutableStateOf(true) }
  var destination by remember { mutableStateOf("") }
  var pickup by remember { mutableStateOf("Current location (demo)") }
  var selected by remember { mutableStateOf<Quote?>(null) }
  var inTrip by remember { mutableStateOf(false) }
  var message by remember { mutableStateOf("") }
  var countdown by remember { mutableStateOf<Int?>(null) }
  var contactName by remember { mutableStateOf("") }
  var contactPhone by remember { mutableStateOf("") }
  var contacts by remember { mutableStateOf(prefs.getString("contacts", "") ?: "") }
  var link by remember { mutableStateOf("") }
  val enabledProviders = remember { mutableStateMapOf("Uber" to true, "Ola" to true, "Rapido" to true) }
  val quotes by model.quotes.collectAsState()
  val fix by LocalTrack.location.collectAsState()
  val tracking by LocalTrack.status.collectAsState()
  val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
   if (result.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION)) {
    if (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true || result[Manifest.permission.ACCESS_FINE_LOCATION] == true) startTracking() else message = "Location denied. Demo remains available."
   }
  }
  fun share() {
   val q = selected
   val text = buildString {
    append("RideHub DEMO trip\n${q?.provider ?: "No provider"} • ${q?.type ?: "No ride"}\nFrom: $pickup\nTo: $destination\nDriver and fares are simulated.\n")
    val l = fix
    if (l != null && isLocationFresh(l.elapsedRealtimeNanos, SystemClock.elapsedRealtimeNanos())) append("One-time location snapshot (not live): https://maps.google.com/?q=${l.latitude},${l.longitude}\nAccuracy ±${l.accuracy.toInt()} m\n")
    else append("No fresh location available.\n")
    if (permittedRapidoLink(link)) append("User-provided Rapido tracking link: $link\n")
    append("RideHub hosted live tracking is not connected.")
   }
   launch(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Review and share trip"))
  }
  LaunchedEffect(Unit) { delay(1400); splash = false }
  DisposableEffect(Unit) {
   val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) countdown = null }
   lifecycle.addObserver(observer)
   onDispose { lifecycle.removeObserver(observer) }
  }
  LaunchedEffect(countdown) { val c = countdown; if (c != null) { if (c > 0) { delay(1000); countdown = c - 1 } else { countdown = null; launch(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))) } } }
  if (countdown != null) AlertDialog(onDismissRequest = { countdown = null }, title = { Text("SOS • ${countdown}s", color = Red) }, text = { Text("112 dialer will open. You must press Call. No automatic call or contact alert.") }, confirmButton = { TextButton(onClick = { countdown = null }) { Text("Cancel SOS") } })
  if (splash) { Column(Modifier.fillMaxSize().background(Color(0xFF080A0E))) { City(Modifier.weight(1f)); Text("RIDEHUB", Modifier.padding(32.dp), fontSize = 34.sp); Text("Every route. One place.", Modifier.padding(start = 32.dp, bottom = 48.dp), color = Color.Gray) }; return }
  Scaffold(bottomBar = { if (screen != "Welcome") NavigationBar { listOf("Explore", "Accounts", "RideSafe").forEach { s -> NavigationBarItem(selected = screen == s, onClick = { screen = s }, icon = { Text(if (s == "Explore") "↗" else if (s == "Accounts") "◎" else "◇") }, label = { Text(s) }) } } }) { padding ->
   Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
    Text("RIDEHUB", fontSize = 26.sp)
    Text("PERSONAL PREVIEW • SIMULATED RIDES", color = Blue, fontSize = 11.sp)
    if (message.isNotBlank()) Text(message, color = Color.LightGray)
    when (screen) {
     "Welcome" -> {
      City(Modifier.fillMaxWidth().height(260.dp))
      Text("Your city. Your choice.", fontSize = 30.sp)
      Text("Compare demo fares across Uber, Ola and Rapido. RideSafe uses your phone's GPS only when you start it. No provider passwords, booking or live fares.")
      Text("Sharing opens Android's share sheet for your review. GPS stays in memory and clears when tracking stops. Trusted contacts stay on this device; backups are disabled.")
      Button(onClick = { prefs.edit().putBoolean("onboarded", true).apply(); screen = "Explore" }) { Text("Explore RideHub") }
     }
     "Accounts" -> {
      Text("Your providers", fontSize = 25.sp)
      listOf("Uber", "Ola", "Rapido").forEach { p -> Card { Column(Modifier.padding(18.dp)) { Text(p, fontSize = 20.sp); Text("Not connected • authorized integration required"); OutlinedButton(onClick = { message = "$p connection placeholder. OAuth/API access must be approved by the provider. RideHub never asks for passwords." }) { Text("Connection details") } } } }
      Text("Real quotes, availability, booking, cancellation and driver updates require provider-authorized APIs. No scraping or automatic booking.")
     }
     "Explore" -> {
      City(Modifier.fillMaxWidth().height(220.dp))
      Text("Illustrated map • demo route", color = Color.Gray)
      OutlinedTextField(value = pickup, onValueChange = { pickup = it.take(120) }, label = { Text("Pickup") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(value = destination, onValueChange = { destination = it.take(120) }, label = { Text("Where to? Search demo places") }, modifier = Modifier.fillMaxWidth())
      listOf("HITEC City", "Secunderabad Station", "Airport").filter { destination.isBlank() || it.contains(destination, true) }.forEach { place -> TextButton(onClick = { destination = place }) { Text(place) } }
      Button(enabled = destination.isNotBlank() && pickup.isNotBlank(), onClick = { quotes.filter { enabledProviders[it.provider] == true }.forEach { model.search(it.provider) } }) { Text("Compare simulated fares") }
      quotes.forEach { q -> val enabled = enabledProviders[q.provider] == true; var menu by remember { mutableStateOf(false) }
       Card { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row { Text(q.provider, Modifier.weight(1f), fontSize = 20.sp); Switch(checked = enabled, onCheckedChange = { enabledProviders[q.provider] = it; if (!it) model.cancel(q.provider) }) }
        Box { OutlinedButton(onClick = { menu = true }, enabled = enabled) { Text("${q.type} ▾") }; DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) { listOf("Car", "Bike", "Auto", "Premium").forEach { type -> DropdownMenuItem(text = { Text(type) }, onClick = { model.type(q.provider, type); menu = false }) } } }
        Text(if (q.phase == SearchPhase.Ready) "₹${q.fare} • simulated • 4 min demo ETA" else q.phase.name)
        Row { TextButton(enabled = enabled && destination.isNotBlank(), onClick = { model.search(q.provider) }) { Text("Search / retry") }; TextButton(onClick = { model.cancel(q.provider) }) { Text("Cancel") } }
        if (q.phase == SearchPhase.Ready) Button(onClick = { selected = q; screen = "Details" }) { Text("View demo ride") }
       } }
      }
     }
     "Details" -> {
      val q = selected
      Text(if (inTrip) "On your way • DEMO" else "Driver preview • DEMO", fontSize = 26.sp)
      City(Modifier.fillMaxWidth().height(230.dp))
      Text("${q?.provider} • ${q?.type} • ₹${q?.fare} simulated")
      Text("Demo Driver • 4.8 rating\nDEMO 0000 • Sample vehicle\n$pickup → $destination\nNo actual driver, booking or arrival tracking.")
      Button(onClick = { if (inTrip) stopService(Intent(this@MainActivity, TrackingService::class.java)); inTrip = !inTrip }) { Text(if (inTrip) "End demo trip" else "Start demo trip") }
      Button(onClick = { share() }) { Text("Share trip • review details") }
      OutlinedButton(onClick = { screen = "RideSafe" }) { Text("Open RideSafe") }
     }
     "RideSafe" -> {
      Text("RideSafe", fontSize = 28.sp)
      Text("$tracking • phone GPS only")
      Text(fix?.let { "Location fix available • accuracy ±${it.accuracy.toInt()} m" } ?: "No GPS fix. Location is never simulated.")
      Text("Start while this screen is open. A persistent notification provides Stop. No server uploads; tracking may be interrupted by Android or power settings.")
      Button(onClick = {
       if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) startTracking()
       else permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
      }) { Text("Start local GPS tracking") }
      if (Build.VERSION.SDK_INT >= 33) TextButton(onClick = { permission.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) }) { Text("Allow tracking notification") }
      OutlinedButton(onClick = { stopService(Intent(this@MainActivity, TrackingService::class.java)) }) { Text("Stop and clear GPS") }
      Button(onClick = { share() }) { Text("Share trip / location snapshot") }
      Text("Hosted live tracking unavailable. A secure backend with expiring, revocable links is required.")
      Text("Trusted contacts • stored locally", fontSize = 20.sp)
      OutlinedTextField(value = contactName, onValueChange = { contactName = it.take(60) }, label = { Text("Name") })
      OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it.take(20) }, label = { Text("Phone") })
      TextButton(onClick = { if (contactName.isNotBlank() && Regex("[+0-9 ()-]{7,20}").matches(contactPhone)) { contacts += "${contactName.replace("\n", " ")}: $contactPhone\n"; prefs.edit().putString("contacts", contacts).apply(); contactName = ""; contactPhone = "" } else message = "Enter a name and valid phone number." }) { Text("Save contact") }
      Text(contacts.ifBlank { "No contacts saved. Sharing recipients are selected in Android's share sheet." })
      TextButton(onClick = { contacts = ""; prefs.edit().remove("contacts").apply() }) { Text("Delete all contacts") }
      OutlinedTextField(value = link, onValueChange = { link = it.take(200) }, label = { Text("Paste your Rapido share link") }, modifier = Modifier.fillMaxWidth())
      OutlinedButton(enabled = permittedRapidoLink(link), onClick = { launch(Intent(Intent.ACTION_VIEW, Uri.parse(link))) }) { Text("Open official tracking page") }
      Text("Link stays in memory. RideHub does not extract locations or validate trip status. Only share links you have permission to share.")
      Button(colors = ButtonDefaults.buttonColors(containerColor = Red), onClick = { countdown = 5 }) { Text("SOS • open 112 dialer") }
      Text("SOS does not dispatch help or alert contacts. Cancel within 5 seconds; then confirm the call in your dialer.", color = Color.Gray)
     }
    }
   }
  }
 }
 private fun startTracking() { try { startForegroundService(Intent(this, TrackingService::class.java)) } catch (_: Exception) { LocalTrack.status.value = "Unable to start • check permissions" } }
 private fun launch(intent: Intent) { try { startActivity(intent) } catch (_: ActivityNotFoundException) { LocalTrack.status.value = "No app available for this action" } }
}
@Composable fun City(modifier: Modifier) {
 Canvas(modifier.background(Color(0xFF0B0E13), RoundedCornerShape(22.dp))) {
  for (x in 0..12) for (y in 0..9) { val w = size.width / 13; val h = size.height / 10; drawRect(Color(0xFF181D25), Offset(x*w+4, y*h+4), androidx.compose.ui.geometry.Size(w-9,h-9)) }
  for (i in 0..8) drawLine(Color(0xFF28303A), Offset(i * size.width / 8, 0f), Offset(i * size.width / 8 + size.width * .25f, size.height), 2f)
  val pts = listOf(Offset(size.width*.15f,size.height*.8f),Offset(size.width*.4f,size.height*.8f),Offset(size.width*.4f,size.height*.3f),Offset(size.width*.8f,size.height*.3f))
  pts.zipWithNext().forEach { (a,b) -> drawLine(Blue.copy(alpha=.16f),a,b,20f); drawLine(Blue,a,b,4f) }
  drawLine(Red.copy(alpha=.2f), Offset(0f,size.height*.45f),Offset(size.width*.65f,size.height*.45f),16f)
  drawLine(Red, Offset(0f,size.height*.45f),Offset(size.width*.65f,size.height*.45f),3f)
  drawCircle(Color.White,7f,pts.last()); drawCircle(Blue,7f,pts.first())
 }
}
