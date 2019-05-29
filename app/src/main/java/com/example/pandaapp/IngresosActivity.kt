package com.example.pandaapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.pandaapp.Model.Producto
import com.example.pandaapp.ViewHolder.ProductoViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.activity_ingresos.*
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

        val firebaseRecyclerAdapter=object :FirebaseRecyclerAdapter<Producto,ProductoViewHolder>(option){
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ProductoViewHolder {

                val itemv=LayoutInflater.from(this@IngresosActivity).inflate(R.layout.producto_item,p0,false)
                return ProductoViewHolder(itemv)
            }
            override fun onBindViewHolder(holder: ProductoViewHolder, position: Int, model: Producto){

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