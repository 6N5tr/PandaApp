package com.example .pandaapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AlertDialogLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.pandaapp.Class.Estructura
import com.example.pandaapp.Class.notification
import com.example.pandaapp.Comun.Comun
import com.example.pandaapp.Database.Database
import com.example.pandaapp.Model.DetallePedidos
import com.example.pandaapp.Model.Vista
import com.example.pandaapp.ViewHolder.InlineScanActivity
import com.example.pandaapp.ViewHolder.MenuViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.github.kittinunf.fuel.httpPost
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.mancj.materialsearchbar.MaterialSearchBar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.cantidad_dialog.*
import kotlinx.android.synthetic.main.cantidad_dialog.view.*
import kotlinx.android.synthetic.main.cantidad_dialog.view.Aceptar
import kotlinx.android.synthetic.main.cantidad_dialog.view.Cancelar
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.loginingresos_dialog.view.*
import java.util.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var p="Panda/"
    var database = FirebaseDatabase.getInstance()
    var ref = database.getReference(p+"Views")

    lateinit var mRecyclerView:RecyclerView
    lateinit var show_progress:ProgressBar
    var position =0

   //Cargar Datos en Lista de Sugerencias
    var sugerenciasLista= ArrayList<String>()
    var mSearchBar:MaterialSearchBar?=null

    //SPEECH
    private val REQUEST_CODE_SPEECH_INPUT=100

    private val Code=1

    var barcode=""

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
                            database.getReference(p+"Views").orderByChild("Name").startAt(searchBar.text.first().toString().toUpperCase()+searchBar.text.toString().substring(1,searchBar.text.length))
                                .endAt(searchBar.text.substring(1,searchBar.text.length)+"\uf8ff"),Vista::class.java
                        )
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

