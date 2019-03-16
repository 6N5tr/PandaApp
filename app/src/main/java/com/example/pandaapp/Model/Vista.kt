package com.example.pandaapp.Model

class Vista{
    var Name:String?=null
    var Photo:String?=null
    var Price:Double?=null
    var Id:Int?=null

    constructor():this("","",0.00,0){}

    constructor(Name: String?, Photo: String?, Price: Double?,Id:Int?) {
        this.Name = Name
        this.Photo = Photo
        this.Price = Price
        this.Id=Id
    }


}