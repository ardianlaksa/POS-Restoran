package com.dnhsolution.restokabmalang.transaksi.keranjang.tambah

import android.app.Activity.RESULT_OK
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
import android.widget.*
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
import com.dnhsolution.restokabmalang.databinding.FragmentProdukListBinding
import com.dnhsolution.restokabmalang.sistem.produk.ItemProduk
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable
import com.dnhsolution.restokabmalang.transaksi.tab_fragment.ProdukListAdapter
import com.dnhsolution.restokabmalang.transaksi.tab_fragment.ProdukListElement
import com.dnhsolution.restokabmalang.transaksi.tab_fragment.ProdukListJsonTask
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.ProdukOnTask
import com.dnhsolution.restokabmalang.utilities.Url
import com.dnhsolution.restokabmalang.utilities.dialog.AdapterWizard
import com.dnhsolution.restokabmalang.utilities.dialog.ItemView
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set
import kotlin.math.min

class TambahProdukTransaksiFragment:Fragment(), ProdukOnTask {

    private lateinit var tvBadgeMenuLanjut: TextView
    private var argumenValue2: ArrayList<*>? = null
    private var transaksiFragment: TambahProdukTransaksiActivity? = null
    private lateinit var argumenValue: String
    private lateinit var searchView: SearchView
    private var valueArgsFromKeranjang: Int? = null
    private var produkAdapter: ProdukListAdapter? = null
    private var idPengguna: String? = null
    private var idTmpUsaha: String? = null
    private var uuid: String? = null

    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    var produks:ArrayList<ProdukListElement> = ArrayList()
    private var argTab = arrayOf("")

    var databaseHandler: DatabaseHandler? = null

    var nama: String = ""
    var email: String = ""
    var telp: String = ""
    var alamat: String = ""
    var isSearch = false

    lateinit var alertDialog: AlertDialog.Builder
    lateinit var dialog: AlertDialog
    val keyParams = "params"
    val keyParams2 = "params2"

