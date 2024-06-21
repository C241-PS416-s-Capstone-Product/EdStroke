package com.capstone.edstroke.view.maps

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edstroke.R
import com.capstone.edstroke.data.model.nearby.ModelResults
import com.capstone.edstroke.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import im.delight.android.location.SimpleLocation
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val REQ_PERMISSION = 100
    private lateinit var binding: ActivityMapsBinding
    var permissionArrays = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    lateinit var mapsView: GoogleMap
    lateinit var simpleLocation: SimpleLocation
    lateinit var progressDialog: ProgressDialog
    lateinit var mapViewModel: MapViewModel
    lateinit var mainAdapter: MapAdapter
    lateinit var strCurrentLocation: String
    var strCurrentLatitude = 0.0
    var strCurrentLongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        val setPermission = Build.VERSION.SDK_INT
        if (setPermission > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkIfAlreadyhavePermission() && checkIfAlreadyhavePermission2()) {
            } else {
                requestPermissions(permissionArrays, 101)
            }
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon Tunggu…")
        progressDialog.setCancelable(false)
        progressDialog.setMessage("sedang menampilkan lokasi rumah sakit")

        simpleLocation = SimpleLocation(this)
        if (!simpleLocation.hasLocationEnabled()) {
            SimpleLocation.openSettings(this)
        }

        //get location
        strCurrentLatitude = simpleLocation.latitude
        strCurrentLongitude = simpleLocation.longitude

        //set location lat long
        strCurrentLocation = "$strCurrentLatitude,$strCurrentLongitude"

        val supportMapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        supportMapFragment.getMapAsync(this)

        mainAdapter = MapAdapter(this)
        binding.rvListLocation.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        binding.rvListLocation.setAdapter(mainAdapter)
        binding.rvListLocation.setHasFixedSize(true)
    }

    private fun checkIfAlreadyhavePermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkIfAlreadyhavePermission2(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                val intent = intent
                finish()
                startActivity(intent)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapsView = googleMap

        //set text location
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addressList = geocoder.getFromLocation(strCurrentLatitude, strCurrentLongitude, 1)
            if (addressList != null && addressList.size > 0) {
                val strCity = addressList[0].locality
                val tvCity = findViewById<TextView>(R.id.tvCity)
                tvCity.text = strCity
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //viewmodel
        getLocationViewModel()
    }

    //get multiple marker
    private fun getLocationViewModel() {
        mapViewModel = ViewModelProvider(this, NewInstanceFactory()).get(MapViewModel::class.java)
        mapViewModel.setMarkerLocation(strCurrentLocation)
        progressDialog.show()
        mapViewModel.getMarkerLocation().observe(this) { mapResults ->
            if (mapResults.size != 0) {
                mainAdapter.setLocationAdapter(mapResults)
                //get multiple marker
                getMarker(mapResults)
                progressDialog.dismiss()
            } else {
                Toast.makeText(
                    this,
                    "Oops, tidak bisa mendapatkan lokasi kamu!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getMarker(modelResultsArrayList: ArrayList<ModelResults>) {
        for (i in modelResultsArrayList.indices) {
            //set LatLong from API
            val latLngMarker = LatLng(
                modelResultsArrayList[i].modelGeometry.modelLocation.lat,
                modelResultsArrayList[i].modelGeometry.modelLocation.lng
            )

            //get LatLong to Marker
            mapsView.addMarker(
                MarkerOptions()
                    .position(latLngMarker)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title(modelResultsArrayList[i].name)
            )

            //show Marker
            val latLngResult = LatLng(
                modelResultsArrayList[0].modelGeometry.modelLocation.lat,
                modelResultsArrayList[0].modelGeometry.modelLocation.lng
            )

            //set position marker
            mapsView.moveCamera(CameraUpdateFactory.newLatLng(latLngResult))
            mapsView.animateCamera(
                CameraUpdateFactory
                    .newLatLngZoom(
                        LatLng(
                            latLngResult.latitude,
                            latLngResult.longitude
                        ), 14f
                    )
            )
            mapsView.uiSettings.setAllGesturesEnabled(true)
            mapsView.uiSettings.isZoomGesturesEnabled = true
        }

        //click marker for change position recyclerview
        mapsView.setOnMarkerClickListener { marker ->
            val markerPosition = marker.position
            mapsView.addMarker(
                MarkerOptions()
                    .position(markerPosition)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
            var markerSelected = -1
            for (i in modelResultsArrayList.indices) {
                if (markerPosition.latitude == modelResultsArrayList[i].modelGeometry.modelLocation.lat && markerPosition.longitude == modelResultsArrayList[i].modelGeometry.modelLocation.lng) {
                    markerSelected = i
                }
            }
            val cameraPosition = CameraPosition.Builder().target(markerPosition).zoom(14f).build()
            mapsView.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            mainAdapter.notifyDataSetChanged()
            binding.rvListLocation.smoothScrollToPosition(markerSelected)
            marker.showInfoWindow()
            false
        }
    }

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PERMISSION && resultCode == RESULT_OK) {
            getLocationViewModel()
        }
    }

}
