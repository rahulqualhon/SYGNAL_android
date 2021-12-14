package com.android.fragment

import android.R
import android.hardware.SensorEvent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Spinner

import com.android.activity.BaseActivity
import com.android.utils.ResponseHandle
import com.nispok.snackbar.listeners.ActionClickListener

import org.json.JSONObject


open class BaseFragment : Fragment(), AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    lateinit var baseActivity: BaseActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseActivity = (activity as BaseActivity?)!!
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
        baseActivity.hideSoftKeyboard()
        baseActivity!!.invalidateOptionsMenu()
    }

    override fun onClick(v: View) {

    }

    fun showToast(msg: String) {
        baseActivity.showToast(msg)
    }

    fun showSnackBar(message: String, action: String, actionClickListener: ActionClickListener) {
        baseActivity.showSnackBar(message, action, actionClickListener)
    }

    fun showSnackBar(msg: String) {
        baseActivity.showSnackBar(msg)
    }


    fun log(s: String) {
        baseActivity.log(s)
    }


    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {

    }
    protected open fun setSpinnerAdapter(spinner: Spinner, list: ArrayList<String>) {
        val staticAdapter: ArrayAdapter<Any?> = ArrayAdapter<Any?>(baseActivity, R.layout.simple_spinner_item, list as List<Any?>)
        staticAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = staticAdapter
    }

    open fun onSensorChanged(event: SensorEvent) {}
}
