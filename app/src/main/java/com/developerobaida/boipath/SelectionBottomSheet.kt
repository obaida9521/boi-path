package com.developerobaida.boipath

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.adapter.CategoryAdapter
import com.developerobaida.boipath.api.ApiController
import com.developerobaida.boipath.databinding.BottomSheetLayoutBinding
import com.developerobaida.boipath.model.CategoryModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectionBottomSheet(private val listener: (String) -> Unit) : BottomSheetDialogFragment() {
    lateinit var binding: BottomSheetLayoutBinding
    private val apiService = ApiController.instance?.api

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCategories()


        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedChip = group.findViewById<Chip>(checkedId)
            selectedChip?.let {
                listener(it.text.toString())
                dismiss()
            }
        }
    }

    private fun getCategories() {
        apiService?.getCategories()?.enqueue(object : Callback<List<CategoryModel>> {
            override fun onResponse(
                call: Call<List<CategoryModel>>,
                response: Response<List<CategoryModel>>
            ) {
                if (response.isSuccessful) {
                    val categories = response.body()
                    categories?.let { populateChips(it) }
                } else {
                    Log.e("API_ERROR", "Category Response not successful: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<CategoryModel>>, t: Throwable) {
                Log.e("API_ERROR", "Failed to fetch Category: ${t.message}")
            }
        })
    }

    private fun populateChips(categories: List<CategoryModel>) {
        binding.chipGroup.removeAllViews()

        for (category in categories) {
            val chip = Chip(requireContext()).apply {
                text = category.categoryName
                id = View.generateViewId()

                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                typeface = ResourcesCompat.getFont(requireContext(), R.font.noto_serif)

                chipStrokeColor = ContextCompat.getColorStateList(requireContext(), R.color.darkish)
                chipStrokeWidth = 2f

                isCheckable = true
            }

            binding.chipGroup.addView(chip)
        }
    }
}