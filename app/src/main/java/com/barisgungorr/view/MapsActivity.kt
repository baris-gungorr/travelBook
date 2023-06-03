package com.barisgungorr.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.barisgungorr.model.Place
import com.barisgungorr.roomdb.PlaceDao
import com.barisgungorr.roomdb.PlaceDatabase
import com.barisgungorr.travelbook.R
import com.barisgungorr.travelbook.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


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

    private lateinit var db : PlaceDatabase
    private lateinit var placeDao : PlaceDao
    val compositeDisposable = CompositeDisposable()
    var placeFromMain : Place? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        sharedPreferences = this.getSharedPreferences("com.barisgungorr.view", MODE_PRIVATE)  // init işlemi
        selectedLatidude = 0.0
        selectedLongitude = 0.0

        db = Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places")
          //  .allowMainThreadQueries()  ->MainThread içerisinde yap işlemleri diyoruz
            .build()

        placeDao = db.placeDao()
        binding.button.isEnabled = false

    }

    override fun onMapReady(googleMap: GoogleMap) { // harita hazır olduğunda çağrılan fonksiyon
        mMap = googleMap
        mMap.setOnMapClickListener(this)  // aktivite uygulayacak demeliyiz uzun tıklanınca ne olacak  <harita-listener> bağlantısı için gerekiyor
        val intent = intent
        val info = intent.getStringExtra("info")

        if (info == "new") {
            binding.button.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.GONE

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager  // init ediyoruz konum yöneticisini (as diyerek casting ediyoruz bundan eminim)

            locationListener = object  : LocationListener{  // init ediyoruz konum lisınırını
                override fun onLocationChanged(location: Location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)  //daha önce kaydedilmiş bir veri var mı kontrol etmek için kullanıyoruz yok ise

                    if (trackBoolean == false) {
                        val userLocation = LatLng(location.latitude,location.longitude)  // location'u lat long'a çevirme
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

            }else {  // izinin olduğu kısım
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,0f,locationListener) // lokasyon aldığımız kısım cihazı yorar güncellene
                //saniyede 1 için 1000 , mesafe için distance 10metre
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) //son bilinen konumu almak
                if (lastLocation != null){
                    val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude) // konumun daha önce alınmama ihtimaline karşı bir kontrol yaptık
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,10f))
                }
                mMap.isMyLocationEnabled = true // konumu etkinleştirdik mi

            }
            //LocationManager  -> location ile ilgili tüm işlemleri ele alıyor
            //LocationListener  -> konumda bir değişiklik olursa haber ver


            //Add a marker in Sydney and move the camera
            //val eifel = LatLng(48.85391,2.2913515)
            //mMap.addMarker(MarkerOptions().position(eifel).title("Eiffel Tower")) //mark ekleme
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eifel,10f))


        }else {
            mMap.clear()
            placeFromMain = intent.getSerializableExtra("selectedPlace") as? Place
            placeFromMain?.let {
                val latlng = LatLng(it.latitude,it.longitude)
                mMap.addMarker(MarkerOptions().position(latlng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15f))

                binding.placeText.setText(it.name)
                binding.button.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE


            }
        }


        // latitude,longitude -> enlem ve boylam
        //val sydney = LatLng(-34,0,151.0)  burayı eifel vs istediğimiz yeri ekleyebiliriz
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney)) -> bana bir pozisyon ver diyor kamera açıldığında beni buradan başlat diyoruz
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,10f)) -> zoom' işlemi yaparak istediğimiz yere daha yakın bir başlangıç yaptırabiliriz
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in sydney"))

        //** KULLANICI KONUM İŞLEMLERİ



    }
    private fun registerLauncher() {  // permissionLauncher ne olacak

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if (result) {
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // izin verildi
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null) {
                        val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,10f))
                    }
                    mMap.isMyLocationEnabled = true // konumu etkinleştirdik mi                                                                                          // izin verildiyse ==
                }

            }else{
                //reddedildi
                Toast.makeText(this@MapsActivity," izne ihtiyacım var!",Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onMapClick(p0: LatLng) {  // uzun tıkladığımızda ne olacak fonksiyon
        mMap.clear() // eski markerları silmek için kullanıyoruz
        mMap.addMarker(MarkerOptions().position(p0))  // marker ekleme

        selectedLatidude = p0.latitude  // kayıt için enlem ve boylamı ayrı değişkenlere atadık
        selectedLongitude = p0.longitude
        binding.button.isEnabled = true

    }
    fun save(view:View) {
    if (selectedLongitude != null && selectedLatidude != null) {

        val place = Place(binding.placeText.text.toString(),selectedLatidude!!,selectedLongitude!!)
        compositeDisposable.add(
            placeDao.insert(place)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )

            placeDao.insert(place)

    }

    }
    private fun handleResponse() {
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
    fun delete(view: View?) {
        placeFromMain?.let {
            compositeDisposable.add(placeDao.delete(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse))

        }


    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()  // hafızada çok fazla coll tutmaması için çöp torbası mantığı ile
    }

}