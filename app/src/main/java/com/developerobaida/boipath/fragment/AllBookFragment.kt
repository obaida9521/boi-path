package com.developerobaida.boipath.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.R
import com.developerobaida.boipath.adapter.BookAdapter
import com.developerobaida.boipath.databinding.FragmentAllBookBinding
import com.developerobaida.boipath.model.BookModel


class AllBookFragment : Fragment() {
    lateinit var binding: FragmentAllBookBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentAllBookBinding.inflate(inflater,container,false)

        return binding.root
    }

}