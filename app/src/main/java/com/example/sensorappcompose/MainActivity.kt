package com.example.sensorappcompose

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensorappcompose.ui.theme.SensorAppComposeTheme
import kotlin.math.abs

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private var isMoving by mutableStateOf(false)

    // MediaPlayer para los sonidos
    private var stableSound: MediaPlayer? = null
    private var movingSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar sonidos
        stableSound = MediaPlayer.create(this, R.raw.estable) // Archivo de sonido "estable.mp3"
        movingSound = MediaPlayer.create(this, R.raw.movimiento) // Archivo de sonido "movimiento.mp3"

        // Inicializar SensorManager y Sensores
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Registrar los sensores
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        gyroscope?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        setContent {
            SensorAppComposeTheme {
                SensorScreen(isMoving = isMoving) {
                    // Reiniciar los valores y sonidos
                    isMoving = false
                    stableSound?.start() // Reproducir el sonido "Estable"
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    // Verifica si el dispositivo está en una posición estable
                    val newIsMoving = !(x < 0.5 && y < 0.5 && z > 9.5)

                    // Solo reproducir sonidos cuando haya un cambio de estado
                    if (newIsMoving != isMoving) {
                        isMoving = newIsMoving
                        if (isMoving) {
                            movingSound?.start() // Reproducir sonido de movimiento
                        } else {
                            stableSound?.start() // Reproducir sonido de estado "Estable"
                        }
                    }
                }
                Sensor.TYPE_GYROSCOPE -> {
                    val rotation = it.values[2]
                    if (abs(rotation) > 0.5) {
                        isMoving = true
                        movingSound?.start() // Reproducir sonido de movimiento
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario implementar nada aquí
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener la escucha de los sensores cuando la actividad se destruye
        sensorManager.unregisterListener(this)
        stableSound?.release()
        movingSound?.release()
    }
}

@Composable
fun SensorScreen(isMoving: Boolean, onReset: () -> Unit) {
    val sensorState = if (isMoving) "En Movimiento" else "Estable"

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = sensorState, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onReset() }) {
            Text(text = "Reiniciar")
        }
    }
}


