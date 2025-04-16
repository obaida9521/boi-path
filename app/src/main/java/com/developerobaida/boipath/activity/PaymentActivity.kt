package com.developerobaida.boipath.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.developerobaida.boipath.R
import com.developerobaida.boipath.databinding.ActivityPaymentBinding
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCAdditionalInitializer
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCEMITransactionInitializer
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization
import com.sslwireless.sslcommerzlibrary.model.response.SSLCTransactionInfoModel
import com.sslwireless.sslcommerzlibrary.model.util.SSLCCurrencyType
import com.sslwireless.sslcommerzlibrary.model.util.SSLCLanguage
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType
import com.sslwireless.sslcommerzlibrary.view.singleton.IntegrateSSLCommerz
import com.sslwireless.sslcommerzlibrary.viewmodel.listener.SSLCTransactionResponseListener

class PaymentActivity : AppCompatActivity() , SSLCTransactionResponseListener{
    lateinit var binding: ActivityPaymentBinding

    lateinit var sslCommerzInitialization: SSLCommerzInitialization
    lateinit var additionalInitializer: SSLCAdditionalInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.pay.setOnClickListener {
            sslSetup("20")
        }
    }

    private fun sslSetup(amount: String){
        sslCommerzInitialization = SSLCommerzInitialization(
            "nexde63e48d9521823",
            "nexde63e48d9521823@ssl",
            amount.toDouble(),
            SSLCCurrencyType.BDT,
            "$amount",
            "ebook",
            SSLCSdkType.TESTBOX,
            SSLCLanguage.Bangla
        )
        additionalInitializer = SSLCAdditionalInitializer()
        additionalInitializer.valueA = "Value Option 1"
        additionalInitializer.valueB = "Value Option 2"
        additionalInitializer.valueC = "Value Option 3"
        additionalInitializer.valueD = "Value Option 4"

        IntegrateSSLCommerz.getInstance(this)
            .addSSLCommerzInitialization(sslCommerzInitialization)
            .addAdditionalInitializer(additionalInitializer)
            .buildApiCall(this)

    }

    override fun transactionSuccess(p0: SSLCTransactionInfoModel?) {
        Toast.makeText(this@PaymentActivity,"payment successful",Toast.LENGTH_SHORT).show()
    }

    override fun transactionFail(p0: String?) {
        Toast.makeText(this@PaymentActivity,p0,Toast.LENGTH_SHORT).show()
    }

    override fun closed(p0: String?) {
        Toast.makeText(this@PaymentActivity,p0,Toast.LENGTH_SHORT).show()
    }
}