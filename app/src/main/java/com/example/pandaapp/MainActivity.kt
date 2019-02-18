package com.example.pandaapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var imgG=findViewById<ImageView>(R.id.imggif)
        Glide.with(this).asGif()
            .load(R.drawable.pandaintro)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(imgG)


        val Ingreso=findViewById<Button>(R.id.btnIngreso)

        Ingreso.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {

                val intento1 = Intent(this@MainActivity, IngresoActivity::class.java)
                startActivity(intento1)
            }

        })

    }
}
