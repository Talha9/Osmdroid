package com.icon.osmdroid


import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.icon.osmdroid.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {


    private var oneTimeCheck: Boolean = true
    private var i: Int = 0
    private var obtainTypedArray: TypedArray? = null
    private var road: Road? = null
    private var chk: Boolean = false
    private var result: Double = 0.0
    private lateinit var locationOverlay: MyLocationNewOverlay
    private var roadManager: OSRMRoadManager? = null
    lateinit var binding: ActivityMainBinding
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    lateinit var map: MapView
    var MAPBOXSATELLITELABELLED: OnlineTileSourceBase? = null
    lateinit var service: TrackingService
    private val df: DecimalFormat = DecimalFormat("0.000")

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //load/initialize the osmdroid configuration, this can be done
        //load/initialize the osmdroid configuration, this can be done
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        map = findViewById(R.id.map)

        roadManager = OSRMRoadManager(this, "MY_USER_AGENT")

        requestPermissionsIfNecessary(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))

        service = TrackingService()
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(9.5)
        val startPoint = GeoPoint(33.522207, 73.153886)

        mapController.setCenter(startPoint)
        map.setTileSource(TileSourceFactory.MAPNIK)

        obtainTypedArray = resources.obtainTypedArray(R.array.direction_icons)

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()

        map.overlays.add(locationOverlay)
        Handler().postDelayed({
            chk = true

        }, 10000)


        val compassOverlay = CompassOverlay(this, InternalCompassOrientationProvider(this), map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)


        val rotationGestureOverlay = RotationGestureOverlay(this, map)
        rotationGestureOverlay.isEnabled
        map.setMultiTouchControls(true)
        map.overlays.add(rotationGestureOverlay)








        map.invalidate()


        binding.mapOnline1.setOnClickListener {
            mapBoxLayer()
        }
        binding.mapOnline2.setOnClickListener {
            beingMapLayer()
        }
        binding.search.setOnClickListener {
            val waypoints = ArrayList<GeoPoint>()
            waypoints.add(startPoint)
            val endPoint = GeoPoint(32.9425, 73.7257)
            waypoints.add(endPoint)
            val serviceIntent = Intent(this, TrackingService::class.java)
            serviceIntent.putExtra("inputExtra", "Navigation Service")
            ContextCompat.startForegroundService(this, serviceIntent)
            service.play()
            GlobalScope.launch {
                Dispatchers.IO
                readed(roadManager!!, waypoints)
            }

        }
    }

    private suspend fun readed(roadManager: RoadManager, waypoints: java.util.ArrayList<GeoPoint>) {

        road = roadManager.getRoad(waypoints)
        generateNearByMarkers(road)
        val roadOverlay = RoadManager.buildRoadOverlay(road)
   /*     Log.d("LegsTAG", "readed: Duration: ${road!!.mLegs[0].mDuration} \n" +
                " Length: ${road!!.mLegs[0].mLength} \n" +
                " StartIndex: ${road!!.mLegs[0].mStartNodeIndex} \n" +
                " EndIndex: ${road!!.mLegs[0].mEndNodeIndex}")*/
      /*  Log.d("BoundingBoxTAG", "readed: "+road!!.mBoundingBox.)*/
        road!!.buildLegs(waypoints)
        roadOverlay.width = 10.0f
        roadOverlay.color = Color.BLACK
        map.overlays.add(roadOverlay)


        if (road!!.mLength >= 100.0) {
            result = road!!.mLength
        } else if (road!!.mLength >= 1.0) {
            result = Math.round(road!!.mLength * 10) / 10.0
        } else {
            result = road!!.mLength * 1000
        }
        withContext(Dispatchers.Main) {
            binding.speedTxt.text = result.toString()
        }

        val nodeIcon = resources.getDrawable(R.drawable.ic_baseline_change_history_24)

        for (i in road!!.mNodes.indices) {
            val node = road!!.mNodes[i]
            val nodeMarker = Marker(map)
            nodeMarker.position = node.mLocation
            nodeMarker.icon = nodeIcon
            nodeMarker.title = "Step $i "
            nodeMarker.snippet = node.mInstructions
            nodeMarker.subDescription = Road.getLengthDurationText(this, node.mLength, node.mDuration)
            val resourceId: Int =
                obtainTypedArray!!.getResourceId(node.mManeuverType, R.drawable.ic_empty)
            if (resourceId != R.drawable.ic_empty) {
                nodeMarker.image =
                    ResourcesCompat.getDrawable(resources, resourceId, null as Resources.Theme?)
            }

            map.overlays.add(nodeMarker)
        }
        map.invalidate()


    }


    class MapBoxTileSourceFixed(str: String?, i: Int, i2: Int, i3: Int) :
        MapBoxTileSource(str, i, i2, i3, "") {
        override fun getTileURLString(j: Long): String {
            return "https://api.mapbox.com/styles/v1/mapbox/" + mapBoxMapId + "/tiles/" + MapTileIndex.getZoom(
                j
            ) + "/" + MapTileIndex.getX(j) + "/" + MapTileIndex.getY(j) + "?access_token=" + accessToken
        }
    }

    private fun generateNearByMarkers(point: Road?) {
        GlobalScope.launch {
               val poiProvider = NominatimPOIProvider("OSMBonusPackTutoUserAgent")
               val pois = poiProvider.getPOIAlong(point!!.routeLow,"fuel",100,0.2)
           /* val poiProvider = GeoNamesPOIProvider("ta99lha")
            val pois = poiProvider.getPOICloseTo(startPoint, 50, 10.0)*/

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
        map.onResume()
        registerReceiver(receiver, IntentFilter("SendMessage"))

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

    private fun GetDistance(lStart: LatLng, lEnd: LatLng): Double? {
        var valueInMeter: Double = 0.0
        var valuesInKm: Double = 0.0
        if (lStart.latitude != 0.0 && lStart.longitude != 0.0) {
            valueInMeter = SphericalUtil.computeDistanceBetween(
                LatLng(lStart.latitude, lStart.longitude),
                LatLng(lEnd.latitude, lEnd.longitude)
            )
            valuesInKm = valueInMeter / 1000
        }
        return valuesInKm
    }


    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent != null) {
                if (intent.action == "SendMessage") {
                    if (chk) {

                        val dis = df.format(
                            GetDistance(
                                LatLng(
                                    locationOverlay.myLocation.latitude,
                                    locationOverlay.myLocation.longitude
                                ),
                                LatLng(
                                    road!!.mNodes[i].mLocation.latitude,
                                    road!!.mNodes[i].mLocation.longitude
                                )
                            )
                        )
                        if (dis != null) {
                            Log.d("GetDistanceTAG", "onReceive: " + dis.toDouble())
                            if (dis.toDouble() <= 0.005) {
                                i++
                                Log.d("GlideTAG", "onReceive: OK")
                                val resourceId: Int = obtainTypedArray!!.getResourceId(
                                    road!!.mNodes[i].mManeuverType,
                                    R.drawable.ic_empty
                                )
                                binding.desTxt.text=road!!.mNodes[i].mInstructions
                                if (resourceId != R.drawable.ic_empty) {
                                    Glide.with(this@MainActivity).load(ResourcesCompat.getDrawable(resources, resourceId, null as Resources.Theme?)).into(binding.headingdImg)
                                }else{
                                    Glide.with(this@MainActivity).load(ResourcesCompat.getDrawable(resources, R.drawable.ic_continue, null as Resources.Theme?)).into(binding.headingdImg)
                                }
                            }
                        }
                    }
                }
            }


        }
    }
}