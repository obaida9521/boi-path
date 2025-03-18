package com.developerobaida.boipath.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.R
import com.developerobaida.boipath.adapter.AdapterCart
import com.developerobaida.boipath.data.repository.CartRepository
import com.developerobaida.boipath.databinding.ActivityCartBinding
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity(), AdapterCart.OnDeleteClickListener {

    private lateinit var cartAdapter: AdapterCart
    lateinit var binding: ActivityCartBinding
    lateinit var repository: CartRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        repository = CartRepository(this)

        getBooks()

        binding.toolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        binding.clear.setOnClickListener {
            lifecycleScope.launch {
                repository.clearCart()
                getBooks()
                cartAdapter.notifyDataSetChanged()
                Toast.makeText(this@CartActivity,"Cleared all",Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun getBooks(){
        binding.cartRec.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            var totalPrice =0
            val cartItems = repository.getAllCartItems()
            cartAdapter = AdapterCart(cartItems,this@CartActivity)
            binding.cartRec.adapter = cartAdapter

            for (i in 0 until cartItems.size) {
                totalPrice += cartItems[i].price.toInt()
            }
            binding.priceTv.text = totalPrice.toString()
        }
    }
    override fun onDeleteClick(position: Int,id: Int) {
        lifecycleScope.launch {
            repository.deleteItem(id)
            getBooks()
            cartAdapter.notifyDataSetChanged()
            Toast.makeText(this@CartActivity,"item deleted",Toast.LENGTH_SHORT).show()
        }
    }
}