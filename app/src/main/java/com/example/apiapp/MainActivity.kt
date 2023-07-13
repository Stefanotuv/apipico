package com.example.apiapp

import android.util.Log
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var urlEditText: EditText
    private lateinit var methodSpinner: Spinner
    private lateinit var keyValueLayout: LinearLayout
    private lateinit var keyEditText: EditText
    private lateinit var valueEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var responseTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlEditText = findViewById(R.id.urlEditText)
        urlEditText = findViewById(R.id.urlEditText)
        urlEditText.setText("192.168.2.242/api")
        methodSpinner = findViewById(R.id.methodSpinner)
        keyValueLayout = findViewById(R.id.keyValueLayout)
        keyEditText = findViewById(R.id.keyEditText)
        valueEditText = findViewById(R.id.valueEditText)
        sendButton = findViewById(R.id.sendButton)
        responseTextView = findViewById(R.id.responseTextView)

        sendButton.setOnClickListener { sendRequest() }

        val methods = arrayOf("GET", "POST")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, methods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        methodSpinner.adapter = adapter

        methodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 0) {
                    keyValueLayout.visibility = View.GONE
                } else {
                    keyValueLayout.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun sendRequest() {
        val url = urlEditText.text.toString()
        val method = methodSpinner.selectedItem.toString()

        if (method == "GET") {
            GetRequestTask().execute(url)
        } else {
            val key = keyEditText.text.toString()
            val value = valueEditText.text.toString()

            val data = JSONObject()
            data.put(key, value)

            PostRequestTask().execute(url, data.toString())
        }
    }



    private inner class GetRequestTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg urls: String): String {
            val urlString = urls[0]
            val url = addProtocolToUrl(urlString)

            val connection: HttpURLConnection
            val response: String

            try {
                connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()

                val inputStream = BufferedInputStream(connection.inputStream)
                response = convertStreamToString(inputStream)
            } catch (e: Exception) {
                Log.e(TAG, "Error in GET request: ${e.message}")
                return ""
            }

            return response
        }

        override fun onPostExecute(result: String) {
            responseTextView.text = result
        }
    }

    private inner class PostRequestTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String {
            val urlString = params[0]
            val data = params[1]
            val url = addProtocolToUrl(urlString)

            val connection: HttpURLConnection
            val response: String

            try {
                val url = URL(url)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val outputStream = BufferedOutputStream(connection.outputStream)
                val writer = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))
                writer.write(data)
                writer.flush()
                writer.close()

                val inputStream = BufferedInputStream(connection.inputStream)
                response = convertStreamToString(inputStream)
            } catch (e: Exception) {
                Log.e(TAG, "Error in POST request: ${e.message}")
                return ""
            }

            return response
        }

        override fun onPostExecute(result: String) {
            responseTextView.text = result
        }
    }

    private fun addProtocolToUrl(urlString: String): String {
        return if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            "http://$urlString"
        } else {
            urlString
        }
    }


    private fun convertStreamToString(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String? = null

        try {
            while ({ line = reader.readLine(); line }() != null) {
                stringBuilder.append(line).append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return stringBuilder.toString()
    }
}
