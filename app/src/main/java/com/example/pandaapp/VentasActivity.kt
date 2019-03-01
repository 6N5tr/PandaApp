package com.example.pandaapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.pandaapp.Comun.Comun
import com.example.pandaapp.Database.Database
import com.example.pandaapp.Model.DetallePedidos
import com.example.pandaapp.Model.Request
import com.example.pandaapp.ViewHolder.VentasAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_ventas.*
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.*


class VentasActivity : AppCompatActivity() {


    lateinit var mRecyclerView: RecyclerView
    lateinit var mLayout:RecyclerView.LayoutManager

    lateinit var mDatabase:FirebaseDatabase
    lateinit var mRequest:DatabaseReference

    lateinit var mTextTotal:TextView
    lateinit var mButtonPlace: Button

    private var mFechayHora:String?=null


    var mVentas: List<DetallePedidos> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas)

        //Firebase
        mDatabase=FirebaseDatabase.getInstance()
        mRequest=mDatabase.getReference("Ventas")


        //init
        mRecyclerView=findViewById(R.id.listaVentas)
        mRecyclerView.setHasFixedSize(true)
        mLayout=LinearLayoutManager(this)
        mRecyclerView.layoutManager=mLayout


        mTextTotal=findViewById(R.id.total)
        mButtonPlace=findViewById(R.id.btnVentaRealizada)

        mFechayHora= LocalDateTime.now().toString()


        btnVentaRealizada.setOnClickListener{
            // Initialize a new instance of
            val builder = AlertDialog.Builder(this@VentasActivity)

            // Set the alert dialog title
            builder.setTitle("Finalizar Venta")

            // Display a message on alert dialog
            builder.setMessage("Desea finalizar la venta?")

            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton("Aceptar"){dialog, which ->
                // Do something when user press the positive button
                var request=Request(Comun.currentUser,mTextTotal.text.toString(),Detalle = mVentas,FechayHora = mFechayHora )

                mRequest.child(Comun.currentUser+" "+System.currentTimeMillis().toString()).setValue(request)

                Database(baseContext).borrarTodoVentas()

                finish()

                Toast.makeText(applicationContext,"Venta Realizada!",Toast.LENGTH_SHORT).show()

            }


            // Display a negative button on alert dialog
            builder.setNegativeButton("Cancelar"){dialog,which ->
               builder.show().dismiss()
            }


             // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()

            // Display the alert dialog on app interface
            dialog.show()







        }
        cargarListaComida()

    }

    fun cargarListaComida(){

        mVentas=Database(context = this).getVentas()
        var mAdap=VentasAdapter(this,mVentas)
        mRecyclerView.adapter=mAdap

        var total:Double=0.0

        for(e in mVentas)
            total+=((e.PrecioProducto)?.toDouble()!!)*((e.CantidadProducto)?.toDouble()!!)


        var local= Locale("en","US")
        var frmt= NumberFormat.getCurrencyInstance(local)
        mTextTotal.text = frmt.format(total)

    }
}
