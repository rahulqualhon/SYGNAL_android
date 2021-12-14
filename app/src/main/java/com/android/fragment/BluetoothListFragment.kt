package com.android.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Parcel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.activity.BaseActivity
import com.android.activity.MainActivity
import com.android.adapter.Bluetooth_Adapter
import com.android.databinding.FragmentBluetoothListBinding
import com.android.models.BluetoothDeviceModel
import java.math.BigDecimal
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.ToneGenerator

import com.android.R


import android.media.RingtoneManager
import android.net.Uri
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import android.media.Ringtone
import android.util.Log
import android.widget.Toast
import java.util.*


class BluetoothListFragment : BaseFragment(), SensorEventListener, LocationListener {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothRegistered = false
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val bluetoothDeviceModels = HashMap<String, BluetoothDeviceModel>()
    private var dialog: Dialog? = null
    private var bluetooth_Adapter: Bluetooth_Adapter? = null
    private var locationManager: LocationManager? = null

    private var gasName = ""
    private var percentage = ""
    private var minAlert = ""
    private var maxAlert = ""
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private var isPremission = false
    private var isFound = false
    var player: MediaPlayer? = null
    private lateinit var binding: FragmentBluetoothListBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (baseActivity as MainActivity).setToolbarHeadings("Tracker list", true)
        binding = FragmentBluetoothListBinding.inflate(inflater)
        return binding.root


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addGasBt.setOnClickListener(this)
        val bluetoothManager =
            baseActivity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        sensorManager = baseActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager


        if (arguments != null) {
            gasName = requireArguments().getString("gasName")!!.toString()
            percentage = requireArguments().getString("percentage")!!.toString()
            minAlert = requireArguments().getString("minAlert")!!.toString()
            maxAlert = requireArguments().getString("alertValue")!!.toString()
            locationManager = activity!!.getSystemService(LOCATION_SERVICE) as LocationManager

        }
    }


