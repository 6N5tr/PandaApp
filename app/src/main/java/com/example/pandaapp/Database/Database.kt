package com.example.pandaapp.Database

import android.content.Context
import android.database.sqlite.SQLiteQueryBuilder
import com.example.pandaapp.Model.DetallePedidos
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper



var DB_NAME:String="PandaDB.db"
var DB_Ver:Int=1


class Database(context: Context?) : SQLiteAssetHelper(context, DB_NAME, null, DB_Ver) {

    fun getVentas(): List<DetallePedidos> {

        var db=readableDatabase
        var qb=SQLiteQueryBuilder()

        var sqlSelect= arrayOf("IdProducto","NombreProducto","CantidadProducto","PrecioProducto")
        var sqlTable="DetallePedidos"

        qb.tables=sqlTable
        var c=qb.query(db,sqlSelect,null,null,null,null,null)

        val result = arrayListOf<DetallePedidos>()
        //val result = lista

        if(c.moveToFirst()){
            do{
                result.add(
                    (DetallePedidos(
                            c.getString(c.getColumnIndex("IdProducto")),
                            c.getString(c.getColumnIndex("NombreProducto")),
                            c.getString(c.getColumnIndex("CantidadProducto")),
                            c.getString(c.getColumnIndex("PrecioProducto")))
                    )
                            )

            }while (c.moveToNext())
        }
        return result
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

    fun editVenta(Id:Int,cantidad:String){
        var db=readableDatabase
        var query= String.format("UPDATE DETALLEPEDIDOS\n" +
                "SET CantidadProducto = "+cantidad.toInt()+"\n" +
                "WHERE IdProducto="+Id+";")
        db.execSQL(query)
    }

    fun eliminaItem(Id:Int){
        var db=readableDatabase
        var query= String.format("DELETE FROM DETALLEPEDIDOS\n" +
                "WHERE IdProducto="+Id+";")
        db.execSQL(query)
    }

     fun checkItem(nombre:String):Boolean{
        var db=readableDatabase
        var query= String.format("SELECT * FROM DETALLEPEDIDOS\n" +
                "WHERE NombreProducto="+nombre+";")

        db.execSQL(query)
        val cursor = db.rawQuery(query, null)

        if(cursor.count <= 0){
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }


}

