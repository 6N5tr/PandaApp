package com.example.pandaapp

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.pandaapp.Comun.Comun
import com.example.pandaapp.Database.Database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*




class MainActivity : AppCompatActivity() {


    var database = FirebaseDatabase.getInstance()
    var ref = database.getReference("User")
    var position =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Database(baseContext).borrarTodoVentas()

        var imgG=findViewById<ImageView>(R.id.imggif)
        Glide.with(this).asGif()
            .load(R.drawable.pandaintro)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(imgG)


        btnIngreso.setOnClickListener{
            this.ingreso()
        }
    }

    fun ingreso(){
        val pD= ProgressDialog(this@MainActivity)
        pD.setMessage("Espere porfavor...")
        pD.show()

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

               for ( i in 1.rangeTo(p0.childrenCount)){


                    val valor=p0.child(i.toString()).child("Name").value.toString()

                    if(valor.equals(edtName.text.toString())){
                        position=i.toInt()
                        break
                    }

                }

                //if(p0.child(position.toString()).exists()){}
                if(edtPass.text.toString().equals(p0.child(position.toString()).child("Pass").value.toString())){
                    Toast.makeText(this@MainActivity,"Ingreso Valido", Toast.LENGTH_SHORT).show()
                    pD.dismiss()
                    Comun.currentUser=p0.child(position.toString()).child("Name").value.toString()
                    val intento = Intent(this@MainActivity,HomeActivity::class.java)
                    startActivity(intento)
                    position=0
                    finish()
                }else{
                    Toast.makeText(this@MainActivity,"Contraseña y/o usuario no validos!",Toast.LENGTH_SHORT).show()
                    pD.dismiss()
                    position=0
                }


                pD.dismiss()

            }

        })





    }


}
