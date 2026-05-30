package com.example.fisioaging.ui.marcha

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.model.Usuario
import com.example.fisioaging.util.SessionManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

private enum class EstadoTeste {
    PRONTO,
    PREPARANDO,
    RODANDO,
    CONCLUIDO
}

class MarchaExecucaoActivity :
    AppCompatActivity(),
    SensorEventListener {

    private lateinit var textTimer: TextView
    private lateinit var lblStatus: TextView
    private lateinit var txtNomePaciente: TextView

    private lateinit var layoutBotaoPlay: LinearLayout
    private lateinit var layoutBotoesRodando: LinearLayout
    private lateinit var layoutBotoesConcluido: LinearLayout

    private lateinit var btnPlay: ImageButton
    private lateinit var btnRestartRodando: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var btnRestartConcluido: ImageButton
    private lateinit var btnDiscard: ImageButton
    private lateinit var btnSave: ImageButton

    private var timer: CountDownTimer? = null
    private var timerPreparacao: CountDownTimer? = null

    private val tempoTotalEmMillis = 120000L

    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null
    private var giroscopio: Sensor? = null

    private val dadosColetados = mutableListOf<JSONObject>()

    private var tempoInicioTeste = 0L
    private var paciente: Usuario? = null

    private lateinit var sessionManager: SessionManager

    private var contagemRepeticoes = 0
    private var ultimoTempo = 0L
    private var fase = 0

    private var toneGenerator: ToneGenerator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcha_execucao)

        paciente =
            intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario

        supportActionBar?.title = "Acompanhar Teste"

        sessionManager = SessionManager(this)

        inicializarUI()
        configurarSensores()
        configurarBotoes()

        txtNomePaciente.text =
            "Paciente: ${paciente?.name ?: "Paciente não identificado"}"

        toneGenerator =
            ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        atualizarUI(EstadoTeste.PRONTO)
    }

    private fun inicializarUI() {
        txtNomePaciente = findViewById(R.id.text_nome_paciente)
        textTimer = findViewById(R.id.text_timer_contador)
        lblStatus = findViewById(R.id.lbl_status_teste)

        layoutBotaoPlay = findViewById(R.id.layout_botao_play)
        layoutBotoesRodando = findViewById(R.id.layout_botoes_rodando)
        layoutBotoesConcluido = findViewById(R.id.layout_botoes_concluido)

        btnPlay = findViewById(R.id.btn_play)
        btnRestartRodando = findViewById(R.id.btn_restart_rodando)
        btnStop = findViewById(R.id.btn_stop)
        btnRestartConcluido = findViewById(R.id.btn_restart_concluido)
        btnDiscard = findViewById(R.id.btn_discard)
        btnSave = findViewById(R.id.btn_save)
    }

    private fun configurarSensores() {
        sensorManager =
            getSystemService(SENSOR_SERVICE) as SensorManager

        acelerometro =
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        giroscopio =
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    private fun configurarBotoes() {

        btnPlay.setOnClickListener {
            atualizarUI(EstadoTeste.PREPARANDO)
            iniciarTimerPreparacao()
        }

        btnStop.setOnClickListener {
            pararColeta()
            atualizarUI(EstadoTeste.CONCLUIDO)
        }

        btnRestartRodando.setOnClickListener {
            pararColeta()
            atualizarUI(EstadoTeste.PRONTO)
        }

        btnRestartConcluido.setOnClickListener {
            atualizarUI(EstadoTeste.PRONTO)
        }

        btnDiscard.setOnClickListener {
            Toast.makeText(
                this,
                "Teste descartado.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

        btnSave.setOnClickListener {

            salvarJSON()

            Toast.makeText(
                this,
                "Teste salvo com sucesso.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    private fun iniciarTimerPreparacao() {
        timerPreparacao =
            object : CountDownTimer(3000, 1000) {

                override fun onTick(ms: Long) {
                    textTimer.text =
                        ((ms / 1000) + 1).toString()

                    lblStatus.text = "Preparar"
                }

                override fun onFinish() {
                    iniciarColeta()
                    iniciarTimer()
                    atualizarUI(EstadoTeste.RODANDO)
                }
            }

        timerPreparacao?.start()
    }

    private fun iniciarTimer() {
        timer =
            object : CountDownTimer(
                tempoTotalEmMillis,
                1000
            ) {

                override fun onTick(ms: Long) {

                    val min =
                        (ms / 1000) / 60

                    val sec =
                        (ms / 1000) % 60

                    textTimer.text =
                        String.format("%d:%02d", min, sec)

                    lblStatus.text =
                        "Tempo Restante"
                }

                override fun onFinish() {
                    pararColeta()
                    atualizarUI(EstadoTeste.CONCLUIDO)
                }
            }

        timer?.start()
    }

    private fun iniciarColeta() {
        tempoInicioTeste =
            System.currentTimeMillis()
    }

    private fun pararColeta() {
        timer?.cancel()
        sensorManager.unregisterListener(this)
    }

    private fun atualizarUI(
        estado: EstadoTeste
    ) {

        layoutBotaoPlay.visibility = View.GONE
        layoutBotoesRodando.visibility = View.GONE
        layoutBotoesConcluido.visibility = View.GONE

        when (estado) {

            EstadoTeste.PRONTO -> {
                textTimer.text = "2:00"
                lblStatus.text = "Tempo Restante"
                layoutBotaoPlay.visibility = View.VISIBLE
            }

            EstadoTeste.PREPARANDO -> {
                layoutBotoesRodando.visibility = View.VISIBLE
            }

            EstadoTeste.RODANDO -> {
                layoutBotoesRodando.visibility = View.VISIBLE
            }

            EstadoTeste.CONCLUIDO -> {
                lblStatus.text = "Teste interrompido"
                layoutBotoesConcluido.visibility = View.VISIBLE
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {}
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun salvarJSON() {}
}