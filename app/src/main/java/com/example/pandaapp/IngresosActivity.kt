package com.example.pandaapp

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.example.pandaapp.Database.Database
import com.example.pandaapp.Model.DetallePedidos
import com.example.pandaapp.Model.Producto
import com.example.pandaapp.Model.Vista
import com.example.pandaapp.ViewHolder.MenuViewHolder
import com.example.pandaapp.ViewHolder.ProductoViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_ingresos.*
import kotlinx.android.synthetic.main.cantidad_dialog.view.*
import kotlinx.android.synthetic.main.content_home.*

class IngresosActivity : AppCompatActivity() {

    var database = FirebaseDatabase.getInstance()
    var ref = database.getReference("Views")

    lateinit var mRecyclerView: RecyclerView

    var mSearchBar: MaterialSearchBar? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingresos)

        mRecyclerView=findViewById(R.id.recycler_producto)
        mRecyclerView.layoutManager= GridLayoutManager(this,1)

        mSearchBar = findViewById(R.id.searchBarProducto)

        val option = FirebaseRecyclerOptions.Builder<Producto>()
            .setQuery(ref.orderByChild("Name"),Producto::class.java)
            .build()

        val firebaseRecyclerAdapter=
            object :FirebaseRecyclerAdapter<Producto,ProductoViewHolder>(option){
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ProductoViewHolder {

                val itemv=LayoutInflater.from(this@IngresosActivity)
                    .inflate(R.layout.producto_item,p0,false)
                return ProductoViewHolder(itemv)
            }
                override fun onBindViewHolder(holder: ProductoViewHolder, position: Int, model: Producto) {

                    val refid=getRef(position).key.toString()
                    ref.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }
                        override fun onDataChange(p0: DataSnapshot) {

                            holder.mNombre.text = " "+model.Name

                          /*  holder.itemView.setOnClickListener{

                                val mDialogView=LayoutInflater.from(this@HomeActivity).inflate(R.layout.cantidad_dialog,null)
                                val mBuilder= AlertDialog.Builder(this@HomeActivity)
                                    .setView(mDialogView)
                                    .setTitle("Agregar Cantidad")
                                val txtFullName=headerView.findViewById<TextView>(R.id.textView)
                                val mAlertDialog=mBuilder.show()
                                mDialogView.inputcantidad.findFocus()

                                val inputManager: InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)

//3
                                mDialogView.Aceptar.setOnClickListener{
                                    val cantidad = mDialogView.inputcantidad.text.toString()
                                    if (cantidad.isEmpty()) {
                                        Toast.makeText(this@HomeActivity,"Agregue la cantidad", Toast.LENGTH_SHORT).show()
                                    }
                                    else {

                                        val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                        mAlertDialog.dismiss()

                                        //Agregar comparacion de pedidos.
                                        if(Database(this@HomeActivity).checkItem(model.Name.toString())==true){
                                            var IdPro= Database(this@HomeActivity).checkId(model.Name.toString())
                                            var cantPro= Database(this@HomeActivity).getCant(model.Name.toString())
                                            val cantidad = mDialogView.inputcantidad.text.toString()
                                            Database(this@HomeActivity).editVenta(
                                                Id= IdPro!!.toInt(),
                                                cantidad = (cantidad.toInt()+cantPro).toString()
                                            )
                                            //Mensaje que se muestra cuando un producto ya esta agragado a las ventas y se desea agregar o modicar el valor de la cantida de este
                                            Toast.makeText(this@HomeActivity,""+model.Name.toString()+" Cantidad Total: "+(cantidad.toInt()+cantPro).toString(), Toast.LENGTH_SHORT).show()
                                        }
                                        else{
                                            Database(this@HomeActivity).addToVentas(
                                                DetallePedidos(
                                                    IdProducto =model.Id.toString(),
                                                    NombreProducto = model.Name,
                                                    CantidadProducto = cantidad,
                                                    PrecioProducto = model.Price.toString()
                                                )
                                            )

                                            Toast.makeText(
                                                this@HomeActivity,
                                                "Item agregado a la venta",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    }
                                }
                                mDialogView.Cancelar.setOnClickListener{
                                    val inputManager: InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputManager.hideSoftInputFromWindow(mDialogView.windowToken,0)
                                    mAlertDialog.dismiss()

                                }

                            }*/

                        }


                    })
                }
        }
        mRecyclerView.adapter=firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

        this.searchBarProducto.setCardViewElevation(10)

        this.searchBarProducto.addTextChangeListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


                if (searchBarProducto.text.isEmpty()) {

                } else {
                    val option = FirebaseRecyclerOptions.Builder<Producto>()
                        .setQuery(
                            database.getReference("Views").orderByChild("Name").startAt(
                                searchBarProducto.text.first().toString().toUpperCase() + searchBarProducto.text.toString().substring(
                                    1,
                                    searchBarProducto.text.length
                                )
                            )
                                .endAt(searchBarProducto.text.substring(1, searchBarProducto.text.length) + "\uf8ff"),
                            Producto::class.java
                        )
                        .build()

                    val firebaseRecyclerAdapter =
                        object : FirebaseRecyclerAdapter<Producto, ProductoViewHolder>(option) {
                            override fun onBindViewHolder(holder: ProductoViewHolder, position: Int, model: Producto) {

                                val refid=getRef(position).key.toString()
                                ref.child(refid).addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                    }
                                    override fun onDataChange(p0: DataSnapshot) {

                                        holder.mNombre.text = " "+model.Name

                                        /*  holder.itemView.setOnClickListener{

                                              val mDialogView=LayoutInflater.from(this@HomeActivity).inflate(R.layout.cantidad_dialog,null)
                                              val mBuilder= AlertDialog.Builder(this@HomeActivity)
                                                  .setView(mDialogView)
                                                  .setTitle("Agregar Cantidad")
                                              val txtFullName=headerView.findViewById<TextView>(R.id.textView)
                                              val mAlertDialog=mBuilder.show()
                                              mDialogView.inputcantidad.findFocus()

                                              val inputManager: InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                              inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)

              //3
                                              mDialogView.Aceptar.setOnClickListener{
                                                  val cantidad = mDialogView.inputcantidad.text.toString()
                                                  if (cantidad.isEmpty()) {
                                                      Toast.makeText(this@HomeActivity,"Agregue la cantidad", Toast.LENGTH_SHORT).show()
                                                  }
                                                  else {

                                                      val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                      inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                                      mAlertDialog.dismiss()

                                                      //Agregar comparacion de pedidos.
                                                      if(Database(this@HomeActivity).checkItem(model.Name.toString())==true){
                                                          var IdPro= Database(this@HomeActivity).checkId(model.Name.toString())
                                                          var cantPro= Database(this@HomeActivity).getCant(model.Name.toString())
                                                          val cantidad = mDialogView.inputcantidad.text.toString()
                                                          Database(this@HomeActivity).editVenta(
                                                              Id= IdPro!!.toInt(),
                                                              cantidad = (cantidad.toInt()+cantPro).toString()
                                                          )
                                                          //Mensaje que se muestra cuando un producto ya esta agragado a las ventas y se desea agregar o modicar el valor de la cantida de este
                                                          Toast.makeText(this@HomeActivity,""+model.Name.toString()+" Cantidad Total: "+(cantidad.toInt()+cantPro).toString(), Toast.LENGTH_SHORT).show()
                                                      }
                                                      else{
                                                          Database(this@HomeActivity).addToVentas(
                                                              DetallePedidos(
                                                                  IdProducto =model.Id.toString(),
                                                                  NombreProducto = model.Name,
                                                                  CantidadProducto = cantidad,
                                                                  PrecioProducto = model.Price.toString()
                                                              )
                                                          )

                                                          Toast.makeText(
                                                              this@HomeActivity,
                                                              "Item agregado a la venta",
                                                              Toast.LENGTH_SHORT
                                                          ).show()
                                                      }

                                                  }
                                              }
                                              mDialogView.Cancelar.setOnClickListener{
                                                  val inputManager: InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                  inputManager.hideSoftInputFromWindow(mDialogView.windowToken,0)
                                                  mAlertDialog.dismiss()

                                              }

                                          }*/

                                    }


                                })
                            }

                            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ProductoViewHolder {

                                val itemv = LayoutInflater.from(this@IngresosActivity)
                                    .inflate(R.layout.producto_item, p0, false)
                                return ProductoViewHolder(itemv)
                            }



                        }
                    mRecyclerView.adapter = firebaseRecyclerAdapter
                    firebaseRecyclerAdapter.startListening()
                }
            }

        })
    }
}