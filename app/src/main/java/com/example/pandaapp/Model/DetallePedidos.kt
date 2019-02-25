package com.example.pandaapp.Model

class DetallePedidos {
    var IdDetallePedio:String?=null
    var IdProducto:String?=null
    var NombreProducto:String?=null
    var CantidadProducto:String?=null
    var PrecioProducto:String?=null

    constructor(
        IdDetallePedio: String?,
        IdProducto: String?,
        NombreProducto: String?,
        CantidadProducto: String?,
        PrecioProducto: String?
    ) {
        this.IdDetallePedio = IdDetallePedio
        this.IdProducto = IdProducto
        this.NombreProducto = NombreProducto
        this.CantidadProducto = CantidadProducto
        this.PrecioProducto = PrecioProducto
    }



}