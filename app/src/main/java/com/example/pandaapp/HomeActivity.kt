package com.example.pandaapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.pandaapp.Comun.Comun
import com.example.pandaapp.Database.Database
import com.example.pandaapp.Model.DetallePedidos
import com.example.pandaapp.Model.Vista
import com.example.pandaapp.R.id.*
import com.example.pandaapp.ViewHolder.MenuViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.cantidad_dialog.view.*
import kotlinx.android.synthetic.main.content_home.*
import java.text.DecimalFormat
import java.util.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var database = FirebaseDatabase.getInstance()
    var ref = database.getReference("Views")

    lateinit var mRecyclerView:RecyclerView

    lateinit var show_progress:ProgressBar
    var butspech: Int =MaterialSearchBar.BUTTON_SPEECH

   // var searchAdapter:FirebaseRecyclerAdapter<Vista, MenuViewHolder>?=null
    var sugerenciasLista= ArrayList<String>()
    var mSearchBar:MaterialSearchBar?=null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar=findViewById<Toolbar>(R.id.toolbar)
        var headerView=nav_view.getHeaderView(0)
        val txtFullName=headerView.findViewById<TextView>(R.id.textView)

        mSearchBar = findViewById(R.id.searchBar)
        CargarSugerencias()
        this.searchBar.lastSuggestions=sugerenciasLista
        this.searchBar.setCardViewElevation(10)
        this.searchBar.addTextChangeListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var sugerencias= ArrayList<String>()
                for (coincidencia in sugerenciasLista) {

                    if(coincidencia.toLowerCase().contains(searchBar.text.toLowerCase()))
                        sugerencias.add(coincidencia)
                }
                searchBar.lastSuggestions=sugerencias
                //Toast.makeText(this@HomeActivity,"Click 5", Toast.LENGTH_SHORT).show()
            }

        })
        this.searchBar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{
            override fun onButtonClicked(buttonCode: Int) {
                when(buttonCode){
                    MaterialSearchBar.BUTTON_SPEECH ->
                    {
                        if(MaterialSearchBar.BUTTON_SPEECH.equals(1)){
                            Toast.makeText(this@HomeActivity,"Speech", Toast.LENGTH_SHORT).show()
                        }
                           else{
                            Toast.makeText(this@HomeActivity,"No Speech", Toast.LENGTH_SHORT).show()
                        }
                    }

                }


                //openVoiceRecognizer()
                    //


            }

            override fun onSearchStateChanged(enabled: Boolean) {
                if(!enabled){
                    val option = FirebaseRecyclerOptions.Builder<Vista>()
                        .setQuery(ref,Vista::class.java)
                        .build()

                    val firebaseRecyclerAdapter=object :FirebaseRecyclerAdapter<Vista,MenuViewHolder>(option){
                        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MenuViewHolder {
                            val itemv=LayoutInflater.from(this@HomeActivity).inflate(R.layout.menu_item,p0,false)
                            return MenuViewHolder(itemv)
                        }

                        override fun onBindViewHolder(holder: MenuViewHolder, position: Int, model: Vista) {
                            val refid=getRef(position).key.toString()
                            ref.child(refid).addValueEventListener(object :ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {
                                }
                                override fun onDataChange(p0: DataSnapshot) {
                                    val number: String ="%.2f".format(model.Price)
                                    show_progress.visibility=if(itemCount==0) View.VISIBLE else View.GONE
                                    holder.mNombre.setText(" "+model.Name)
                                    holder.mPrecio.setText(number+" ")
                                    Picasso.get().load(model.Photo).into(holder.mImagen)


                                    holder.itemView.setOnClickListener{

                                        /*Toast.makeText(this@HomeActivity," "+model.Name+" "+position+" "+model.Price, Toast.LENGTH_SHORT).show()*/

                                        /*val intent= Intent(this@HomeActivity,InfoActivity::class.java)
                                        intent.putExtra("FirebaseImagen",model.Photo)
                                        intent.putExtra("FirebaseNombre",model.Name)
                                        startActivity(intent)*/

                                        val mDialogView=LayoutInflater.from(this@HomeActivity).inflate(R.layout.cantidad_dialog,null)
                                        val mBuilder=AlertDialog.Builder(this@HomeActivity)
                                            .setView(mDialogView)
                                            .setTitle("Agregar Cantidad")
                                        val mAlertDialog=mBuilder.show()

                                        val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)


                                        mDialogView.Aceptar.setOnClickListener{
                                            val cantidad=mDialogView.cantidad.text.toString()
                                            val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                            inputManager.hideSoftInputFromWindow(mDialogView.windowToken,0)
                                            mAlertDialog.dismiss()
                                            Database(this@HomeActivity).addToVentas( DetallePedidos(
                                                IdProducto = position.toString(),
                                                NombreProducto = model.Name,
                                                CantidadProducto = cantidad,
                                                PrecioProducto = model.Price.toString()
                                            ))

                                            Toast.makeText(this@HomeActivity,"Item agregado a la venta", Toast.LENGTH_SHORT).show()
                                        }
                                        mDialogView.Cancelar.setOnClickListener{
                                            val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                            inputManager.hideSoftInputFromWindow(mDialogView.windowToken,0)
                                            mAlertDialog.dismiss()

                                        }

                                    }

                                }


                            })
                        }


                    }

                    mRecyclerView.adapter=firebaseRecyclerAdapter
                    firebaseRecyclerAdapter.startListening()
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startsearch(text)
                //Toast.makeText(this@HomeActivity,"Search 1", Toast.LENGTH_SHORT).show()


            }
        })




        //Name En La Barra
        toolbar.setTitle(Comun.currentUser)
        setSupportActionBar(toolbar)
        txtFullName.setText(Comun.currentUser)
        mRecyclerView=findViewById(R.id.recycler_menu)
        mRecyclerView.layoutManager=GridLayoutManager(this,3)
        //progressbar
        show_progress=findViewById(R.id.probar)
        //Recycler
        val option = FirebaseRecyclerOptions.Builder<Vista>()
            .setQuery(ref,Vista::class.java)
            .build()

        val firebaseRecyclerAdapter=object :FirebaseRecyclerAdapter<Vista,MenuViewHolder>(option){
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MenuViewHolder {
                val itemv=LayoutInflater.from(this@HomeActivity).inflate(R.layout.menu_item,p0,false)
                return MenuViewHolder(itemv)
            }

            override fun onBindViewHolder(holder: MenuViewHolder, position: Int, model: Vista) {
                val refid=getRef(position).key.toString()
                ref.child(refid).addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        val number: String ="%.2f".format(model.Price)
                        show_progress.visibility=if(itemCount==0) View.VISIBLE else View.GONE
                        holder.mNombre.setText(" "+model.Name)
                        holder.mPrecio.setText(number+" ")
                        Picasso.get().load(model.Photo).into(holder.mImagen)


                        holder.itemView.setOnClickListener{

                            /*Toast.makeText(this@HomeActivity," "+model.Name+" "+position+" "+model.Price, Toast.LENGTH_SHORT).show()*/

                            /*val intent= Intent(this@HomeActivity,InfoActivity::class.java)
                            intent.putExtra("FirebaseImagen",model.Photo)
                            intent.putExtra("FirebaseNombre",model.Name)
                            startActivity(intent)*/

                            val mDialogView=LayoutInflater.from(this@HomeActivity).inflate(R.layout.cantidad_dialog,null)
                            val mBuilder=AlertDialog.Builder(this@HomeActivity)
                                .setView(mDialogView)
                                .setTitle("Agregar Cantidad")
                            val mAlertDialog=mBuilder.show()

                            val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)


                            mDialogView.Aceptar.setOnClickListener{
                                val cantidad=mDialogView.cantidad.text.toString()
                                val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                inputManager.hideSoftInputFromWindow(mDialogView.windowToken,0)
                                mAlertDialog.dismiss()
                                Database(this@HomeActivity).addToVentas( DetallePedidos(
                                    IdProducto = position.toString(),
                                    NombreProducto = model.Name,
                                    CantidadProducto = cantidad,
                                    PrecioProducto = model.Price.toString()
                                ))

                                Toast.makeText(this@HomeActivity,"Item agregado a la venta", Toast.LENGTH_SHORT).show()
                            }
                            mDialogView.Cancelar.setOnClickListener{
                                val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                inputManager.hideSoftInputFromWindow(mDialogView.windowToken,0)
                                mAlertDialog.dismiss()

                            }

                        }

                    }


                })
            }


        }

        mRecyclerView.adapter=firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

        //Loadmenu
        fab.setOnClickListener { view ->

            /*Snackbar.make(view, "Preventa", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()*/

             var VentasIntent=Intent(this,VentasActivity::class.java)
            startActivity(VentasIntent)

        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)





    }


    private fun startsearch(text: CharSequence?) {
       // var ref = database.getReference("Views")
        val option = FirebaseRecyclerOptions.Builder<Vista>()
            .setQuery(database.getReference("Views").orderByChild("Name").equalTo(text.toString()),Vista::class.java)
            .build()

        val firebaseRecyclerAdapter=object :FirebaseRecyclerAdapter<Vista,MenuViewHolder>(option){
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MenuViewHolder {
                val itemv=LayoutInflater.from(this@HomeActivity).inflate(R.layout.menu_item,p0,false)
                return MenuViewHolder(itemv)
            }

            override fun onBindViewHolder(holder: MenuViewHolder, position: Int, model: Vista) {
                val refid=getRef(position).key.toString()
                ref.child(refid).addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        val number: String ="%.2f".format(model.Price)
                        show_progress.visibility=if(itemCount==0) View.VISIBLE else View.GONE
                        holder.mNombre.setText(" "+model.Name)
                        holder.mPrecio.setText(number+" ")
                        Picasso.get().load(model.Photo).into(holder.mImagen)


                        holder.itemView.setOnClickListener{

                            val mDialogView=LayoutInflater.from(this@HomeActivity).inflate(R.layout.cantidad_dialog,null)
                            val mBuilder=AlertDialog.Builder(this@HomeActivity)
                                .setView(mDialogView)
                                .setTitle("Agregar Cantidad")
                            val mAlertDialog=mBuilder.show()

                            val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)


                            mDialogView.Aceptar.setOnClickListener{
                                val cantidad=mDialogView.cantidad.text.toString()
                                val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                inputManager.hideSoftInputFromWindow(mDialogView.windowToken,0)
                                mAlertDialog.dismiss()
                                Database(this@HomeActivity).addToVentas( DetallePedidos(
                                    IdProducto = position.toString(),
                                    NombreProducto = model.Name,
                                    CantidadProducto = cantidad,
                                    PrecioProducto = model.Price.toString()
                                ))

                                Toast.makeText(this@HomeActivity,"Item agregado a la venta", Toast.LENGTH_SHORT).show()
                            }
                            mDialogView.Cancelar.setOnClickListener{
                                val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                inputManager.hideSoftInputFromWindow(mDialogView.windowToken,0)
                                mAlertDialog.dismiss()

                            }

                        }

                    }


                })
            }


        }

        mRecyclerView.adapter=firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    fun CargarSugerencias(){

        Toast.makeText(this@HomeActivity,"Sugerencias Cargadas", Toast.LENGTH_SHORT).show()

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                for (i in 1.rangeTo(p0.childrenCount)) {

                    val item = p0.child(i.toString()).child("Name").getValue().toString()
                    var price:Double=p0.child(i.toString()).child("Price").getValue().toString().toDouble()
                    val number: String ="%.2f".format(price)

                    sugerenciasLista.add(item+" "+number)

                }
            }

        })

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_menu -> {
                // Handle the camera action
            }
            R.id.nav_ventas -> {

            }
            R.id.nav_productos -> {

            }
            R.id.nav_salir -> {

            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}




