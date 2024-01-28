package com.project.android_midtrans

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.midtrans.sdk.uikit.api.model.*
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import com.project.android_midtrans.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AdapterCable
    private var dataList: List<DataItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        adapter = AdapterCable(dataList)
        binding.rvItems.layoutManager = LinearLayoutManager(this)
        val newDataList = mutableListOf<DataItem>(
            DataItem("Cable 1", "ID1", "100000", false),
            DataItem("Cable 2", "ID2", "200000", false),
            DataItem("Cable 3", "ID3", "300000", false),
            DataItem("Cable 4", "ID4", "400000", false),
            DataItem("Cable 5", "ID5", "500000", false),
            DataItem("Cable 6", "ID6", "600000", false),
            DataItem("Cable 7", "ID7", "700000", false)
        )
        binding.rvItems.adapter = adapter
        adapter.setData(newDataList)

        UiKitApi.Builder()
            .withMerchantClientKey("SB-Mid-client-zxsIf1O0m5osrifr")
            .withContext(applicationContext)
            .withMerchantUrl("https://klephtic-electricit.000webhostapp.com/preresponse.php/")
            .enableLog(true)
            .withColorTheme(CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
            .build()
        setLocaleNew("id")

        binding.btnBayar.setOnClickListener {
            onTransactionButtonClick(it)
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result?.resultCode == RESULT_OK) {
            result.data?.let {
                val transactionResult = it.getParcelableExtra<TransactionResult>(UiKitConstants.KEY_TRANSACTION_RESULT)
                Toast.makeText(this,"${transactionResult?.transactionId}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onTransactionButtonClick(view: View) {
        val checkedItems = adapter.getCheckedItems()
        var amount = 0.0

        val itemDetails = ArrayList<ItemDetails>()

        for (item in checkedItems) {
            val name = item.name
            val id = item.id
            val price = item.price.toDouble()

            amount += price

            val detail = ItemDetails(id, price, 1, name)
            itemDetails.add(detail)
        }

        UiKitApi.getDefaultInstance().startPaymentUiFlow(
            this@MainActivity,
            launcher,
            SnapTransactionDetail(UUID.randomUUID().toString(), amount, "IDR"),
            CustomerDetails(
                "budi-6789",
                "Budi",
                "Utomo",
                "budi@utomo.com",
                "0213213123",
                null,
                null
            ), // Customer Details
            itemDetails,
            CreditCard(false, null, null, null, null, null, null, null, null, null),
            "customerIdentifier"+System.currentTimeMillis().toString(),
            null, null, null,
            Expiry(SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault()).format(Date()), Expiry.UNIT_DAY, 1),
        )
    }

    private fun setLocaleNew(languageCode: String?) {
        val locales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(locales)
    }
}