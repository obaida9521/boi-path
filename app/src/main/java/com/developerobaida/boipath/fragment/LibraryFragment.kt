package com.developerobaida.boipath.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.SelectionBottomSheet
import com.developerobaida.boipath.adapter.LibraryBookAdapter
import com.developerobaida.boipath.data.repository.DownloadedBookRepository
import com.developerobaida.boipath.databinding.FragmentLibraryBinding
import kotlinx.coroutines.launch


class LibraryFragment : Fragment() {
    lateinit var binding: FragmentLibraryBinding
    lateinit var repository: DownloadedBookRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLibraryBinding.inflate(inflater,container,false)
        repository = context?.let { DownloadedBookRepository(it) }!!
        getBooks()

        binding.card.setOnClickListener {
            binding.card.setOnClickListener {
                val bottomSheet = SelectionBottomSheet { selectedText ->
                    binding.tvCat.text = selectedText
                    //getBooksByCat(selectedText)
                }
                bottomSheet.show(childFragmentManager, "SelectionBottomSheet")
            }
        }

        return binding.root
    }

    private fun getBooks(){
        binding.rec.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch {
            val books = repository.getAllBooks()
            var adapter = LibraryBookAdapter(books)
            binding.rec.adapter = adapter
            binding.rec.layoutManager = LinearLayoutManager(context)
            binding.rec.hasFixedSize()
        }
    }

}