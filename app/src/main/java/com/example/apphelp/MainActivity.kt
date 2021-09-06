package com.example.apphelp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*
import java.math.RoundingMode.valueOf
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.concurrent.timer


var myLongitude: Double = 0.0
var myLatitude: Double = 0.0
lateinit var mp: MediaPlayer

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap?= null
    lateinit var  mapView: MapView
    private val MAP_VIEW_BUNDLE_KEY= "MapViewBundleKey"
    private val DEFAULT_ZOOM = 15f
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    lateinit var mDatabaseHelper: DatabaseHelper


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        mapView.onResume()
        mMap = googleMap

        askPermissionLocation()

        //it checks if it has permission to locate the phone
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap!!.setMyLocationEnabled(true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // when the user clicks on the "where am I?" button the function getCurrentLocation is called
        buttonLoc.setOnClickListener{
            getCurrentLocation()
        }
        // when the user clicks on the "settings" button the function Activity2 is called
        buttonSett.setOnClickListener {
            val m_intent = Intent(this@MainActivity, Activity2::class.java)
            startActivity(m_intent)
        }
        // when the user clicks on the "SOS" button the functions sendSms and reproduceSound are called
        buttonSOS.setOnClickListener {
            sendSms()
            reproduceSound()
        }
        // when the user clicks on the "stop sound" the function stopSound is called
        buttSound.setOnClickListener {
            stopSound()
        }
        mapView = findViewById(R.id.map1)

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        mDatabaseHelper = DatabaseHelper(this)

    }

    public override fun onSaveInstanceState(outState: Bundle){
        super.onSaveInstanceState(outState)

        askPermissionLocation()
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    //if the phone does not have permission to locate, it asks the user to grant it
    private fun askPermissionLocation() {
        askPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) {
            getCurrentLocation() //if the permission is granted it calls the function getCurrentLocation()

        }.onDeclined{e-> //if the permission isn't granted
            if(e.hasDenied()) {
                e.denied.forEach {
                }
                AlertDialog.Builder(this) // creates a pop up to ask the user to give the permission
                    .setMessage("Please accept permissions. Otherwise you will not be able to use some of our important features")
                    .setPositiveButton("yes") { _, _ -> //if the user click on "yes", askPermission
                        e.askAgain()
                    }
                    .setNegativeButton("no") { dialog, _ -> //if the user click on "no", the pop up closes without performing any action
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@MainActivity)

        try {
            val location = fusedLocationProviderClient!!.lastLocation

            location.addOnCompleteListener(object : OnCompleteListener<Location> {
                override fun onComplete(loc: Task<Location>) {
                    if (loc.isSuccessful) {
                        val currentLocation = loc.result
                        if(currentLocation != null) {
                            moveCamera(//to move map to current location
                                LatLng(currentLocation.latitude, currentLocation.longitude),
                                DEFAULT_ZOOM
                            )
                        }
                        myLongitude = currentLocation.longitude
                        myLatitude = currentLocation.latitude
                    }else {
                        askPermissionLocation() //if the phone does not have permission to locate, it asks the user to grant it
                    }
                }
            })
        } catch (se: Exception) {
            Log.e("TAG", "Security Exception")
        }
    }
    private fun moveCamera(latLng: LatLng, zoom: Float){
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun sendSms() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) +
                    ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS))
            != PackageManager.PERMISSION_GRANTED) { //if the phone does not have permission to send sms, it asks the user to grant it
            askPermissionSms()
        } else {
            thread { // a thread is used so you can send sms every x time with the updated position
                var phone: Cursor = mDatabaseHelper.getData()
                var listPhone: ArrayList<String> = ArrayList()
                if(phone.moveToFirst()) {
                    do{
                    listPhone.add(phone.getString(2))
                    }while (phone.moveToNext())
                }

                var obj = SmsManager.getDefault()

                getCurrentLocation() //per prendere la posizione corrente (per sms)

                for(k in 0..5) {
                    var i = listPhone.size
                    var j = 0
                    while (j < i) {
                        var number = listPhone[j]
                        obj.sendTextMessage("$number",
                            null,
                            "HELP ME, my position is: \n $myLatitude, $myLongitude ",
                            null,
                            null)
                        j++
                    }
                    Thread.sleep(60000)
                } //5 messages are sent to each emergency contact, one message per minute
            }
        }
    }
    //if the phone does not have permission to send sms, it asks the user to grant it
    private fun askPermissionSms() {
        askPermission(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS
        ) {
            sendSms() //if the permission is granted it calls the function sendSms()
        }.onDeclined{e->
            if(e.hasDenied()) { //if the permession isn't granted
                e.denied.forEach {
                }
                AlertDialog.Builder(this) //create a pop up to ask the user to give the permission
                    .setMessage("Please accept permissions. Otherwise you will not be able to use some of our important features")
                    .setPositiveButton("yes") { _, _ -> //if the user click on "yes", askPermission
                        e.askAgain()
                    }
                    .setNegativeButton("no") { dialog, _ -> //if the user click on "no", the pop up closes without performing any action
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }
    //to play the sound imported into resources
    private fun reproduceSound() {
        mp = MediaPlayer.create(this, R.raw.police)
        mp.start()

    }
    //to stop the sound
    private fun stopSound() {
        mp.stop()
    }
}

