package com.icon.osmdroid


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.icon.osmdroid.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex











class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    lateinit var map: MapView
    var MAPBOXSATELLITELABELLED: OnlineTileSourceBase? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //load/initialize the osmdroid configuration, this can be done
        //load/initialize the osmdroid configuration, this can be done
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        map = findViewById(R.id.map)




        requestPermissionsIfNecessary(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))


        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setZoom(9.5)
        val startPoint = GeoPoint(33.6844, 73.0479)
        mapController.setCenter(startPoint)
        map.setTileSource(TileSourceFactory.MAPNIK)

        /* map.setTileSource(object : OnlineTileSourceBase(
             "USGS Topo",
             0,
             18,
             256,
             "",
             arrayOf("http://basemap.nationalmap.gov/ArcGIS/rest/services/USGSTopo/MapServer/tile/")
         ) {
             override fun getTileURLString(pMapTileIndex: Long): String {
                 return (baseUrl
                         + MapTileIndex.getZoom(pMapTileIndex)
                         + "/" + MapTileIndex.getY(pMapTileIndex)
                         + "/" + MapTileIndex.getX(pMapTileIndex)
                         + mImageFilenameEnding)
             }
         })*/




        generateNearByMarkers(startPoint)

        binding.mapOnline1.setOnClickListener {
            mapBoxLayer()
        }
        binding.mapOnline2.setOnClickListener {
            beingMapLayer()
        }
        binding.mapOnline3.setOnClickListener {

        }


    }

    class MapBoxTileSourceFixed(str: String?, i: Int, i2: Int, i3: Int) : MapBoxTileSource(str, i, i2, i3, "") {
        override fun getTileURLString(j: Long): String {
            return "https://api.mapbox.com/styles/v1/mapbox/" + mapBoxMapId + "/tiles/" + MapTileIndex.getZoom(j) + "/" + MapTileIndex.getX(j) + "/" + MapTileIndex.getY(j) + "?access_token=" + accessToken
        }
    }

    private fun generateNearByMarkers(startPoint: GeoPoint) {
        GlobalScope.launch {
            /*   val poiProvider = NominatimPOIProvider("OSMBonusPackTutoUserAgent")
               val pois = poiProvider.get(startPoint, "Zoo", 50,
                   0.2)*/
            val poiProvider = GeoNamesPOIProvider("ta99lha")
            val pois = poiProvider.getPOICloseTo(startPoint, 50, 10.0)

            val poiMarkers = FolderOverlay(this@MainActivity)
            withContext(Dispatchers.Main) {
                map.overlays.add(poiMarkers)
            }
            val poiIcon = getDrawable(R.drawable.ic_baseline_question_mark_24)
            for (poi in pois) {
                val poiMarker = Marker(map)
                poiMarker.position = poi.mLocation
                poiMarker.icon = poiIcon
                val v = LayoutInflater.from(this@MainActivity).inflate(R.layout.custom_window, null)
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

                poiMarker.infoWindow = CustomInfoWindow(v, map)
                poiMarkers.add(poiMarker)

            }
        }
    }

    private fun beingMapLayer() {
        setBingKey("AoexEyqVex1qAdw1WdPm-gAot8bO_-Tf6B-5ZfH5jWGaOc7Q_0GSgy6ZilW2HPWn")
        val bing = BingMapTileSource(null)
        bing.style = IMAGERYSET_ROAD
        bing.initMetaData()

        map.setTileSource(bing)
    }

    private fun mapBoxLayer() {
        /*val tileSource = MapBoxTileSource()
        tileSource.accessToken =
            "pk.eyJ1IjoidGE5OWxoYXMiLCJhIjoiY2wxa2FjbGJtMTR2eTNjbzVkOXFtanB5diJ9.PlnVv_gCJNphxnKzftfAlw"
        tileSource.setMapboxMapid("satellite-streets-v11")
        map.controller.setZoom(1)
        map.controller.setCenter(GeoPoint(39.8282, 98.5795))
        map.isTilesScaledToDpi = true
        map.setTileSource(tileSource)*/

        val mapBoxTileSourceFixed = MapBoxTileSourceFixed("MapBoxSatelliteLabelled", 1, 19, 256)
        this.MAPBOXSATELLITELABELLED = mapBoxTileSourceFixed
        mapBoxTileSourceFixed.retrieveAccessToken(this)
        (MAPBOXSATELLITELABELLED as MapBoxTileSource?)!!.retrieveMapBoxMapId(this)
        this.map.setTileSource(this.MAPBOXSATELLITELABELLED)
    }


    override fun onResume() {
        super.onResume()
        map.onResume();
    }

    override fun onPause() {
        map.onPause()
        super.onPause()

    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
}