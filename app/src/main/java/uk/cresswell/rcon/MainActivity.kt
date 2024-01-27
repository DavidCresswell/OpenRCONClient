package uk.cresswell.rcon

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var serverList: ArrayList<Server>
    private lateinit var serverAdapter: ArrayAdapter<Server>
    private lateinit var sharedPreferences: SharedPreferences

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

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ServerList", MODE_PRIVATE)

        // Load server list from SharedPreferences
        loadServerList()

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

                // Save server list to SharedPreferences
                saveServerList()
            }
        }
    }

    private fun loadServerList() {
        val serverListJson = sharedPreferences.getString("serverList", null)
        if (serverListJson != null) {
            val servers = Gson().fromJson(serverListJson, Array<Server>::class.java)
            serverList.addAll(servers)
            serverAdapter.notifyDataSetChanged()
        }
    }

    private fun saveServerList() {
        val serverListJson = Gson().toJson(serverList)
        sharedPreferences.edit().putString("serverList", serverListJson).apply()
    }

    companion object {
        private const val ADD_SERVER_REQUEST = 1
    }
}

