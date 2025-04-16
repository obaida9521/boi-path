package com.developerobaida.boipath.utils

import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.developerobaida.boipath.R
import com.developerobaida.boipath.databinding.EditOptionsBinding
import com.developerobaida.boipath.interfaces.ReaderOptionsListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CustomBottomSheet(private val listener: ReaderOptionsListener,private val totalPages: Int,
                        private val currentPage: Int) : BottomSheetDialogFragment() {

    lateinit var binding: EditOptionsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EditOptionsBinding.inflate(inflater,container,false)
        //val view = inflater.inflate(R.layout.edit_options, container, false)

        var currentTextSize = 20

        fontSelection()
        /*binding.pageTv.text = "${currentPage + 1} / $totalPages"
        binding.pageIndicator.max = totalPages
        binding.pageIndicator.progress = currentPage
        binding.pageIndicator.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                listener.scrollToPages(p1)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })*/

        binding.increaseText.setOnClickListener {
            if (currentTextSize <= 40) {
                currentTextSize += 2
                listener.adjustTextSizes(currentTextSize)
            }
        }

        binding.decreaseText.setOnClickListener {
            if (currentTextSize >= 16) {
                currentTextSize -= 2
                listener.adjustTextSizes(currentTextSize)
            }
        }

        /*binding.nextPage.setOnClickListener {
            listener.goToNextPages()

            binding.pageIndicator.max = totalPages
            binding.pageIndicator.progress = currentPage
        }

        binding.prevPage.setOnClickListener {
            listener.goToPreviousPages()

            binding.pageIndicator.max = totalPages
            binding.pageIndicator.progress = currentPage
        }*/

        binding.dark.setOnClickListener {
            listener.changeBackgroundColors("#5c3b3b", "#ffe8e8")
        }

        binding.cyan.setOnClickListener {
            listener.changeBackgroundColors("#ffeeb5", "#000000")
        }

        binding.alignGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.left -> listener.adjustTextAlignments("left")
                R.id.center -> listener.adjustTextAlignments("center")
                R.id.right -> listener.adjustTextAlignments("right")
                R.id.justify -> listener.adjustTextAlignments("justify")
            }
        }

        val currentBrightness: Int = getScreenBrightness()
        binding.brightnessIndicator.progress = currentBrightness
        binding.brightnessIndicator.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                listener.adjustBrightnesses(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.lineHeightSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val lineHeight = 1.0f + (progress / 20f)
                listener.updateLineHeights(lineHeight)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.letterSpacingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                listener.updateLetterSpacings(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.wordSpacingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                listener.updateWordSpacings(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


        return binding.root
    }

    fun fontSelection(){
        val fonts = context?.assets?.list("fonts")?.filter {
            it.endsWith(".ttf") || it.endsWith(".otf")
        } ?: emptyList()

        fonts.forEach {
            val newRadioButton = RadioButton(context).apply {
                id = View.generateViewId()
                tag = it
                text = "আমার সোনার বাংলা"
                textSize = 20F
                gravity = Gravity.CENTER
                setPadding(15,10,15,10)
                typeface = Typeface.createFromAsset(context.assets, "fonts/${it}")
                background = ContextCompat.getDrawable(context, R.drawable.radio_button_selector)
                buttonDrawable = ContextCompat.getDrawable(context, android.R.color.transparent)
            }

            val params = RadioGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    10,5,10,5
                )
            }

            newRadioButton.layoutParams = params

            binding.fontGroup.addView(newRadioButton)
        }

        binding.fontGroup.setOnCheckedChangeListener { group, i ->
            val selectedRadioButton = group.findViewById<RadioButton>(i)
            val selectedFont = selectedRadioButton.tag as String
            listener.changeFonts("file:///android_asset/fonts/$selectedFont")
        }
    }

    private fun getScreenBrightness(): Int {
        try {
            return Settings.System.getInt(context?.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            return 125
        }
    }
}