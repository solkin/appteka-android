package com.tomclaw.appsend.util

class VariableBuffer {
    val buffer: ByteArray = ByteArray(MAXIMUM_BUFFER_SIZE)
    private var executeStart: Long = 0
    private var executeTime: Long = 0
    var averageSpeed = 0f
        private set
    var nextBufferSize = MINIMUM_BUFFER_SIZE
        private set

    fun onExecuteStart() {
        executeStart = System.currentTimeMillis()
        executeTime = 0
    }

    fun onExecuteCompleted(read: Int) {
        executeTime = System.currentTimeMillis() - executeStart
        executeStart = 0
        if (executeTime > 0) {
            val bytesPerSecond = (1000 * read / executeTime).toFloat()
            averageSpeed = if (averageSpeed > 0) {
                (averageSpeed + bytesPerSecond) / 2
            } else {
                bytesPerSecond
            }
            var size = read * 3 / 2
            if (size < MINIMUM_BUFFER_SIZE) {
                size = MINIMUM_BUFFER_SIZE
            } else if (size > MAXIMUM_BUFFER_SIZE) {
                size = MAXIMUM_BUFFER_SIZE
            }
            nextBufferSize = size
            println("[buffer] read bytes: $read; average speed: $averageSpeed; next buffer size: $nextBufferSize")
        }
    }

}

private const val MINIMUM_BUFFER_SIZE = 128 * 1024
private const val MAXIMUM_BUFFER_SIZE = 1024 * 1024
