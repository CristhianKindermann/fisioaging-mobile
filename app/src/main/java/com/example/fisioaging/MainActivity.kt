package com.example.fisioaging

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Novo Teste"

        val cardMarcha: TextView = findViewById(R.id.card_marcha)
        val cardPontaPes: TextView = findViewById(R.id.card_ponta_pes)

        cardMarcha.setOnClickListener {
            val intent = Intent(this, TesteDetalhesActivity::class.java)
            startActivity(intent)
        }

        cardPontaPes.setOnClickListener {

            val intent = Intent(this, SincroniaActivity::class.java)
            startActivity(intent)
        }

    }
}