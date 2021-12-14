/*
 * Copyright (C) 2014 Pietro Rampini - PiKo Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import com.android.activity.BaseActivity
import com.android.models.Store
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient
import cz.msebera.android.httpclient.params.BasicHttpParams
import cz.msebera.android.httpclient.params.HttpConnectionParams
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


/**
 * Heart of the library. Check if an update is available for download parsing the desktop Play Store/Amazon App Store page of your app.
 *
 * @author Pietro Rampini (rampini.pietro@gmail.com)
 */
class CheckAppUpdate : AsyncTask<String, Int, Int>() {
    private var actionListener: BaseActivity.ActionListener? = null

    private var mStore = Store.GOOGLE_PLAY
    @SuppressLint("StaticFieldLeak")
    private var mContext: BaseActivity? = null
    private var mVersionDownloadable: String? = null

    private val versionName: String
        @Throws(PackageManager.NameNotFoundException::class)
        get() = mContext!!.packageManager.getPackageInfo(mContext!!.packageName, 0).versionName


    fun appHasUpdateVersion(store: Store, actionListener: BaseActivity.ActionListener) {
        this.actionListener = actionListener
        this.mStore = store
        checkAppUpdate!!.execute()
    }

    fun appHasUpdateVersion(actionListener: BaseActivity.ActionListener) {
        this.actionListener = actionListener
        checkAppUpdate!!.execute()
    }

