package com.example.fisioaging

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

private enum class EstadoTeste { PRONTO, RODANDO, CONCLUIDO }

class TesteEmAndamentoActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var textTimer: TextView

    // Layouts dos botões
    private lateinit var layoutBotaoPlay: LinearLayout
    private lateinit var layoutBotoesRodando: LinearLayout
    private lateinit var layoutBotoesConcluido: LinearLayout

    // Botões
    private lateinit var btnPlay: ImageButton
    private lateinit var btnRestartRodando: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var btnRestartConcluido: ImageButton
    private lateinit var btnDiscard: ImageButton
    private lateinit var btnSave: ImageButton

    private var timer: CountDownTimer? = null
    private val tempoTotalEmMillis: Long = 2 * 60 * 1000 // 2 minutos

    // --- Variáveis do Sensor ---
    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null
    private val dadosColetados = mutableListOf<String>()
    private var tempoInicioTeste: Long = 0

    // ----------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teste_em_andamento)

        supportActionBar?.title = "2 Minutos Marcha estacionária"
        textTimer = findViewById(R.id.text_timer_contador)

        layoutBotaoPlay = findViewById(R.id.layout_botao_play)
        layoutBotoesRodando = findViewById(R.id.layout_botoes_rodando)
        layoutBotoesConcluido = findViewById(R.id.layout_botoes_concluido)

        btnPlay = findViewById(R.id.btn_play)
        btnRestartRodando = findViewById(R.id.btn_restart_rodando)
        btnStop = findViewById(R.id.btn_stop)
        btnRestartConcluido = findViewById(R.id.btn_restart_concluido)
        btnDiscard = findViewById(R.id.btn_discard)
        btnSave = findViewById(R.id.btn_save)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        if (acelerometro == null) {
            acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }

        configurarListenersBotoes()

        atualizarVisibilidade(EstadoTeste.PRONTO)
    }

    private fun configurarListenersBotoes() {
        // Pronto
        btnPlay.setOnClickListener {
            iniciarColetaSensor()
            iniciarTimer(tempoTotalEmMillis)
            atualizarVisibilidade(EstadoTeste.RODANDO)
        }

        // Rodando
        btnRestartRodando.setOnClickListener {
            timer?.cancel()
            pararColetaSensor()
            atualizarVisibilidade(EstadoTeste.PRONTO)
            Toast.makeText(this, "Timer Reiniciado", Toast.LENGTH_SHORT).show()
        }
        btnStop.setOnClickListener {
            timer?.cancel()
            pararColetaSensor()
            atualizarVisibilidade(EstadoTeste.CONCLUIDO)
            Toast.makeText(this, "Teste Parado", Toast.LENGTH_SHORT).show()
        }

        // Concluido
        btnRestartConcluido.setOnClickListener {
            atualizarVisibilidade(EstadoTeste.PRONTO)
        }
        btnDiscard.setOnClickListener {
            Toast.makeText(this, "Teste descartado", Toast.LENGTH_SHORT).show()
            finish()
        }
        btnSave.setOnClickListener {
            salvarDadosCSV()
            finish()
        }
    }

    private fun atualizarVisibilidade(novoEstado: EstadoTeste) {
        layoutBotaoPlay.visibility = View.GONE
        layoutBotoesRodando.visibility = View.GONE
        layoutBotoesConcluido.visibility = View.GONE

        when (novoEstado) {
            EstadoTeste.PRONTO -> {
                textTimer.text = "2:00"
                dadosColetados.clear()
                layoutBotaoPlay.visibility = View.VISIBLE
            }
            EstadoTeste.RODANDO -> {
                layoutBotoesRodando.visibility = View.VISIBLE
            }
            EstadoTeste.CONCLUIDO -> {
                layoutBotoesConcluido.visibility = View.VISIBLE
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { eventoNaoNulo ->
            if (eventoNaoNulo.sensor.type == acelerometro?.type) {
                val tempoRelativo = System.currentTimeMillis() - tempoInicioTeste
                val x = eventoNaoNulo.values[0]
                val y = eventoNaoNulo.values[1]
                val z = eventoNaoNulo.values[2]
                val linhaCsv = "$tempoRelativo;${x.toString().replace('.', ',')};${y.toString().replace('.', ',')};${z.toString().replace('.', ',')}"
                dadosColetados.add(linhaCsv)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    // Func de controle

    private fun iniciarColetaSensor() {
        dadosColetados.clear()
        tempoInicioTeste = System.currentTimeMillis()
        acelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun pararColetaSensor() {
        sensorManager.unregisterListener(this)
    }

    // Func Salva dados no CSV
    private fun salvarDadosCSV() {
        if (dadosColetados.isEmpty()) {
            Toast.makeText(this, "Nenhum dado coletado.", Toast.LENGTH_SHORT).show()
            return
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val nomeArquivo = "teste_$timestamp.csv"

        try {
            val fileOutputStream: FileOutputStream = openFileOutput(nomeArquivo, Context.MODE_PRIVATE)
            fileOutputStream.write("time;x;y;z\n".toByteArray())
            dadosColetados.forEach { linha ->
                fileOutputStream.write("$linha\n".toByteArray())
            }
            fileOutputStream.close()
            Toast.makeText(this, "Teste salvo como $nomeArquivo", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar o teste", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Func inicia Timer
    private fun iniciarTimer(duracao: Long) {
        timer = object : CountDownTimer(duracao, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutos = (millisUntilFinished / 1000) / 60
                val segundos = (millisUntilFinished / 1000) % 60
                textTimer.text = String.format("%d:%02d", minutos, segundos)
            }

            override fun onFinish() {
                pararColetaSensor()
                textTimer.text = "0:00"
                atualizarVisibilidade(EstadoTeste.CONCLUIDO)
                Toast.makeText(this@TesteEmAndamentoActivity, "Teste Concluído!", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        pararColetaSensor()
    }
}