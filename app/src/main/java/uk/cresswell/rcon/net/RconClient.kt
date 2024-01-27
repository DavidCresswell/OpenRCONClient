package uk.cresswell.rcon.net

import java.net.Socket
import java.nio.ByteBuffer
import kotlinx.coroutines.*

class RconClient(
        val address: String,
        val port: Int,
        val password: String,
        val callback: ((String, Int) -> Unit)
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    // socket
    private val socket = Socket()
    // set timeouts
    init {
        socket.soTimeout = 3000
        socket.connect(java.net.InetSocketAddress(address, port), 3000)
    }
    private var currentRequestId = 1
    var isOpen = false
        private set

    // input stream
    private val inputStream = socket.getInputStream()

    // output stream
    private val outputStream = socket.getOutputStream()

    // send a command
    private fun sendCommand(command: String, type: Int) {
        // allocate buffer
        val commandBytes = command.toByteArray(Charsets.US_ASCII)
        val buffer = ByteBuffer.allocate(14 + commandBytes.size)
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
        // write length
        buffer.putInt(10 + commandBytes.size)
        // write request id
        buffer.putInt(currentRequestId++)
        // write type
        buffer.putInt(type)
        // write body
        buffer.put(commandBytes)
        // write null terminators
        buffer.put(0)
        buffer.put(0)
        // send buffer
        outputStream.write(buffer.array())
        outputStream.flush()
    }

    public fun sendCommand(command: String) {
        sendCommand(command, 2)
    }

    private fun readPackets() {
        socket.soTimeout = 0
        coroutineScope.launch {
            try {
                while (isOpen) {
                    // read 4 bytes for packet length
                    val lengthBytes = ByteArray(4)
                    var readCount = 0
                    while (readCount < 4) {
                        val thisRead = inputStream.read(lengthBytes, readCount, 4 - readCount)
                        if (thisRead == -1) {
                            isOpen = false
                            callback("Connection Closed", -2)
                            return@launch
                        }
                        readCount += thisRead
                    }
                    val length = ByteBuffer.wrap(lengthBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).int
                    if (length < 10) {
                        isOpen = false
                        callback("Invalid packet length: ${length}", -2)
                        return@launch
                    } else if (length > 32768) { // much longer than spec to handle non-standard implementations
                        isOpen = false
                        callback("Packet too long: ${length}", -2)
                    }
                    // read packet
                    val packetBytes = ByteArray(length)
                    readCount = 0
                    while (readCount < length) {
                        readCount += inputStream.read(packetBytes, readCount, length - readCount)
                    }
                    // parse packet
                    val packet = ByteBuffer.wrap(packetBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN)
                    val requestId = packet.int
                    val responseType = packet.int
                    val body = ByteArray(length - 10)
                    packet.get(body)
                    val response = String(body, Charsets.UTF_8)
                    // call callback
                    callback(response, responseType)
                }
            } catch (e: Exception) {
                isOpen = false
                callback("${e}", -1)
            }
        }
    }

    // close the connection
    public fun close() {
        isOpen = false
        socket.close()
    }

    // start the client
    public fun start() {
        isOpen = true
        sendCommand(password, 3)
        readPackets()
    }
}
