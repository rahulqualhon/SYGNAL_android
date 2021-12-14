package com.android.fragment

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Parcel
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.R
import com.android.activity.BaseActivity
import com.android.activity.MainActivity
import com.android.adapter.Bluetooth_Adapter
import com.android.databinding.FragmentCreateSessionBinding
import com.android.models.BluetoothDeviceModel
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList


class CreateSessionFragment : BaseFragment(), SensorEventListener {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothRegistered = false
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val bluetoothDeviceModels = HashMap<String, BluetoothDeviceModel>()
    private var dialog: Dialog? = null
    private var bluetooth_Adapter: Bluetooth_Adapter? = null
    private var btListRV: RecyclerView? = null
    lateinit var fragmentCreateSessionBinding: FragmentCreateSessionBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (baseActivity as MainActivity).setToolbarHeadings("Create session", true)
        fragmentCreateSessionBinding = FragmentCreateSessionBinding.inflate(inflater)
        return fragmentCreateSessionBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bluetoothManager =
            baseActivity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        setDistance()
        fragmentCreateSessionBinding.startBT.setOnClickListener(this)
        sensorManager = baseActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // create a listener for gyroscope

        // create a listener for gyroscope


    }


    override fun onClick(v: View) {
        super.onClick(v)
        if (v == fragmentCreateSessionBinding.startBT) {
            checkBluetooth()
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                var distance: Double = calculteBTDistance(rssi)
                var bigDecimal: BigDecimal
                if (device!!.name != null) {
                    val bluetoothDeviceModel = BluetoothDeviceModel(Parcel.obtain())
                    bluetoothDeviceModel.bluetooth_name = device.name
                    bluetoothDeviceModel.bluetooth_rssi = baseActivity.getString(
                        R.string.formatter_db,
                        rssi.toString()
                    ) // MAC address
                    distance *= 3.3
                    bigDecimal = twoFraction(distance)
                    bluetoothDeviceModel.distancevalue = distance
                    bluetoothDeviceModel.distance = baseActivity.getString(
                        R.string.formatter_meters,
                        bigDecimal.toString() + "ft"
                    )
                    // showToast("Device name "+device.name+"& distance "+bluetoothDeviceModel.distance )
//                    bluetoothDeviceModel.date =
//                        DateFormat.format("MMM dd,yyyy hh:mm:ss", Date()).toString()
                    bluetoothDeviceModels[device.address] = bluetoothDeviceModel
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                baseActivity.startProgressDialog()
                bluetoothDeviceModels.clear()


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                baseActivity.stopProgressDialog()
//                if (refreshV != null)
//                    refreshV.clearAnimation();
                if (dialog != null && dialog!!.isShowing()) {
                    setAdapterList(bluetoothDeviceModels)
                } else {
                    ShowListDialog()
                }

            }
        }

    }

    private fun setAdapterList(bluetoothDeviceModels: HashMap<String, BluetoothDeviceModel>) {
        val values: Collection<BluetoothDeviceModel?> = bluetoothDeviceModels.values
        val listOfValues =
            java.util.ArrayList<BluetoothDeviceModel?>(values)
        btListRV!!.adapter = Bluetooth_Adapter(baseActivity,listOfValues);
    }

    private fun ShowListDialog() {
        baseActivity.gotoFragmentWithStack(BluetoothListFragment())


//        dialog = Dialog(baseActivity)
//        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog!!.setContentView(R.layout.fragment_bluetooth_list)
//        Objects.requireNonNull<Window>(dialog!!.getWindow())
//            .setBackgroundDrawable(ColorDrawable(Color.WHITE))
//        val dm = DisplayMetrics()
//        baseActivity.windowManager.defaultDisplay.getMetrics(dm)
//        dialog!!.getWindow()!!.setLayout(dm.widthPixels, ViewGroup.LayoutParams.MATCH_PARENT)
//        dialog!!.show()
//        btListRV = dialog!!.findViewById(R.id.btListRV)
//        val close_dialog = dialog!!.findViewById<ImageView>(R.id.close_dialog)
//        val reloadIV = dialog!!.findViewById<ImageView>(R.id.clearTV)
//        val noTv = dialog!!.findViewById<TextView>(R.id.noTV)
//        val alertsTitleTV = dialog!!.findViewById<TextView>(R.id.alertsTitleTV)
//        alertsTitleTV.text = "Find nearby device via Bluetooth"
//        btListRV!!.setLayoutManager(LinearLayoutManager(baseActivity))
//        if (bluetoothDeviceModels.size > 0) {
//            noTv.visibility = View.GONE
//            bluetooth_Adapter = null
//            setAdapterList(bluetoothDeviceModels)
//        } else {
//            btListRV!!.setVisibility(View.GONE)
//            noTv.visibility = View.VISIBLE
//        }
//        close_dialog.setOnClickListener {
//            dialog!!.dismiss()
//        }
    }

    fun calculteBTDistance(rssi: Int): Double {
        val A = 80
        val n = 4.3
        val iRssi = Math.abs(rssi)
        val power = (iRssi - A) / (10 * n)
        return Math.pow(10.0, power)
    }

    fun twoFraction(num: Double): BigDecimal {
        return try {
            var bd = BigDecimal(num.toString())
            bd = bd.setScale(2, BigDecimal.ROUND_FLOOR)
            bd
        } catch (e: Exception) {
            e.printStackTrace()
            BigDecimal(2)
        }
    }

    private fun checkBluetooth() {
        if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 100)
        } else {
            baseActivity.checkSelfPermission(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), object : BaseActivity.PermissionCallback {
                    override fun permGranted() {
                        registerBroadcastReciever()
                    }

                    override fun permDenied() {}
                })
        }
    }

    private fun registerBroadcastReciever() {
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        baseActivity.registerReceiver(receiver, filter)
        bluetoothRegistered = true
        bluetoothAdapter!!.startDiscovery()
    }

    private fun setDistance() {
        val distance = ArrayList<String>()
        distance.add("500m")
        distance.add("1km")
        distance.add("3km")
        distance.add("5km")
        distance.add("10km")
        distance.add("20km")
        distance.add("50km")
        distance.add("100km")
        distance.add("No Limit")
        setSpinnerAdapter(fragmentCreateSessionBinding.distanceSP, distance)
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothRegistered) {
            registerBroadcastReciever()
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.also { accelerometer ->
            sensorManager.registerListener(
                this@CreateSessionFragment,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (bluetoothRegistered) baseActivity.unregisterReceiver(receiver)
        sensorManager.unregisterListener(this)
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // "orientationAngles" now has up-to-date information.
    }




}