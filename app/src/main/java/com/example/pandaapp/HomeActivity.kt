package com.example.pandaapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.support.design.button.MaterialButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
import com.google.zxing.integration.android.IntentIntegrator
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.cantidad_dialog.*
import kotlinx.android.synthetic.main.cantidad_dialog.view.*
import kotlinx.android.synthetic.main.content_home.*
import java.text.DecimalFormat
import java.util.*
import java.util.jar.Attributes

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var database = FirebaseDatabase.getInstance()
    var ref = database.getReference("Views")


    lateinit var mRecyclerView:RecyclerView

    lateinit var show_progress:ProgressBar

   //Cargar Datos en Lista de Sugerencias
    var sugerenciasLista= ArrayList<String>()
    var mSearchBar:MaterialSearchBar?=null

    //SPEECH
    private val REQUEST_CODE_SPEECH_INPUT=100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar=findViewById<Toolbar>(R.id.toolbar)
        var headerView=nav_view.getHeaderView(0)
        val txtFullName=headerView.findViewById<TextView>(R.id.textView)

        var codebar=findViewById<ImageView>(R.id.barcode)
        codebar.setOnClickListener {
            getCodebar()
        }



        mSearchBar = findViewById(R.id.searchBar)
        CargarSugerencias()
        //this.searchBar.lastSuggestions=sugerenciasLista
        this.searchBar.setCardViewElevation(10)

        this.searchBar.addTextChangeListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (searchBar.text.isEmpty()) {

                }
                else{
                                           val option = FirebaseRecyclerOptions.Builder<Vista>()
                            .setQuery(
                                database.getReference("Views").orderByChild("Name").startAt(searchBar.text.first().toString().toUpperCase()).endAt(searchBar.text.toString()+"\uf8ff"),
                                Vista::class.java
                            )
                            .build()

                        val firebaseRecyclerAdapter =
                            object : FirebaseRecyclerAdapter<Vista, MenuViewHolder>(option) {
                                override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MenuViewHolder {
                                    val itemv = LayoutInflater.from(this@HomeActivity)
                                        .inflate(R.layout.menu_item, p0, false)
                                    return MenuViewHolder(itemv)
                                }

                                override fun onBindViewHolder(holder: MenuViewHolder, position: Int, model: Vista) {
                                    val refid = getRef(position).key.toString()
                                    ref.child(refid).addValueEventListener(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {
                                        }

                                        override fun onDataChange(p0: DataSnapshot) {

                                            val number: String = "%.2f".format(model.Price)
                                            show_progress.visibility =
                                                if (itemCount == 0) View.VISIBLE else View.GONE
                                            holder.mNombre.text = " " + model.Name
                                            holder.mPrecio.text = number + " "
                                            Picasso.get().load(model.Photo).into(holder.mImagen)


                                            holder.itemView.setOnClickListener {

                                                val mDialogView = LayoutInflater.from(this@HomeActivity)
                                                    .inflate(R.layout.cantidad_dialog, null)
                                                val mBuilder = AlertDialog.Builder(this@HomeActivity)
                                                    .setView(mDialogView)
                                                    .setTitle("Agregar Cantidad")
                                                val mAlertDialog = mBuilder.show()

                                                val inputManager: InputMethodManager =
                                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)


                                                mDialogView.Aceptar.setOnClickListener{
                                                    val cantidad = mDialogView.cantidad.text.toString()
                                                    if (cantidad.isEmpty()) {
                                                        Toast.makeText(this@HomeActivity,"Agregue la cantidad", Toast.LENGTH_SHORT).show()
                                                    }
                                                    else {

                                                        val inputManager: InputMethodManager =
                                                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                        inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                                        mAlertDialog.dismiss()
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
                                                mDialogView.Cancelar.setOnClickListener {
                                                    val inputManager: InputMethodManager =
                                                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                    inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                                    mAlertDialog.dismiss()

                                                }

                                            }

                                        }


                                    })
                                }


                            }

                        mRecyclerView.adapter = firebaseRecyclerAdapter
                        firebaseRecyclerAdapter.startListening()




                }
            }

        })
        this.searchBar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{
            override fun onButtonClicked(buttonCode: Int) {
                when(buttonCode){
                    MaterialSearchBar.BUTTON_SPEECH ->
                    {
                        speak()

                    }


                }

            }


            override fun onSearchStateChanged(enabled: Boolean) {

                if(!enabled){
                     val option = FirebaseRecyclerOptions.Builder<Vista>()
                        .setQuery(ref.orderByChild("Name"),Vista::class.java)
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
                                    holder.mNombre.text = " "+model.Name
                                    holder.mPrecio.text = number+" "
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
                                            val cantidad = mDialogView.cantidad.text.toString()
                                            if (cantidad.isEmpty()) {
                                                Toast.makeText(this@HomeActivity,"Agregue la cantidad", Toast.LENGTH_SHORT).show()
                                            }
                                            else {

                                                val inputManager: InputMethodManager =
                                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                                mAlertDialog.dismiss()
                                                Database(this@HomeActivity).addToVentas(
                                                    DetallePedidos(
                                                        IdProducto = model.Id.toString(),
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
            }
        })




        //Name En La Barra
        toolbar.title = Comun.currentUser
        setSupportActionBar(toolbar)
        txtFullName.text = Comun.currentUser
        mRecyclerView=findViewById(R.id.recycler_menu)
        mRecyclerView.layoutManager= GridLayoutManager(this,3)
        //progressbar
        show_progress=findViewById(R.id.probar)
        //Recycler

        val option = FirebaseRecyclerOptions.Builder<Vista>()
            .setQuery(ref.orderByChild("Name"),Vista::class.java)
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
                        holder.mNombre.text = " "+model.Name
                        holder.mPrecio.text = number+" "
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
                                val cantidad = mDialogView.cantidad.text.toString()
                                if (cantidad.isEmpty()) {
                                    Toast.makeText(this@HomeActivity,"Agregue la cantidad", Toast.LENGTH_SHORT).show()
                                }
                                else {

                                    val inputManager: InputMethodManager =
                                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                    mAlertDialog.dismiss()
                                    Database(this@HomeActivity).addToVentas(
                                        DetallePedidos(
                                            IdProducto = model.Id.toString(),
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
            finish()

        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

    }

    private fun getCodebar() {

        val scanner=IntentIntegrator(this)
        scanner.initiateScan()
    }



    private fun speak() {
        val mIntent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Usa tu voz...")

        try {
            startActivityForResult(mIntent,REQUEST_CODE_SPEECH_INPUT)
        }catch (e:Exception){
            //Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CODE_SPEECH_INPUT->{
                if(resultCode== Activity.RESULT_OK && null!=data){
                    val result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    mSearchBar!!.text = result[0]
                }
            }

        }
        when(resultCode){
            Activity.RESULT_OK->{
                Toast.makeText(this, "Codigo Obtenido", Toast.LENGTH_LONG).show()
                val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (result != null) {
                    if (result.contents == null) {
                        Toast.makeText(this, "Escaneo Terminado!", Toast.LENGTH_LONG).show()
                    } else {

                        var codebar= result.contents
                        val option = FirebaseRecyclerOptions.Builder<Vista>()
                            .setQuery(database.getReference("Views").orderByChild("Codebar").equalTo(codebar.toString()),Vista::class.java)
                            .build()
                        val firebaseRecyclerAdapter =
                            object : FirebaseRecyclerAdapter<Vista, MenuViewHolder>(option) {
                                override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MenuViewHolder {
                                    val itemv = LayoutInflater.from(this@HomeActivity)
                                        .inflate(R.layout.menu_item, p0, false)
                                    return MenuViewHolder(itemv)
                                }

                                override fun onBindViewHolder(holder: MenuViewHolder, position: Int, model: Vista) {
                                    val refid = getRef(position).key.toString()
                                    ref.child(refid).addValueEventListener(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {
                                        }

                                        override fun onDataChange(p0: DataSnapshot) {

                                            val number: String = "%.2f".format(model.Price)
                                            show_progress.visibility =
                                                if (itemCount == 0) View.VISIBLE else View.GONE
                                            holder.mNombre.text = " " + model.Name
                                            holder.mPrecio.text = number + " "
                                            Picasso.get().load(model.Photo).into(holder.mImagen)


                                            holder.itemView.setOnClickListener {

                                                val mDialogView = LayoutInflater.from(this@HomeActivity)
                                                    .inflate(R.layout.cantidad_dialog, null)
                                                val mBuilder = AlertDialog.Builder(this@HomeActivity)
                                                    .setView(mDialogView)
                                                    .setTitle("Agregar Cantidad")
                                                val mAlertDialog = mBuilder.show()

                                                val inputManager: InputMethodManager =
                                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)


                                                mDialogView.Aceptar.setOnClickListener{
                                                    val cantidad = mDialogView.cantidad.text.toString()
                                                    if (cantidad.isEmpty()) {
                                                        Toast.makeText(this@HomeActivity,"Agregue la cantidad", Toast.LENGTH_SHORT).show()
                                                    }
                                                    else {

                                                        val inputManager: InputMethodManager =
                                                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                        inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                                        mAlertDialog.dismiss()
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
                                                mDialogView.Cancelar.setOnClickListener {
                                                    val inputManager: InputMethodManager =
                                                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                    inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                                    mAlertDialog.dismiss()

                                                }

                                            }

                                        }


                                    })
                                }


                            }

                        mRecyclerView.adapter = firebaseRecyclerAdapter
                        firebaseRecyclerAdapter.startListening()

                        mSearchBar?.enableSearch()
                        Toast.makeText(this, "Escaneo Terminado!", Toast.LENGTH_LONG).show()

                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }

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
                        holder.mNombre.text = " "+model.Name
                        holder.mPrecio.text = number+" "
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
                                val cantidad = mDialogView.cantidad.text.toString()
                                if (cantidad.isEmpty()) {
                                                 Toast.makeText(this@HomeActivity,"Agregue la cantidad", Toast.LENGTH_SHORT).show()
                                }
                                else {

                                    val inputManager: InputMethodManager =
                                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                    mAlertDialog.dismiss()
                                    Database(this@HomeActivity).addToVentas(
                                        DetallePedidos(
                                            IdProducto = model.Id.toString(),
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

        //Toast.makeText(this@HomeActivity,"Sugerencias Cargadas", Toast.LENGTH_SHORT).show()

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                sugerenciasLista.clear()

                for (i in 1.rangeTo(p0.childrenCount)) {

                    val item = p0.child(i.toString()).child("Name").value.toString()


                    sugerenciasLista.add(item)

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