//1
                                        holder.itemView.setOnClickListener{

                                            hideKeyboard()

                                        val mDialogView=LayoutInflater.from(this@HomeActivity).inflate(R.layout.cantidad_dialog,null)
                                        val mBuilder=AlertDialog.Builder(this@HomeActivity)
                                            .setView(mDialogView)
                                            .setTitle("Agregar Cantidad "+model.Name)
                                        val mAlertDialog=mBuilder.show()

                                        // open the soft keyboard

                                        val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
                                        mDialogView.inputcantidad.requestFocus()

//2
                                        mDialogView.Aceptar.setOnClickListener{
                                            val cantidad = mDialogView.inputcantidad.text.toString()
                                            if (cantidad.isEmpty()) {
                                                Toast.makeText(this@HomeActivity,"Agregue la cantidad", Toast.LENGTH_SHORT).show()
                                            }
                                            else {

                                                val inputManager: InputMethodManager =
                                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                                mAlertDialog.dismiss()

                                                //Agregar comparacion de pedidos.
                                                if(Database(this@HomeActivity).checkItem(model.Name.toString())==true){
                                                    var IdPro=Database(this@HomeActivity).checkId(model.Name.toString())
                                                    var cantPro=Database(this@HomeActivity).getCant(model.Name.toString())
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

                                        hideKeyboard()

                                        val mDialogView=LayoutInflater.from(this@HomeActivity).inflate(R.layout.cantidad_dialog,null)
                                        val mBuilder=AlertDialog.Builder(this@HomeActivity)
                                            .setView(mDialogView)
                                            .setTitle("Agregar Cantidad "+model.Name)
                                        val mAlertDialog=mBuilder.show()


                                        // open the soft keyboard

                                        val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
                                        mDialogView.inputcantidad.requestFocus()

//2
                                        mDialogView.Aceptar.setOnClickListener{
                                            val cantidad = mDialogView.inputcantidad.text.toString()
                                            if (cantidad.isEmpty()) {
                                                Toast.makeText(this@HomeActivity,"Agregue la cantidad", Toast.LENGTH_SHORT).show()
                                            }
                                            else {

                                                val inputManager: InputMethodManager =
                                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                                mAlertDialog.dismiss()

                                                //Agregar comparacion de pedidos.
                                                if(Database(this@HomeActivity).checkItem(model.Name.toString())==true){
                                                    var IdPro=Database(this@HomeActivity).checkId(model.Name.toString())
                                                    var cantPro=Database(this@HomeActivity).getCant(model.Name.toString())
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

                            val mDialogView=LayoutInflater.from(this@HomeActivity).inflate(R.layout.cantidad_dialog,null)
                            val mBuilder=AlertDialog.Builder(this@HomeActivity)
                                .setView(mDialogView)
                                .setTitle("Agregar Cantidad "+model.Name)

                            val mAlertDialog=mBuilder.show()



                            val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
                            mDialogView.inputcantidad.requestFocus()


//3
                            mDialogView.Aceptar.setOnClickListener{
                                val cantidad = mDialogView.inputcantidad.text.toString()
                                if (cantidad.isEmpty()) {
                                    Toast.makeText(this@HomeActivity,"Agregue la cantidad", Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    mDialogView.inputcantidad.requestFocus()
                                    val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                    mAlertDialog.dismiss()

                                    //Agregar comparacion de pedidos.
                                    if(Database(this@HomeActivity).checkItem(model.Name.toString())==true){
                                        var IdPro=Database(this@HomeActivity).checkId(model.Name.toString())
                                        var cantPro=Database(this@HomeActivity).getCant(model.Name.toString())
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

        val intent = Intent(this, InlineScanActivity::class.java)
        startActivityForResult(intent, Code)


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
                val name = data?.getStringExtra(InlineScanActivity.Code)
                val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (name != null) {
                    if (name.equals("")) {
                        Toast.makeText(this, "Escaneo Terminado!", Toast.LENGTH_LONG).show()
                    } else {

                        var codebar= name
                        val option = FirebaseRecyclerOptions.Builder<Vista>()
                            .setQuery(database.getReference("Views").orderByChild("Codebar").equalTo(codebar.toString()),Vista::class.java)
                            .build()

                        val firebaseRecyclerAdapter =object : FirebaseRecyclerAdapter<Vista, MenuViewHolder>(option) {
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

                                            if (!(this@HomeActivity).isFinishing) {
                                                //show dialog
                                                val mDialogView = LayoutInflater.from(this@HomeActivity)
                                                    .inflate(R.layout.cantidad_dialog, null)
                                                val mBuilder = AlertDialog.Builder(this@HomeActivity)
                                                    .setView(mDialogView)
                                                    .setTitle("Agregar Cantidad " + model.Name)

                                                val mAlertDialog = mBuilder.show()

                                                val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
                                                mDialogView.inputcantidad.requestFocus()

//4

                                                mDialogView.Aceptar.setOnClickListener {
                                                    val cantidad = mDialogView.inputcantidad.text.toString()
                                                    if (cantidad.isEmpty()) {
                                                        Toast.makeText(
                                                            this@HomeActivity,
                                                            "Agregue la cantidad",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {

                                                        val inputManager: InputMethodManager =
                                                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                        inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                                        mAlertDialog.dismiss()

                                                        //Agregar comparacion de pedidos.
                                                        if (Database(this@HomeActivity).checkItem(model.Name.toString()) == true) {
                                                            var IdPro =
                                                                Database(this@HomeActivity).checkId(model.Name.toString())
                                                            var cantPro =
                                                                Database(this@HomeActivity).getCant(model.Name.toString())
                                                            val cantidad = mDialogView.inputcantidad.text.toString()
                                                            Database(this@HomeActivity).editVenta(
                                                                Id = IdPro!!.toInt(),
                                                                cantidad = (cantidad.toInt() + cantPro).toString()
                                                            )
                                                            //Mensaje que se muestra cuando un producto ya esta agragado a las ventas y se desea agregar o modicar el valor de la cantida de este
                                                            Toast.makeText(
                                                                this@HomeActivity,
                                                                "" + model.Name.toString() + " Cantidad Total: " + (cantidad.toInt() + cantPro).toString(),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
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

                       /* var HomeIntent= Intent(this,HomeActivity::class.java)
                        startActivity(HomeIntent)
                        finish()*/

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
            .setQuery(
                database.getReference("Views").orderByChild("Name").startAt(searchBar.text.first().toString().toUpperCase()+searchBar.text.toString().substring(1,searchBar.text.length))
                    .endAt(searchBar.text.toString().substring(1,searchBar.text.length)+"\uf8ff"),
                Vista::class.java
            )
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


                        holder.itemView.setOnClickListener {
                            if (!(this@HomeActivity).isFinishing) {
                                val mDialogView = LayoutInflater.from(this@HomeActivity)
                                    .inflate(R.layout.cantidad_dialog, null)
                                val mBuilder = AlertDialog.Builder(this@HomeActivity)
                                    .setView(mDialogView)
                                    .setTitle("Agregar Cantidad " + model.Name)

                                val mAlertDialog = mBuilder.show()

                                val inputManager: InputMethodManager =
                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                                mDialogView.inputcantidad.requestFocus()

//5
                                mDialogView.Aceptar.setOnClickListener {
                                    val cantidad = mDialogView.inputcantidad.text.toString()
                                    if (cantidad.isEmpty()) {
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "Agregue la cantidad",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {

                                        val inputManager: InputMethodManager =
                                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        inputManager.hideSoftInputFromWindow(
                                            mDialogView.windowToken,
                                            0
                                        )
                                        mAlertDialog.dismiss()

                                        //Agregar comparacion de pedidos.
                                        if (Database(this@HomeActivity).checkItem(model.Name.toString()) == true) {

                                            var IdPro =
                                                Database(this@HomeActivity).checkId(model.Name.toString())
                                            var cantPro =
                                                Database(this@HomeActivity).getCant(model.Name.toString())
                                            val cantidad = mDialogView.inputcantidad.text.toString()
                                            Database(this@HomeActivity).editVenta(
                                                Id = IdPro!!.toInt(),
                                                cantidad = (cantidad.toInt() + cantPro).toString()
                                            )
                                            //Mensaje que se muestra cuando un producto ya esta agragado a las ventas y se desea agregar o modicar el valor de la cantida de este
                                            Toast.makeText(
                                                this@HomeActivity,
                                                "" + model.Name.toString() + " Cantidad Total: " + (cantidad.toInt() + cantPro).toString(),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
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
                                }
                                mDialogView.Cancelar.setOnClickListener {
                                    val inputManager: InputMethodManager =
                                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputManager.hideSoftInputFromWindow(mDialogView.windowToken, 0)
                                    mAlertDialog.dismiss()

                                }

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



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_Ingresos -> {

                //Aqui contraseña y uso

                val mDialogView=LayoutInflater.from(this@HomeActivity).inflate(R.layout.loginingresos_dialog,null)
                val mBuilder=AlertDialog.Builder(this@HomeActivity)
                    .setView(mDialogView)
                    .setTitle("Seguridad")
                val mAlertDialog=mBuilder.show()

                val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
                mDialogView.usuario.requestFocus()

//5
                mDialogView.Aceptar.setOnClickListener{
                    val user = mDialogView.usuario.text.toString()
                    val contr=mDialogView.contraseña.text.toString()
                    if ( user.isEmpty()) {
                        Toast.makeText(this@HomeActivity,"Digite su usuario", Toast.LENGTH_SHORT).show()
                    }
                    else if(contr.isEmpty()){
                        Toast.makeText(this@HomeActivity,"Digite su contraseña", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        var ref1 = database.getReference("User")
                        var position =0


                        val pD= ProgressDialog(this@HomeActivity)
                        pD.setMessage("Espere porfavor...")
                        pD.show()

                        ref1.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {

                            }

                            override fun onDataChange(p0: DataSnapshot) {

                                for ( i in 1.rangeTo(p0.childrenCount)){


                                    val valor=p0.child(i.toString()).child("Name").value.toString()

                                    if(valor.equals("Eve")){
                                        position=i.toInt()
                                        break
                                    }

                                }

                                //if(p0.child(position.toString()).exists()){}
                                if(contr.equals(p0.child(position.toString()).child("Pass").value.toString())){
                                    Toast.makeText(this@HomeActivity,"Ingreso Valido", Toast.LENGTH_SHORT).show()
                                    pD.dismiss()
                                    Comun.currentUser=p0.child(position.toString()).child("Name").value.toString()
                                    val intento = Intent(this@HomeActivity,IngresosActivity::class.java)
                                    startActivity(intento)
                                    val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputManager.hideSoftInputFromWindow(mDialogView.windowToken,0)
                                    mAlertDialog.dismiss()
                                    position=0
                                    finish()
                                }else{
                                    Toast.makeText(this@HomeActivity,"Contraseña y/o usuario no validos!",Toast.LENGTH_SHORT).show()
                                    pD.dismiss()
                                    position=0
                                }


                                pD.dismiss()

                            }

                        })





                    }

                    }
                mDialogView.Cancelar.setOnClickListener{
                    val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(mDialogView.windowToken,0)
                    mAlertDialog.dismiss()

                }



            }
            R.id.nav_Reportes-> {


            }
            R.id.nav_Observaciones -> {
                val post= Estructura(
                    "eRl36Bv4DFc:APA91bGACko4g3ikV3TVldBpDqA_SE5YTNQLhKHJ2gm0wxAZ90vzmwmipIG6Q3uEILpzxnEPnB6LQgJvnKiaYS3WCLbpfhvfU01bZ970pQGTSO4CHPOerUgvTiK7B3ndXtPoNrkUudVX",
                    notification(title = "hola",body = "chao")
                )
                val postJson = Gson().toJson(post)

                "https://fcm.googleapis.com/fcm/send".httpPost()
                    .header(
                        "Content-Type" to "application/json",
                        "Authorization" to "key=AAAAipzqo8Q:APA91bFj6kxPPulVslLckgeEVbw-yoy_rrH27uXR3kMQrBvt94SC2d-fCbZJJcKSmujH-9GwHJPyCAao6L8clpA5W8-nPjrLm4yK2CnLJZJw3qrFdQGLWd5_dq7hzp3fVF82WWUhMvKy")
                    .body(postJson.toString()).response { req, res, result ->
                        Toast.makeText(this@HomeActivity,"Mensaje Enviado", Toast.LENGTH_SHORT).show()
                    }
            }
            R.id.nav_Salir -> {

            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }



    fun openSoftKeyboard(context: Context, view: View) {
        view.requestFocus()
        // open the soft keyboard
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun dispatchKeyEvent(e: KeyEvent): Boolean {

        if (e.action == KeyEvent.ACTION_DOWN) {
            //Log.i(FragmentActivity.TAG, "dispatchKeyEvent: $e")
            val pressedKey = e.unicodeChar.toChar()
            barcode += pressedKey
        }
        if (e.action == KeyEvent.ACTION_DOWN && e.keyCode == KeyEvent.KEYCODE_ENTER) {

            // Toast.makeText(this@HomeActivity,"Codigo de Barras --->>> $barcode", Toast.LENGTH_SHORT).show()

            val result = barcode
            if (result != null) {

                if (result == null) {
                    Toast.makeText(this, "No Agregado al Carrito!", Toast.LENGTH_LONG).show()
                } else {
                    if(result.length>=0){

                        var codebar= result
                        //searchBar.text=codebar
                        val option = FirebaseRecyclerOptions.Builder<Vista>()
                            .setQuery(database.getReference("Views").orderByChild("Codebar").equalTo(codebar.substring(0,codebar.length-1)),Vista::class.java)
                            .build()

                        val firebaseRecyclerAdapter =object : FirebaseRecyclerAdapter<Vista, MenuViewHolder>(option) {
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


                                            if (!(this@HomeActivity).isFinishing) {
                                                val mDialogView = LayoutInflater.from(this@HomeActivity)
                                                    .inflate(R.layout.cantidad_dialog, null)
                                                val mBuilder = AlertDialog.Builder(this@HomeActivity)
                                                    .setView(mDialogView)
                                                    .setTitle("Agregar Cantidad")
                                                val mAlertDialog = mBuilder.show()

                                                val inputManager: InputMethodManager =
                                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                inputManager.toggleSoftInput(
                                                    InputMethodManager.SHOW_FORCED,
                                                    0
                                                )

//2
                                                mDialogView.Aceptar.setOnClickListener {
                                                    val cantidad =
                                                        mDialogView.inputcantidad.text.toString()
                                                    if (cantidad.isEmpty()) {
                                                        Toast.makeText(
                                                            this@HomeActivity,
                                                            "Agregue la cantidad",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {

                                                        val inputManager: InputMethodManager =
                                                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                        inputManager.hideSoftInputFromWindow(
                                                            mDialogView.windowToken,
                                                            0
                                                        )
                                                        mAlertDialog.dismiss()

                                                        //Agregar comparacion de pedidos.
                                                        if (Database(this@HomeActivity).checkItem(model.Name.toString()) == true) {
                                                            var IdPro =
                                                                Database(this@HomeActivity).checkId(
                                                                    model.Name.toString()
                                                                )
                                                            var cantPro =
                                                                Database(this@HomeActivity).getCant(
                                                                    model.Name.toString()
                                                                )
                                                            val cantidad =
                                                                mDialogView.inputcantidad.text.toString()
                                                            Database(this@HomeActivity).editVenta(
                                                                Id = IdPro!!.toInt(),
                                                                cantidad = (cantidad.toInt() + cantPro).toString()
                                                            )
                                                            //Mensaje que se muestra cuando un producto ya esta agragado a las ventas y se desea agregar o modicar el valor de la cantida de este
                                                            Toast.makeText(
                                                                this@HomeActivity,
                                                                "" + model.Name.toString() + " Cantidad Total: " + (cantidad.toInt() + cantPro).toString(),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
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


                                                }
                                                mDialogView.Cancelar.setOnClickListener {
                                                    val inputManager: InputMethodManager =
                                                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                    inputManager.hideSoftInputFromWindow(
                                                        mDialogView.windowToken,
                                                        0
                                                    )
                                                    mAlertDialog.dismiss()
                                                    finish()
                                                    startActivity(getIntent())

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
                    else{
                        Toast.makeText(this, "Vuelva a Escanear!", Toast.LENGTH_LONG).show()
                    }


                }
            } else {
                Toast.makeText(this, "No Agregado al Carrito!", Toast.LENGTH_LONG).show()
            }


            barcode = ""



        }

        return false
    }


}





