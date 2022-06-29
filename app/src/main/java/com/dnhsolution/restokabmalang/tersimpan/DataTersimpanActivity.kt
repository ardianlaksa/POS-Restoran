package com.dnhsolution.restokabmalang.tersimpan

import android.app.*
import com.dnhsolution.restokabmalang.MainActivity.Companion.pajakPersen
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.tersimpan.ItemTersimpan
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.dnhsolution.restokabmalang.tersimpan.DetailTersimpanAdapter
import com.dnhsolution.restokabmalang.tersimpan.ItemDetailTersimpan
import android.os.Bundle
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import android.content.DialogInterface
import android.widget.Toast
import com.dnhsolution.restokabmalang.tersimpan.TersimpanAdater
import android.os.Looper
import com.dnhsolution.restokabmalang.tersimpan.CekDataTersimpanNetworkTask
import android.os.AsyncTask
import org.json.JSONObject
import org.json.JSONException
import org.json.JSONArray
import androidx.recyclerview.widget.DefaultItemAnimator
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import androidx.core.app.NotificationCompat
import android.content.Intent
import com.dnhsolution.restokabmalang.tersimpan.DataTersimpanActivity
import android.graphics.BitmapFactory
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.dnhsolution.restokabmalang.utilities.dialog.AdapterWizard
import androidx.core.content.ContextCompat
import com.dnhsolution.restokabmalang.cetak.MainCetak
import com.dnhsolution.restokabmalang.cetak.MainCetakLokal
import com.dnhsolution.restokabmalang.utilities.*
import com.dnhsolution.restokabmalang.utilities.OnDataFetched
import com.dnhsolution.restokabmalang.utilities.dialog.ItemView
import java.lang.Exception
import java.util.ArrayList

