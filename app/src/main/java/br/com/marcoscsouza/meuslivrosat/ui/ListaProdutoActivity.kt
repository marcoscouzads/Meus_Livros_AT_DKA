package br.com.marcoscsouza.meuslivrosat.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.marcoscsouza.meuslivrosat.R
import br.com.marcoscsouza.meuslivrosat.adapter.ProdutoAdapter
import br.com.marcoscsouza.meuslivrosat.databinding.ActivityListaProdutoBinding
import br.com.marcoscsouza.meuslivrosat.db.Produto
import br.com.marcoscsouza.meuslivrosat.ui.user.LogarUsuarioActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ListaProdutoActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityListaProdutoBinding.inflate(layoutInflater)
    }
    private val firebaseAuth = Firebase.auth
    private val firestore = Firebase.firestore
    private val adapter = ProdutoAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "Lista de Produtos"

        rvProdutos()
        botaoFab()
        Thread(Runnable {
            buscarTodosProdutosNoFirestoreTempoReal()
        }).start()
    }

    override fun onResume() {
        super.onResume()
        runOnUiThread {
            if (!estaLogado()) {
                val i = Intent(this, LogarUsuarioActivity::class.java)
                startActivity(i)
            }
        }
    }

    private fun buscarTodosProdutosNoFirestoreTempoReal() {
        firestore.collection("produtos")
            .addSnapshotListener { s, _ ->
                s?.let { snapshot ->
                    val produtos = mutableListOf<Produto>()

                    for (documento in snapshot.documents) {
                        Log.i("listagem", "Doc find tempo real ${documento.data}")
                        val produtoDocumento = documento.toObject<ProdutoDocumento>()
                        produtoDocumento?.let { produtoDocumentoNaoNulo ->
                            produtos.add(produtoDocumentoNaoNulo.paraProduto(documento.id))
                        }
                    }
                    adapter.atualiza(produtos)
                }
            }
    }

    private fun rvProdutos() {
        val rv = binding.rvLista
        rv.adapter = adapter
        adapter.ClicaNoItem = {
            val i = Intent(
                this,
                DetalheProdutoActivity::class.java
            ).apply {
                putExtra("PRODUTO_ID", it.id)
            }
            startActivity(i)
        }
    }

    private fun botaoFab() {
        val fab = binding.fabProduto
        fab.setOnClickListener {
            val intent = Intent(this, CadastroProdutoActivity::class.java)
            startActivity(intent)
        }
    }

    fun estaLogado(): Boolean {
        val userFire: FirebaseUser? = firebaseAuth.currentUser
        return if (userFire != null) {
            true
        } else {
            Toast.makeText(this, "Usuário não está logado!", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private class ProdutoDocumento(
        val nome: String = "",
        val descricao: String = ""
    ) {
        fun paraProduto(id: String): Produto {
            return Produto(id = id, nome = nome, descricao = descricao)
        }
    }

    //  Configurar menu de usuario
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menuuser, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.exitMenu -> {
                Toast.makeText(this, "Usuário deslogado.", Toast.LENGTH_SHORT).show()
                firebaseAuth.signOut()
                val i = Intent(this, LogarUsuarioActivity::class.java)
                startActivity(i)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}