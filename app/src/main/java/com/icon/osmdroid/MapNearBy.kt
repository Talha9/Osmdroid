package com.icon.osmdroid

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.icon.osmdroid.databinding.ActivityMapNearByBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker

class MapNearBy : AppCompatActivity() {
    lateinit var binding:ActivityMapNearByBinding
    var poiMarkers = FolderOverlay(this@MapNearBy)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMapNearByBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //set Default Map Zoom
        binding.map.setBuiltInZoomControls(true)
        val mapController = binding.map.controller
        mapController.setZoom(9.5)

        //set Default Location
        val startPoint = GeoPoint(33.522207, 73.153886)
        mapController.setCenter(startPoint)


        //set MultiTouch Control
        binding.map.setMultiTouchControls(true)


        //set Default Map Style
        binding.map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)



        binding.geoNames.setOnClickListener {
            generateGeoNamesNearByMarkers(startPoint)
        }
        binding.nomination.setOnClickListener {
            generateNominationNearByMarkers(startPoint)
        }


    }

    override fun onResume() {
        super.onResume()
       binding.map.onResume()


    }

    override fun onPause() {
        binding.map.onPause()
        super.onPause()

    }

    private fun generateGeoNamesNearByMarkers(point: GeoPoint) {
        binding.map.overlays.remove(poiMarkers)
        poiMarkers= FolderOverlay(this)
        GlobalScope.launch {
            val poiProvider = GeoNamesPOIProvider("ta99lha")
            val pois = poiProvider.getPOICloseTo(point, 50, 10.0)

            withContext(Dispatchers.Main) {
                binding.map.overlays.add(poiMarkers)
            }


            val poiIcon = getDrawable(R.drawable.ic_baseline_question_mark_24)

            poiMarkers.remove(poiMarkers)

            for (poi in pois) {
                val poiMarker = Marker(binding.map)
                poiMarker.position = poi.mLocation
                poiMarker.icon = poiIcon
                val v = LayoutInflater.from(this@MapNearBy).inflate(R.layout.custom_window, null)
                val myTitle = v.findViewById<TextView>(R.id.title)
                val myDesc = v.findViewById<TextView>(R.id.desc)
                val img = v.findViewById<ImageView>(R.id.img)
                val closeBtn = v.findViewById<ImageView>(R.id.closeBtn)
                withContext(Dispatchers.Main) {
                    myTitle.text = poi.mType
                    myDesc.text = poi.mDescription
                    img.setImageDrawable(getDrawable(R.drawable.ic_baseline_close_24))
                    closeBtn.setOnClickListener {
                        poiMarker.closeInfoWindow()
                    }
                    img.setOnClickListener {
                        if (poi.mUrl != null) {
                            val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(poi.mUrl))
                            startActivity(myIntent)
                        }
                    }
                }

                poiMarker.infoWindow = CustomInfoWindow(v, binding.map)

                poiMarkers.add(poiMarker)

            }
        }
    }

    private fun generateNominationNearByMarkers(point: GeoPoint) {
        binding.map.overlays.remove(poiMarkers)
        poiMarkers= FolderOverlay(this)
        GlobalScope.launch {
            val poiProvider = NominatimPOIProvider("OSMBonusPackTutoUserAgent")
            val pois = poiProvider.getPOICloseTo(point,"fuel",100,2.0)

            withContext(Dispatchers.Main) {
                binding.map.overlays.add(poiMarkers)
            }
            val poiIcon = getDrawable(R.drawable.ic_baseline_question_mark_24)


            for (poi in pois) {
                val poiMarker = Marker(binding.map)
                poiMarker.position = poi.mLocation
                poiMarker.icon = poiIcon
                val v = LayoutInflater.from(this@MapNearBy).inflate(R.layout.custom_window, null)
                val myTitle = v.findViewById<TextView>(R.id.title)
                val myDesc = v.findViewById<TextView>(R.id.desc)
                val img = v.findViewById<ImageView>(R.id.img)
                val closeBtn = v.findViewById<ImageView>(R.id.closeBtn)
                withContext(Dispatchers.Main) {
                    myTitle.text = poi.mType
                    myDesc.text = poi.mDescription
                    img.setImageDrawable(getDrawable(R.drawable.ic_baseline_close_24))
                    closeBtn.setOnClickListener {
                        poiMarker.closeInfoWindow()
                    }
                    img.setOnClickListener {
                        if (poi.mUrl != null) {
                            val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(poi.mUrl))
                            startActivity(myIntent)
                        }
                    }
                }

                poiMarker.infoWindow = CustomInfoWindow(v, binding.map)
                poiMarkers.add(poiMarker)

            }
        }
    }
}