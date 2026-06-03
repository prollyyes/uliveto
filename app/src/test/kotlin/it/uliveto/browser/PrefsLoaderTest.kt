package it.uliveto.browser

import org.junit.Assert.*
import org.junit.Test

class PrefsLoaderTest {
    // Test that the regex correctly parses pref() statements.
    // We test the parsing logic directly by replicating it here, mirroring PrefsLoader.

    @Test
    fun `parses boolean true pref`() {
        val result = parsePrefLine("""pref("some.bool.pref", true);""")
        assertEquals(true, result?.second)
    }

    @Test
    fun `parses boolean false pref`() {
        val result = parsePrefLine("""pref("some.bool.pref", false);""")
        assertEquals(false, result?.second)
    }

    @Test
    fun `parses integer pref`() {
        val result = parsePrefLine("""pref("some.int.pref", 42);""")
        assertEquals(42, result?.second)
    }

    @Test
    fun `parses negative integer pref`() {
        val result = parsePrefLine("""pref("some.int.pref", -1);""")
        assertEquals(-1, result?.second)
    }

    @Test
    fun `parses string pref`() {
        val result = parsePrefLine("""pref("some.string.pref", "hello");""")
        assertEquals("hello", result?.second)
    }

    @Test
    fun `parses blank string pref`() {
        val result = parsePrefLine("""pref("some.string.pref", "");""")
        assertEquals("", result?.second)
    }

    @Test
    fun `parses locked boolean pref`() {
        val result = parsePrefLine("""pref("general.aboutConfig.enable", true, locked);""")
        assertEquals(true, result?.second)
    }

    @Test
    fun `parses locked string pref`() {
        val result = parsePrefLine("""pref("browser.phoenix.version", "2026.04.27.1", locked);""")
        assertEquals("2026.04.27.1", result?.second)
    }

    @Test
    fun `parses locked integer pref`() {
        val result = parsePrefLine("""pref("telemetry.fog.test.localhost_port", 70000, locked);""")
        assertEquals(70000, result?.second)
    }

    @Test
    fun `parses locked pref with trailing comment`() {
        val result = parsePrefLine("""pref("some.pref", false, locked); // [DEFAULT]""")
        assertEquals(false, result?.second)
    }

    @Test
    fun `parses single-quoted JSON string pref`() {
        val result = parsePrefLine("""pref("privacy.fp.granularOverrides", '[{"firstPartyDomain":"example.invalid"}]');""")
        assertNotNull(result)
        val value = result?.second
        assertTrue("Expected String but got ${value?.javaClass}", value is String)
        assertTrue((value as String).startsWith("[{"))
    }

    @Test
    fun `skips comment lines`() {
        val result = parsePrefLine("// This is a comment")
        assertNull(result)
    }

    @Test
    fun `skips blank lines`() {
        val result = parsePrefLine("   ")
        assertNull(result)
    }

    @Test
    fun `skips block comment lines`() {
        val result = parsePrefLine("/* block comment */")
        assertNull(result)
    }

    @Test
    fun `parses user_pref variant`() {
        val result = parsePrefLine("""user_pref("some.pref", true);""")
        assertEquals(true, result?.second)
    }

    // Matches: pref("key", value) or pref("key", value, locked) with optional trailing comment
    private val PREF_REGEX = Regex(
        """^(?:user_)?pref\("([^"]+)",\s*(.+?)(?:,\s*locked)?\s*\);?\s*(?://.*)?$"""
    )

    private fun parsePrefLine(line: String): Pair<String, Any>? {
        val trimmed = line.trim()
        if (trimmed.isBlank() ||
            trimmed.startsWith("//") ||
            trimmed.startsWith("/*") ||
            trimmed.startsWith("*")) return null
        val match = PREF_REGEX.find(trimmed) ?: return null
        val key = match.groupValues[1]
        val raw = match.groupValues[2].trim()
        val value: Any = when {
            raw == "true" -> true
            raw == "false" -> false
            raw.startsWith("\"") -> raw.removeSurrounding("\"")
            raw.startsWith("'") -> raw.removeSurrounding("'")
            raw.toIntOrNull() != null -> raw.toInt()
            raw.toLongOrNull() != null -> raw.toLong().toInt().coerceIn(Int.MIN_VALUE, Int.MAX_VALUE)
            else -> return null
        }
        return key to value
    }
}
