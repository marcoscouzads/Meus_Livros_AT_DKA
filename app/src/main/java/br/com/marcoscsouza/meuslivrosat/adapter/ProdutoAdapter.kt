package br.com.marcoscsouza.meuslivrosat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.marcoscsouza.meuslivrosat.databinding.ProdutoItemBinding
import br.com.marcoscsouza.meuslivrosat.db.Produto

class ProdutoAdapter(
    private val context: Context,
    produtos: List<Produto> = emptyList(),
    var ClicaNoItem: (produto: Produto) -> Unit = {}
) : RecyclerView.Adapter<ProdutoAdapter.ViewHolder>() {
    private val produtos = produtos.toMutableList()

    inner class ViewHolder(private val binding: ProdutoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var produto: Produto

        init {
            itemView.setOnClickListener {
                if (::produto.isInitialized) {
                    ClicaNoItem(produto)
                }
            }
        }

        fun vincula(produto: Produto) {
            this.produto = produto
            val nome = binding.produtoNome
            nome.text = produto.nome
            val descricao = binding.produtoDescricao
            descricao.text = produto.descricao

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ProdutoItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produto = produtos[position]
        holder.vincula(produto)
    }

    override fun getItemCount(): Int = produtos.size

    fun atualiza(produtos: List<Produto>) {
        this.produtos.clear()
        this.produtos.addAll(produtos)
        notifyDataSetChanged()
    }
}