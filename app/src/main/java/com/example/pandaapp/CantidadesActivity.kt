package com.example.pandaapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class CantidadesActivity : AppCompatActivity() {


    lateinit var mQuantity:TextView
    lateinit var mCantidad:TextView
    lateinit var mTotal:TextView
    lateinit var mTexto:TextView

    var p="Panda/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cantidades)



        var Quantity=intent.getStringExtra("Quantity")
        var Cantidad=intent.getStringExtra("Cantidad")
        var Total=intent.getStringExtra("Total")
        var InfoCant=intent.getStringExtra("infoCantidad")


        mQuantity=findViewById(R.id.cantidadexistente)
        mCantidad=findViewById(R.id.cantidadagregada)
        mTotal=findViewById(R.id.cantidadtotal)
        mTexto=findViewById(R.id.textoCantidad)

        mQuantity.text=""+Quantity
        mCantidad.text=""+Cantidad
        mTotal.text=""+Total



        if(InfoCant=="true"){

            mTexto.text="Cantidad Agregada: "

        }else{
            mTexto.text="Cantidad Quitada:    "

        }

    }


    override fun onBackPressed() {
        var HomeIntent= Intent(this,IngresosActivity::class.java)
        startActivity(HomeIntent)
        finish()
    }
}
