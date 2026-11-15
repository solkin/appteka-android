package com.tomclaw.appsend.util.bdui

import android.content.ContextWrapper
import org.junit.Assert.assertEquals
import org.junit.Test

class BduiFlipperTest {

    @Test
    fun `interval follows both initial configuration and native setter writes`() {
        val flipper = BduiFlipper(ContextWrapper(null), 700)

        assertEquals(700, flipper.currentInterval)
        flipper.setFlipInterval(1500)
        assertEquals(1500, flipper.currentInterval)
        flipper.setFlipInterval(0)
        assertEquals(0, flipper.currentInterval)
    }

    @Test
    fun `a second flipper keeps its own default interval`() {
        val first = BduiFlipper(ContextWrapper(null))
        val second = BduiFlipper(ContextWrapper(null))

        first.setFlipInterval(100)

        assertEquals(3000, second.currentInterval)
    }
}
