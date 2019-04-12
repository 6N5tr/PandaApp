package com.example.pandaapp.Messaging

import android.util.Log
import android.widget.Toast
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import android.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.internal.FirebaseAppHelper.getToken


    const val eltak="NOTICIAS"

class MyFirebaseInstanceIdService: FirebaseInstanceIdService(){

    override fun onTokenRefresh() {
        super.onTokenRefresh()

        var tok=FirebaseInstanceId.getInstance().token
        Log.d(eltak,"Token: "+tok)

        sendRegistrationToServer(tok!!)
    }

    private fun sendRegistrationToServer(token: String) {

        var database = FirebaseDatabase.getInstance()
        var ref = database.getReference("Token")

        // then store your token ID
        ref.setValue(token)

    }


}

