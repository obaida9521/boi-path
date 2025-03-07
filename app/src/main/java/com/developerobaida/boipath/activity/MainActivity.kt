package com.developerobaida.boipath.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.developerobaida.boipath.R
import com.developerobaida.boipath.databinding.ActivityMainBinding
import com.developerobaida.boipath.fragment.BestSellFragment
import com.developerobaida.boipath.fragment.CartFragment
import com.developerobaida.boipath.fragment.HomeFragment
import com.developerobaida.boipath.fragment.LibraryFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(HomeFragment())

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> loadFragment(HomeFragment())
                R.id.bestSell -> loadFragment(BestSellFragment())

                R.id.library -> loadFragment(LibraryFragment())
            }
            true
        }

        binding.profile.setOnClickListener {
            startActivity(Intent(this,ProfileActivity::class.java))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame, fragment).commit()
    }
}