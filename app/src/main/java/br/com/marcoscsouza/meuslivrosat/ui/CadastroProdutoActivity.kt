package br.com.marcoscsouza.meuslivrosat.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.com.marcoscsouza.meuslivrosat.R
import br.com.marcoscsouza.meuslivrosat.databinding.ActivityCadastroProdutoBinding
import br.com.marcoscsouza.meuslivrosat.db.Produto
import br.com.marcoscsouza.meuslivrosat.ui.user.LogarUsuarioActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

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



        Thread(Runnable {
            cadastrarProdutoFirestore()
        }).start()

//        Toast.makeText(this, "rodar: ${doInBackground()}", Toast.LENGTH_SHORT).show()


        produtoId = intent.getStringExtra("PRODUTO_ID")
    }

//    protected fun doInBackground(vararg voids: Void?): String? {
//        val r = Random()
//        val n: Int = r.nextInt(11)
//
//        val s = n * 200
//        try {
//            Thread.sleep(s.toLong())
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
//
//        return "Awake at last after sleeping for $s milliseconds!"
//    }

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

            Log.i("salvar", "produto salvo ${documento.id}")

            try {
                Toast.makeText(this, "Cadastrando produto...", Toast.LENGTH_SHORT).show()

                Thread.sleep(1000)
                lifecycleScope.launch {
                    delay(2500L)
                    documento.set(produtoDocumento)
                    finish()
                }
                Toast.makeText(this, "Produto cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                Intent(this, ListaProdutoActivity::class.java).also {
                    startActivity(it)
                }

            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
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
            buscarProdutoPorIdFirestore()
        }).start()
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