package com.android.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.activity.MainActivity
import com.android.databinding.FragmentAddGasBinding
import android.widget.ArrayAdapter
import com.android.R
import com.android.utils.DataInterface
import android.content.Intent





class AddGasFragment : BaseFragment() {

    private lateinit var binding: FragmentAddGasBinding
    private val arrSelectGas = arrayOf(
        "Oxygen",
        "Carbon Monoxide ( CO )",
        "Methane", "Chlotine (CL2)",
        "Hydrogen Sulfide (H2S)",
        "sulfur Dioxide (SO2)",
        "Nitric Oxide ( NO )",
        "Chlorine Dioxide (CLO2)",
        "Hydrogen Cyanide (HCN)",
        "Hydrogen Chloride (HCL)",
        "Phosphine (PH3)",
        "Nitrogen Oxide (NO2)",
        "Hydrogen (H2)",
        "Ammonia (NH3)",
        "Carbon Dioxide (CO2)",
        "Gamma", "VOC", "PID", "LEL",
    )
    private val arrSelectGasScale =
        arrayOf("%", "%LEL", "PPM", "PPB", "%VOL", "µSv", "µSv/h", "µRem", "mRem")

    private var count = 0.0
    private var counts = 0.0
    private  var dataInterface: DataInterface? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (baseActivity as MainActivity).setToolbarHeadings("Create Session", true)
        binding = FragmentAddGasBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGasTypeSp()
        setGasScaleSp()
        setOnClick()
        val text = String.format("%.1f", count)
        binding.maTextCountTv.text = "$text ft"
        val text1 = String.format("%.1f", counts)
        binding.alTextCountTv.text = "$text1 ft"
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View) {
        super.onClick(v)
        when (v) {
            binding.maDecrementIv -> {
                count -= .5
                if (count < 0.0) {
                    count = 0.0
                }
                val text = String.format("%.1f", count)
                binding.maTextCountTv.text = "$text ft"

            }
            binding.maIncrementIv -> {
                if(count<30.0){
                    count += .5
                    val text = String.format("%.1f", count)
                    binding.maTextCountTv.text = "$text ft"
                }

            }
            binding.alDecrementIv -> {
                counts -= .5
                if (counts < 0.0) {
                    counts = 0.0
                }
                val text = String.format("%.1f", counts)
                binding.alTextCountTv.text = "$text ft"

            }
            binding.alIncrementIv -> {
                if(count<30.0) {
                    counts += .5
                    val text = String.format("%.1f", counts)
                    binding.alTextCountTv.text = "$text ft"
                }

            }

            binding.saveBtn->{
                val min = binding.maTextCountTv.text.toString().trim().replace(" ft","")
                val max = binding.alTextCountTv.text.toString().trim().replace(" ft","")

                when {
                    min.toDouble() == 0.0 -> {
                        baseActivity.showSnackBar("min Alert is not equal to zero")
                    }
                   max.toDouble() == 0.0 -> {
                        baseActivity.showSnackBar("Alert value is not equal to zero")
                    }
                    min.toDouble() < max.toDouble() -> {
                        val bundle = Bundle()
                        bundle.putString("gasName", binding.gasTypeSp.selectedItem.toString().trim())
                        bundle.putString("percentage", "%")
                        bundle.putString("minAlert", binding.maTextCountTv.text.toString().trim())
                        bundle.putString("alertValue",  binding.alTextCountTv.text.toString().trim())
                        val data = BluetoothListFragment()
                        data.arguments = bundle
                        baseActivity.gotoFragmentWithStack(data)

                    }
                    else -> {
                        baseActivity.showSnackBar("Min value always less than Alert value")
                    }
                }

            }
        }

    }

    private fun setOnClick() {
        binding.maIncrementIv.setOnClickListener(this)
        binding.maDecrementIv.setOnClickListener(this)
        binding.alIncrementIv.setOnClickListener(this)
        binding.alDecrementIv.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
    }


    private fun setGasTypeSp() {
        val adapter = ArrayAdapter<Any?>(
            baseActivity, android.R.layout.simple_list_item_1, arrSelectGas
        )
        adapter.setDropDownViewResource(R.layout.custome_spinner)
        binding.gasTypeSp.adapter = adapter
    }

    private fun setGasScaleSp() {
        val adapter = ArrayAdapter<Any?>(
            baseActivity, android.R.layout.simple_list_item_1, arrSelectGasScale
        )
        adapter.setDropDownViewResource(R.layout.custome_spinner)
        binding.percentageSp.adapter = adapter
    }



}
