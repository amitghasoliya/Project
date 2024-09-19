package com.amitghasoliya.project

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Callback
import retrofit2.Response
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val titleTextView: TextView = findViewById(R.id.title)
        val descriptionTextView: TextView = findViewById(R.id.description)

        fetchDataFromAPI {title,description ->
            runOnUiThread {
                titleTextView.text = title
                descriptionTextView.text = description
            }
        }

        val copyTitle: LinearLayout = findViewById(R.id.copyTitle)
        val shareDesc: LinearLayout = findViewById(R.id.shareDesc)
        val copyDesc: LinearLayout = findViewById(R.id.copyDesc)
        val shareTitle: LinearLayout = findViewById(R.id.shareTitle)

        copyTitle.setOnClickListener {
            val textToCopy = titleTextView.text
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        copyDesc.setOnClickListener {
            val textToCopy = descriptionTextView.text
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        shareTitle.setOnClickListener {
            val textToShare = titleTextView.text
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textToShare)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        shareDesc.setOnClickListener {
            val textToShare = descriptionTextView.text
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textToShare)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    private fun createRetrofitService(): ApiService {
        val client = getUnsafeOkHttpClient()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://jsonkeeper.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }

    private fun fetchDataFromAPI(callback: (String, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val apiService = createRetrofitService()

            apiService.fetchData().enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        val content = apiResponse?.choices?.get(0)?.message?.content!!
                        val jsonObject = JSONObject(content)

                        val titles = jsonObject.getJSONArray("titles").let { array ->
                            (0 until array.length()).map { array.getString(it) }
                        }
                        val description = jsonObject.getString("description")
                        callback(titles[0], description)
                    } else {
                        Log.e("Test1", "${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("Test2", "API Test2", t)
                }
            })
        }
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) =
                    Unit
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) =
                    Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}


