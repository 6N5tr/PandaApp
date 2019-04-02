package com.example.pandaapp.Messaging

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.example.pandaapp.R
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseInstanceService: FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)

        showNot(p0!!.notification!!.title!!, p0.notification!!.body!!)

    }

    fun showNot(titulo:String,cuerpo:String){

        var NotChannelId="com.example.pandaapp.test"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val notificationChannel =NotificationChannel(NotChannelId, "Notification", NotificationManager.IMPORTANCE_DEFAULT)


            notificationChannel.description ="Panda"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor=Color.BLUE
            notificationChannel.vibrationPattern= longArrayOf(0,1000,500,1000)
            notificationManager.createNotificationChannel(notificationChannel)



        }
        var notBuilder= NotificationCompat.Builder(this,NotChannelId)

            notBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.splash)
            .setContentTitle(titulo)
            .setContentInfo("Info")
            .setContentText(cuerpo)

        notificationManager.notify(Random.nextInt(),notBuilder.build())
    }

}