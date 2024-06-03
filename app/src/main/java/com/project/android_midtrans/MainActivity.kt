package com.project.android_midtrans

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.midtrans.sdk.uikit.api.model.*
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import com.midtrans.sdk.uikit.internal.util.UiKitConstants.STATUS_CANCELED
import com.midtrans.sdk.uikit.internal.util.UiKitConstants.STATUS_FAILED
import com.midtrans.sdk.uikit.internal.util.UiKitConstants.STATUS_INVALID
import com.midtrans.sdk.uikit.internal.util.UiKitConstants.STATUS_PENDING
import com.midtrans.sdk.uikit.internal.util.UiKitConstants.STATUS_SUCCESS
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
        supportActionBar?.title = "Midtrans Payment"

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
                Log.d("TransactionResult", "Transaction Result: ${transactionResult?.transactionId}, ${transactionResult?.status}")
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
            ),
            itemDetails,
            CreditCard(true,
                null,
                Authentication.AUTH_3DS,
                CreditCard.MIGS,
                BankType.BRI,
                null,
                null,
                null,
                null,
                null,),
            "customerIdentifier"+System.currentTimeMillis().toString(),
            PaymentCallback("https://klephtic-electricit.000webhostapp.com/response.php"), null, null,
            Expiry(SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault()).format(Date()), Expiry.UNIT_MINUTE, 5),
        )
    }

    private fun setLocaleNew(languageCode: String?) {
        val locales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val transactionResult = data?.getParcelableExtra<TransactionResult>(
                UiKitConstants.KEY_TRANSACTION_RESULT
            )
            if (transactionResult != null) {
                when (transactionResult.status) {
                    STATUS_SUCCESS -> {
                        Toast.makeText(this, "Transaction Finished. ID: " + transactionResult.transactionId, Toast.LENGTH_LONG).show()
                        Log.d("TransactionResult", "Transaction Result: ${transactionResult.transactionId}, ${transactionResult.status}")
                    }
                    STATUS_PENDING -> {
                        Toast.makeText(this, "Transaction Pending. ID: " + transactionResult.transactionId, Toast.LENGTH_LONG).show()
                        Log.d("TransactionResult", "Transaction Result: ${transactionResult.transactionId}, ${transactionResult.status}")
                    }
                    STATUS_FAILED -> {
                        Toast.makeText(this, "Transaction Failed. ID: " + transactionResult.transactionId, Toast.LENGTH_LONG).show()
                        Log.d("TransactionResult", "Transaction Result: ${transactionResult.transactionId}, ${transactionResult.status}")
                    }
                    STATUS_CANCELED -> {
                        Toast.makeText(this, "Transaction Cancelled", Toast.LENGTH_LONG).show()
                        Log.d("TransactionResult", "Transaction Result: ${transactionResult.transactionId}, ${transactionResult.status}")
                    }
                    STATUS_INVALID -> {
                        Toast.makeText(this, "Transaction Invalid. ID: " + transactionResult.transactionId, Toast.LENGTH_LONG).show()
                        Log.d("TransactionResult", "Transaction Result: ${transactionResult.transactionId}, ${transactionResult.status}")
                    }
                    else -> {
                        Toast.makeText(this, "Transaction ID: " + transactionResult.transactionId + ". Message: " + transactionResult.status, Toast.LENGTH_LONG).show()
                        Log.d("TransactionResult", "Transaction Result: ${transactionResult.transactionId}, ${transactionResult.status}")
                    }
                }
            } else {
                Toast.makeText(this, "Transaction Invalid", Toast.LENGTH_LONG).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}