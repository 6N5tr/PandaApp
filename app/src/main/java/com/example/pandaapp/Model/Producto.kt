package com.example.pandaapp.Model

class Producto{
    var Name:String?=null
    var Price:Double?=null


    constructor():this("",0.0)

    constructor(Name: String?, Price: Double?) {
        this.Name = Name
        this.Price = Price
    }


}