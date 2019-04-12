package com.example.pandaapp.Messaging

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random
import android.R
import android.app.PendingIntent
import android.content.Intent








class MyFirebaseInstanceService: FirebaseMessagingService() {


    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)

        var from = p0?.from
        Log.d(eltak,"Mensaje recibido de:" + from)



        if(p0?.notification !=null){
            Log.d(eltak,"Notifi: "+ p0.notification!!.body)
            mostrarNotificacion(p0.notification!!.title, p0.notification!!.body)
        }
        else{
            mostrarNotificacio(p0!!.data)
            Log.d(eltak,"Notifi 2: "+ p0.data)
        }

        if(p0.data!!.isNotEmpty()){
            Log.d(eltak,"Data: "+ p0.data)
        }


    }

    private fun mostrarNotificacion(title: String?, body: String?) {

        var sound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


        var notBuild=NotificationCompat.Builder(this)
        notBuild.setSmallIcon(com.example.pandaapp.R.drawable.pandaintro)
        notBuild.setContentTitle(title)
        notBuild.setContentText(body)
        notBuild.setAutoCancel(true)
        notBuild.setSound(sound)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

       notificationManager.notify(0,notBuild.build())


    }

    private fun mostrarNotificacio(data:Map<String,String>) {
        var title=data.toString()
        Log.d(eltak,"Title: "+title.toString())
        var body=data.get("bod").toString()

        var sound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


        var notBuild=NotificationCompat.Builder(this)
        notBuild.setSmallIcon(com.example.pandaapp.R.drawable.pandaintro)
        notBuild.setContentTitle(title)
        notBuild.setContentText(body)
        notBuild.setAutoCancel(true)
        notBuild.setSound(sound)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0,notBuild.build())
    }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        Log.d(eltak,"NewToken: "+p0)

    }
}