package com.example.pandaapp

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.pandaapp.Comun.Comun
import com.example.pandaapp.Database.Database
import com.example.pandaapp.Model.DetallePedidos
import com.example.pandaapp.Model.Request
import com.example.pandaapp.ViewHolder.VentasAdapter
import kotlinx.android.synthetic.main.activity_ventas.*
import kotlinx.android.synthetic.main.cantidad_dialog.view.*
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.*
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import com.example.pandaapp.Model.Producto
import com.example.pandaapp.Swipe.SwipeToDeleteCallback
import com.google.firebase.database.*


class VentasActivity : AppCompatActivity() {

    var evento=0

    var database = FirebaseDatabase.getInstance()
    var ref = database.getReference("Views")

    lateinit var mRecyclerView: RecyclerView
    lateinit var mLayout:RecyclerView.LayoutManager

    lateinit var mDatabase:FirebaseDatabase
    lateinit var mRequest:DatabaseReference

    lateinit var mTextTotal:TextView
    lateinit var mButtonPlace: Button

    private var mFechayHora:String?=null

    var total:Double=0.0
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

        CargarTotalVentas()


        btnVentaRealizada.setOnClickListener{

            if(total>0){
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


                    for (item in mVentas) {
                        var id=item.IdProducto.toString()
                        var cantidad=item.CantidadProducto.toString()


                        ref.child(id).child("Quantity").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {

                                var cantidadExistente = p0.value.toString()

                                var valor=cantidadExistente.toInt()-(cantidad.toInt())
                                ref.child(id).child("Quantity").setValue(valor)

                            }
                        })


                    }


                    //Restar cantidades a productos.





                    var HomeIntent= Intent(this,HomeActivity::class.java)
                    startActivity(HomeIntent)

                    finish()

                    Toast.makeText(applicationContext,"Venta Realizada!",Toast.LENGTH_SHORT).show()

                }


                // Display a negative button on alert dialog
                builder.setNegativeButton("Cancelar"){dialog,which ->
                    builder.show().dismiss()
                    Toast.makeText(applicationContext,"Venta Cancelada!",Toast.LENGTH_SHORT).show()
                }


                // Finally, make the alert dialog using builder
                val dialog: AlertDialog = builder.create()

                // Display the alert dialog on app interface
                dialog.show()



            }
            else{

                Toast.makeText(applicationContext,"Agregue productos para la venta!",Toast.LENGTH_SHORT).show()
            }



        }
        btnCancelarVenta.setOnClickListener{
            val builder = AlertDialog.Builder(this@VentasActivity)

            // Set the alert dialog title
            builder.setTitle("Cancelar Venta")

            // Display a message on alert dialog
            builder.setMessage("Desea cancelar la venta?")

            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton("Aceptar"){dialog, which ->
                // Do something when user press the positive button


                Database(this).borrarTodoVentas()
                mVentas= Database(context = this).getVentas()
                var mAdap=VentasAdapter(this,mVentas)
                mRecyclerView.adapter=mAdap

                var HomeIntent= Intent(this,HomeActivity::class.java)
                startActivity(HomeIntent)

                finish()

                Toast.makeText(applicationContext,"Venta Cancelada!",Toast.LENGTH_SHORT).show()

            }


            // Display a negative button on alert dialog
            builder.setNegativeButton("Cancelar"){dialog,which ->
                builder.show().dismiss()
                Toast.makeText(applicationContext,"No se ha cancelado la venta!",Toast.LENGTH_SHORT).show()
            }


            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()

            // Display the alert dialog on app interface
            dialog.show()


        }



    }

   
    override fun onBackPressed() {
        var HomeIntent= Intent(this,HomeActivity::class.java)
        startActivity(HomeIntent)
        finish()

    }
    fun CargarTotalVentas(){

        mVentas=Database(context = this).getVentas()
        var mAdap=VentasAdapter(this,mVentas)
        mRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter=mAdap

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                var IdPro=mVentas.get(viewHolder.adapterPosition).IdProducto
                Database(this@VentasActivity).eliminaItem(
                    Id= IdPro!!.toInt()
                )
                mAdap.removeAt(viewHolder.adapterPosition)
                Toast.makeText(this@VentasActivity,"Venta Modificada!",Toast.LENGTH_SHORT).show()
                CargarTotalVentas()

            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)

        mAdap.setOnClickListener(View.OnClickListener {
            var IdPro=mVentas.get(mRecyclerView.getChildAdapterPosition(it)).IdProducto

            val mDialogView= LayoutInflater.from(this@VentasActivity).inflate(R.layout.cantidad_dialog,null)
            val mBuilder=AlertDialog.Builder(this@VentasActivity)
                .setView(mDialogView)
                .setTitle("Agregar Nueva Cantidad")
            val mAlertDialog=mBuilder.show()

            val inputManager: InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)


            mDialogView.Aceptar.setOnClickListener{
                val cantidad = mDialogView.inputcantidad.text.toString()
                if (cantidad.isEmpty()) {
                    Toast.makeText(this@VentasActivity,"Agregue la nueva cantidad", Toast.LENGTH_SHORT).show()
                }
                else {

                    val inputManager: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                    mAlertDialog.dismiss()

                    Database(this@VentasActivity).editVenta(
                        Id= IdPro!!.toInt(),
                        cantidad = cantidad
                    )

                    total=0.0

                    CargarTotalVentas()

                    Toast.makeText(
                        this@VentasActivity,
                        "Venta modificada!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            mDialogView.Cancelar.setOnClickListener{
                val inputManager: InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(mDialogView.windowToken,0)
                mAlertDialog.dismiss()

            }



        })


           for(e in mVentas)
            total+=((e.PrecioProducto)?.toDouble()!!)*((e.CantidadProducto)?.toDouble()!!)


        var local= Locale("en","US")
        var frmt= NumberFormat.getCurrencyInstance(local)
        mTextTotal.text = frmt.format(total)

    }
}
