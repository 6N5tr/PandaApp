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
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlinx.android.synthetic.main.cambio_dialog.*
import kotlinx.android.synthetic.main.cantidad_dialog.view.Aceptar
import kotlinx.android.synthetic.main.cantidad_dialog.view.Cancelar
import kotlinx.android.synthetic.main.cantidad_dialog.view.inputcantidad
import kotlinx.android.synthetic.main.nav_header_home.view.*
import kotlinx.android.synthetic.main.pago_dialog.*
import kotlinx.android.synthetic.main.pago_dialog.view.*
import java.math.BigDecimal


class VentasActivity : AppCompatActivity() {


    var p=""
    var database = FirebaseDatabase.getInstance()
    var ref = database.getReference(p+"Views")

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
        mRequest=mDatabase.getReference(p+"Ventas")


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



                val mDialogView1=LayoutInflater.from(this@VentasActivity).inflate(R.layout.pago_dialog,null)
                val mBuilder=AlertDialog.Builder(this@VentasActivity)
                    .setView(mDialogView1)
                    .setTitle("Agregar Valor Monetario: ")
                val mAlertDialog=mBuilder.show()

                // open the soft keyboard

                val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
                mDialogView1.inputcantidad.requestFocus()

                var current = ""
                var local= Locale("en","US")
                var frmt= NumberFormat.getCurrencyInstance(local)

                mDialogView1.inputcantidad.addTextChangedListener(
                    object : TextWatcher {

                        override fun afterTextChanged(s: Editable) {



                        }

                        override fun beforeTextChanged(s: CharSequence, start: Int,
                                                       count: Int, after: Int) {
                        }

                        override fun onTextChanged(s: CharSequence, start: Int,
                                                   before: Int, count: Int) {
                            if(!s.toString().equals(current)){
                                mDialogView1.inputcantidad.removeTextChangedListener(this)
                                var replaceable = String.format("[%s,.\\s]",
                                    NumberFormat.getCurrencyInstance().getCurrency()
                                        .getSymbol())

                                var pago = s.toString().replace(replaceable.toRegex(),"")
                                Toast.makeText(applicationContext,"Pago: ${pago}",Toast.LENGTH_SHORT).show()

                                var parsed = BigDecimal(pago).setScale(2, BigDecimal.ROUND_FLOOR).divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
                                var formatted = NumberFormat.getCurrencyInstance().format(parsed)


//0
                                current = formatted
                                mDialogView1.inputcantidad.setText(formatted)
                                mDialogView1.inputcantidad.setSelection(formatted.length)

                                mDialogView1.inputcantidad.addTextChangedListener(this)
                            }

                        }

                    })



                mDialogView1.Aceptar.setOnClickListener{

                    val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(mDialogView1.windowToken,0)
                    mAlertDialog.dismiss()
                    var entrada=mDialogView1.inputcantidad.text.toString().replace(',', '.')
                    Toast.makeText(applicationContext,"Entrada: ${entrada}!",Toast.LENGTH_SHORT).show()
                    var pago=0.00
                    if (entrada==""){
                        pago=0.00
                    }
                    else{
                        pago = entrada.substring(1,mDialogView1.inputcantidad.text.toString().length).replace("\\s".toRegex(), "").toDouble()
                    }

                    /*if(pago==0.00){
                        pago = 0.00
                    }
                    else {
                       pago = entrada.substring(2,mDialogView1.inputcantidad.text.toString().length).toDouble()
                    }*/
                    val mDialogView=LayoutInflater.from(this@VentasActivity).inflate(R.layout.cambio_dialog,null)
                    val mBuilder=AlertDialog.Builder(this@VentasActivity)
                        .setView(mDialogView)


                    var local= Locale("en","US")
                    var frmt= NumberFormat.getCurrencyInstance(local)

                    val pago1=mDialogView.findViewById<TextView>(R.id.pagoventa1)
                    pago1.text=""+frmt.format(pago)

                    val total1=mDialogView.findViewById<TextView>(R.id.totalventa1)
                    total1.text=""+frmt.format(total)


                    val cambio1=mDialogView.findViewById<TextView>(R.id.cambioventa1)

                    if(pago==0.00){
                       cambio1.text="$0.00"
                    }
                    else{
                        Toast.makeText(applicationContext,"Pago: ${pago}!",Toast.LENGTH_SHORT).show()
                        cambio1.text=""+frmt.format(pago.toDouble()-total)
                    }


                    val mAlertDialog=mBuilder.show()

                    mDialogView.Aceptar.setOnClickListener{
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
                        mAlertDialog.dismiss()
                    }


                    // Display a negative button on alert dialog
                    mDialogView.Cancelar.setOnClickListener{
                        mAlertDialog.dismiss()
                        Toast.makeText(applicationContext,"Venta Cancelada!",Toast.LENGTH_SHORT).show()
                    }






                }
                mDialogView1.Cancelar.setOnClickListener{
                    val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(mDialogView1.windowToken,0)
                    mAlertDialog.dismiss()
                }



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
                Toast.makeText(this@VentasActivity,"Venta Modificada!"+IdPro,Toast.LENGTH_SHORT).show()
                total=0.0

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
    fun afterTextChanged(s: Editable?) {
        var doubleValue = 0.0
        if (s != null) {
            try {
                doubleValue = java.lang.Double.parseDouble(s.toString().replace(',', '.'))
            } catch (e: NumberFormatException) {
                //Error
            }

        }
        //Do something with doubleValue
    }
}
