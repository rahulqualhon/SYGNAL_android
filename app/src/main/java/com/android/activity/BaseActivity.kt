package com.android.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment


import com.android.R
import com.android.utils.Const
import com.android.utils.LocationUtil
import com.android.utils.NetworkUtils
import com.android.utils.PrefStore
import com.android.utils.ResponseHandle

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nispok.snackbar.BuildConfig
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager
import com.nispok.snackbar.enums.SnackbarType
import com.nispok.snackbar.listeners.ActionClickListener
import com.squareup.picasso.Picasso

import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.UnsupportedEncodingException
import java.lang.reflect.Type
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.Locale
import java.util.Objects


abstract class BaseActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var inflater: LayoutInflater
    lateinit var store: PrefStore
    var permCallback: PermissionCallback? = null
    lateinit var responseHandle: ResponseHandle
    private var toast: Toast? = null
    private var progressDialog: Dialog? = null
    private var inputMethodManager: InputMethodManager? = null
    private var networkSnackbar: Snackbar? = null
    private val networkErrorReceiver = NetworkErrorReceiver()
    var params: MutableMap<String, String> = HashMap()
    var jsonObject = JSONObject()

    val uniqueDeviceId: String
        @SuppressLint("HardwareIds")
        get() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

    val isNetworkAvailable: Boolean
        get() {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager
                    .activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this@BaseActivity.overridePendingTransition(R.anim.slide_in,
                R.anim.slide_out)

        inputMethodManager = this
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        store = PrefStore(this)
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        strictModeThread()
        transitionSlideInHorizontal()
        initializeProgressDialog()
       // initializeNetworkBroadcast()
    }


    fun setActionBarTitleInCenter(title: String) {
        val view = inflater.inflate(R.layout.custom_action_bar, null)
        val titleTV = view.findViewById<View>(R.id.titleTV) as TextView
        titleTV.text = title

        val params = ActionBar.LayoutParams(//Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER)
        if (supportActionBar != null)
            supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setCustomView(view, params)


    }

    override fun onPause() {
        super.onPause()

    }

    private fun initializeNetworkBroadcast() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(networkErrorReceiver, intentFilter)
    }


    inner class NetworkErrorReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val status = NetworkUtils.getConnectivityStatusString(context)
            if (status != null) {
                //showNetworkAlert(status)
            }
        }
    }

    private fun showNetworkAlert(status: String) {
        showSnackBar(status, getString(R.string.retry), ActionClickListener { snackbar ->
            snackbar.dismiss()
            if (!isNetworkAvailable) {
                showNetworkAlert(status)
            }
        })
    }

    fun changeDateFormat(dateString: String?, sourceDateFormat: String, targetDateFormat: String): String {
        if (dateString == null || dateString.isEmpty()) {
            return ""
        }
        val inputDateFromat = SimpleDateFormat(sourceDateFormat, Locale.getDefault())
        var date = Date()
        try {
            date = inputDateFromat.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val outputDateFormat = SimpleDateFormat(targetDateFormat, Locale.getDefault())
        return outputDateFormat.format(date)
    }

    fun changeDateFormatFromDate(sourceDate: Date?, targetDateFormat: String?): String {
        if (sourceDate == null || targetDateFormat == null || targetDateFormat.isEmpty()) {
            return ""
        }
        val outputDateFormat = SimpleDateFormat(targetDateFormat, Locale.getDefault())
        return outputDateFormat.format(sourceDate)
    }

    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, Const.PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show()
            } else {
                log(getString(R.string.this_device_is_not_supported))
                finish()
            }
            return false
        }
        return true
    }


    fun checkWriteSettingPermission(context: Activity, permCallback: PermissionCallback) {
        this.permCallback = permCallback
        val permission: Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(context)
        } else {
            permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        }
        if (permission) {
            permCallback.permGranted()
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + context.packageName)
                context.startActivityForResult(intent, 123)
            } else {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.WRITE_SETTINGS), 99)
            }
        }
    }

    fun checkSelfPermission(perms: Array<String>, permCallback: PermissionCallback) {
        this.permCallback = permCallback
        ActivityCompat.requestPermissions(this, perms, 99)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var permGrantedBool = false
        when (requestCode) {
            99 -> {
                for (grantResult in grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, getString(R.string.not_sufficient_permissions)
                                + getString(R.string.app_name)
                                + getString(R.string.permissions), Toast.LENGTH_SHORT).show()
                        permGrantedBool = false
                        break
                    } else {
                        permGrantedBool = true
                    }
                }
                if (permCallback != null) {
                    if (permGrantedBool)
                        permCallback!!.permGranted()
                    else
                        permCallback!!.permDenied()
                }
            }
        }
    }

    interface PermissionCallback {
        fun permGranted()

        fun permDenied()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LocationUtil.onActivityResult(requestCode, resultCode)
        if (requestCode == 123) {
            // ---------------------------- Write Setting  ---------------------
        }
    }





    fun exitFromApp() {
        finish()
    }

    fun hideSoftKeyboard() {
        try {
            if (currentFocus != null) {
                inputMethodManager!!.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun isValidMail(email: String): Boolean {
        return email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())
    }

    fun isValidPassword(password: String): Boolean {
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[!&^%$#@()=*/.+_-])(?=\\S+$).{8,}$".toRegex())
    }

    fun keyHash() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash:>>>>>>>>>>>>>>>", "" + Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

    }

    fun log(string: String) {
        if (BuildConfig.DEBUG)
            Log.e(getString(R.string.app_name), string)
    }

    private fun initializeProgressDialog() {
        progressDialog = Dialog(this, R.style.transparent_dialog_borderless)
        val view = View.inflate(this, R.layout.progress_dialog, null)
        progressDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog!!.setContentView(view)
        Objects.requireNonNull(progressDialog!!.window)!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // txtMsgTV = (TextView) view.findViewById(R.id.txtMsgTV);
        progressDialog!!.setCancelable(false)
    }

    fun showSnackBar(message: String?, action: String, actionClickListener: ActionClickListener) {
        if (networkSnackbar != null && networkSnackbar!!.isShowing)
            networkSnackbar!!.dismiss()
        if (message == null)
            return
        networkSnackbar = Snackbar.with(applicationContext) // context
                .text(message) // text to be displayed
                .type(SnackbarType.MULTI_LINE)
                .swipeToDismiss(false)
                .position(Snackbar.SnackbarPosition.TOP)
                .dismissOnActionClicked(false)
                .textColor(Color.WHITE) // change the text color
                .textTypeface(Typeface.DEFAULT) // change the text font
                .animation(true)
                //                        .color(Color.BLUE) // change the background color
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                .actionLabel(action) // action button label
                .actionColor(Color.RED) // action button label color
                .actionLabelTypeface(Typeface.DEFAULT_BOLD) // change the action button font
                .actionListener(actionClickListener)// action button's ActionClickListener
        SnackbarManager.show(networkSnackbar!!, this) // activity where it is displayed

    }

    fun showSnackBar(message: String?) {
        if (message == null)
            return
        SnackbarManager.show(
                Snackbar.with(applicationContext) // context
                        .text(message) // text to be displayed
                        .type(SnackbarType.MULTI_LINE)
                        .swipeToDismiss(true)
                        .position(Snackbar.SnackbarPosition.BOTTOM)
                        .actionLabel(null)
                        .textColor(Color.WHITE) // change the text color
                        .textTypeface(Typeface.DEFAULT) // change the text font
                        .animation(true)
                        //                        .color(Color.BLUE) // change the background color
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT), this) // activity where it is displayed
    }

    fun startProgressDialog() {
        if (progressDialog != null && !progressDialog!!.isShowing) {
            try {
                progressDialog!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun stopProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            try {
                progressDialog!!.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun showToast(msg: String) {
        toast!!.setText(msg)
        toast!!.show()
    }

    private fun strictModeThread() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .permitAll().build())
    }

    fun transitionSlideInHorizontal() {
        this.overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left)
    }

    override fun onClick(v: View) {

    }


    fun getUriForFile(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val packageId = context.packageName
                FileProvider.getUriForFile(context, packageId, file)
            } catch (e: IllegalArgumentException) {
                throw SecurityException()
            }

        } else {
            Uri.fromFile(file)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }


    interface RetryClickListener {
        fun onActionClicked()
    }

    interface ActionListener {
        fun onActionResult()
    }
    open fun gotoFragmentWithStack(fragment: Fragment?) {
        this.supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in,
            R.anim.slide_out
        ).addToBackStack(null).replace(R.id.content_frame, fragment!!).commit()
    }

    open fun gotoFragmentWithOutStack(fragment: Fragment?) {
        this.supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in,
            R.anim.slide_out
        ).replace(R.id.content_frame, fragment!!).commit()
    }

    companion object {
        var picasso: Picasso? = null
        private val gson = Gson()


        fun getStringFromArray(strings: ArrayList<String>): String {
            return gson.toJson(strings)
        }

        fun getArrayFromString(time: String): ArrayList<String>? {
            val type = object : TypeToken<ArrayList<String>>() {

            }.type
            return gson.fromJson<ArrayList<String>>(time, type)
        }
    }
}
