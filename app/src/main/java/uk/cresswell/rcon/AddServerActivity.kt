// Copyright (C) 2024 David Cresswell
// Licensed under GPLv3

package uk.cresswell.rcon

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddServerActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var portEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_server)

        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText)
        addressEditText = findViewById(R.id.addressEditText)
        portEditText = findViewById(R.id.portEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val addButton: Button = findViewById(R.id.addButton)

        addButton.setOnClickListener {
            addServer()
        }
    }

    private fun addServer() {
        val name: String = nameEditText.text.toString()
        val address: String = addressEditText.text.toString()
        val portStr: String = portEditText.text.toString()
        val password: String = passwordEditText.text.toString()

        if (name.isEmpty() || address.isEmpty() || portStr.isEmpty() || password.isEmpty()) {
            return
        }

        val port: Int = portStr.toInt()

        val newServer = Server(name, address, port, password)

        intent.putExtra("newServer", newServer)
        setResult(RESULT_OK, intent)
        finish()
    }
}
