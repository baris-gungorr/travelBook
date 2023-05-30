package com.barisgungorr

import android.Manifest
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMaps4Binding
    private lateinit var locationManager : LocationManager // konum alma
    private lateinit var locationListener: LocationListener // konum alma
    private lateinit var permissionLauncher : ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        binding = ActivityMaps4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //casting-- ben bunun location service olduğundan eminim diyoruz
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object  : LocationListener{
            override fun onLocationChanged(location: Location) {
              // println("location: "+ location.toString())
                val userLocation = LatLng(location.latitude,location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))

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

            }else{
                //reddedildi
                Toast.makeText(this@MapsActivity," izne ihtiyacım var!",Toast.LENGTH_LONG).show()
            }
        }

    }

}