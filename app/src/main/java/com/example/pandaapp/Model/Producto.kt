package com.example.pandaapp.Model

class Producto{
    var Name:String?=null
    var Quantity:Int?=null

    constructor():this("",0)

    constructor(Name: String?,Quantity:Int?) {
        this.Name = Name
        this.Quantity=Quantity
    }


}
