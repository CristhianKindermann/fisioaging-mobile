package com.example.fisioaging

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class SincroniaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SincroniaAdapter
    private var listaTestesSalvos = mutableListOf<TesteSalvo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sincronia)
        supportActionBar?.title = "Sincronizar Testes"

        recyclerView = findViewById(R.id.recycler_view_testes)
        adapter = SincroniaAdapter(listaTestesSalvos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val btnSincronizar: Button = findViewById(R.id.btn_sincronizar)
        val btnApagar: Button = findViewById(R.id.btn_apagar)

        btnApagar.setOnClickListener {
            apagarArquivosSelecionados()
        }

        btnSincronizar.setOnClickListener {
            sincronizarArquivosSelecionados()
        }
    }

    override fun onResume() {
        super.onResume()
        carregarTestesSalvos()
    }

    private fun carregarTestesSalvos() {
        listaTestesSalvos.clear()

        val diretorioArquivos = filesDir

        val arquivos = diretorioArquivos.listFiles { _, nome ->
            nome.startsWith("teste_") && nome.endsWith(".csv")
        }

        arquivos?.forEach {
            listaTestesSalvos.add(TesteSalvo(it))
        }

        listaTestesSalvos.sortByDescending { it.arquivo.lastModified() }

        adapter.notifyDataSetChanged()
    }

    private fun apagarArquivosSelecionados() {
        val selecionados = adapter.getItensSelecionados()
        if (selecionados.isEmpty()) {
            Toast.makeText(this, "Nenhum teste selecionado", Toast.LENGTH_SHORT).show()
            return
        }

        var contagemApagados = 0
        selecionados.forEach { testeSalvo ->
            if (testeSalvo.arquivo.delete()) {
                contagemApagados++
            }
        }

        Toast.makeText(this, "$contagemApagados testes apagados", Toast.LENGTH_SHORT).show()

        carregarTestesSalvos()
    }

    private fun sincronizarArquivosSelecionados() {
        val selecionados = adapter.getItensSelecionados()
        if (selecionados.isEmpty()) {
            Toast.makeText(this, "Nenhum teste selecionado para sincronizar", Toast.LENGTH_SHORT).show()
            return
        }

        var contagemSincronizados = 0
        selecionados.forEach { testeSalvo ->
            if (testeSalvo.arquivo.delete()) {
                contagemSincronizados++
            }
        }

        Toast.makeText(this, "$contagemSincronizados testes sincronizados (e apagados)", Toast.LENGTH_SHORT).show()

        carregarTestesSalvos()
    }
}