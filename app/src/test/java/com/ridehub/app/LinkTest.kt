package com.ridehub.app
import org.junit.Assert.*
import org.junit.Test
class LinkTest {
 @Test fun acceptsOnlyOfficialShape() { assertTrue(permittedRapidoLink("https://trk.rapido.bike/RAPIDO/0123456789abcdef01234567")) }
 @Test fun rejectsUnsafeLinks() {
  listOf("http://trk.rapido.bike/RAPIDO/0123456789abcdef01234567", "https://trk.rapido.bike.evil.test/RAPIDO/0123456789abcdef01234567", "https://user@trk.rapido.bike/RAPIDO/0123456789abcdef01234567", "https://trk.rapido.bike:443/RAPIDO/0123456789abcdef01234567", "https://trk.rapido.bike/RAPIDO/0123456789abcdef01234567?redirect=evil", "javascript:alert(1)", "garbage").forEach { assertFalse(it, permittedRapidoLink(it)) }
 }

 @Test fun rejectsEncodedPathVariants() {
  val token = "0123456789abcdef01234567"
  listOf(
   "https://trk.rapido.bike/%52APIDO/$token",
   "https://trk.rapido.bike/RAPIDO%2F$token",
   "https://trk.rapido.bike/RAPIDO/%30${token.drop(1)}",
   "https://trk.rapido.bike/RAPIDO/%2530${token.drop(1)}"
  ).forEach { assertFalse(it, permittedRapidoLink(it)) }
 }
 @Test fun rejectsUnsupportedPathAndTokenShapes() {
  val base = "https://trk.rapido.bike/RAPIDO/"
  listOf("", "0123456789abcdef0123456", "0123456789abcdef012345678",
   "0123456789abcdef0123456g", "0123456789abcdef01234567/",
   "../RAPIDO/0123456789abcdef01234567", "%", "%GG"
  ).forEach { assertFalse(it, permittedRapidoLink(base + it)) }
 }
 @Test fun acceptsUppercaseHexTokenWithoutRewriting() {
  assertTrue(permittedRapidoLink("https://trk.rapido.bike/RAPIDO/ABCDEF0123456789ABCDEF01"))
 }
}
