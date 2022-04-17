package com.icon.osmdroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.icon.osmdroid.databinding.ActivityMapStylesBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex

class MapStyles : AppCompatActivity() {
    lateinit var binding:ActivityMapStylesBinding
    var MAPBOXSATELLITELABELLED: OnlineTileSourceBase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        binding= ActivityMapStylesBinding.inflate(layoutInflater)
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


        clickListeners()

    }

    private fun clickListeners() {
        binding.defaultMap.setOnClickListener {
            binding.map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        }

        binding.imageRysetAerialBeingMap.setOnClickListener {
            BingMapTileSource.setBingKey("AoexEyqVex1qAdw1WdPm-gAot8bO_-Tf6B-5ZfH5jWGaOc7Q_0GSgy6ZilW2HPWn")
            val bing = BingMapTileSource(null)
            bing.style = BingMapTileSource.IMAGERYSET_AERIAL
            bing.initMetaData()
            binding.map.setTileSource(bing)
        }
        binding.imageRysetRoadBeingMap.setOnClickListener {
            BingMapTileSource.setBingKey("AoexEyqVex1qAdw1WdPm-gAot8bO_-Tf6B-5ZfH5jWGaOc7Q_0GSgy6ZilW2HPWn")
            val bing = BingMapTileSource(null)
            bing.style = BingMapTileSource.IMAGERYSET_ROAD
            bing.initMetaData()
            binding.map.setTileSource(bing)
        }
        binding.mapBoxMap.setOnClickListener {
            mapBoxLayer()
        }
        binding.mapnikMap.setOnClickListener {
            binding.map.setTileSource(TileSourceFactory.MAPNIK)
        }
        binding.openTopoMap.setOnClickListener {
            binding.map.setTileSource(TileSourceFactory.OpenTopo)
        }
        binding.publicTransportMap.setOnClickListener {
            binding.map.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT)
        }
        binding.satelliteMap.setOnClickListener {
            binding.map.setTileSource(TileSourceFactory.USGS_SAT)
        }
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

        val mapBoxTileSourceFixed =
            MapBoxTileSourceFixed("MapBoxSatelliteLabelled", 1, 19, 256)
        this.MAPBOXSATELLITELABELLED = mapBoxTileSourceFixed
        mapBoxTileSourceFixed.retrieveAccessToken(this)
        (MAPBOXSATELLITELABELLED as MapBoxTileSource?)!!.retrieveMapBoxMapId(this)
        binding.map.setTileSource(this.MAPBOXSATELLITELABELLED)
    }


    class MapBoxTileSourceFixed(str: String?, i: Int, i2: Int, i3: Int) :
        MapBoxTileSource(str, i, i2, i3, "") {
        override fun getTileURLString(j: Long): String {
            return "https://api.mapbox.com/styles/v1/mapbox/" + mapBoxMapId + "/tiles/" + MapTileIndex.getZoom(
                j
            ) + "/" + MapTileIndex.getX(j) + "/" + MapTileIndex.getY(j) + "?access_token=" + accessToken
        }
    }

}