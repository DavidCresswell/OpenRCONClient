// Copyright (C) 2024 David Cresswell
// Licensed under GPLv3

package uk.cresswell.rcon

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import uk.cresswell.rcon.net.RconClient

class ConsoleActivity() : AppCompatActivity() {
    var rcon : RconClient? = null
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
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }


    fun connect() {
        coroutineScope.launch {
            try {
                val rcon = RconClient.open(server.address, server.port, server.password)
                withContext(Dispatchers.Main) {
                    appendAndScroll("Connected to ${server.name}", "[%s]")
                }
                this@ConsoleActivity.rcon = rcon
            } catch (e: Exception) {
                this@ConsoleActivity.rcon = null
                withContext(Dispatchers.Main) {
                    appendAndScroll("Connection failed: ${e}", "[%s]")
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
            appendAndScroll("$input", "> %s")
            consoleInput.text.clear()
            if (input.isEmpty()) {
                return
            }
            coroutineScope.launch {
                try {
                    var rcon = this@ConsoleActivity.rcon
                    if (rcon == null) {
                        connect()
                        rcon = this@ConsoleActivity.rcon
                    }
                    if (rcon != null) {
                        val response = rcon.sendCommand(input)
                        withContext(Dispatchers.Main) {
                            appendAndScroll("$response", "< %s")
                        }
                    }
                } catch (e: Exception) {
                    this@ConsoleActivity.rcon = null
                    withContext(Dispatchers.Main) {
                        appendAndScroll("Error: ${e}", "[%s]")
                    }
                    connect()
                }
            }
        }

        sendButton.setOnClickListener { sendInput() }

        consoleInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                sendInput()
                true
            } else {
                false
            }
        }
        connect()
    }
}