//

    override fun onClick(v: View) {
        super.onClick(v)
        when (v) {
            binding.addGasBt -> {
                baseActivity.gotoFragmentWithStack(AddGasFragment())
            }
        }

    }

    override fun onLocationChanged(location: Location) {
        checkBluetooth()
    }


    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val range: Double
            val min:String
            val max:String
            var bigDecimal: BigDecimal

            val bluetoothDeviceModel = BluetoothDeviceModel(Parcel.obtain())

                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val rssi =
                        intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                    var distance: Double = calculateBTDistance(rssi)

                    if (device!!.name != null && device.name.contains(gasName) ) {
                        bluetoothDeviceModel.bluetooth_name = device.name
                        //bluetoothDeviceModel.gasScale = percentage
                        bluetoothDeviceModel.maxValue = maxAlert
                        bluetoothDeviceModel.minValue = minAlert
                        min = minAlert.trim().replace(" ft", "")
                        max = maxAlert.trim().replace(" ft", "")

                        val value = max.toDouble() - min.toDouble()
                        val res: Double = distance / value * 10
                        val calculatedDistance: Double = twoFraction(res).toDouble()
                        bluetoothDeviceModel.gasScale = twoFraction(res).toString()
                        when {
                            calculatedDistance < min.toDouble() -> {
                                bluetoothDeviceModel.gasScale = "0.0"
                            }
                            calculatedDistance > max.toDouble() -> {
                                bluetoothDeviceModel.gasScale = "100"
                            }
                            else -> {
                                bluetoothDeviceModel.gasScale = twoFraction(res).toString()
                            }
                        }
                        bluetoothDeviceModel.bluetooth_rssi = baseActivity.getString(
                            R.string.formatter_db,
                            rssi.toString()
                        ) // MAC address
                        distance *= 3.3
                        bigDecimal = twoFraction(distance)
                        bluetoothDeviceModel.distancevalue = distance
                        range = bigDecimal.toDouble()
                        bluetoothDeviceModel.isDistance = range in min.toDouble()..max.toDouble()
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
                    if(!isFound){
                        baseActivity.startProgressDialog()
                    }

                    bluetoothDeviceModels.clear()
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                    if(!isFound){
                        baseActivity.stopProgressDialog()
                    }

//                if (refreshV != null)
//                    refreshV.clearAnimation();
                    if (dialog != null && dialog!!.isShowing) {
                        setAdapterList(bluetoothDeviceModels)
                    } else {
                        if (bluetoothDeviceModels.size > 0) {
                            isFound = true
                            binding.noTV.visibility = View.GONE
                            bluetooth_Adapter = null
                            setAdapterList(bluetoothDeviceModels)
                        } else {
                            binding.btListRV.visibility = View.GONE
                            binding.noTV.visibility = View.VISIBLE

                        }
                    }

                }

        }

    }

    private fun setAdapterList(bluetoothDeviceModels: HashMap<String, BluetoothDeviceModel>) {
        val values: Collection<BluetoothDeviceModel?> = bluetoothDeviceModels.values
        val listOfValues =
            java.util.ArrayList<BluetoothDeviceModel?>(values)
        playAlertTone(listOfValues[0]?.isDistance!!)
        binding.btListRV.adapter = Bluetooth_Adapter(baseActivity,listOfValues)
    }

    fun calculateBTDistance(rssi: Int): Double {
        val A = 80
        val n = 4.3
        val iRssi = Math.abs(rssi)
        val power = (iRssi - A) / (10 * n)
        return Math.pow(10.0, power)
    }

    fun twoFraction(num: Double): BigDecimal {
        return try {
            var bd = BigDecimal(num.toString())
            bd = bd.setScale(1, BigDecimal.ROUND_FLOOR)
            bd
        } catch (e: Exception) {
            e.printStackTrace()
            BigDecimal(1)
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
                        isPremission = true
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


    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (bluetoothRegistered) {
            registerBroadcastReciever()
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.also { accelerometer ->
            sensorManager.registerListener(
                this@BluetoothListFragment,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        if(isPremission){
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 1.0f, this)
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1.0f, this)
        }else{
            baseActivity.checkSelfPermission(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), object : BaseActivity.PermissionCallback {
                    override fun permGranted() {
                        isPremission = true
                    }

                    override fun permDenied() {

                    }
                })
        }

    }


    override fun onPause() {
        super.onPause()
        if (locationManager != null) {
            locationManager?.removeUpdates(this)
        }
        if (bluetoothRegistered) baseActivity.unregisterReceiver(receiver)
        sensorManager.unregisterListener(this)
        if (player != null){
            player?.stop()
            player?.release()
            player=null
        }
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

    fun playAlertTone(isFound: Boolean) {
        if(!isFound) {
            if (player == null) {
                player = MediaPlayer()
                player = MediaPlayer.create(baseActivity, R.raw.beep_09)
                player?.start()
            }
            player?.setOnCompletionListener(OnCompletionListener { mp: MediaPlayer? -> player!!.start() })
           // player?.isLooping=true

        }else if (player != null){
            player?.stop()
            player?.release()
            player=null
        }


//        if(!isFound){
//            player.start();
//        }else{
//            player.release();
//        }
//        player.start();


//        Thread t = null;
//        Thread t = new Thread() {
//            public void run() {
//                if(!isFound){
//                    if(player.isPlaying()){
//                        player.release();
//                    }
//                    while (true) {
//                        player = MediaPlayer.create(context, R.raw.beep_09);
//                        player.start();
//                        Log.d("cmskjdncd","start");
//                        try {
//
//                            // 100 milisecond is duration gap between two beep
//                            Thread.sleep(player.getDuration() + 100);
//                            player.release();
//                        } catch (InterruptedException e) {
//                            Log.d("cmskjdncd","csbcsjbcb");
//                            e.printStackTrace();
//                        }
//                    }
//                }else{
//                    if(player!=null){
//                        player.release();
//                    }
//
//                }
//
//            }
//        };
//        t.start();
//
//    }
    }

}