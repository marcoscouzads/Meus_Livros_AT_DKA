package br.com.marcoscsouza.meuslivrosat.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.marcoscsouza.meuslivrosat.R
import br.com.marcoscsouza.meuslivrosat.databinding.ActivityCadastroProdutoBinding
import br.com.marcoscsouza.meuslivrosat.db.Produto
import br.com.marcoscsouza.meuslivrosat.ui.user.LogarUsuarioActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class CadastroProdutoActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityCadastroProdutoBinding.inflate(layoutInflater)
    }
    private val firebaseAuth = Firebase.auth
    private val firestore = Firebase.firestore
    private var produtoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        cadastrarProdutoFirestore()
        produtoId = intent.getStringExtra("PRODUTO_ID")
    }

    private fun cadastrarProdutoFirestore() {
        val btSalvar = binding.btSalvar

        btSalvar.setOnClickListener {
            val campoNome = binding.cadastroProdutoNome
            val nome = campoNome.text.toString()
            val campoDescricao = binding.cadastroProdutoDescricao
            val descricao = campoDescricao.text.toString()

            val produtoDocumento = ProdutoDocumento(nome = nome, descricao = descricao)
            val colecao = firestore.collection("produtos")
            val documento = produtoId?.let {
                colecao.document(it)
            } ?: colecao.document()
            documento.set(produtoDocumento)

            Log.i("salvar", "produto salvo ${documento.id}")
            val i = Intent(this, ListaProdutoActivity::class.java)
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!estaLogado()) {
            val i = Intent(this, LogarUsuarioActivity::class.java)
            startActivity(i)
        }
        buscarProdutoPorIdFirestore()
    }

    private fun buscarProdutoPorIdFirestore() {
        produtoId = intent.getStringExtra("PRODUTO_ID")

        firestore.collection("produtos")
            .document(produtoId.toString())
            .addSnapshotListener { s, _ ->
                s?.let { document ->
                    document.toObject<ProdutoDocumento>()
                        ?.paraProduto(document.id)
                        ?.let { produto ->
                            with(binding) {
                                title = "editar activity"
                                cadastroProdutoNome.setText(produto.nome)
                                cadastroProdutoDescricao.setText(produto.descricao)
                            }
                        }

                }
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

    fun estaLogado(): Boolean {
        val userFire: FirebaseUser? = firebaseAuth.currentUser
        return if (userFire != null) {
            true
        } else {
            Toast.makeText(this, "Usuário não está logado!", Toast.LENGTH_SHORT).show()
            false
        }
    }

    class ProdutoDocumento(

        val nome: String = "",
        val descricao: String = ""
    ) {
        fun paraProduto(id: String): Produto {
            return Produto(id = id, nome = nome, descricao = descricao)
        }
    }
}