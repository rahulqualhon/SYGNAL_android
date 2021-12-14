package com.android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList
import java.util.HashMap

import cz.msebera.android.httpclient.HttpEntity
import cz.msebera.android.httpclient.HttpResponse
import cz.msebera.android.httpclient.client.HttpClient
import cz.msebera.android.httpclient.client.methods.HttpPost
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient

class GoogleApisHandle {
    private var routeMap: GoogleMap? = null
    private var context: Context? = null
    private var origin: LatLng? = null
    private var destination: LatLng? = null

    private var onPolyLineReceived: OnPolyLineReceived? = null
    private var roateMarkerRunnable: RoateMarkerRunnable? = null
    private var animateMarkerRunnable: AnimateMarkerRunnable? = null
    private val handler = Handler()

    private fun setAct(mAct: Context) {
        this.context = mAct
    }

    fun decodeAddressFromLatLng(lat: Double, lang: Double): String {
        try {
            val geocoder: Geocoder
            var fullAddress = "Not Found"
            val addresses: List<Address>
            geocoder = Geocoder(context)
            if (lat != 0.0 || lang != 0.0) {
                addresses = geocoder.getFromLocation(lat, lang, 1)
                if (addresses.size > 0) {
                    val address = addresses[0].getAddressLine(0)
                    val city = addresses[0].getAddressLine(1)
                    val country = addresses[0].getAddressLine(2)
                    val state = addresses[0].subLocality
                    fullAddress = (address
                            ?: "") + (if (city != null) ", $city" else "") + (if (state != null) ", $state" else "") + if (country != null) ", $country" else ""
                } else if (fullAddress == "" || fullAddress == "Not Found") {
                    val json = getJSONfromURL("http://maps.googleapis.com/maps/api/geocode/json?latlng=$lat,$lang&sensor=true")
                    try {
                        if (json!!.getJSONArray("results").length() > 0)
                            return json.getJSONArray("results").getJSONObject(0).getString("formatted_address")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
                return fullAddress
            } else {
                return fullAddress
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Not Found"
        }

    }

    fun getLatLngFromAddress(address: String): LatLng {
        val addresses: List<Address>
        val geocoder = Geocoder(context)
        try {
            val addressList = geocoder.getFromLocationName(address, 1)
            if (addressList.size > 0)
                return LatLng(addressList[0].latitude, addressList[0].longitude)
            else {
                val `object` = getJSONfromURL("https://maps.googleapis.com/maps/api/geocode/json?address=" + address.replace(" ", "%20"))
                val jsonObject = `object`!!.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location")
                return LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lng"))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return LatLng(0.0, 0.0)
    }

    private fun getJSONfromURL(url: String): JSONObject? {

        // initialize
        var `is`: InputStream? = null
        var result = ""
        var jObject: JSONObject? = null


        try {
            val httpclient = DefaultHttpClient()
            val httppost = HttpPost(url)
            val response = httpclient.execute(httppost)
            val entity = response.entity
            `is` = entity.content

        } catch (e: Exception) {
            Log.e("log_tag", "Error in http connection $e")
        }

        // convert response to string
        try {
            val reader = BufferedReader(InputStreamReader(`is`, "iso-8859-1"), 8)
            val sb = StringBuilder()
            val line: String? =reader.readLine()
            while (line  != null) {
                sb.append(line + "\n")
            }
            `is`!!.close()
            result = sb.toString()
        } catch (e: Exception) {
            Log.e("log_tag", "Error converting result $e")
        }

        // try parse the string to a JSON object
        try {
            jObject = JSONObject(result)
        } catch (e: JSONException) {
            Log.e("log_tag", "Error parsing data $e")
        }

        return jObject
    }

    fun getDirectionsUrl(origin: LatLng, dest: LatLng, googleMap: GoogleMap) {
        val str_origin = ("origin=" + origin.latitude + ","
                + origin.longitude)
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude
        val sensor = "sensor=false"
        val parameters = "$str_origin&$str_dest&$sensor"
        val output = "json"
        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
        DownloadTask(origin, dest, googleMap).execute(url)
    }

    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()
            iStream = urlConnection.inputStream
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            val line = br.readLine();
            while (line != null) {
                sb.append(line)
            }
            data = sb.toString()
            br.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            iStream?.close()
            urlConnection?.disconnect()
        }
        return data
    }

    private fun bearingBetweenLocations(fromLat: Double, fromLong: Double, toLat: Double, toLong: Double): Double {
        val PI = 3.14159
        val lat1 = fromLat * PI / 180
        val long1 = fromLong * PI / 180
        val lat2 = toLat * PI / 180
        val long2 = toLong * PI / 180

        val dLon = long2 - long1

        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)

        var brng = Math.atan2(y, x)

        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360

        return brng
    }


    fun animateMarkerToGB(finalPosition: LatLng, oldMarker: Marker, isCameraAnimate: Boolean, googleMap: GoogleMap) {

        val startPosition = oldMarker.position
        val lat1 = startPosition.latitude
        val lng1 = startPosition.longitude
        val lat2 = finalPosition.latitude
        val lng2 = finalPosition.longitude
        if (roateMarkerRunnable != null)
            handler.removeCallbacks(roateMarkerRunnable!!)
        if (animateMarkerRunnable != null)
            handler.removeCallbacks(animateMarkerRunnable!!)
        rotateMarker(lat1, lng1, lat2, lng2, oldMarker, handler, isCameraAnimate, googleMap)

    }


    private inner class AnimateMarkerRunnable internal constructor(internal var start: Long, internal var durationInMs: Float, internal var interpolator: Interpolator, internal var oldMarker: Marker, internal var latLngInterpolator: LatLngInterpolator, internal var startPosition: LatLng, internal var finalPosition: LatLng, internal var isCameraAnimate: Boolean, internal var googleMap: GoogleMap, internal var handler: Handler) : Runnable {
        internal var elapsed: Long = 0
        internal var t: Float = 0.toFloat()
        internal var v: Float = 0.toFloat()

        override fun run() {
            elapsed = SystemClock.uptimeMillis() - start
            t = elapsed / durationInMs
            v = interpolator.getInterpolation(t)
            oldMarker.position = latLngInterpolator.interpolate(v, startPosition, finalPosition)
            if (isCameraAnimate) {
                val builder = LatLngBounds.Builder()
                builder.include(startPosition)
                builder.include(finalPosition)
                val bounds = builder.build()
                val padding = 10
                val cu = CameraUpdateFactory.newLatLngBounds(bounds,
                        padding)
                googleMap.animateCamera(cu)
            }
            //Repeat till progress is complete.
            if (t < 1) {                     // Post again 16ms later.
                animateMarkerRunnable?.let { handler.postDelayed(it, 16) }
            } else {
                animateMarkerRunnable?.let { handler.removeCallbacks(it) }
                animateMarkerRunnable = null
            }
        }
    }

    fun rotateMarker(fromLat: Double, fromLong: Double, toLat: Double, toLong: Double, marker: Marker, handler: Handler, isCameraAnimate: Boolean, googleMap: GoogleMap) {
        val brng = bearingBetweenLocations(fromLat, fromLong, toLat, toLong)
        val start = SystemClock.uptimeMillis()
        val startRotation = marker.rotation
        val toRotation = brng.toFloat()
        val duration: Long = 1000
        val finalPosition = LatLng(toLat, toLong)
        val interpolator = LinearInterpolator()
        roateMarkerRunnable = RoateMarkerRunnable(start, interpolator, duration.toFloat(), toRotation, startRotation, marker, handler, finalPosition, isCameraAnimate, googleMap)
        handler.post(roateMarkerRunnable!!)
    }

    private inner class RoateMarkerRunnable(internal var start: Long, internal var interpolator: Interpolator, internal var duration: Float, internal var toRotation: Float, internal var startRotation: Float, internal var marker: Marker, internal var handler: Handler, internal var finalPosition: LatLng, internal var isCameraAnimate: Boolean, internal var googleMap: GoogleMap) : Runnable {

        override fun run() {
            val elapsed = SystemClock.uptimeMillis() - start
            val t = interpolator.getInterpolation(elapsed.toFloat() / duration)

            val rot = t * toRotation + (1 - t) * startRotation

            marker.rotation = if (-rot >= 180) rot / 2 else rot
            if (t < 1.0) {
                // Post again 16ms later.
                roateMarkerRunnable?.let { handler.postDelayed(it, 16) }
            } else {
                roateMarkerRunnable?.let { handler.removeCallbacks(it) }
                roateMarkerRunnable = null
                animateMarker(marker, finalPosition, isCameraAnimate, googleMap)
            }
        }
    }

    private fun animateMarker(oldMarker: Marker, finalPosition: LatLng, isCameraAnimate: Boolean, googleMap: GoogleMap) {
        val startPosition = oldMarker.position
        val latLngInterpolator = LatLngInterpolator.Spherical()
        val start = SystemClock.uptimeMillis()
        val interpolator = AccelerateDecelerateInterpolator()
        val durationInMs = (9 * 1000).toFloat()
        animateMarkerRunnable = AnimateMarkerRunnable(start, durationInMs, interpolator, oldMarker, latLngInterpolator, startPosition, finalPosition, isCameraAnimate, googleMap, handler)
        handler.post(animateMarkerRunnable!!)
    }


    interface DistanceCalculated {

        fun sendDistance(distance: Double)
    }

    inner class DownloadTask : AsyncTask<String, Void, String> {

        constructor(source: LatLng, dest: LatLng, map: GoogleMap) {

            origin = source
            destination = dest
            routeMap = map
        }

        constructor(source: LatLng, dest: LatLng, distanceCalculated: DistanceCalculated) {

            onDistanceCalculated = distanceCalculated
            origin = source
            destination = dest
        }


        override fun doInBackground(vararg url: String): String {
            var data = ""
            try {
                data = downloadUrl(url[0])
            } catch (e: Exception) {
                Log.d("Background Task", e.toString())
            }

            return data
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            val parserTask = ParserTask()
            parserTask.execute(result)

        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class ParserTask : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {
        override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>? {
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                jObject = JSONObject(jsonData[0])
                val parser = DirectionsJSONParser()
                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return routes
        }

        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            var points: ArrayList<LatLng>? = null
            var lineOptions = PolylineOptions()
            if (result == null) {
                return
            }
            if (result.size < 1) {
                return
            }

            for (i in result.indices) {
                points = ArrayList()
                lineOptions = PolylineOptions()
                val path = result[i]
                for (j in path.indices) {
                    val point = path[j]
                    if (j == 0) {
                        val line = point["distance"]
                        if (line != null) {
                            val parts = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            val distance = java.lang.Double.parseDouble(parts[0].replace(",", "."))

                            val dis = Math.ceil(distance).toInt()

                            if (onDistanceCalculated != null) {
                                onDistanceCalculated!!.sendDistance(distance)
                            }
                        }
                        continue

                    } else if (j == 1) {

                        val duration = point["duration"]
                        if (duration!!.contains("hours") && (duration.contains("mins") || duration
                                        .contains("min"))) {

                            val arr = duration.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            var timeDur = 0
                            for (k in arr.indices) {
                                if (k == 0)
                                    timeDur = Integer.parseInt(arr[k]) * 60
                                if (k == 2)
                                    timeDur = timeDur + Integer.parseInt(arr[k])

                            }

                            //                            totalDuration = String.valueOf(timeDur);

                        } else if (duration.contains("mins") || duration.contains("min")) {
                            val words = duration.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            //                            totalDuration = words[0];
                        }
                        continue
                    }

                    val lat = java.lang.Double.parseDouble(point["lat"]!!)
                    val lng = java.lang.Double.parseDouble(point["lng"]!!)
                    val position = LatLng(lat, lng)
                    points.add(position)
                }

                lineOptions.addAll(points)
                lineOptions.width(5f)
                lineOptions.color(Color.BLUE)
            }

            if (routeMap != null && onPolyLineReceived != null) {
                routeMap!!.addPolyline(lineOptions)
                if (onPolyLineReceived != null)
                    onPolyLineReceived!!.onPolyLineReceived(origin, destination, routeMap!!)
                val builder = LatLngBounds.Builder()
                builder.include(origin!!)
                builder.include(destination!!)
                val bounds = builder.build()
                val padding = 10
                val cu = CameraUpdateFactory.newLatLngBounds(bounds,
                        padding)
                // routeMap.moveCamera(cu);
                routeMap!!.animateCamera(cu)
            }
        }
    }

    fun setPolyLineReceivedListener(onPolyLineReceived: OnPolyLineReceived) {
        this.onPolyLineReceived = onPolyLineReceived
    }

    interface OnPolyLineReceived {
        fun onPolyLineReceived(origin: LatLng?, destination: LatLng?, routeMap: GoogleMap)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private val mapUtils = GoogleApisHandle()
        private var onDistanceCalculated: DistanceCalculated? = null

        fun with(context: Context): GoogleApisHandle {
            mapUtils.setAct(context)
            return mapUtils
        }
    }


}
