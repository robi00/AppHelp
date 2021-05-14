package com.example.apphelp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap?= null
    lateinit var  mapView: MapView
    private val MAP_VIEW_BUNDLE_KEY= "MapViewBundleKey"

    private val DEFAULT_ZOOM = 15f
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onMapReady(googleMap: GoogleMap?) {
        mapView.onResume()
        mMap = googleMap

        askPermissionLocation()

        //si vede se si ha il permesso di localizzare telefono
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

        mapView = findViewById<MapView>(R.id.map1)

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

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

    //se non si ha il permesso di localizzare, il telefono chiede a utente di concederglielo
    private fun askPermissionLocation() {
        askPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) {

            getCurrentLocation()
        }.onDeclined{e->
            if(e.hasDenied()) {
                e.denied.forEach {
                }
                AlertDialog.Builder(this)
                    .setMessage("Please accept permissions. Otherwise you will not be able to use some of our important features")
                    .setPositiveButton("yes") { _, _ ->
                        e.askAgain()
                    }
                    .setNegativeButton("no") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun getCurrentLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@MainActivity)

        try {
            @SuppressLint("MissingPermission") val location =
                fusedLocationProviderClient!!.getLastLocation()

            location.addOnCompleteListener(object : OnCompleteListener<Location> {
                override fun onComplete(loc: Task<Location>) {
                    if (loc.isSuccessful) {
                        val currentLocation = loc.result as Location?
                        if(currentLocation != null) {
                            moveCamera(//to move map tu current location
                                LatLng(currentLocation.latitude, currentLocation.longitude),
                                DEFAULT_ZOOM
                            )
                        }
                    }else {
                        askPermissionLocation()
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
}

