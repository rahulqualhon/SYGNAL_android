package com.android.utils

import android.annotation.SuppressLint
import android.os.AsyncTask

import com.android.activity.BaseActivity

import org.json.JSONObject

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


import cz.msebera.android.httpclient.HttpHeaders.USER_AGENT

class HttpPostHandling : AsyncTask<String, Void, Boolean> {
    private var jsonObject: JSONObject? = null
    private var api: String? = null
    private var response: String? = null
    private var responseHandle: ResponseHandle? = null

    @SuppressLint("StaticFieldLeak")
    private var baseActivity: BaseActivity? = null


    constructor(activity: BaseActivity, jsonObject: JSONObject, api: String, responseHandle: ResponseHandle) {
        this.baseActivity = activity
        this.jsonObject = jsonObject
        this.api = api
        this.responseHandle = responseHandle
    }

    constructor(activity: BaseActivity, api: String, responseHandle: ResponseHandle) {
        this.baseActivity = activity
        this.api = api
        this.responseHandle = responseHandle
    }

    override fun onPreExecute() {
        baseActivity!!.startProgressDialog()
        super.onPreExecute()

    }

    @SuppressLint("WrongThread")
    override fun doInBackground(vararg strings: String): Boolean? {
        try {
            if (jsonObject != null) {
                response = POST(jsonObject.toString())
            } else {
                response = GET()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return if (response != null) {
            response!!.contains("Status")
        } else {
            false
        }
    }

    private fun GET(): String? {
        val result = StringBuilder()
        try {
            val url = URL(Const.Web_Url + api!!)
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "GET"
            httpURLConnection.setRequestProperty("User-Agent", USER_AGENT)
            //            httpURLConnection.setRequestProperty("appid", Const.app_id);
            //            httpURLConnection.setRequestProperty("userid", baseActivity.store.getString("UserID"));
            //            httpURLConnection.setRequestProperty("token", baseActivity.store.getString("Token"));
            httpURLConnection.connect()
            if (httpURLConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(
                        InputStreamReader(httpURLConnection.inputStream))
                val line: String
                line=reader.readLine();
                while (line!= null) {
                    result.append(line)
                }
                reader.close()
            }
            val HttpResult = httpURLConnection.responseCode
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                return result.toString()
            }


        } catch (e: Exception) {
            e.printStackTrace()

        }


        return null
    }


    override fun onPostExecute(result: Boolean?) {
        try {
            if (result!!) {
                val jsonObject = JSONObject(response)
                when (jsonObject.getString("Status")) {
                    "2" -> {
                    }
                    else -> responseHandle!!.onSuccess(jsonObject, api)
                }
            } else {
                responseHandle!!.onError(api, jsonObject)
            }
        } catch (e: Exception) {
            baseActivity!!.stopProgressDialog()
            e.printStackTrace()
        }

    }

    private fun POST(toString: String): String? {
        val result = StringBuilder()
        try {
            val url = URL(Const.Web_Url + api!!)
            val postDataBytes = toString.toByteArray(StandardCharsets.UTF_8)
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.doOutput = true
            httpURLConnection.requestMethod = "POST"
            httpURLConnection.setRequestProperty("appid", Const.app_id)
            httpURLConnection.setRequestProperty("userid", baseActivity!!.store.getString("UserID"))
            httpURLConnection.setRequestProperty("token", baseActivity!!.store.getString("Token"))
            httpURLConnection.outputStream.write(postDataBytes)
            httpURLConnection.connect()
            if (httpURLConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(
                        InputStreamReader(httpURLConnection.inputStream))
                val line: String
                line=reader.readLine();
                while (line!= null) {
                    result.append(line)
                }
                reader.close()
            }
            val HttpResult = httpURLConnection.responseCode
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                return result.toString()
            }


        } catch (e: Exception) {
            e.printStackTrace()

        }


        return null
    }
}
