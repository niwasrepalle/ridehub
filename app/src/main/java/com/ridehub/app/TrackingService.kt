package com.ridehub.app

import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.*
import kotlinx.coroutines.flow.MutableStateFlow

object LocalTrack {
 val location = MutableStateFlow<Location?>(null)
 val status = MutableStateFlow("Stopped")
}
class TrackingService : Service(), LocationListener {
 private lateinit var manager: LocationManager
 override fun onBind(intent: Intent?) = null
 override fun onCreate() {
  super.onCreate()
  manager = getSystemService(LocationManager::class.java)
  getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("tracking", "RideSafe tracking", NotificationManager.IMPORTANCE_LOW))
 }
 override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
  if (intent?.action == "STOP") { stopSelf(); return START_NOT_STICKY }
  if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { stopSelf(); return START_NOT_STICKY }
  val stop = PendingIntent.getService(this, 1, Intent(this, TrackingService::class.java).setAction("STOP"), PendingIntent.FLAG_IMMUTABLE)
  startForeground(1, Notification.Builder(this, "tracking").setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle("RideSafe • local GPS active").setContentText("No location uploaded. Tap Stop to finish.").addAction(Notification.Action.Builder(null, "Stop", stop).build()).setOngoing(true).build())
  try {
   val provider = if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
   if (manager.isProviderEnabled(provider)) { manager.requestLocationUpdates(provider, 5000L, 10f, this); LocalTrack.status.value = "Waiting for GPS fix" } else LocalTrack.status.value = "Location disabled • enable in Settings"
  } catch (_: SecurityException) { LocalTrack.status.value = "Permission revoked"; stopSelf() } catch (_: IllegalArgumentException) { LocalTrack.status.value = "No location provider"; stopSelf() }
  return START_NOT_STICKY
 }
 override fun onLocationChanged(location: Location) { LocalTrack.location.value = location; LocalTrack.status.value = "Local tracking active" }
 override fun onProviderDisabled(provider: String) { LocalTrack.status.value = "Location provider disabled" }
 override fun onDestroy() { manager.removeUpdates(this); LocalTrack.location.value = null; LocalTrack.status.value = "Stopped"; super.onDestroy() }
}
