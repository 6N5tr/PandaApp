package com.example.pandaapp.Model

class DetallePedidos {
    var IdProducto:String?=null
    var NombreProducto:String?=null
    var CantidadProducto:String?=null
    var PrecioProducto:String?=null

    constructor():this("","","",""){}

    constructor(
        IdProducto: String?,
        NombreProducto: String?,
        CantidadProducto: String?,
        PrecioProducto: String?
    ) {
        this.IdProducto = IdProducto
        this.NombreProducto = NombreProducto
        this.CantidadProducto = CantidadProducto
        this.PrecioProducto = PrecioProducto
    }



}