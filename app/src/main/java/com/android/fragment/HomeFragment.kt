package com.android.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.R
import com.android.activity.MainActivity
import com.android.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment() {

private lateinit var fragmentHomeBinding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (baseActivity as MainActivity).setToolbarHeadings("SYGNAL",false)
        fragmentHomeBinding= FragmentHomeBinding.inflate(inflater)
        return fragmentHomeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentHomeBinding.sessionCV.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if(v==fragmentHomeBinding.sessionCV){
            baseActivity.gotoFragmentWithStack(BluetoothListFragment())
        }
    }

}