package com.example.fisioaging.ui.selecao

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.model.Usuario
import com.example.fisioaging.ui.marcha.MarchaDetalhesActivity
import com.example.fisioaging.ui.utt.UttDetalhesActivity

class SelecaoTesteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selecao_teste)

        // Recupera os dados do paciente selecionado na listagem anterior
        val paciente = intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario

        supportActionBar?.title = "Selecionar Avaliação"

        // Exibe o nome do paciente no subtítulo da Action Bar para manter o contexto visual
        if (paciente != null) {
            supportActionBar?.subtitle = "Paciente: ${paciente.name}"
        }

        val btnMarcha = findViewById<Button>(R.id.btn_marcha)
        val btnUtt = findViewById<Button>(R.id.btn_utt)

        // Direciona para o fluxo do teste de Marcha Estacionária
        btnMarcha.setOnClickListener {
            val intent = Intent(this, MarchaDetalhesActivity::class.java)
            intent.putExtra("PACIENTE_SELECIONADO", paciente) // Propaga o objeto para a próxima tela
            startActivity(intent)
        }

        // Direciona para o fluxo do teste UTT (Ponta dos Pés)
        btnUtt.setOnClickListener {
            val intent = Intent(this, UttDetalhesActivity::class.java)
            intent.putExtra("PACIENTE_SELECIONADO", paciente) // Propaga o objeto para a próxima tela
            startActivity(intent)
        }
    }
}