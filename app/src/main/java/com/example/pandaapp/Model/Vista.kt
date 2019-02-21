package com.example.pandaapp.Model

class Vista{
    var Name:String?=null
    var Photo:String?=null

    constructor():this("",""){}


    constructor(Nombre: String?, Imagen: String?) {
        this.Photo = Imagen
        this.Name = Nombre
    }





}