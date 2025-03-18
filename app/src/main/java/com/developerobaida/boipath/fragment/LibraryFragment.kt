package com.developerobaida.boipath.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.adapter.LibraryBookAdapter
import com.developerobaida.boipath.databinding.FragmentLibraryBinding
import com.developerobaida.boipath.model.BookModel


class LibraryFragment : Fragment() {
    lateinit var binding: FragmentLibraryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLibraryBinding.inflate(inflater,container,false)
        val list = listOf(

            BookModel(0,"","","","",0,0,0.0,"",0,"",""),
            BookModel(0,"","","","",0,0,0.0,"",0,"",""),
            BookModel(0,"","","","",0,0,0.0,"",0,"",""),
            BookModel(0,"","","","",0,0,0.0,"",0,"",""),
            BookModel(0,"","","","",0,0,0.0,"",0,"",""),
            BookModel(0,"","","","",0,0,0.0,"",0,"",""),
            BookModel(0,"","","","",0,0,0.0,"",0,"",""),
            BookModel(0,"","","","",0,0,0.0,"",0,"",""),
            BookModel(0,"","","","",0,0,0.0,"",0,"","")
        )
        var adapter = LibraryBookAdapter(list)
        binding.rec.adapter = adapter
        binding.rec.layoutManager = LinearLayoutManager(context)
        binding.rec.hasFixedSize()


        return binding.root
    }



}