    companion object {
        @JvmStatic
        fun newInstance(params: String, params2: ArrayList<ProdukSerializable>?) = TambahProdukTransaksiFragment().apply {
            arguments = Bundle().apply {
                putString(keyParams,params)
                putParcelableArrayList(keyParams2,params2)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        transaksiFragment = (activity as TambahProdukTransaksiActivity)
    }

    private lateinit var binding: FragmentProdukListBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProdukListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        idTmpUsaha = TambahProdukTransaksiActivity.idTmpUsaha
        idPengguna = TambahProdukTransaksiActivity.idPengguna
        uuid = TambahProdukTransaksiActivity.uuid
        argumenValue = arguments?.get(keyParams).toString()
        argumenValue2 = arguments?.get(keyParams2) as ArrayList<*>?
        databaseHandler = DatabaseHandler(requireContext())
        val jenisPajak = MainActivity.jenisPajak

        argTab = MainActivity.argTab

        if (CheckNetwork().checkingNetwork(requireContext()))
            startShimmering(true)
        else
            startShimmering(false)

        if(produkAdapter == null)
            if(CheckNetwork().checkingNetwork(requireContext())) {
//                val stringUrl = "${Url.getProduk}?idTmpUsaha=$idTmpUsaha&jenisProduk=${arguments?.get(keyParams).toString()}"
                val stringUrl = "${Url.getProduk}?idTmpUsaha=$idTmpUsaha&" +
                        "jenisProduk=${arguments?.get(keyParams).toString()}&" +
                        "idPengguna=$idPengguna&uuid=$uuid&jenisPajak=$jenisPajak"
                Log.i(_tag,stringUrl)
                jsonTask = ProdukListJsonTask(this).execute(stringUrl)
            } else {
                getDataLokal()
            }
        else {
            binding.gvMainActivity.adapter = produkAdapter
        }

        binding.gvMainActivity.setOnItemClickListener { _, _, position, _ ->
            val produk = produks[position]
//            argumenValue = produk.jnsProduk
            transaksiFragment?.tambahDataApsList(argumenValue,produk)
            val isFavorit = produk.toggleFavorite()
            if(isFavorit) TambahProdukTransaksiActivity.jumlahProdukTerpilih++
            else TambahProdukTransaksiActivity.jumlahProdukTerpilih--
            setupBadge()
            produkAdapter?.notifyDataSetChanged()
        }

        if(MainActivity.adTransaksi == 1) return

        MainActivity.adTransaksi = 1
    }

    private fun startShimmering(arg: Boolean){
        if(arg) {
            binding.gvMainActivity.visibility = View.GONE
            binding.shimmerViewContainer.visibility = View.VISIBLE
            binding.shimmerViewContainer.startShimmerAnimation()
        } else {
            binding.shimmerViewContainer.stopShimmerAnimation()
            binding.shimmerViewContainer.visibility = View.GONE
            binding.gvMainActivity.visibility = View.VISIBLE
            binding.gvMainActivity.visibility = View.VISIBLE
        }
    }

    private fun tampilAlertDialogTutorial() {
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
                    val isPajak = rArray.getJSONObject(i).getString("ISPAJAK")
                    val jnsProduk = rArray.getJSONObject(i).getString("JENIS_PRODUK")
                    val kode = rArray.getJSONObject(i).getString("KODE")

                    val keteranganLengkap = "$kode:$keterangan"

                    produks.add(
                        ProdukListElement(
                            idBarang,nmBarang, harga, foto, keteranganLengkap,"server",isPajak,jnsProduk)
                    )
                }

                if (produkAdapter != null) produkAdapter?.notifyDataSetChanged()
                else produkAdapter =
                    ProdukListAdapter(requireContext(), produks)

                binding.gvMainActivity.adapter = produkAdapter

                if (argumenValue2 != null) {
                    for (favoriteId in argumenValue2!! as ArrayList<ProdukSerializable>) {
                        for (Produk in produks) {
                            if (Produk.idItem == favoriteId.idItem) {
                                Produk.isFavorite = true
//                                transaksiFragment?.tambahDataApsList(argumenValue,Produk)
                                break
                            }
                        }
                    }
                }
                startShimmering(false)
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                startShimmering(false)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
            startShimmering(false)
        }
    }

    private fun getDataLokal() {
        produks.clear()

        val jmlData = databaseHandler!!.CountDataProduk()
        if (jmlData == 0) {
            Toast.makeText(context,R.string.empty_data,Toast.LENGTH_SHORT).show()
        }

        val listProduk: List<ItemProduk> = databaseHandler!!.getDataProduk2(arguments?.get(keyParams).toString())

        for(e in listProduk){
            val idBarang = e.id_barang.toInt()
            val harga = e.harga
            val foto = e.url_image
            val nmBarang = e.nama_barang
            val keterangan = e.keterangan

            produks.add(
                ProdukListElement(
                    idBarang,nmBarang, harga, foto, keterangan, "lokal",e.isPajak,e.jenisProduk)
            )

            if (argumenValue2 != null) {
                for (favoriteId in argumenValue2!! as ArrayList<ProdukSerializable>) {
                    for (Produk in produks) {
                        if (Produk.idItem == favoriteId.idItem) {
                            Produk.isFavorite = true
//                            transaksiFragment?.tambahDataApsList(argumenValue,Produk)
                            break
                        }
                    }
                }
            }
        }

        if (produkAdapter != null) produkAdapter?.notifyDataSetChanged()
        else produkAdapter =
            ProdukListAdapter(requireContext(), produks)

        binding.gvMainActivity.adapter = produkAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_transaksi, menu)
        val menuItem = menu.findItem(R.id.action_menu_lanjut)

        val actionView = menuItem.actionView
        val flCustomTambahBadge = actionView?.findViewById(R.id.flCustomTambahBadge) as FrameLayout
        tvBadgeMenuLanjut = actionView?.findViewById(R.id.cart_badge) as TextView
        flCustomTambahBadge.setOnClickListener {
            (activity as TambahProdukTransaksiActivity).binding.toolbar.collapseActionView()
            menuLanjutHandler()
        }

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
        setupBadge()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_menu_lanjut -> {
                (activity as TambahProdukTransaksiActivity).binding.toolbar.collapseActionView()
//                println("tambah : ${transaksiFragment?.apsMakanan?.size} " +
//                        "${transaksiFragment?.apsMinuman?.size} ${transaksiFragment?.apsDll?.size}")
                menuLanjutHandler()
                true
            } R.id.action_menu_cari -> {
                true
            } R.id.action_menu_bantuan -> {
                tampilAlertDialogTutorial()
                true
            }else -> super.onOptionsItemSelected(item)
        }
    }

    private fun menuLanjutHandler(){
        val handler = Handler(Looper.getMainLooper())
        val arrayProdukSerialization = ArrayList<ProdukSerializable>()

//        for(valueTab in argTab){
//            if(valueTab != argumenValue) {
//                val dataList = transaksiFragment?.tampilDataApsList(valueTab)
//                dataList?.let {
//                    for (value in it) {
//                        if (value.isFavorite) {
//                            arrayProdukSerialization.add(
//                                ProdukSerializable(
//                                    value.idItem, value.name, value.price
//                                    , value.imageUrl,value.price.toInt(), 1, value.isPajak
//                                )
//                            )
//                        }
//                    }
//                }
//            }
//        }

        handler.postDelayed({
//            for (value in produks) {
//                if (value.isFavorite) {
//                    arrayProdukSerialization.add(
//                        ProdukSerializable(
//                            value.idItem, value.name, value.price
//                            , value.imageUrl,value.price.toInt(), 1, value.isPajak
//                        )
//                    )
//                }
//            }

            val dataList = transaksiFragment?.tampilDataApsList()
            dataList?.let {
                for (value in it) {
                    arrayProdukSerialization.add(
                        ProdukSerializable(
                            value.idItem, value.name, value.price
                            , value.imageUrl,value.price.toInt(), 1, value.isPajak, value.description
                        )
                    )
                }
            }

//            val  valueArgsFromKeranjangActivity = (activity as TambahProdukTransaksiActivity).valueArgsFromKeranjang
//
//            for(value in valueArgsFromKeranjangActivity!!) {
//                var found = false
//                for(value2 in arrayProdukSerialization){
//                    if(value.idItem == value2.idItem) {
//                        found = true
//                        break
//                    }
//                }
//                if (!found) arrayProdukSerialization.add(value)
//            }

            if (arrayProdukSerialization.size > 0) {
                val intent = Intent()
                val args = Bundle()
                args.putSerializable("ARRAYLIST", arrayProdukSerialization)
                intent.putExtra("BUNDLE", args)
                println("$_tag ${arrayProdukSerialization.size}")
                (context as TambahProdukTransaksiActivity).setResult(RESULT_OK, intent)
                (context as TambahProdukTransaksiActivity).finish()
            } else
                Toast.makeText(requireContext(), R.string.silakan_cek_input_data, Toast.LENGTH_SHORT).show()
        }, 1000)
    }

    fun dialogKelengkapan() {
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
                val idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "0")
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

    private fun setupBadge() {
        val jumlahProdukTerpilih = TambahProdukTransaksiActivity.jumlahProdukTerpilih
        if (jumlahProdukTerpilih == 0) {
            if (tvBadgeMenuLanjut.visibility != View.GONE) {
                tvBadgeMenuLanjut.visibility = View.GONE
            }
        } else {
            tvBadgeMenuLanjut.text = java.lang.String.valueOf(min(jumlahProdukTerpilih, 99))
            if (tvBadgeMenuLanjut.visibility != View.VISIBLE) {
                tvBadgeMenuLanjut.visibility = View.VISIBLE
            }
        }
    }
}

