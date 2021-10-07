package com.dnhsolution.restokabmalang.transaksi.produk_list

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.sistem.produk.ItemProduk
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable
import com.dnhsolution.restokabmalang.transaksi.selected_produk_list.SelectedProdukListActivity
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.ProdukOnTask
import com.dnhsolution.restokabmalang.utilities.Url
import com.dnhsolution.restokabmalang.utilities.dialog.AdapterWizard
import com.dnhsolution.restokabmalang.utilities.dialog.ItemView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_produk_list.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class ProdukListFragment:Fragment(), ProdukOnTask {

    private lateinit var searchView: SearchView
    private var valueArgsFromKeranjang: Int? = null
    private var produkAdapter: ProdukListAdapter? = null
    private var idTmpUsaha: String = "0"

    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    private val favoritedBookNamesKey = "favoritedBookNamesKey"
    var produkSerializable: ProdukSerializable? = null
    private var produks:ArrayList<ProdukListElement> = ArrayList()

    var databaseHandler: DatabaseHandler? = null

    var nama: String = ""
    var email: String = ""
    var telp: String = ""
    var alamat: String = ""
    var isSearch = false

    lateinit var alertDialog: AlertDialog.Builder
    lateinit var dialog: AlertDialog

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
//            val data: Intent? = result.data
            for(v in produks){
                if(v.isFavorite)
                    v.toggleFavorite()
            }
            produkAdapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_produk_list,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        Log.i(_tag,"onCreate")

        val sharedPreferences = context?.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences?.getString(Url.SESSION_ID_TEMPAT_USAHA, "-1").toString()

        databaseHandler = DatabaseHandler(requireContext())

        val alamat = sharedPreferences?.getString(Url.SESSION_ALAMAT, "0")
        val email = sharedPreferences?.getString(Url.SESSION_EMAIL, "0")
        val telp = sharedPreferences?.getString(Url.SESSION_TELP, "0")
        val namausaha = sharedPreferences?.getString(Url.SESSION_NAMA_TEMPAT_USAHA, "0")

//        if(alamat!!.equals("0", ignoreCase = true) || email!!.equals("0", ignoreCase = true) ||
//            telp!!.equals("0", ignoreCase = true) || namausaha!!.equals("0", ignoreCase = true)){
//            DialogKelengkapan()
//        }

        if(CheckNetwork().checkingNetwork(requireContext())) {
            val stringUrl = "${Url.getProduk}?idTmpUsaha=$idTmpUsaha"
            Log.i(_tag,stringUrl)
            jsonTask = ProdukListJsonTask(this).execute(stringUrl)
        } else {
            getDataLokal()
           //Toast.makeText(context, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
        }

//        getDataLokal()

        gvMainActivity.setOnItemClickListener { _, _, position, _ ->
            val produk = produks[position]
            produk.toggleFavorite()
            produkAdapter?.notifyDataSetChanged()
        }

        if(MainActivity.adTransaksi == 1) return

//        tampilAlertDialogTutorial()

        MainActivity.adTransaksi = 1
    }

    private fun tampilAlertDialogTutorial(){
//        val alertDialog = AlertDialog.Builder(requireContext()).create()
//        alertDialog.setMessage("" +
//                "1. Pilih produk yang akan digunakan\n" +
//                "    untuk transaksi, centang hijau\n" +
//                "    saat produk terpilih.\n" +
//                "2. Tombol icon (+) samping icon [?]\n" +
//                "    di kanan atas untuk mulai\n" +
//                "    transaksi dengan produk yang\n" +
//                "    dipilih.")
//        alertDialog.setButton(
//            AlertDialog.BUTTON_NEGATIVE, "OK"
//        ) { dialog, _ -> dialog.dismiss() }
//        alertDialog.show()
        openDialog()
    }

    fun openDialog() {
        alertDialog = AlertDialog.Builder(requireContext())
        val rowList: View = layoutInflater.inflate(R.layout.dialog_tutorial, null)
        var listView = rowList.findViewById<ListView>(R.id.listView)
        val arrayList: ArrayList<ItemView> = ArrayList<ItemView>()
        arrayList.add(ItemView("1", "Pilih produk yang akan digunakan untuk transaksi, centang hijau saat produk terpilih."))
        arrayList.add(ItemView("2", "Tombol icon (+) samping icon [?] di kanan atas untuk mulai transaksi dengan produk yang dipilih."))
        val tutorialArrayAdapter = AdapterWizard(requireContext(), arrayList)
        listView.setAdapter(tutorialArrayAdapter)
        alertDialog.setView(rowList)
        dialog = alertDialog.create()
        dialog.show()
    }

    override fun produkOnTask(result: String?) {
        if (result == null) {
            Toast.makeText(context,R.string.error_get_data,Toast.LENGTH_SHORT).show()
            return
        } else if (result == "") {
            Toast.makeText(context,R.string.empty_data,Toast.LENGTH_SHORT).show()
            return
        }

        Log.e("Debug", "Response from url:$result")
        try {
            val jsonObj = JSONObject(result)
            val success = jsonObj.getInt("success")
            val message = jsonObj.getString("message")
            if (success == 1) {

                val rArray = jsonObj.getJSONArray("result")
                for (i in 0 until rArray.length()) {

                    val idBarang = rArray.getJSONObject(i).getInt("ID_BARANG")
                    val harga = rArray.getJSONObject(i).getString("HARGA")
                    val foto = rArray.getJSONObject(i).getString("FOTO")
                    val nmBarang = rArray.getJSONObject(i).getString("NM_BARANG")
                    val keterangan = rArray.getJSONObject(i).getString("KETERANGAN")
                    val isPajak = rArray.getJSONObject(i).getInt("ISPAJAK")
                    val jnsProduk = rArray.getJSONObject(i).getInt("JENIS_PRODUK")

                    produks.add(
                        ProdukListElement(
                            idBarang,nmBarang, harga, foto, keterangan,"server",isPajak,jnsProduk)
                    )
                }

                if (produkAdapter != null) produkAdapter?.notifyDataSetChanged()
                else produkAdapter =
                    ProdukListAdapter(requireContext(), produks)

                if (gvMainActivity == null) return
                gvMainActivity.adapter = produkAdapter

            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDataLokal() {
        produks.clear()

        val jmlData = databaseHandler!!.CountDataProduk()
        if (jmlData == 0) {
            Toast.makeText(context,R.string.empty_data,Toast.LENGTH_SHORT).show()
        }

        val listProduk: List<ItemProduk> = databaseHandler!!.dataProduk2

        for(e in listProduk){
            val idBarang = e.id_barang.toInt()
            val harga = e.harga
            val foto = e.url_image
            val nmBarang = e.nama_barang
            val keterangan = e.keterangan

            produks.add(
                ProdukListElement(
                    idBarang,nmBarang, harga, foto, keterangan, "lokal",-1,-1)
            )
        }

        if (produkAdapter != null) produkAdapter?.notifyDataSetChanged()
        else produkAdapter =
            ProdukListAdapter(requireContext(), produks)

        if (gvMainActivity == null) return
        gvMainActivity.adapter = produkAdapter
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_transaksi, menu)
        val searchItem = menu.findItem(R.id.action_menu_cari)
        searchView = searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.onActionViewExpanded()
        val stringTextSearch:CharSequence = getString(R.string.cari)
        val ss1 = SpannableString(stringTextSearch)
        ss1.setSpan(RelativeSizeSpan(0.7f), 0, ss1.length, 0) // set size
        val searchEditText = searchView.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText
        searchEditText.hint = ss1

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (produkAdapter != null) produkAdapter!!.filter.filter(newText)
                return false
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                isSearch = true
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                isSearch = false
                if (produkAdapter != null) produkAdapter!!.filter.filter("")
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_menu_lanjut -> {
                (activity as MainActivity).toolbar.collapseActionView()
                val handler = Handler(Looper.getMainLooper())
                val arrayProdukSerialization = ArrayList<ProdukSerializable>()
                handler.postDelayed({
                    produkSerializable = ProdukSerializable()
                    for (value in produks) {
                        if (value.isFavorite) {
                            arrayProdukSerialization.add(
                                ProdukSerializable(
                                    value.idItem, value.name, value.price
                                    , value.imageUrl,value.price.toInt(), 1, value.status
                                )
                            )
//                        produkSerializable?.idItem = value.idItem
//                        produkSerializable?.name = value.name
//                        produkSerializable?.price = value.price
//                        produkSerializable?.imgResource = value.imageResource
//                        produkSerializable?.imgUrl = value.imageUrl
                        }
                    }
                    if (arrayProdukSerialization.size > 0) {
                        val intent = Intent(context, SelectedProdukListActivity::class.java)
                        val args = Bundle()
                        args.putSerializable("ARRAYLIST", arrayProdukSerialization)
                        intent.putExtra("BUNDLE", args)
//                    startActivity(intent)
                        resultLauncher.launch(intent)
                    } else
                        Toast.makeText(requireContext(), R.string.silakan_cek_input_data, Toast.LENGTH_SHORT).show()
                }, 1000)

                true
            } R.id.action_menu_cari -> {
                true
            } R.id.action_menu_bantuan -> {
                tampilAlertDialogTutorial()
                true
            }else -> super.onOptionsItemSelected(item)
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

    fun DialogKelengkapan() {
        val dialogBuilder = AlertDialog.Builder(context).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_detail_op, null)

        val etNamaUsaha: EditText
        val etEmail: EditText
        val etTelp: EditText
        val etAlamat: EditText
        val btnSimpan: Button

        etNamaUsaha = dialogView.findViewById<View>(R.id.etNamaUsaha) as EditText
        etEmail = dialogView.findViewById<View>(R.id.etEmail) as EditText
        etTelp = dialogView.findViewById<View>(R.id.etTelp) as EditText
        etAlamat = dialogView.findViewById<View>(R.id.etAlamat) as EditText

        btnSimpan = dialogView.findViewById<View>(R.id.btnSimpan) as Button


        btnSimpan.setOnClickListener {
            nama = etNamaUsaha.getText().toString()
            email  = etEmail.getText().toString()
            telp = etTelp.getText().toString()
            alamat = etAlamat.getText().toString()

            if (nama.trim({ it <= ' ' }).equals("", ignoreCase = true)) {
                etNamaUsaha.requestFocus()
                etNamaUsaha.setError("Silahkan isi form ini !")
            } else if (email.trim({ it <= ' ' }).equals("", ignoreCase = true)) {
                etEmail.requestFocus()
                etEmail.setError("Silahkan isi form ini !")
            } else if (telp.trim({ it <= ' ' }).equals("", ignoreCase = true)) {
                etTelp.requestFocus()
                etTelp.setError("Silahkan isi form ini !")
            } else if (alamat.trim({ it <= ' ' }).equals("", ignoreCase = true)) {
                etAlamat.requestFocus()
                etAlamat.setError("Silahkan isi form ini !")
            } else {
                sendData()
                dialogBuilder.dismiss()
            }
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    private fun sendData() {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Loading...")
        progressDialog.show()
        val queue = Volley.newRequestQueue(context)
        val url = Url.serverPos + "setKelengkapanData"
        //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    Log.i("json", jsonObject.toString())
                    val jsonArray = jsonObject.getJSONArray("result")
                    var json = jsonArray.getJSONObject(0)
                    val pesan = json.getString("pesan")
                    when {
                        pesan.equals("0", ignoreCase = true) -> {
                            Toast.makeText(
                                context,
                                "Gagal Melengkapi data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        pesan.equals("1", ignoreCase = true) -> {
                            Toast.makeText(
                                context,
                                "Sukses Melengkapi data",
                                Toast.LENGTH_SHORT
                            ).show()
                            val sharedPreferences =
                                requireContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)

                            //membuat editor untuk menyimpan data ke shared preferences
                            val editor = sharedPreferences.edit()

                            //menambah data ke editor
                            editor.putString(Url.SESSION_NAMA_TEMPAT_USAHA, nama)
                            editor.putString(Url.SESSION_ALAMAT, alamat)
                            editor.putString(Url.SESSION_EMAIL, email)
                            editor.putString(Url.SESSION_TELP, telp)
                            editor.putString(Url.SESSION_KELENGKAPAN, "1")

                            //menyimpan data ke editor
                            editor.apply()
                        }
                        else -> {
                            Toast.makeText(
                                context,
                                "Jaringan masih sibuk !",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                val sharedPreferences: SharedPreferences =
                    context!!.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
                val idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "-1")
                params["alamat"] = alamat
                params["nama_usaha"] = nama
                params["email"] = email
                params["telp"] = telp
                params["id_pengguna"] = idPengguna.toString()

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

            override fun retry(error: VolleyError) {

            }
        }

        queue.add(stringRequest)

    }
}

