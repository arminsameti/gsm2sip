package com.callagent.gateway

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/** The module's sepolicy.rule must stay the single narrow rule that lets
 *  Android 17's privileged-app domain reach Magisk's su socket — the same
 *  permission Magisk itself grants the older priv_app domain. */
class MagiskSepolicyRuleTest {
    @Test
    fun ruleIsExactlyTheSuSocketConnect() {
        val f = listOf("../magisk/sepolicy.rule", "magisk/sepolicy.rule")
            .map(::File).first { it.exists() }
        val rules = f.readLines().map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
        assertEquals(
            listOf("allow priv_app_36 magisk unix_stream_socket { getopt connectto }"),
            rules)
    }
}