    override fun doInBackground(vararg notused: String): Int? {
        if (mContext!!.store.getBoolean("DON'T SHOW AGAIN", false)) {
            return NETWORK_ERROR
        } else if (mContext!!.isNetworkAvailable) {
            try {
                val params = BasicHttpParams()
                HttpConnectionParams.setConnectionTimeout(params, 4000)
                HttpConnectionParams.setSoTimeout(params, 5000)
                val client = DefaultHttpClient(params)
                if (mStore === Store.GOOGLE_PLAY) {
                    val request = HttpGet(PLAY_STORE_ROOT_WEB + mContext!!.packageName) // Set the right Play Store page by getting package name.
                    val response = client.execute(request)
                    val `is` = response.entity.content
                    val reader = BufferedReader(InputStreamReader(`is`))
                    val line: String = reader.readLine()
                    while (line.length > 0) {
                        if (line.contains(PLAY_STORE_HTML_TAGS_TO_GET_RIGHT_POSITION)) { // Obtain HTML line contaning version available in Play Store
                            val containingVersion = line.substring(line.lastIndexOf(PLAY_STORE_HTML_TAGS_TO_GET_RIGHT_POSITION) + 28)  // Get the String starting with version available + Other HTML tags
                            val removingUnusefulTags = containingVersion.split(PLAY_STORE_HTML_TAGS_TO_REMOVE_USELESS_CONTENT.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // Remove useless HTML tags
                            mVersionDownloadable = removingUnusefulTags[0] // Obtain version available
                        } else if (line.contains(PLAY_STORE_PACKAGE_NOT_PUBLISHED_IDENTIFIER)) { // This packages has not been found in Play Store
                            return PACKAGE_NOT_PUBLISHED
                        }
                    }
                    return if (mVersionDownloadable == null) {
                        STORE_ERROR
                    } else if (containsNumber(mVersionDownloadable!!)) {
                        VERSION_DOWNLOADABLE_FOUND
                    } else {
                        MULTIPLE_APKS_PUBLISHED
                    }
                } else if (mStore === Store.AMAZON) {
                    val request = HttpGet(AMAZON_STORE_ROOT_WEB + mContext!!.packageName) // Set the right Amazon App Store page by getting package name.
                    val response = client.execute(request)
                    val `is` = response.entity.content
                    val reader = BufferedReader(InputStreamReader(`is`))
                    val line: String = reader.readLine()
                    while (line.length > 0) {
                        if (line.contains(AMAZON_STORE_HTML_TAGS_TO_GET_RIGHT_LINE)) { // Obtain HTML line contaning version available in Amazon App Store
                            val versionDownloadableWithTags = line.substring(38) // Get the String starting with version available + Other HTML tags
                            mVersionDownloadable = versionDownloadableWithTags.substring(0, versionDownloadableWithTags.length - 5) // Remove useless HTML tags
                            return VERSION_DOWNLOADABLE_FOUND
                        } else if (line.contains(AMAZON_STORE_PACKAGE_NOT_PUBLISHED_IDENTIFIER)) { // This packages has not been found in Amazon App Store
                            return PACKAGE_NOT_PUBLISHED
                        }
                    }
                }
            } catch (connectionError: IOException) {
                //   Network.logConnectionError();
                return NETWORK_ERROR
            }

        } else {
            return NETWORK_ERROR
        }
        return null
    }


    override fun onPostExecute(result: Int?) {
        mContext!!.log("onPostExecute: $result     $mVersionDownloadable")
        if (result == VERSION_DOWNLOADABLE_FOUND) {
            try {
                if (Integer.parseInt(mVersionDownloadable!!.replace(".", "")) > Integer.parseInt(versionName.replace(".", "")))
                    showDialogUpdateApp()
                else if (actionListener != null) actionListener!!.onActionResult()
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

        } else if (actionListener != null) actionListener!!.onActionResult()
    }

    private fun showDialogUpdateApp() {
        val sb = SpannableStringBuilder("New update available!")
        val bss = StyleSpan(android.graphics.Typeface.BOLD)
        sb.setSpan(bss, 0, "New update available!".length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        AlertDialog.Builder(mContext).setTitle(sb.toString()).setMessage("Update " + mVersionDownloadable + " is available to download.Downloading the latest update you will get the latest features,improvements and bug fixes of " + mContext!!.getString(com.android.R.string.app_name)).setPositiveButton("UPDATE") { dialog, which ->
            if (mStore === Store.GOOGLE_PLAY)
                gotoUpdatePlaystoreApp()
            else
                gotoUpdateAmazonApp()
            dialog.dismiss()
            mContext!!.finish()
        }.setNegativeButton("CANCEL") { dialog, which ->
            dialog.dismiss()
            if (actionListener != null) actionListener!!.onActionResult()
        }.setNeutralButton("DON'T SHOW AGAIN") { dialog, which ->
            mContext!!.store.setBoolean("DON'T SHOW AGAIN", true)
            dialog.dismiss()
            if (actionListener != null) actionListener!!.onActionResult()
        }.setCancelable(false).show()
    }


    private fun containsNumber(string: String): Boolean {
        return string.matches(".*[0-9].*".toRegex())
    }

    private fun gotoUpdatePlaystoreApp() {
        val appPackageName = mContext!!.packageName
        try {
            mContext!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            mContext!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }

    }

    private fun gotoUpdateAmazonApp() {
        try {
            mContext!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("amzn://apps/android?p=" + mContext!!.packageName)))
        } catch (anfe: android.content.ActivityNotFoundException) {
            mContext!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=" + mContext!!.packageName)))
        }

    }

    companion object {
        private val PLAY_STORE_ROOT_WEB = "https://play.google.com/store/apps/details?id="
        private val PLAY_STORE_HTML_TAGS_TO_GET_RIGHT_POSITION = "itemprop=\"softwareVersion\"> "
        private val PLAY_STORE_HTML_TAGS_TO_REMOVE_USELESS_CONTENT = "  </div> </div>"
        private val PLAY_STORE_PACKAGE_NOT_PUBLISHED_IDENTIFIER = "We're sorry, the requested URL was not found on this server."
        private val AMAZON_STORE_ROOT_WEB = "http://www.amazon.com/gp/mas/dl/android?p="
        private val AMAZON_STORE_HTML_TAGS_TO_GET_RIGHT_LINE = "<li><strong>Version:</strong>"
        private val AMAZON_STORE_PACKAGE_NOT_PUBLISHED_IDENTIFIER = "<title>Amazon.com: Apps for Android</title>"

        private val VERSION_DOWNLOADABLE_FOUND = 0
        private val MULTIPLE_APKS_PUBLISHED = 1
        private val NETWORK_ERROR = 2
        private val PACKAGE_NOT_PUBLISHED = 3
        private val STORE_ERROR = 4
        private var checkAppUpdate: CheckAppUpdate? = null

        fun with(activity: BaseActivity): CheckAppUpdate {
            checkAppUpdate = CheckAppUpdate()
            checkAppUpdate!!.mContext = activity
            return checkAppUpdate as CheckAppUpdate
        }
    }
}
