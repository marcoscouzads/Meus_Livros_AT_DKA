package br.com.marcoscsouza.meuslivrosat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import br.com.marcoscsouza.meuslivrosat.services.Endpoint
import br.com.marcoscsouza.meuslivrosat.services.NetworkUtils
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_api_list.*
import retrofit2.Call
import retrofit2.Response

class ApiListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_list)

        getCurrencies()

        btConvert.setOnClickListener { convertMoney() }

    }
    fun convertMoney(){
        val retrofitClient = NetworkUtils.getRetrofitInstance("https://cdn.jsdelivr.net/")
        val endpoint = retrofitClient.create(Endpoint::class.java)

        endpoint.getCurrencyRate(spFrom.selectedItem.toString(), spTo.selectedItem.toString()).enqueue(object :
            retrofit2.Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                var data = response.body()?.entrySet()?.find { it.key == spTo.selectedItem.toString() }
                val rate : Double = data?.value.toString().toDouble()
                val conversion = etValueFrom.text.toString().toDouble() * rate

                tvResult.setText(conversion.toString())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                println("Não foi")
            }

        })
    }

    fun getCurrencies(){
        val retrofitClient = NetworkUtils.getRetrofitInstance("https://cdn.jsdelivr.net/")
        val endpoint = retrofitClient.create(Endpoint::class.java)

        endpoint.getCurrencies().enqueue(object : retrofit2.Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                var data = mutableListOf<String>()

                response.body()?.keySet()?.iterator()?.forEach {
                    data.add(it)
                }

                val posBRL = data.indexOf("brl")
                val posUSD = data.indexOf("usd")

                val adapter = ArrayAdapter(baseContext, android.R.layout.simple_spinner_dropdown_item, data)
                spFrom.adapter = adapter
                spTo.adapter = adapter

                spFrom.setSelection(posUSD)
                spTo.setSelection(posBRL)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                println("Não foi")
            }

        })
    }


}