package com.tomclaw.appsend.install

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallSessionsTest {

    private val tracker = InstallTracker()
    private val sessions = InstallSessions(tracker)

    @Test
    fun `a late cancellation result does not interrupt a retry`() {
        val cancelled = requireNotNull(sessions.start("app"))
        sessions.attach(cancelled, 1)
        sessions.finish(cancelled)

        val retry = requireNotNull(sessions.start("app"))
        sessions.attach(retry, 2)
        sessions.update(retry, InstallStatus.Writing(50))
        val status = tracker.status("app").test()

        sessions.onResult("app", 1) { sessions.finish(it) }

        assertTrue(sessions.isCurrent(retry))
        assertEquals(setOf(2), sessions.ids())
        status.assertValue(InstallStatus.Writing(50))

        sessions.update(retry, InstallStatus.Installing)
        sessions.onResult("app", 2) { sessions.finish(it) }

        assertNull(sessions.current("app"))
        status.assertValues(
            InstallStatus.Writing(50),
            InstallStatus.Installing,
            InstallStatus.Idle,
        )
    }

    @Test
    fun `a late result does not take over a retry still waiting in the queue`() {
        val cancelled = requireNotNull(sessions.start("app"))
        sessions.attach(cancelled, 1)
        sessions.finish(cancelled)
        val retry = requireNotNull(sessions.start("app"))

        sessions.onResult("app", 1) { sessions.update(it, InstallStatus.Confirming) }

        assertTrue(sessions.isCurrent(retry))
        assertNull(retry.id)
        tracker.status("app").test().assertValue(InstallStatus.Writing(0))
    }

    @Test
    fun `a cancelled queued attempt cannot claim or update its retry`() {
        val cancelled = requireNotNull(sessions.start("app"))
        sessions.finish(cancelled)
        val retry = requireNotNull(sessions.start("app"))

        assertFalse(sessions.attach(cancelled, 1))
        assertFalse(sessions.update(cancelled, InstallStatus.Writing(75)))
        assertFalse(sessions.finish(cancelled))
        assertTrue(sessions.attach(retry, 2))
        assertTrue(sessions.isCurrent(retry))
        tracker.status("app").test().assertValue(InstallStatus.Writing(0))
    }

    @Test
    fun `a result after cancellation cannot restore the cancelled attempt`() {
        val cancelled = requireNotNull(sessions.start("app"))
        sessions.attach(cancelled, 1)
        sessions.finish(cancelled)
        var handled = false

        sessions.onResult("app", 1) { handled = true }

        assertFalse(handled)
        assertNull(sessions.current("app"))
        tracker.status("app").test().assertValue(InstallStatus.Idle)
    }

    @Test
    fun `a new process restores a session from its pending result`() {
        sessions.onResult("app", 1) { sessions.update(it, InstallStatus.Confirming) }

        val restored = requireNotNull(sessions.current("app"))
        assertEquals(1, restored.id)
        tracker.status("app").test().assertValue(InstallStatus.Confirming)
        assertTrue(sessions.finish(restored))
    }

    @Test
    fun `a repeated tap joins the active attempt`() {
        val active = requireNotNull(sessions.start("app"))
        sessions.update(active, InstallStatus.Writing(50))

        assertNull(sessions.start("app"))
        assertTrue(sessions.isCurrent(active))
        assertNotNull(sessions.start("other"))
        tracker.status("app").test().assertValue(InstallStatus.Writing(50))
    }

}
