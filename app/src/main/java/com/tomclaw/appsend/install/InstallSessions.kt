package com.tomclaw.appsend.install

internal class InstallSessions(private val tracker: InstallTracker) {

    // An idle entry distinguishes a finished attempt from a result restoring a new process.
    private val attempts = HashMap<String, Attempt?>()

    @Synchronized
    fun start(key: String): Attempt? {
        if (attempts[key] != null) return null
        val attempt = Attempt(key)
        attempts[key] = attempt
        tracker.update(key, InstallStatus.Writing(0))
        return attempt
    }

    @Synchronized
    fun current(key: String): Attempt? = attempts[key]

    @Synchronized
    fun isCurrent(attempt: Attempt): Boolean = attempts[attempt.key] === attempt

    fun attach(attempt: Attempt, id: Int): Boolean = withCurrent(attempt) {
        attempt.id = id
    }

    fun update(attempt: Attempt, status: InstallStatus): Boolean = withCurrent(attempt) {
        tracker.update(attempt.key, status)
    }

    fun finish(attempt: Attempt): Boolean = withCurrent(attempt) {
        attempts[attempt.key] = null
        tracker.update(attempt.key, InstallStatus.Idle)
    }

    /** Runs [action] under the lock, so a cancellation can't land in the middle of it. */
    @Synchronized
    fun withCurrent(attempt: Attempt, action: () -> Unit): Boolean {
        if (!isCurrent(attempt)) return false
        action()
        return true
    }

    @Synchronized
    fun onResult(key: String, id: Int, action: (Attempt) -> Unit) {
        if (id < 0) return
        val attempt = if (attempts.containsKey(key)) {
            attempts[key]?.takeIf { it.id == id } ?: return
        } else {
            Attempt(key, id).also { attempts[key] = it }
        }
        action(attempt)
    }

    @Synchronized
    fun ids(): Set<Int> = attempts.values.mapNotNull { it?.id }.toSet()

    class Attempt internal constructor(val key: String, var id: Int? = null)

}
