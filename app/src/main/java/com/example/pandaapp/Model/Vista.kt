package com.example.pandaapp.Model

class Vista{
    var Name:String?=null
    var Photo:String?=null
    var Price:Double?=null
    var Id:Int?=null
    var Codebar:String?=null

    constructor():this("","",0.00,0,"")

    constructor(Name: String?, Photo: String?, Price: Double?,Id:Int?,Codebar:String?) {
        this.Name = Name
        this.Photo = Photo
        this.Price = Price
        this.Id=Id
        this.Codebar=Codebar

    }


}