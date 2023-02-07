package com.oppwa.mobile.connect.demo.task

import android.util.JsonReader
import android.util.Log
import com.oppwa.mobile.connect.demo.common.Constants
import com.oppwa.mobile.connect.demo.common.Constants.CONNECTION_TIMEOUT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.invoke
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class RequestManager {

    @ExperimentalCoroutinesApi
    suspend fun requestCheckoutId(amount: String,
                                  currency: String) = Dispatchers.Default {
        val urlString: String = Constants.BASE_URL + "/token?" +
                "amount=" + amount +
                "&currency=" + currency +
                "&paymentType=PA" +
                /* store notificationUrl on your server to change it any time without updating the app */
                "&notificationUrl=" + Constants.NOTIFICATION_URL

        return@Default makeRequest(urlString, "checkoutId")
    }

    @ExperimentalCoroutinesApi
    suspend fun requestPaymentStatus(resourcePath: String) = Dispatchers.Default {
        val urlString: String = Constants.BASE_URL + "/status?resourcePath=" +
                URLEncoder.encode(resourcePath, "UTF-8")

        return@Default makeRequest(urlString, "paymentResult")
    }

    private fun makeRequest(urlString: String, keyName: String): String? {
        val url: URL
        var connection: HttpURLConnection? = null
        var resultString: String? = null

        try {
            url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECTION_TIMEOUT

            JsonReader(InputStreamReader(connection.inputStream, "UTF-8")).use { jsonReader ->

                jsonReader.beginObject()

                while (jsonReader.hasNext()) {
                    if (jsonReader.nextName() == keyName) {
                        resultString = jsonReader.nextString()
                    } else {
                        jsonReader.skipValue()
                    }
                }

                jsonReader.endObject()
            }

            Log.d(Constants.LOG_TAG, "$keyName: $resultString")
        } catch (e: Exception) {
            Log.e(Constants.LOG_TAG, "Error: ", e)
        } finally {
            connection?.disconnect()
        }

        return resultString
    }
}