class DataTersimpanActivity : AppCompatActivity(), OnDataFetched, DataTersimpanLongClick {
    var sharedPreferences: SharedPreferences? = null
    var rvData: RecyclerView? = null
    var dataTersimpan: MutableList<ItemTersimpan>? = null
    var linearLayout: LinearLayout? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var dividerItemDecoration: DividerItemDecoration? = null
    private var adapter: RecyclerView.Adapter<*>? = null
    var ChildView: View? = null
    var RecyclerViewClickedItemPos = 0
    var databaseHandler: DatabaseHandler? = null
    var tvKet: TextView? = null
    var fab_upload: FloatingActionButton? = null
    var detailTersimpanAdater: DetailTersimpanAdapter? = null
    var itemDetailTersimpans: MutableList<ItemDetailTersimpan> = ArrayList()
    var tv_count: TextView? = null
    var jml_data = 0
    var progressdialog: ProgressDialog? = null
    var datax = 0
    var status = 0
    private var isRunnerRunning = false
    private var menuTemp: Menu? = null
    private var statusJaringan = 0
    private val _tag = javaClass.simpleName
    private var tipeStruk: String? = null
    private var idPengguna: String? = null
    private var idTmptUsaha: String? = null
    private var uuid: String? = null
    private var pajakPersen = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        val label = sharedPreferences?.getString(Url.setLabel, "Belum disetting")
        tipeStruk = sharedPreferences?.getString(Url.SESSION_TIPE_STRUK, "")
        idPengguna = sharedPreferences?.getString(Url.SESSION_ID_PENGGUNA, "0")
        idTmptUsaha = sharedPreferences?.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
        uuid = sharedPreferences?.getString(Url.SESSION_UUID, "")
        pajakPersen = MainActivity.pajakPersen
        val tema = sharedPreferences?.getString(Url.setTema, "0")
        if (tema.equals("0", ignoreCase = true)) {
            this@DataTersimpanActivity.setTheme(R.style.Theme_First)
        } else if (tema.equals("1", ignoreCase = true)) {
            this@DataTersimpanActivity.setTheme(R.style.Theme_Second)
        } else if (tema.equals("2", ignoreCase = true)) {
            this@DataTersimpanActivity.setTheme(R.style.Theme_Third)
        } else if (tema.equals("3", ignoreCase = true)) {
            this@DataTersimpanActivity.setTheme(R.style.Theme_Fourth)
        } else if (tema.equals("4", ignoreCase = true)) {
            this@DataTersimpanActivity.setTheme(R.style.Theme_Fifth)
        } else if (tema.equals("5", ignoreCase = true)) {
            this@DataTersimpanActivity.setTheme(R.style.Theme_Sixth)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_tersimpan)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(label)
        databaseHandler = DatabaseHandler(this@DataTersimpanActivity)
        rvData = findViewById<View>(R.id.rvData) as RecyclerView
        tvKet = findViewById<View>(R.id.tvKet) as TextView
        tv_count = findViewById<View>(R.id.text_count) as TextView
        fab_upload = findViewById(R.id.fab)
        fab_upload?.let {
            it.setOnClickListener(View.OnClickListener { view: View? ->
                if (CheckNetwork().checkingNetwork(this) && statusJaringan == 1) {
                    jml_data = databaseHandler!!.CountDataTersimpanUpload()
                    val builder = AlertDialog.Builder(this@DataTersimpanActivity)
                    builder.setMessage("Lanjut upload $jml_data data ke server ?")
                    builder.setCancelable(true)
                    builder.setPositiveButton(
                        "Ya"
                    ) { dialog: DialogInterface?, id: Int ->
                        if (CheckNetwork().checkingNetwork(
                                applicationContext
                            )
                        ) {
                            sendData()
                        } else {
                            Toast.makeText(
                                this@DataTersimpanActivity,
                                R.string.tidak_terkoneksi_internet,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    builder.setNegativeButton(
                        "Tidak"
                    ) { dialog: DialogInterface, id: Int -> dialog.cancel() }
                    val alert = builder.create()
                    alert.show()
                } else Toast.makeText(this, R.string.tidak_terkoneksi_internet, Toast.LENGTH_SHORT)
                    .show()
            })
        }
        dataTersimpan = ArrayList()
        adapter = TersimpanAdater(dataTersimpan, this@DataTersimpanActivity, this)
        linearLayoutManager = LinearLayoutManager(this@DataTersimpanActivity)
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        dividerItemDecoration =
            DividerItemDecoration(applicationContext, linearLayoutManager!!.orientation)
        rvData!!.setHasFixedSize(true)
        rvData!!.layoutManager = linearLayoutManager
        rvData!!.adapter = adapter
        val url = Url.serverPos + "getProduk?idTmpUsaha=" + idTmptUsaha +
                "&jenisProduk=0&idPengguna=" + idPengguna + "&uuid=" + uuid
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (!isRunnerRunning) {
                    val runner1 = TaskRunner()
                    runner1.executeAsync(
                        CekDataTersimpanNetworkTask(
                            this@DataTersimpanActivity,
                            url
                        )
                    )
                    isRunnerRunning = true
                }
                handler.postDelayed(this, 5000)
            }
        })
        data

//        rvData.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
//
//            GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
//
//                @Override public boolean onSingleTapUp(MotionEvent motionEvent) {
//                    return true;
//                }
//
//            });
//            @Override
//            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
//                ChildView = rvData.findChildViewUnder(e.getX(), e.getY());
//
//                if(ChildView != null && gestureDetector.onTouchEvent(e)) {
//                    RecyclerViewClickedItemPos = rvData.getChildAdapterPosition(ChildView);
////                    Log.d("hhhhuu", String.valueOf(RecyclerViewClickedItemPos));
//                    int id_data = dataTersimpan.get(RecyclerViewClickedItemPos).getId();
//                    DialogDetailTrx(id_data);
//                    //Toast.makeText(DataTersimpanActivity.this, String.valueOf(id_data), Toast.LENGTH_SHORT).show();
//                }
//                return false;
//            }
//
//            @Override
//            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
//
//            }
//
//            @Override
//            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//
//            }
//        });
    }

    private fun sendData() {
        class SendData : AsyncTask<Void?, Int?, String>() {
            override fun onPreExecute() {
                super.onPreExecute()
                progressdialog = ProgressDialog(this@DataTersimpanActivity)
                progressdialog!!.isIndeterminate = false
                progressdialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                progressdialog!!.setCancelable(true)
                progressdialog!!.setMessage("Upload data ke server ...")
                progressdialog!!.max = jml_data
                progressdialog!!.show()
                progressdialog!!.setButton(
                    DialogInterface.BUTTON_NEGATIVE, "Minimize",
                    null as DialogInterface.OnClickListener?
                )
            }

            override fun onProgressUpdate(vararg values: Int?) {
                progressdialog!!.progress = values[0]!!
            }

            override fun onPostExecute(s: String) {
                super.onPostExecute(s)
                Log.d("HASIL", s)
                var hasil = ""
                if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                try {
                    val jsonObject = JSONObject(s)
                    hasil = jsonObject.getString("message")
                    Log.d("HASIL_JSON", jsonObject.getString("message"))
                    if (hasil.equals("Berhasil.", ignoreCase = true)) {
                        if (datax == jml_data) {
                            sendNotification("$datax data berhasil diupload !")
                            if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                            Log.d("INFORMASI", "suksesUpload: ")
                            for (v in dataTersimpan!!) {
                                val posisi = v.getNo() - 1
                                dataTersimpan!![posisi].setStatus("1")
                                adapter!!.notifyItemChanged(posisi)
                            }
                            showHideFabUpload()
                        }
                    } else if (hasil.equals("Gagal.", ignoreCase = true)) {
                        if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                        Log.d("INFORMASI", "gagalUpload: ")
                    } else {
                        if (progressdialog!!.isShowing) progressdialog!!.dismiss()
                        Log.d("INFORMASI", "gagalUpload: $s")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun doInBackground(vararg params: Void?): String {
                val listDataTersimpanUpload = databaseHandler!!.dataTersimpanUpload
                var msg = ""
                for (f in listDataTersimpanUpload as List<ItemTersimpan>) {
                    try {
                        val u = UploadData()
                        val rootObject = JSONObject()
                        rootObject.put("uuid", uuid)
                        rootObject.put("idTmptUsaha", idTmptUsaha)
                        rootObject.put("user", idPengguna)
                        rootObject.put("disc_rp", f.getDisc_rp())
                        var disc = f.getDisc()
                        if (disc.isEmpty()) {
                            disc = f.getDisc_rp()
                        }
                        rootObject.put("disc", disc)
                        rootObject.put("omzet", f.getOmzet())
                        rootObject.put("pajakRp", f.getPajakRp())
                        rootObject.put("pajakPersen", pajakPersen)
                        rootObject.put("bayar", f.getBayar())
                        rootObject.put("idHiburanNomor", f.getIdHiburanNomor())
                        rootObject.put("nominalServiceCharge", f.getNominalServiceCharge())
                        val jsonArr = JSONArray()
                        val listDetailTrx=
                            databaseHandler!!.getDetailTersimpan(f.getId().toString())
                        for (d in listDetailTrx as List<ItemDetailTersimpan>) {
                            val pnObj = JSONObject()
                            pnObj.put("idProduk", d.getId_produk())
                            pnObj.put("nmProduk", d.getNama())
                            pnObj.put("qty", d.getQty())
                            pnObj.put("hrgProduk", d.getHarga())
                            pnObj.put("isPajak", d.getIsPajak())
                            pnObj.put("tipeStruk", tipeStruk)
                            pnObj.put("keterangan", d.getKeterangan())
                            pnObj.put("seriProduk", d.getSeriProduk())
                            pnObj.put("rangeTransaksiKarcisAwal",d.getRangeTransaksiKarcisAwal())
                            jsonArr.put(pnObj)
                        }
                        rootObject.put("produk", jsonArr)
                        msg = u.uploadData(rootObject.toString())
                        Log.e("INFO_PENTING", "NUMBER: $status")
//                        status
//                        publishProgress(status)
                        Log.e(_tag, msg)
                        val jsonMsg = JSONObject(msg)
                        if (jsonMsg.getString("message").equals("Berhasil.", ignoreCase = true)) {
                            datax++
                            databaseHandler!!.updateDataTersimpan(ItemTersimpan(f.getId(), "1"))
                        } else {
                            Log.d("INFO_PENTING", "doInBackground: $msg")
                        }
                    } catch (e: Exception) {
                        cancel(true)
                        e.printStackTrace()
                    }
                }
                return msg
            }
        }

        val uv = SendData()
        uv.execute()
    }

    fun DialogDetailTrx(idTrx: Int) {
        val dialogBuilder = AlertDialog.Builder(this).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_detail_rekap_harian, null)
        val rvDetail: RecyclerView
        val tvTrx: TextView
        rvDetail = dialogView.findViewById<View>(R.id.recyclerView) as RecyclerView
        tvTrx = dialogView.findViewById<View>(R.id.tvNoTrx) as TextView
        tvTrx.text = idTrx.toString()
        detailTersimpanAdater =
            DetailTersimpanAdapter(itemDetailTersimpans, this@DataTersimpanActivity)
        val mLayoutManagerss: RecyclerView.LayoutManager =
            LinearLayoutManager(this@DataTersimpanActivity)
        rvDetail.layoutManager = mLayoutManagerss
        rvDetail.itemAnimator = DefaultItemAnimator()
        rvDetail.adapter = detailTersimpanAdater
        getDetailTersimpan(idTrx)
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    private fun getDetailTersimpan(idTrx: Int) {
        itemDetailTersimpans.clear()
        val db = databaseHandler!!.readableDatabase
        val mCount = db.rawQuery("select * from detail_transaksi where id_trx='$idTrx'", null)
        mCount.moveToFirst()
        val countTersimpan = mCount.getInt(0)
        Log.d(
            "DETAIL_TERSIMPAN",
            "getDetailTersimpan: " + mCount.getInt(0) + "/" + mCount.getInt(1) + "/" + mCount.getString(
                2
            ) + "/" + mCount.getString(3) + "/" + mCount.getInt(4) + "/" + mCount.getInt(5)
        )
        mCount.close()
        try {
            val listDetailTersimpan =
                databaseHandler!!.getDetailTersimpan(idTrx.toString())
            var no = 1
            for (f in listDetailTersimpan as List<ItemDetailTersimpan>) {
                val it = ItemDetailTersimpan()
                it.setId(f.getId())
                it.setNama(f.getNama())
                it.setQty(f.getQty())
                it.setHarga(f.getHarga())
                it.setNo(no)
                val total = f.getQty().toInt() * f.getHarga().toInt()
                it.setTotal(total.toString())
                it.setIsPajak(f.isPajak)
                itemDetailTersimpans.add(it)
                no++
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        detailTersimpanAdater!!.notifyDataSetChanged()
    }

    private val data: Unit
        get() {
            dataTersimpan!!.clear()
            val jml_data = databaseHandler!!.CountDataTersimpan2()
            if (jml_data == 0) {
                tvKet!!.visibility = View.VISIBLE
                fab_upload!!.visibility = View.GONE
            } else {
                showHideFabUpload()
                tvKet!!.visibility = View.GONE
            }
            try {
                val listDataTersimpan = databaseHandler!!.dataTersimpan
                var nomer = 1
                for (f in listDataTersimpan as List<ItemTersimpan> ) {
                    val it = ItemTersimpan()
                    it.setId(f.getId())
                    it.setNo(nomer)
                    it.setTanggal_trx(f.getTanggal_trx())
                    it.setDisc(f.getDisc())
                    it.setOmzet(f.getOmzet())
                    it.setDisc_rp(f.getDisc_rp())
                    it.setStatus(f.getStatus())
                    it.setPajakRp(f.getPajakRp())
                    dataTersimpan!!.add(it)
                    nomer++
                }
            } catch (e: SQLiteException) {
                e.printStackTrace()
            }
            Log.d("OP_TERUPDATE", dataTersimpan!!.size.toString())
            adapter!!.notifyDataSetChanged()
        }

    override fun onResume() {
        super.onResume()
        val label = sharedPreferences!!.getString(Url.setLabel, "Belum disetting")
        supportActionBar!!.title = label
        val tema = sharedPreferences!!.getString(Url.setTema, "0")
        if (tema.equals("0", ignoreCase = true)) {
            this.setTheme(R.style.Theme_First)
        } else if (tema.equals("1", ignoreCase = true)) {
            this.setTheme(R.style.Theme_Second)
        } else if (tema.equals("2", ignoreCase = true)) {
            this.setTheme(R.style.Theme_Third)
        } else if (tema.equals("3", ignoreCase = true)) {
            this.setTheme(R.style.Theme_Fourth)
        } else if (tema.equals("4", ignoreCase = true)) {
            this.setTheme(R.style.Theme_Fifth)
        } else if (tema.equals("5", ignoreCase = true)) {
            this.setTheme(R.style.Theme_Sixth)
        }
    }

    fun sendNotification(message: String?) {
        val builder = NotificationCompat.Builder(this)
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
        val intent = Intent(applicationContext, DataTersimpanActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        builder.setContentIntent(pendingIntent)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo_sipanji))
        val app_name = R.string.app_name.toString()
        builder.setContentTitle(app_name)
        builder.setContentText(message)
        // builder.setSubText("Tap to view the website.");
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Will display the notification in the notification bar
        notificationManager.notify(1, builder.build())
    }

    private fun showHideFabUpload() {
        val jmlData = databaseHandler!!.CountDataTersimpanUpload()
        if (jmlData == 0) {
            fab_upload!!.visibility = View.GONE
            tv_count!!.text = ""
            tv_count!!.visibility = View.INVISIBLE
        } else if (jmlData <= 9) {
            tv_count!!.visibility = View.VISIBLE
            tv_count!!.text = jmlData.toString()
        } else {
            tv_count!!.visibility = View.VISIBLE
            tv_count!!.text = "9+"
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_tersimpan, menu)
        menuTemp = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_menu_bantuan) {
            tampilAlertDialogTutorial()
            return true
        } else if (item.itemId == R.id.action_menu_wifi) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun tampilAlertDialogTutorial() {
        val alertDialog = AlertDialog.Builder(this@DataTersimpanActivity).create()
        val rowList = layoutInflater.inflate(R.layout.dialog_tutorial, null)
        val listView = rowList.findViewById<ListView>(R.id.listView)
        val tutorialArrayAdapter: AdapterWizard
        val arrayList = ArrayList<ItemView>()
        arrayList.add(
            ItemView(
                "1",
                "Status Belum Sinkron warna orange : menandakan data transaksi belum tersinkron dengan server."
            )
        )
        arrayList.add(
            ItemView(
                "2",
                "Status Sudah sinkron warna hijau : menandakan data transaksi sudah tersinkron dengan server."
            )
        )
        arrayList.add(
            ItemView(
                "3",
                "Saat ada data dengan status Belum Sinkron, akan tampil tombol icon Upload warna hijau. Tombol ini digunakan untuk upload data transaksi yang Belum Sinkron ke server."
            )
        )
        arrayList.add(
            ItemView(
                "4",
                "Angka background merah diatas tombol upload menandakan jumlah data dengan status Belum Sinkron."
            )
        )
        tutorialArrayAdapter = AdapterWizard(this@DataTersimpanActivity, arrayList)
        listView.adapter = tutorialArrayAdapter
        alertDialog.setView(rowList)
        alertDialog.show()
    }

    override fun showProgressBar() {}
    override fun hideProgressBar() {}
    override fun setDataInPageWithResult(result: Any?) {
        if (result == null) return
        gantiIconWifi(result.toString().equals("1", ignoreCase = true))
        isRunnerRunning = false
    }

    fun gantiIconWifi(value: Boolean) {
        if (value) {
            menuTemp!!.getItem(1).icon =
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_wifi_24_green)
            statusJaringan = 1
        } else {
            menuTemp!!.getItem(1).icon =
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_wifi_24_gray)
            statusJaringan = 0
        }
    }

    override fun dataTersimpanLongClick(result: String?) {
        resultLauncher.launch(Intent(applicationContext, MainCetakLokal::class.java).putExtra("idTrx", result))
    }

    var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val a = data?.extras
//            Toast.makeText(requireContext(), "OK",Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "10001"
        private const val default_notification_channel_id = "default"
    }
}