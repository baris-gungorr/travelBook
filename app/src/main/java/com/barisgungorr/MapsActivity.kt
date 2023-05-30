package com.barisgungorr

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
import com.barisgungorr.travelbook.databinding.ActivityMapsBinding
import com.google.android.material.snackbar.Snackbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapClickListener {  // burada uzun tıkladığımızda ne olacağını söylediğimiz listener'ı ekledik

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager : LocationManager // konum almak için yapı 1 -konum yöneticisi
    private lateinit var locationListener: LocationListener // konum alma yapı 2  - konum dinleyicisi
    private lateinit var permissionLauncher : ActivityResultLauncher<String> // izin işlemleri -> kullanmasak bile bir register işlemi yapmamız gerekiyor
    private lateinit var sharedPreferences: SharedPreferences // son konum kaydetmek için
    private var trackBoolean : Boolean? = false // takip bulyını
    private var selectedLatidude : Double? = null
    private  var selectedLongitude : Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        sharedPreferences = this.getSharedPreferences("com.barisgungorr", MODE_PRIVATE)  // init işlemi
        selectedLatidude = 0.0
        selectedLongitude = 0.0

    }

    override fun onMapReady(googleMap: GoogleMap) { // harita hazır olduğunda çağrılan fonksiyon
        mMap = googleMap
        mMap.setOnMapClickListener(this)  // aktivite uygulayacak demeliyiz uzun tıklanınca ne olacak  <harita-listener> bağlantısı için gerekiyor

        // latitude,longitude -> enlem ve boylam
        //val sydney = LatLng(-34,0,151.0)  burayı eifel vs istediğimiz yeri ekleyebiliriz

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney)) -> bana bir pozisyon ver diyor kamera açıldığında beni buradan başlat diyoruz
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,10f)) -> zoom' işlemi yaparak istediğimiz yere daha yakın bir başlangıç yaptırabiliriz
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in sydney"))

        //** KULLANICI KONUM İŞLEMLERİ

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager  // init ediyoruz konum yöneticisini (as diyerek casting ediyoruz bundan eminim)

        locationListener = object  : LocationListener{  // init ediyoruz konum lisınırını
            override fun onLocationChanged(location: Location) {
                trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)  //daha önce kaydedilmiş bir veri var mı kontrol etmek için kullanıyoruz yok ise

                if (trackBoolean == false!!) {
                    val userLocation = LatLng(location.latitude,location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,10f))
                    sharedPreferences.edit().putBoolean("trackBoolean",true).apply() // tekrar çağrılıyor ve değeri true yani bir konum alındıysa tekrar çağrılmıyor
                }
            }
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { // önceki vers.Uyumluluğu için contexCompat
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.root,"İzne ihtiyacım var ",Snackbar.LENGTH_INDEFINITE).setAction("İzne ihtiyacım var !") {
                    //izin iste
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

                }.show()
            }else{
            // izin iste
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

            }

        }else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener) // lokasyon aldığımız kısım cihazı yorar güncellene
                                                                                                                            //saniyede 1 için 1000 , mesafe için distance 10metre
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) //son bilinen konum
            if (lastLocation != null){
                val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude) // konumun daha önce alınmama ihtimaline karşı bir kontrol yaptık
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,10f))
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
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) // izin verildiyse ==
                // izin verildi
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                }
                mMap.isMyLocationEnabled = true // konumu etkinleştirdik mi


            }else{
                //reddedildi
                Toast.makeText(this@MapsActivity," izne ihtiyacım var!",Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onMapClick(p0: LatLng) {  // uzun tıkladığımızda ne olacak fonksiyonu
        mMap.clear() // eski markerları silmek için kullanıyoruz
        mMap.addMarker(MarkerOptions().position(p0))  // marker ekleme

        selectedLatidude = p0.latitude  // kayıt için enlem ve boylamı ayrı değişkenlere atadık
        selectedLongitude = p0.longitude

    }
    fun save(view:View) {

    }
    fun deleteButton(view: View) {


    }

}