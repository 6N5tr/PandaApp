package com.example.pandaapp

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.text.Layout
import android.view.MotionEvent
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {

    val duracionani: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)


        startAnimcacion()

    }


    fun startAnimcacion (){
        val animacion = ValueAnimator.ofFloat(0f , 1f)

        animacion.addUpdateListener {
            val valor = it.animatedValue as Float

            imageView.scaleX = valor
            imageView.scaleY = valor

        }

        animacion.interpolator = BounceInterpolator()
        animacion.duration = duracionani

        val intent = Intent(this,MainActivity::class.java)

        inilayout.setOnTouchListener {v: View, m: MotionEvent ->
           animacion.cancel()
                     true
        }
        animacion.addListener(object : Animator.AnimatorListener{

            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                startActivity(intent)
                finish()
            }

            override fun onAnimationCancel(animation: Animator?) {


            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
        animacion.start()

    }

}
