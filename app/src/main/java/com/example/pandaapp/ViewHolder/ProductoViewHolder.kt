package com.example.pandaapp.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.pandaapp.R
import kotlinx.android.synthetic.main.menu_item.view.*


class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var mNombre:TextView=itemView.findViewById(R.id.producto_name)

}
