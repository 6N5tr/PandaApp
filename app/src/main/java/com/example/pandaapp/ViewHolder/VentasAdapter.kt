package com.example.pandaapp.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.pandaapp.Model.DetallePedidos

import android.R
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import com.amulyakhare.textdrawable.TextDrawable
import com.bumptech.glide.load.engine.bitmap_recycle.IntegerArrayAdapter
import java.text.NumberFormat
import java.util.*
import kotlin.contracts.contract


class VentasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){


    var mNombre:TextView=itemView.findViewById(com.example.pandaapp.R.id.venta_item_name)
    var mImagen: ImageView =itemView.findViewById(com.example.pandaapp.R.id.venta_item_img)
    var mPrecio:TextView=itemView.findViewById(com.example.pandaapp.R.id.venta_item_price)


}

class VentasAdapter(context:Context,listData:List<DetallePedidos>): RecyclerView.Adapter<VentasViewHolder>(),View.OnClickListener {
    override fun onClick(v: View?) {
    if(listener!=null){
        listener!!.onClick(v)
    }
    }

    var listener:View.OnClickListener?=null



    var listData= listData
    var context=context

     override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VentasViewHolder {


         val itemv=LayoutInflater.from(this.context).inflate(com.example.pandaapp.R.layout.preventa_layout,p0,false)

         itemv.setOnClickListener(this)

         return VentasViewHolder(itemv)

    }

    override fun getItemCount(): Int     {
        return listData.size

    }

    override fun onBindViewHolder(p0: VentasViewHolder, p1: Int) {

        var drawable=TextDrawable.builder()
            .buildRound(    ""+listData.get(p1).CantidadProducto, Color.parseColor("#c66136"))

        p0.mImagen.setImageDrawable(drawable)

        var local= Locale("en","US")
        var frmt=NumberFormat.getCurrencyInstance(local)

        var price=(java.lang.Double.parseDouble(listData.get(p1).PrecioProducto))*(java.lang.Double.parseDouble(listData.get(p1).CantidadProducto))

        p0.mPrecio.text = frmt.format(price)


        p0.mNombre.text = listData.get(p1).NombreProducto





    }

    fun setOnClickListener(listener:View.OnClickListener){
            this.listener=listener
    }

    fun removeAt(position: Int) {
        var lista=listData.toMutableList()
        lista.removeAt(position)
        listData=lista
        notifyItemRemoved(position)
    }


}