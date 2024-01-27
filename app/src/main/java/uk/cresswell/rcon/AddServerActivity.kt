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
            // Display an error message if any field is empty
            // You can customize this part based on your requirements
            return
        }

        val port: Int = portStr.toInt()

        val newServer = Server(name, address, port, password)

        // Pass the new server data back to the calling activity
        intent.putExtra("newServer", newServer)
        setResult(RESULT_OK, intent)
        finish()
    }
}
