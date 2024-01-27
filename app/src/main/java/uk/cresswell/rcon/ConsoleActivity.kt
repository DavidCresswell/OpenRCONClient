package uk.cresswell.rcon

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import nl.vv32.rcon.*;
import kotlinx.coroutines.*

class ConsoleActivity() : AppCompatActivity() {
    var rcon : Rcon? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var server: Server
    private lateinit var consoleInput: EditText
    private lateinit var consoleOutput: TextView

    fun connect() {
        coroutineScope.launch {
            try {
                val rcon = Rcon.open(server.address, server.port)
                if (rcon.authenticate(server.password)) {
                    withContext(Dispatchers.Main) {
                        consoleOutput.append("\n[Connected to ${server.name}]")
                    }
                    this@ConsoleActivity.rcon = rcon
                } else {
                    withContext(Dispatchers.Main) {
                        consoleOutput.append("\n[Authentication failed]")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    consoleOutput.append("\n[Connection failed: ${e}]")
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
        val sendButton: Button = findViewById(R.id.sendButton)

        fun sendInput() {
            val input = consoleInput.text.toString()
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
                            consoleOutput.append("\n< $response")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        consoleOutput.append("\n> $input")
                        consoleInput.text.clear()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        consoleOutput.append("\n[Error: ${e}]")
                    }
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
