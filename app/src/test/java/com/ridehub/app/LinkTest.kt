package com.ridehub.app
import org.junit.Assert.*
import org.junit.Test
class LinkTest {
 @Test fun acceptsOnlyOfficialShape() { assertTrue(permittedRapidoLink("https://trk.rapido.bike/RAPIDO/0123456789abcdef01234567")) }
 @Test fun rejectsUnsafeLinks() {
  listOf("http://trk.rapido.bike/RAPIDO/0123456789abcdef01234567", "https://trk.rapido.bike.evil.test/RAPIDO/0123456789abcdef01234567", "https://user@trk.rapido.bike/RAPIDO/0123456789abcdef01234567", "https://trk.rapido.bike:443/RAPIDO/0123456789abcdef01234567", "https://trk.rapido.bike/RAPIDO/0123456789abcdef01234567?redirect=evil", "javascript:alert(1)", "garbage").forEach { assertFalse(it, permittedRapidoLink(it)) }
 }
}
