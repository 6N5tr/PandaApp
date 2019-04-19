package com.example.pandaapp.Messaging

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.example.pandaapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random


class MyFirebaseInstanceService: FirebaseMessagingService() {


    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)



            if(p0!!.data.isEmpty()) {
                mostrarNotificacion(p0.notification!!.title, p0.notification!!.body)
            }else{
                mostrarNotificacion(p0.data)
            }





        if(p0.data!!.isNotEmpty()){

            Log.d(eltak,"Data: "+ p0.notification!!.title)
            Log.d(eltak,"Data: "+ p0.notification!!.body)
            Log.d(eltak,"Data: "+ p0.data)

        }


    }

   private fun mostrarNotificacion(data: Map<String, String>) {

        var title=data.get("titulo").toString()
        var body=data.get("descripcion").toString()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var chan="com.example.pandaapp.test"


        var sound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var notBuild=NotificationCompat.Builder(this,chan)

        notBuild.setDefaults(Notification.DEFAULT_ALL)
        notBuild.setWhen(System.currentTimeMillis())
        notBuild.setSmallIcon(com.example.pandaapp.R.drawable.solologo)
        notBuild.setContentTitle(title)
        notBuild.setContentText(body)
        notBuild.setAutoCancel(true)
        notBuild.setSound(sound)
        notBuild.setContentInfo("Info")


        notificationManager.notify(Random.nextInt(),notBuild.build())


    }

    private fun mostrarNotificacion(title: String?, body: String?) {


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var chan="com.example.pandaapp.test"


        var sound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var notBuild=NotificationCompat.Builder(this,chan)

        notBuild.setDefaults(Notification.DEFAULT_ALL)
        notBuild.setWhen(System.currentTimeMillis())
        notBuild.setSmallIcon(R.mipmap.ic_panda)
        notBuild.setContentTitle(title)
        notBuild.setContentText(body)
        notBuild.setAutoCancel(true)
        notBuild.setSound(sound)
        notBuild.setContentInfo("Info")


        notificationManager.notify(Random.nextInt(),notBuild.build())


    }



    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        Log.d(eltak,"NewToken: "+p0)

    }
}