package com.dnhsolution.restokabmalang.data.rekap_billing

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.data.rekap_bulanan.*
import com.dnhsolution.restokabmalang.data.rekap_bulanan.task.RekapBulananJsonTask
import com.dnhsolution.restokabmalang.databinding.FragmentRekapBillingBinding
import com.dnhsolution.restokabmalang.utilities.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class RekapBillingFragment : Fragment(), RekapBulananOnTask {

    companion object {
        @JvmStatic
        fun newInstance(params: String) = RekapBillingFragment().apply {
            arguments = Bundle().apply {
                putString("params",params)
            }
        }
    }

    private var idPengguna: String? = null
    private var idTmpUsaha: String? = null
    private lateinit var recyclerView: RecyclerView

    private lateinit var spiThn: Spinner
    private var itemsBilling:ArrayList<RekapBillingListElement>? = null
    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    private var spinThnArray = ArrayList<String>()
    private var isOpenedThn = false
    private var selectedThn = "Tahun"
    private lateinit var btnCari : Button
    private lateinit var btnReset : Button
    private var thnMasaPajak = 0
    private lateinit var binding: FragmentRekapBillingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRekapBillingBinding.inflate(layoutInflater)
        val view = binding.root
//        val view = inflater.inflate(R.layout.fragment_rekap_billing, container, false)
        btnCari = binding.btnCari
        btnReset = binding.btnReset
        spiThn = binding.spinThn
        recyclerView = binding.recyclerView

//        val sharedPreferences = context?.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = MainActivity.idTempatUsaha
        idPengguna = MainActivity.idPengguna

        spiThn.setSelection(0)

        if(CheckNetwork().checkingNetwork(requireContext())) {
            val stringUrl = "${Url.getRekapBilling}?idTmpUsaha=$idTmpUsaha&thnMasaPajak=$thnMasaPajak&idPengguna=$idPengguna"
            Log.i(_tag,stringUrl)
            jsonTask = RekapBulananJsonTask(this).execute(stringUrl)
        } else {
//            Toast.makeText(context, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
            binding.ivIconDataKosong.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_conn_lost,null))
        }

        spiThn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isOpenedThn) {
                    isOpenedThn = true
                    return
                }

                selectedThn = spinThnArray[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        btnCari.setOnClickListener{
            if(CheckNetwork().checkingNetwork(requireContext())) {
                val stringUrl = "${Url.getRekapBilling}?idTmpUsaha=$idTmpUsaha&thnMasaPajak=$thnMasaPajak&idPengguna=$idPengguna"
                Log.i(_tag,stringUrl)
                jsonTask = RekapBulananJsonTask(this).execute(stringUrl)
            } else {
                Toast.makeText(context, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
            }

            btnReset.visibility = View.VISIBLE
        }

        btnReset.setOnClickListener{

            btnReset.visibility = View.GONE
        }

        return view
    }

    override fun rekapBulananOnTask(result: String?) {
        if (result == null) {
//            Toast.makeText(context,R.string.error_get_data,Toast.LENGTH_SHORT).show()
            binding.ivIconDataKosong.visibility = View.VISIBLE
            return
        } else if (result == "") {
//            Toast.makeText(context,R.string.empty_data,Toast.LENGTH_SHORT).show()
            binding.ivIconDataKosong.visibility = View.VISIBLE
            return
        }

        Log.e("Debug", "Response from url:$result")
        try {
            val jsonObj = JSONObject(result)
            val success = jsonObj.getInt("success")
            val message = jsonObj.getString("message")

            itemsBilling = ArrayList()
            itemsBilling!!.clear()

            if (success == 1) {
                val rArray = jsonObj.getJSONArray("result")
                for (i in 0 until rArray.length()) {

                    val idInc = rArray.getJSONObject(i).getString("ID_INC")
                    val masaPajak = rArray.getJSONObject(i).getString("MASA_PAJAK")
                    val tahunMasaPajak = rArray.getJSONObject(i).getString("TAHUN_MASA_PAJAK")
                    val kodeBilling = rArray.getJSONObject(i).getString("KODE_BILING")
                    val pajakTerutang = rArray.getJSONObject(i).getString("PAJAK_TERUTANG")
                    val tglBayar = rArray.getJSONObject(i).getString("TGL_BAYAR")

                    itemsBilling?.add(
                        RekapBillingListElement(
                            idInc,masaPajak,tahunMasaPajak,kodeBilling,pajakTerutang,tglBayar)
                    )

                    if(spinThnArray.size == 0)
                        spinThnArray.add(tahunMasaPajak)

//                    itemsBilling?.let {
//                        if (it.size > 50) {
//                            if (!spinThnArray.contains(tahunMasaPajak)) spinThnArray.add(
//                                tahunMasaPajak
//                            )
//                        }
//                    }
                }

                val spinThnAdapter = context?.let {
                    RekapBulananThnSpinAdapter(
                        it,
                        R.layout.item_spi_bulan,
                        spinThnArray
                    )
                }

                spiThn.adapter = spinThnAdapter

                val adapterList = context?.let {
                    RekapBillingListAdapter(
                        itemsBilling!!,
                        it
                    )
                }

                recyclerView.adapter = adapterList
                recyclerView.layoutManager = (LinearLayoutManager(context))
                recyclerView.scheduleLayoutAnimation()
                binding.ivIconDataKosong.visibility = View.GONE
            } else {
//                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                binding.ivIconDataKosong.visibility = View.VISIBLE
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            //Toast.makeText(context, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
        }
    }

//    private fun getCurrentDate(): String {
//        val current = Date()
//        val frmt = SimpleDateFormat("MM-dd-yyyy")
//        return frmt.format(current)
//    }
}