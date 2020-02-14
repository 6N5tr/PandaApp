package com.example.pandaapp.ViewHolder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.pandaapp.HomeActivity
import com.example.pandaapp.R
import com.google.zxing.ResultPoint
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import kotlinx.android.synthetic.main.activity_inline_scan.*

class InlineScanActivity : AppCompatActivity() {

    lateinit var captureManager: CaptureManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inline_scan)

        captureManager = CaptureManager(this, barcodeView)
        captureManager.initializeFromIntent(intent, savedInstanceState)

            txtResult.text = "scaneando..."
            barcodeView.decodeSingle(object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult?) {
                    result?.let {
                        txtResult.text = it.text

                        if(txtResult.text!=""){
                            getCode()
                        }
                        val vib: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                        if (vib.hasVibrator()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                // void vibrate (VibrationEffect vibe)
                                vib.vibrate(
                                    VibrationEffect.createOneShot(
                                        100,
                                        // The default vibration strength of the device.
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                )
                            } else {
                                // This method was deprecated in API level 26
                                vib.vibrate(100)
                            }
                        }
                    }
                }

                override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                }
            })


    }
    private fun getCode() {
        val resultIntent = Intent()
        resultIntent.putExtra(Code, txtResult.text)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
    companion object {
        @JvmField
        val Code = "com.example.app.Code" //this is basically a static field in java that can be accessed from other classes
    }

    override fun onPause() {
        super.onPause()
        captureManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        captureManager.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.onDestroy()
    }
}
