package com.example.pandaapp.Model

class Request {

    var NombreVendedor:String?=null
    var Total:String?=null
    var FechayHora:String?=null
    var Detalle:List<DetallePedidos>?=null

    constructor(NombreVendedor: String?, Total: String?, FechayHora: String?, Detalle: List<DetallePedidos>?) {
        this.NombreVendedor = NombreVendedor
        this.Total = Total
        this.FechayHora = FechayHora
        this.Detalle = Detalle
    }

    fun Request() {

    }


}