package com.android.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.view.View
import android.widget.*
import com.android.R
import com.android.adapter.DrawerItemCustomAdapter
import com.android.databinding.ActivityMainBinding
import com.android.fragment.HomeFragment
import com.android.models.DataModel
import com.android.utils.Const
import com.android.utils.GoogleApisHandle
import com.android.utils.HttpPostHandling
import com.android.utils.LocationUtil

import org.json.JSONObject
import java.util.*


class MainActivity : BaseActivity() {
    internal lateinit var toolbar: Toolbar
    internal lateinit var titleTV: TextView
    internal lateinit var backV: View
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(R.layout.activity_main)
        setupToolbar()
        gotoFragmentWithOutStack(HomeFragment())
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        titleTV = findViewById(R.id.titleTV)
        backV = findViewById(R.id.backV)
        backV.setOnClickListener(this)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayShowHomeEnabled(false)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        toolbar.subtitle = ""
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == backV) {
            onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    fun setToolbarHeadings(title: String, b: Boolean) {
        titleTV.text = title
        if(b){
            backV.visibility=View.VISIBLE
        }else{
          backV.visibility=View.GONE
        }
    }




}
