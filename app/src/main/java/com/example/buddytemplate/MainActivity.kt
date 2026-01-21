package com.example.buddytemplate

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import com.bfr.buddy.speech.shared.ISTTCallback
import com.bfr.buddy.speech.shared.STTResultsData
import com.bfr.buddy.ui.shared.FacialExpression
import com.bfr.buddy.ui.shared.LabialExpression
import com.bfr.buddy.usb.shared.IUsbCommadRsp
import com.bfr.buddysdk.BuddyActivity
import com.bfr.buddysdk.BuddySDK
import com.bfr.buddysdk.services.speech.STTTask
import java.util.Locale

class MainActivity : BuddyActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // IMPORTANTE: No es seguro incluir ninguna llamada a la API de Buddy en este callback, esperar a onSDKReady

//        // Ejemplo de llamada a otra actividad en  Kotlin
//        val helloButton = findViewById<Button>(R.id.buttonHello)
//        helloButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }

    }

    override fun onSDKReady() {
        BuddySDK.UI.setViewAsFace(findViewById(R.id.view_face)) // NOTA: si no quiere que las
        // pulsaciones en el fondo se redirijan a la aplicación que controla la cara de Buddy,
        // comentar esta línea

        val helloButton = findViewById<Button>(R.id.buttonHello)
        helloButton.setOnClickListener{
            BuddySDK.Speech.startSpeaking("Hola", LabialExpression.SPEAK_NEUTRAL) // TTS
            //BuddySDK.UI.setFacialExpression(FacialExpression.LOVE)
            //BuddySDK.UI.setMood(FacialExpression.LOVE) // Emoción
            //listen() // STT
            //Actuadores
            //sayYes(30f)
            //sayNo(-30f)
            //moveForward(0.5f)
            //rotate(180f)
        }
    }

    fun listen() {
        var sttTask = BuddySDK.Speech.createGoogleSTTTask(Locale.forLanguageTag("es-ES"))
        sttTask.start(true, object: ISTTCallback.Stub() {
            override fun onSuccess(result: STTResultsData?) {
                result?.let {
                    if (it.results.isNotEmpty()) {
                        val spoken = it.results[0].utterance.trim()
                        sttTask?.stop()
                        println(spoken)
                    }
                }
            }
            override fun onError(p0: String?) {}
        })
    }

    fun sayYes(angle: Float) {
        val speed = 30f // Velocidad del movimiento (º/s) : (0.1 - 49.2)

        // IMPORTANTE: debemos esperar a que el robot termine de ejecutar una acción que involucre
        //             los motores antes de solicitar la siguiente
        if (BuddySDK.Actuators.yesStatus == "SET") {
            println("Motor YES ocupado, esperando para reintentar...")
            Handler(Looper.getMainLooper()).postDelayed({
                sayYes(angle)
            }, 100)
            return
        }

        BuddySDK.USB.enableYesMove(true, object: IUsbCommadRsp.Stub() {
            override fun onSuccess(s: String) {
                BuddySDK.USB.buddySayYes(speed, angle, object: IUsbCommadRsp.Stub() {
                    override fun onSuccess(s1: String) {
                        if (s1 == "YES_MOVE_FINISHED") {
                            BuddySDK.USB.buddySayYes(speed, 0f, object: IUsbCommadRsp.Stub() {
                                override fun onSuccess(s2: String) {
                                    println("Movimiento Yes: $s2")
                                }
                                override fun onFailed(p0: String?) {}
                            })
                        }
                    }
                    override fun onFailed(p0: String?) {}
                })
            }
            override fun onFailed(p0: String?) {}
        })
    }

    // NOTA: aparentemente, el Buddy del laboratorio no es capaz de girar la cabeza a la izquierda
    //       de la posición central, por lo que no realizará ningún movimiento si especificamos un
    //       ángulo negativo
    fun sayNo(angle: Float) {
        val speed = 30f // Velocidad del movimiento, º/s: (0.1 a 140)

        // IMPORTANTE: debemos esperar a que el robot termine de ejecutar una acción que involucre
        //             los motores antes de solicitar la siguiente
        if (BuddySDK.Actuators.noStatus == "SET") {
            println("Motor NO ocupado, esperando para reintentar...")
            Handler(Looper.getMainLooper()).postDelayed({
                sayNo(angle)
            }, 100)
            return
        }

        BuddySDK.USB.enableNoMove(true, object: IUsbCommadRsp.Stub() {
            override fun onSuccess(s: String) {
                BuddySDK.USB.buddySayNo(speed, 30f, object: IUsbCommadRsp.Stub() {
                    override fun onSuccess(s1: String) {
                        println(s1)
                        if (s1 == "NO_MOVE_FINISHED") {
                            BuddySDK.USB.buddySayNo(speed, -60f, object: IUsbCommadRsp.Stub() {
                                override fun onSuccess(s2: String) {
                                    println("Movimiento NO: $s2")
                                }
                                override fun onFailed(p0: String?) {}
                            })
                        }
                    }
                    override fun onFailed(p0: String?) {}
                })
            }
            override fun onFailed(p0: String?) {}
        })
    }

    fun moveForward(distance: Float) {
        val speed = 0.2f // Velocidad del movimiento (m/s): (0.05 a 0.7)

        // IMPORTANTE: debemos esperar a que el robot termine de ejecutar una acción que involucre
        //             los motores antes de solicitar la siguiente
        if (BuddySDK.Actuators.leftWheelStatus == "STRAIGHT" ||
            BuddySDK.Actuators.leftWheelStatus == "ROTATE") {
            println("Ruedas ocupadas, esperando para reintentar...")
            Handler(Looper.getMainLooper()).postDelayed({
                moveForward(distance)
            }, 100)
            return
        }

        BuddySDK.USB.enableWheels(true, object: IUsbCommadRsp.Stub() {
            override fun onSuccess(p0: String?) {
                BuddySDK.USB.moveBuddy(speed, distance, object: IUsbCommadRsp.Stub() {
                    override fun onSuccess(p0: String?) {
                        println("Desplazamiento: $p0")
                    }
                    override fun onFailed(p0: String?) {}
                })
            }

            override fun onFailed(p0: String?) {}

        })
    }

    fun rotate(angle: Float) {
        val speed = 80f // Velocidad del movimiento (º/s): (0.1 a 100)

        if (BuddySDK.Actuators.leftWheelStatus == "STRAIGHT" ||
            BuddySDK.Actuators.leftWheelStatus == "ROTATE"
        ) {
            println("Ruedas ocupadas, esperando para reintentar...")
            Handler(Looper.getMainLooper()).postDelayed({
                rotate(angle)
            }, 100)
            return
        }

        BuddySDK.USB.enableWheels(true, object : IUsbCommadRsp.Stub() {
            override fun onSuccess(p0: String?) {
                BuddySDK.USB.rotateBuddy(speed, angle, object : IUsbCommadRsp.Stub() {
                    override fun onSuccess(p0: String?) {
                        println("Rotación: $p0")
                    }
                    override fun onFailed(p0: String?) {}
                })
            }

            override fun onFailed(p0: String?) {}

        })

    }
}

