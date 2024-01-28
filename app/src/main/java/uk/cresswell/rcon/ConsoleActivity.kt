// Copyright (C) 2024 David Cresswell
// Licensed under GPLv3

package uk.cresswell.rcon

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import uk.cresswell.rcon.net.RconClient

class ConsoleActivity() : AppCompatActivity() {
    var rcon: RconClient? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var server: Server
    private lateinit var consoleInput: EditText
    private lateinit var consoleOutput: TextView
    private lateinit var scrollView: ScrollView

    fun appendAndScroll(text: String, format: String = "%s") {
        if (text.isEmpty()) {
            consoleOutput.append("\n" + format.format(""))
        } else {
            text.split("\n").forEach {
                if (!it.isEmpty()) {
                    consoleOutput.append("\n" + format.format(it))
                }
            }
        }
        scrollView.post { scrollView.smoothScrollBy(0, scrollView.getChildAt(0).height) }
    }

    suspend fun connect(): RconClient? {
        try {
            val rcon = RconClient(server.address, server.port, server.password, ::callback)
            rcon.start()
            withContext(Dispatchers.Main) {
                appendAndScroll("Connected to ${server.name}", "[%s]")
            }
            this@ConsoleActivity.rcon = rcon
            return rcon
        } catch (e: Exception) {
            this@ConsoleActivity.rcon = null
            withContext(Dispatchers.Main) { appendAndScroll("Connection failed: ${e}", "[%s]") }
            return null
        }
    }

    fun callback(text: String, type: Int) {
        coroutineScope.launch {
            if (type == 0) {
                withContext(Dispatchers.Main) { appendAndScroll(text, "< %s") }
            } else {
                if (!text.isEmpty()) {
                    withContext(Dispatchers.Main) { appendAndScroll(text, "[%s]") }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_console)
        server = intent.getSerializableExtra("selectedServer") as Server

        consoleOutput = findViewById(R.id.consoleOutput)
        consoleInput = findViewById(R.id.consoleInput)
        scrollView = findViewById(R.id.scrollView)
        val sendButton: Button = findViewById(R.id.sendButton)

        fun sendInput() {
            val input = consoleInput.text.toString()
            consoleInput.text.clear()
            if (input.isEmpty()) {
                return
            }
            coroutineScope.launch {
                try {
                    var rcon = this@ConsoleActivity.rcon
                    if (rcon == null || !rcon.isOpen || !rcon.isReady) {
                        rcon = connect()
                        if (rcon == null) {
                            appendAndScroll("$input", "? %s")
                            return@launch
                        }
                        // wait until connection is ready
                        while (!rcon.isReady) {
                            delay(100)
                            if (!rcon.isOpen) {
                                appendAndScroll("$input", "? %s")
                                return@launch
                            }
                        }
                    }
                    withContext(Dispatchers.Main) { appendAndScroll("$input", "> %s") }
                    rcon.sendCommand(input)
                } catch (e: Exception) {
                    this@ConsoleActivity.rcon = null
                    withContext(Dispatchers.Main) { appendAndScroll("Error: ${e}", "[%s]") }
                    connect()
                }
            }
        }

        sendButton.setOnClickListener { sendInput() }

        consoleInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                            (event.action == KeyEvent.ACTION_DOWN &&
                                    event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                sendInput()
                true
            } else {
                false
            }
        }
        coroutineScope.launch {
            connect()
        }
    }

    override fun onPause() {
        super.onPause()
        rcon?.close()
    }
}
