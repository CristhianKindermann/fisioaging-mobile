package com.example.fisioaging

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TesteDetalhesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teste_detalhes)


        supportActionBar?.title = "2 Minutos Marcha estacionária"


        val textViewTitulo: TextView = findViewById(R.id.text_titulo_teste)
        val textViewDescricao: TextView = findViewById(R.id.text_descricao_teste)
        val buttonIniciarTeste: Button = findViewById(R.id.button_iniciar_teste)

        buttonIniciarTeste.setOnClickListener {
            val intent = Intent(this, TesteEmAndamentoActivity::class.java)
            startActivity(intent)
        }
    }
}