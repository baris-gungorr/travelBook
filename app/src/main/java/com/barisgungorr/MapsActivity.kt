package com.barisgungorr

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.barisgungorr.travelbook.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.barisgungorr.travelbook.databinding.ActivityMaps4Binding
import com.google.android.material.snackbar.Snackbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMaps4Binding
    private lateinit var locationManager : LocationManager // konum alma
    private lateinit var locationListener: LocationListener // konum alma
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean : Boolean? = false
    private var selectedLatidude : Double? = null
    private  var selectedLongitude : Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        binding = ActivityMaps4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        sharedPreferences = this.getSharedPreferences("com.barisgungorr", MODE_PRIVATE)
        selectedLatidude = 0.0
        selectedLongitude = 0.0

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener(this)

        //casting-- ben bunun location service olduğundan eminim diyoruz
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object  : LocationListener{
            override fun onLocationChanged(location: Location) {
                trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)
                if (trackBoolean == false!!) {
                    val userLocation = LatLng(location.latitude,location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,10f))
                }

            }

        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.root,"İzne ihtiyacım var ",Snackbar.LENGTH_INDEFINITE).setAction("ihtiyacım var") {
                    //izin iste
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

                }.show()
            }else{
            // izin iste
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

            }

        }else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener) // lokasyon aldığımız kısım
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastLocation != null){
                val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
            }
            mMap.isMyLocationEnabled = true // konumu etkinleştirdik mi

        }



    //Add a marker in Sydney and move the camera
    //val eifel = LatLng(48.85391,2.2913515)
    //mMap.addMarker(MarkerOptions().position(eifel).title("Eiffel Tower")) //mark ekleme
    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eifel,10f))


    }
    private fun registerLauncher() {  // permissionLauncher ne olacak

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if (result) {
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                // izin verildi
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                }
                mMap.isMyLocationEnabled = true


            }else{
                //reddedildi
                Toast.makeText(this@MapsActivity," izne ihtiyacım var!",Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onMapClick(p0: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0))

        selectedLatidude = p0.latitude
        selectedLongitude = p0.longitude

    }

}