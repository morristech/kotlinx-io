package kotlinx.io.core

expect interface Input {
    var byteOrder: ByteOrder

    fun readByte(): Byte
    fun readShort(): Short
    fun readInt(): Int
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double

    fun readFully(dst: ByteArray, offset: Int, length: Int)
    fun readFully(dst: ShortArray, offset: Int, length: Int)
    fun readFully(dst: IntArray, offset: Int, length: Int)
    fun readFully(dst: LongArray, offset: Int, length: Int)
    fun readFully(dst: FloatArray, offset: Int, length: Int)
    fun readFully(dst: DoubleArray, offset: Int, length: Int)
    fun readFully(dst: BufferView, length: Int)

    fun readAvailable(dst: ByteArray, offset: Int, length: Int): Int
    fun readAvailable(dst: ShortArray, offset: Int, length: Int): Int
    fun readAvailable(dst: IntArray, offset: Int, length: Int): Int
    fun readAvailable(dst: LongArray, offset: Int, length: Int): Int
    fun readAvailable(dst: FloatArray, offset: Int, length: Int): Int
    fun readAvailable(dst: DoubleArray, offset: Int, length: Int): Int
    fun readAvailable(dst: BufferView, length: Int): Int

    fun discard(n: Long): Long

    @Deprecated("Non-public API. Use takeWhile or takeWhileSize instead", level = DeprecationLevel.ERROR)
    fun `$updateRemaining$`(remaining: Int)

    @Deprecated("Non-public API. Use takeWhile or takeWhileSize instead", level = DeprecationLevel.ERROR)
    fun `$ensureNext$`(current: BufferView): BufferView?

    @Deprecated("Non-public API. Use takeWhile or takeWhileSize instead", level = DeprecationLevel.ERROR)
    fun `$prepareRead$`(minSize: Int): BufferView?
}

fun Input.readFully(dst: ByteArray) {
    readFully(dst, 0, dst.size)
}

fun Input.readFully(dst: ShortArray) {
    readFully(dst, 0, dst.size)
}

fun Input.readFully(dst: IntArray) {
    readFully(dst, 0,  dst.size)
}

fun Input.readFully(dst: LongArray) {
    readFully(dst, 0, dst.size)
}

fun Input.readFully(dst: FloatArray) {
    readFully(dst, 0, dst.size)
}

fun Input.readFully(dst: DoubleArray) {
    readFully(dst, 0, dst.size)
}

fun Input.readAvailable(dst: ByteArray): Int = readAvailable(dst, 0, dst.size)
fun Input.readAvailable(dst: ShortArray): Int = readAvailable(dst, 0, dst.size)
fun Input.readAvailable(dst: IntArray): Int = readAvailable(dst, 0, dst.size)
fun Input.readAvailable(dst: LongArray): Int = readAvailable(dst, 0, dst.size)
fun Input.readAvailable(dst: FloatArray): Int = readAvailable(dst, 0, dst.size)
fun Input.readAvailable(dst: DoubleArray): Int = readAvailable(dst, 0, dst.size)

fun Input.discardExact(n: Long) {
    val discarded = discard(n)
    if (discarded != n) {
        throw IllegalStateException("Only $discarded bytes were discarded of $n requested")
    }
}

fun Input.discardExact(n: Int) {
    discardExact(n.toLong())
}

inline fun Input.takeWhile(block: (BufferView) -> Boolean) {
    var current = @Suppress("DEPRECATION_ERROR") `$prepareRead$`(1) ?: return
    var continueFlag = true

    do {
        val before = current.readRemaining
        val after = if (before > 0) {
            continueFlag = block(current)
            current.readRemaining
        } else before

        if (after == 0) {
            current = @Suppress("DEPRECATION_ERROR") `$ensureNext$`(current) ?: break
        } else {
            @Suppress("DEPRECATION_ERROR") `$updateRemaining$`(after)
        }
    } while (continueFlag)
}

inline fun Input.takeWhileSize(block: (BufferView) -> Int) {
    var current = @Suppress("DEPRECATION_ERROR") `$prepareRead$`(1) ?: return
    var size = 1

    do {
        val before = current.readRemaining
        val after: Int

        if (before >= size) {
            try {
                size = block(current)
            } finally {
                after = current.readRemaining
            }
        } else {
            after = before
        }

        if (after == 0) {
            current = @Suppress("DEPRECATION_ERROR") `$ensureNext$`(current) ?: break
        } else if (after < size) {
            current = @Suppress("DEPRECATION_ERROR") `$prepareRead$`(size) ?: break
        } else {
            @Suppress("DEPRECATION_ERROR") `$updateRemaining$`(after)
        }
    } while (size > 0)
}

