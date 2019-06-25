package com.dnhsolution.restokabmalang.home

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.ProdukSerializable
import com.dnhsolution.restokabmalang.Url
import com.dnhsolution.restokabmalang.keranjang.KeranjangActivity
import kotlinx.android.synthetic.main.home_fragment.*
import com.dnhsolution.restokabmalang.R
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class HomeFragment:Fragment() {

    private val favoritedBookNamesKey = "favoritedBookNamesKey"
    var produkSerializable: ProdukSerializable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        val produkAdapter = ProdukAdapter(context, produks)
        gvMainActivity.adapter = produkAdapter

        gvMainActivity.setOnItemClickListener { parent, _, position, id ->
            val produk = produks[position]
            produk.toggleFavorite()
            produkAdapter.notifyDataSetChanged()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val favoritedProdukNames = ArrayList<Int>()
        for (Produk in produks) {
            if (Produk.isFavorite) {
                favoritedProdukNames.add(Produk.idItem)
            }
        }

        outState.putIntegerArrayList(favoritedBookNamesKey, favoritedProdukNames)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater?.inflate(R.menu.menu_lanjut, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_menu_lanjut -> {

                produkSerializable = ProdukSerializable()
                val arrayProdukSerialization = ArrayList<ProdukSerializable>()
                for (value in produks) {
                    if (value.isFavorite) {
                        arrayProdukSerialization.add(
                            ProdukSerializable(
                                value.idItem, value.name, value.price
                                , value.imageResource, value.imageUrl,value.price.toInt()
                            )
                        )
//                        produkSerializable?.idItem = value.idItem
//                        produkSerializable?.name = value.name
//                        produkSerializable?.price = value.price
//                        produkSerializable?.imgResource = value.imageResource
//                        produkSerializable?.imgUrl = value.imageUrl
                    } else {
                        println("kosong")
                    }
                }

                if (arrayProdukSerialization.size > 0) {

                    val intent = Intent(context, KeranjangActivity::class.java)
                    val args = Bundle()
                    args.putSerializable("ARRAYLIST", arrayProdukSerialization)
                    intent.putExtra("BUNDLE", args)
                    startActivity(intent)
                }
                true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        val favoritedBookNames = savedInstanceState?.getIntegerArrayList(favoritedBookNamesKey)

        // warning: typically you should avoid n^2 loops like this, use a Map instead.
        // I'm keeping this because it is more straightforward
        if (favoritedBookNames != null) {
            for (bookName in favoritedBookNames) {
                for (Produk in produks) {
                    if (Produk.idItem == bookName) {
                        Produk.isFavorite = true
                        break
                    }
                }
            }
        }
    }

    fun getdata() {

        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Loading...")
        progressDialog.show()
        val queue = Volley.newRequestQueue(context)
        val url = Url.serverPos + "Auth"
        //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val jsonArray = jsonObject.getJSONArray("result")
                    val json = jsonArray.getJSONObject(0)
                    val pesan = json.getString("pesan")
                    if (pesan.equals("0", ignoreCase = true)) {
                        Toast.makeText(
                            context,
                            "Gagal Login. Username atau Password salah !",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (pesan.equals("1", ignoreCase = true)) {

                    } else {
                        Toast.makeText(context, "Jaringan masih sibuk !", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                //Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss()
            }, Response.ErrorListener { error ->
                progressDialog.dismiss()
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
//                val sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
//                var editor = sharedPreference.edit()
//                editor.putString("username","Anupam")
//                editor.putLong("l",100L)
//                editor.commit()
                params["username"] = ""

                return params
            }
        }
        stringRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {

            }
        }

        queue.add(stringRequest)

    }

    private val produks = arrayOf(
//        ProdukElement(
//            1, "Judul", "10", R.drawable.abc,
//            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
//        ), ProdukElement(
//            2, "Judul", "20", R.drawable.areyoumymother,
//            "http://www.raywenderlich.com/wp-content/uploads/2016/03/areyoumymother.jpg"
//        ), ProdukElement(
//            3, "Judul", "30", R.drawable.whereisbabysbellybutton,
//            "http://www.raywenderlich.com/wp-content/uploads/2016/03/whereisbabysbellybutton.jpg"
//        ), ProdukElement(
//            4, "Judul", "40", R.drawable.onthenightyouwereborn,
//            "http://www.raywenderlich.com/wp-content/uploads/2016/03/onthenightyouwereborn.jpg"
//        ), ProdukElement(
//            5, "Judul", "50", R.drawable.handhandfingersthumb,
//            "http://www.raywenderlich.com/wp-content/uploads/2016/03/handhandfingersthumb.jpg"
//        ), ProdukElement(
//            6, "Judul", "60", R.drawable.theveryhungrycaterpillar,
//            "http://www.raywenderlich.com/wp-content/uploads/2016/03/theveryhungrycaterpillar.jpg"
//        ), ProdukElement(
//            7, "Judul", "70", R.drawable.thegoingtobedbook,
//            "http://www.raywenderlich.com/wp-content/uploads/2016/03/thegoingtobedbook.jpg"
//        ), ProdukElement(
//            8, "Judul", "80", R.drawable.ohbabygobaby,
//            "http://www.raywenderlich.com/wp-content/uploads/2016/03/ohbabygobaby.jpg"
//        ), ProdukElement(
//            9, "Judul", "90", R.drawable.thetoothbook,
//            "http://www.raywenderlich.com/wp-content/uploads/2016/03/thetoothbook.jpg"
//        ), ProdukElement(
//            10, "Judul", "100", R.drawable.onefish,
//            "http://www.raywenderlich.com/wp-content/uploads/2016/03/onefish.jpg"
//        )
        ProdukElement(
            1, "Judul", "10", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
        ,"Diskripsi"), ProdukElement(
            2, "Judul", "20", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
            ,"Diskripsi"), ProdukElement(
            3, "Judul", "30", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
            ,"Diskripsi"), ProdukElement(
            4, "Judul", "40", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
            ,"Diskripsi"), ProdukElement(
            5, "Judul", "50", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
            ,"Diskripsi"), ProdukElement(
            6, "Judul", "60", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
            ,"Diskripsi"), ProdukElement(
            7, "Judul", "70", R.drawable.img_food_example,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/thegoingtobedbook.jpg"
            ,"Diskripsi"), ProdukElement(
            8, "Judul", "80", R.drawable.img_food_example,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/ohbabygobaby.jpg"
            ,"Diskripsi"), ProdukElement(
            9, "Judul", "90", R.drawable.img_food_example,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/thetoothbook.jpg"
            ,"Diskripsi"), ProdukElement(
            10, "Judul", "100", R.drawable.img_food_example,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/onefish.jpg"
            ,"Diskripsi")
    )
}