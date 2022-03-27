package br.com.marcoscsouza.meuslivrosat.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.marcoscsouza.meuslivrosat.R
import br.com.marcoscsouza.meuslivrosat.databinding.ActivityDetalheProdutoBinding
import br.com.marcoscsouza.meuslivrosat.db.Produto
import br.com.marcoscsouza.meuslivrosat.ui.user.LogarUsuarioActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class DetalheProdutoActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityDetalheProdutoBinding.inflate(layoutInflater)
    }
    private val firebaseAuth = Firebase.auth
    private var produto: Produto? = null
    private val firestore = Firebase.firestore
    private var produtoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "Detalhes do Produto"

        produtoId = intent.getStringExtra("PRODUTO_ID")

        Thread(Runnable {
            configurarBtnEditarProduto()
            configurarBtnDeletarProduto()
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
        Thread(Runnable {
            buscarProdutoNoFirestore()
        }).start()
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

    private fun buscarProdutoNoFirestore() {
        firestore.collection("produtos")
            .document(produtoId.toString())
            .addSnapshotListener { s, _ ->
                s?.let { document ->
                    document.toObject<ProdutoDocumento>()?.paraProduto(document.id)
                        ?.let { produto ->
                            with(binding) {
                                activityDetalhesProdutoNome.text = produto.nome
                                activityDetalhesProdutoDescricao.text = produto.descricao
                            }
                        }
                }
            }
    }


    private fun configurarBtnDeletarProduto() {
        val btDeletar = binding.btDeletar
        btDeletar.setOnClickListener {

            try {
                Toast.makeText(this, "Excluindo produto...", Toast.LENGTH_SHORT).show()
                firestore.collection("produtos")
                    .document(produtoId.toString())
                    .delete()
                finish()
                Thread.sleep(2000)
                Toast.makeText(this, "Produto excluido com sucesso!", Toast.LENGTH_SHORT).show()

            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

    private fun configurarBtnEditarProduto() {
        val btEditar = binding.btEditar
        btEditar.setOnClickListener {
            val i = Intent(
                this,
                CadastroProdutoActivity::class.java
            ).apply {
                putExtra("PRODUTO_ID", produtoId)

            }
            startActivity(i)
        }
    }

    fun estaLogado(): Boolean {
        val userFire: FirebaseUser? = firebaseAuth.currentUser
        return if (userFire != null) {
            true
        } else {
            Toast.makeText(this, "Usuário não está logado.", Toast.LENGTH_SHORT).show()
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