package uk.cresswell.rcon
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var serverList: ArrayList<Server>
    private lateinit var serverAdapter: ArrayAdapter<Server>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        val serverListView: ListView = findViewById(R.id.serverListView)
        val addButton: Button = findViewById(R.id.addButton)

        // Initialize server list and adapter
        serverList = ArrayList()
        serverAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, serverList)
        serverListView.adapter = serverAdapter

        addButton.setOnClickListener {
            // Open AddServerActivity to add a new server
            val addServerIntent = Intent(this, AddServerActivity::class.java)
            startActivityForResult(addServerIntent, ADD_SERVER_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_SERVER_REQUEST && resultCode == RESULT_OK) {
            // Retrieve the new server from AddServerActivity
            val newServer = data?.getSerializableExtra("newServer") as? Server
            if (newServer != null) {
                serverList.add(newServer)
                serverAdapter.notifyDataSetChanged()
            }
        }
    }

    companion object {
        private const val ADD_SERVER_REQUEST = 1
    }
}

