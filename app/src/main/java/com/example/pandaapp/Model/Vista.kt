package com.example.pandaapp.Model

class Vista{
    var Name:String?=null
    var Photo:String?=null
    var Price:Double?=null

    constructor():this("","",0.00){}

    constructor(Name: String?, Photo: String?, Price: Double?) {
        this.Name = Name
        this.Photo = Photo
        this.Price = Price
    }


}