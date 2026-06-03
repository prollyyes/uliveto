package it.uliveto.browser.engine

import android.content.Context

object PrefsLoader {
    fun load(context: Context): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        context.assets.open("prefs/phoenix.android.js").bufferedReader().forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank() ||
                trimmed.startsWith("//") ||
                trimmed.startsWith("/*") ||
                trimmed.startsWith("*")) return@forEachLine
            val match = PREF_REGEX.find(trimmed) ?: return@forEachLine
            val key = match.groupValues[1]
            val raw = match.groupValues[2].trim()
            val value: Any = when {
                raw == "true" -> true
                raw == "false" -> false
                raw.startsWith("\"") -> raw.removeSurrounding("\"")
                raw.startsWith("'") -> raw.removeSurrounding("'")
                raw.toIntOrNull() != null -> raw.toInt()
                raw.toLongOrNull() != null -> raw.toLong().toInt().coerceIn(Int.MIN_VALUE, Int.MAX_VALUE)
                else -> error("Unrecognized pref value for key $key: $raw")
            }
            result[key] = value
        }
        return result
    }

    // Matches: pref("key", value) or pref("key", value, locked)
    // Strips optional trailing ", locked" or ", locked" before the closing paren
    private val PREF_REGEX = Regex(
        """^(?:user_)?pref\("([^"]+)",\s*(.+?)(?:,\s*locked)?\s*\);?\s*(?://.*)?$"""
    )
}
