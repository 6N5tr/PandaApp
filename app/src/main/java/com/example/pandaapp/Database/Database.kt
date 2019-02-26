package com.example.pandaapp.Database

import android.content.Context
import android.database.sqlite.SQLiteQueryBuilder
import com.example.pandaapp.Model.DetallePedidos
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper



var DB_NAME:String="PandaDB.db"
var DB_Ver:Int=1


class Database(context: Context?) : SQLiteAssetHelper(context, DB_NAME, null, DB_Ver) {

    fun getVentas(lista:List<DetallePedidos>){

        var db=readableDatabase
        var qb=SQLiteQueryBuilder()

        var sqlSelect= arrayOf("IdProducto","NombreProducto","CantidadProducto","PrecioProducto")
        var sqlTable="DetallePedidos"

        qb.tables=sqlTable
        var c=qb.query(db,sqlSelect,null,null,null,null,null)

        val result = ArrayList<List<DetallePedidos>>()
        //val result = lista

        if(c.moveToFirst()){
            do{
                result.add(
                    (listOf(
                        DetallePedidos(
                            c.getString(c.getColumnIndex("IdProducto")),
                            c.getString(c.getColumnIndex("NombreProducto")),
                            c.getString(c.getColumnIndex("CantidadProducto")),
                            c.getString(c.getColumnIndex("PrecioProducto")))
                    )
                            )
                )
            }while (c.moveToNext())
        }

    }

    fun addToVentas(detalle:DetallePedidos){

        var db=readableDatabase
        var query= String.format("INSERT INTO DETALLEPEDIDOS(IdProducto,NombreProducto,CantidadProducto,PrecioProducto) " +
                "VALUES ('%s','%s','%s','%s');",
            detalle.IdProducto,detalle.NombreProducto,detalle.CantidadProducto,detalle.PrecioProducto)

        db.execSQL(query)
    }

    fun borrarTodoVentas(){
        var db=readableDatabase
        var query= String.format("DELETE FROM DETALLEPEDIDOS")
        db.execSQL(query)
    }



}

