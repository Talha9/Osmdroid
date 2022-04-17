package com.icon.osmdroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.icon.osmdroid.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    lateinit var binding:ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.mapLayersBtn.setOnClickListener {
            val intent=Intent(this,MapStyles::class.java)
            startActivity(intent)
        }

        binding.mapMarkersBtn.setOnClickListener {
            val intent=Intent(this,MapNearBy::class.java)
            startActivity(intent)
        }
        binding.routeNavigationBtn.setOnClickListener {
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
        }


    }
}