package com.example.fisioaging.ui.utt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.model.Usuario

class UttDetalhesActivity : AppCompatActivity() {

    private var paciente: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utt_detalhes)

        // Recupera o paciente selecionado para manter o contexto do teste
        paciente = intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario

        supportActionBar?.title = "Detalhes UTT"
        if (paciente != null) {
            supportActionBar?.subtitle = "Paciente: ${paciente?.name}"
        }

        // Configura o vídeo de instrução com repetição e intervalo de segurança
        val videoView = findViewById<VideoView>(R.id.videoView2)
        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.utt}")
        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { it.isLooping = false }
        videoView.setOnCompletionListener { videoView.postDelayed({ videoView.start() }, 1500) }
        videoView.start()

        val edtPeso = findViewById<EditText>(R.id.edt_peso_paciente)
        val btnIniciar = findViewById<Button>(R.id.btn_iniciar_utt)

        btnIniciar.isEnabled = false

        // Trava de segurança: só permite iniciar a coleta de dados se o examinador informar o peso
        edtPeso.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnIniciar.isEnabled = !s.isNullOrBlank()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnIniciar.setOnClickListener {
            val pesoDigitado = edtPeso.text.toString().toDoubleOrNull() ?: 0.0

            val intentExec = Intent(this, UttExecucaoActivity::class.java)

            // Repassa o paciente e a variável de peso (exclusiva do UTT) para a tela de sensores
            intentExec.putExtra("PACIENTE_SELECIONADO", paciente)
            intentExec.putExtra("PESO_PACIENTE", pesoDigitado)

            startActivity(intentExec)
            finish()
        }
    }
}