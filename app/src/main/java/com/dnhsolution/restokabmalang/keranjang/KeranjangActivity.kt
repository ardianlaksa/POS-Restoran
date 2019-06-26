package com.dnhsolution.restokabmalang.keranjang

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dnhsolution.restokabmalang.KeranjangTransaksiOnTask
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.Url
import kotlinx.android.synthetic.main.activity_keranjang.*
import org.json.JSONArray
import org.json.JSONObject

class KeranjangActivity:AppCompatActivity(),KeranjangProdukItemOnTask
    ,View.OnClickListener, KeranjangTransaksiOnTask {

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bProses -> {
//                val valueDiskon = valueDiskon
//                if ( valueDiskon == "") return
//                else showDialog("Konfirmasi","Apakan anda ingin memproses?")
                showDialog("Konfirmasi","Apakan anda ingin memproses?")
            }
        }
    }

    private var valueDiskon: Int = 0
    private var valueTotalPrice: Int = 0
    private lateinit var obyek:ArrayList<ProdukSerializable>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keranjang)
        setSupportActionBar(toolbar)


        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        val status = sharedPreferences.getString(Url.SESSION_USERNAME, "0")

        bProses.setOnClickListener(this)

        val intent = intent
        val args = intent.getBundleExtra("BUNDLE")
        obyek = args.getSerializable("ARRAYLIST") as ArrayList<ProdukSerializable>
//        val name = obyek.get(1).name

        val produkAdapter = KeranjangProdukListAdapter(obyek, this,this)
        recyclerView.adapter = produkAdapter
        recyclerView?.layoutManager = (LinearLayoutManager(this))

        setTotal()

        etDiskon.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            private var current = ""

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == "") return
                if (s.toString() != current)
                    etDiskon.removeTextChangedListener(this)

                valueDiskon = s.toString().toInt()
                setTotal()

                etDiskon.addTextChangedListener(this)
            }
        })
    }

    override fun keranjangProdukItemOnTask(position:Int, totalPrice: Int, qty: Int) {
        obyek[position].totalPrice = totalPrice
        obyek[position].qty = qty
        setTotal()
    }

    private fun setTotal () {
        var totalPrice = 0
        for (valueTotal in obyek) {
            totalPrice += valueTotal.totalPrice
        }

        var diskon = 0
//        if (!etDiskon.text.toString().isEmpty()) diskon = etDiskon.text.toString().toInt()
        if (!etDiskon.text.toString().isEmpty()) diskon = valueDiskon
        this.valueTotalPrice = totalPrice-(totalPrice*diskon/100)
        val rupiahValue = "Rp ${valueTotalPrice}"
        tvTotal.text = rupiahValue
    }

    fun showDialog(title:String,message:String){
        val builder = AlertDialog.Builder(this)

        // Set the alert dialog title
        builder.setTitle(title)

        // Display a message on alert dialog
        builder.setMessage(message)
            builder.setPositiveButton("Lanjut"){dialog, which ->
//                println(createJson())
                if(CheckNetwork().checkingNetwork(this)) {
                    val params = HashMap<String, String>()
                    params.put("paramsArray",createJson())
                    KeranjangTransaksiJsonTask(this, params).execute(Url.setKeranjangTransaksi)
                } else {
                    Toast.makeText(this, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
                }
            }

        builder.setNegativeButton("Batal"){dialog, which ->
            // Do something when user press the positive button
            dialog.dismiss()
        }


        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
    }

    fun createJson() : String{
        val rootObject= JSONObject()
        rootObject.put("idTmptUsaha","1")
        rootObject.put("user","1")
        rootObject.put("disc",valueDiskon)
        rootObject.put("omzet",valueTotalPrice)

        val jsonArr = JSONArray()

        for (pn in obyek) {
            val pnObj = JSONObject()
            pnObj.put("idProduk", pn.idItem)
            pnObj.put("nmProduk", pn.name)
            pnObj.put("nmProduk", pn.name)
            pnObj.put("qty", pn.qty)
            pnObj.put("hrgProduk", pn.price)
            jsonArr.put(pnObj)
        }
        rootObject.put("produk",jsonArr)

        return rootObject.toString()
    }

    override fun keranjangTransaksiOnTask(result: String?) {
        if (result == null) {
            Toast.makeText(this,R.string.error_get_data,Toast.LENGTH_SHORT).show()
            return
        } else if (result == "") {
            Toast.makeText(this,R.string.empty_data,Toast.LENGTH_SHORT).show()
            return
        }

        Log.e("Debug", "Response from url:$result")
    }
}