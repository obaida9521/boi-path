package com.developerobaida.boipath.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.R
import com.developerobaida.boipath.adapter.SectionsAdapter
import com.developerobaida.boipath.data.repository.DownloadedBookRepository
import com.developerobaida.boipath.databinding.ActivityBookReaderBinding
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.w3c.dom.Element
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class BookReaderActivity : AppCompatActivity() {
    lateinit var binding: ActivityBookReaderBinding
    private var currentPageIndex = 0
    private var totalPages = 0
    private var currentPage = 0
    private var currentTextSize = 20
    private var selectedFont = ""
    private var currentLineHeight = 1.5f
    private var currentLetterSpacing = 1.0f
    private var currentWordSpacing = 5f
    private lateinit var gestureDetector: GestureDetectorCompat
    lateinit var bookRepository: DownloadedBookRepository

    private lateinit var htmlFiles: List<String>
    private lateinit var sectionTitles: List<String>
    private lateinit var targetDir: File

    private var opfDir: String = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        gestureDetector = GestureDetectorCompat(this, MyGestureListener())
        bookRepository = DownloadedBookRepository(this)

        binding.webView.settings.apply {
            allowFileAccess = true
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
        }
        WebView.setWebContentsDebuggingEnabled(true)

        getDetails(intent.getIntExtra("bookId",0))

        binding.options.chapters.setOnClickListener { showSectionsDialog() }
        binding.options.nextPage.setOnClickListener { goToNextPage() }
        binding.options.prevPage.setOnClickListener { goToPreviousPage() }

        binding.options.increaseTextSize.setOnClickListener {
            if (currentTextSize <=40) {
                currentTextSize += 2
                adjustTextSize(currentTextSize)
            }
        }

        binding.options.decreaseTextSize.setOnClickListener {
            if (currentTextSize >= 16 ) {
                currentTextSize -= 2

                adjustTextSize(currentTextSize)
            }
        }
        binding.options.dark.setOnClickListener {
            changeBackgroundColor("#5c3b3b")
            changeTextColor("#ffe8e8")
        }
        binding.options.cyan.setOnClickListener {
            changeBackgroundColor("#ffeeb5")
            changeTextColor("#000000")
        }

        binding.options.alignLeft.setOnClickListener { adjustTextAlignment("left") }
        binding.options.alignCenter.setOnClickListener { adjustTextAlignment("center") }
        binding.options.alignRight.setOnClickListener { adjustTextAlignment("right") }
        binding.options.alignJustify.setOnClickListener { adjustTextAlignment("justify") }
        binding.options.font.setOnClickListener { changeFont("file:///android_asset/fonts/noto_serif.ttf") }
        binding.options.pageIndicator.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                scrollToPage(progress)
                currentPage = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        val currentBrightness: Int = getScreenBrightness()
        binding.options.brightnessIndicator.setProgress(currentBrightness)
        binding.options.brightnessIndicator.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setScreenBrightness(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })


        binding.options.lineHeightSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentLineHeight = 1.0f + (progress / 20f) // Example: Progress 20 -> line-height 2.0
                updateLineHeight(currentLineHeight)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.options.letterSpacingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentLetterSpacing = progress.toFloat() // Example: Progress 5 -> letter-spacing 5px
                updateLetterSpacing(currentLetterSpacing)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.options.wordSpacingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentWordSpacing = progress.toFloat()
                updateWordSpacing(currentWordSpacing)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.webView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun getDetails(id: Int){
        lifecycleScope.launch {
            val booItems = bookRepository.getBookById(id)
            booItems.let {
                setEpub("book_${booItems.bookId}.epub")
                binding.toolbar.title = booItems.bookName
                binding.toolbar.subtitle = booItems.author

                loadCurrentPage()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffY = e2.y - (e1?.y ?: 0f)
            val diffX = e2.x - (e1?.x ?: 0f)
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // Swipe right (previous page)
                        goToPreviousPage()
                    } else {
                        // Swipe left (next page)
                        goToNextPage()
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun calculatePagination() {
        val js = """
        (function() {
            const totalWidth = document.body.scrollWidth;
            const viewWidth = window.innerWidth;
            const totalPages = Math.ceil(totalWidth / viewWidth);
            window.__pagination = {
                totalPages: totalPages,
                pageWidth: viewWidth
            };
            return totalPages;
        })();
    """.trimIndent()

        binding.webView.evaluateJavascript(js) { result ->
            try {
                val clean = result.replace("\"", "").trim()
                totalPages = clean.toInt()
                currentPage = 0
                binding.options.pageIndicator.max = totalPages - 1
                binding.options.pageTv.text = "1 / $totalPages (${sectionTitles.getOrNull(currentPageIndex) ?: ""})"
            } catch (e: Exception) {
                Log.e("EpubReader", "Pagination parse error", e)
            }
        }
    }

    private fun scrollToPage(page: Int) {
        val js = """
        (function() {
            if (window.__pagination) {
                let x = window.__pagination.pageWidth * $page;
                window.scrollTo({ left: x, behavior: 'smooth' });
            }
        })();
    """.trimIndent()

        binding.options.pageTv.text = "${page + 1} / $totalPages"
        binding.webView.evaluateJavascript(js, null)
    }

    private fun setEpub(epubFileName: String){

        val epubFile = File(getExternalFilesDir(null), epubFileName)
        if (!epubFile.exists()) Toast.makeText(this,"file not found!",Toast.LENGTH_SHORT).show()

        targetDir = File(filesDir, "extracted")
        if (!targetDir.exists()) targetDir.mkdirs()
        try {
            unzipEpub(epubFile, targetDir)
        } catch (e: IOException) {
            Log.e("EpubReader", "Error unzipping EPUB file", e)
            return
        }
        val containerFile = File(targetDir, "META-INF/container.xml")
        val opfPath = try {
            parseContainerXml(containerFile)
        } catch (e: Exception) {
            Log.e("EpubReader", "Error parsing container.xml", e)
            return
        }
        val opfFile = File(targetDir, opfPath)
        opfDir = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") else ""
        Log.d("EpubReader", "Fixed OPF Directory: $opfDir")

        /*htmlFiles = try {
            parseOpf(opfFile)
        } catch (e: Exception) {
            Log.e("EpubReader", "Error parsing OPF file", e)
            return
        }*/


        //--------------------------------
        val result = try {
            parseOpf(opfFile)
        } catch (e: Exception) {
            Log.e("EpubReader", "Error parsing OPF file", e)
            return
        }

        htmlFiles = result.first
        sectionTitles = result.second
        //--------------------------------


        if (htmlFiles.isEmpty()) {
            Log.e("EpubReader", "No HTML files found in OPF")
            return
        }
    }

    private fun loadCurrentPage() {
        if (currentPageIndex in htmlFiles.indices) {
            val htmlPath = if (opfDir.isNotEmpty()) "$opfDir/${htmlFiles[currentPageIndex]}" else htmlFiles[currentPageIndex]
            val htmlFile = File(targetDir, htmlPath)
            Log.d("EpubReader", "Final HTML Path: ${htmlFile.absolutePath}")
            if (htmlFile.exists()) {
                binding.webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        calculatePagination()
                        disableScrolling()
                    }
                }
                loadHtmlIntoWebView(binding.webView, htmlFile)
            } else {
                Log.e("EpubReader", "HTML file does not exist: ${htmlFile.absolutePath}")
            }
        }
    }

    private fun unzipEpub(epubFile: File, targetDir: File) {
        val zipFile = ZipFile(epubFile)
        zipFile.entries().asSequence().forEach { entry ->
            val outFile = File(targetDir, entry.name)

            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                try {
                    outFile.parentFile?.mkdirs()
                    outFile.outputStream().use { outputStream ->
                        zipFile.getInputStream(entry).copyTo(outputStream)
                    }
                } catch (e: IOException) {
                    Log.e("EpubReader", "Error extracting file: ${entry.name}", e)
                }
            }
        }
        zipFile.close()
    }

    private fun parseContainerXml(containerFile: File): String {
        try {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(containerFile)
            val root = document.documentElement
            val opfPath = root.getElementsByTagName("rootfile").item(0).attributes.getNamedItem("full-path").nodeValue
            Log.d("EpubReader", "Extracted OPF Path: $opfPath")
            return opfPath
        } catch (e: Exception) {
            Log.e("EpubReader", "Error parsing container.xml", e)
            throw e
        }
    }

    private fun parseOpf(opfFile: File): Pair<List<String>, List<String>> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(opfFile)
        val manifest = document.getElementsByTagName("manifest").item(0) as Element
        val spine = document.getElementsByTagName("spine").item(0) as Element
        val htmlFiles = mutableListOf<String>()

        val manifestItems = manifest.getElementsByTagName("item")
        val spineItems = spine.getElementsByTagName("itemref")

        val idHrefMap = mutableMapOf<String, String>()

        val idTitleMap = mutableMapOf<String, String>()
        val sectionTitles = mutableListOf<String>()

        for (i in 0 until manifestItems.length) {
            val item = manifestItems.item(i) as Element
            val id = item.getAttribute("id")
            val href = item.getAttribute("href")
            idHrefMap[id] = href

            //---- Try to get title from properties or fallback to filename
            val title = item.getAttribute("title") ?:
            item.getAttribute("id") ?:
            href.substringBeforeLast('.').substringAfterLast('/')
            idTitleMap[href] = title

        }

        /*for (i in 0 until spineItems.length) {
            val itemref = spineItems.item(i) as Element
            val idref = itemref.getAttribute("idref")
            idHrefMap[idref]?.let { htmlFiles.add(it) }
        }*/

        // Second pass: process spine items in order
        for (i in 0 until spineItems.length) {
            val itemref = spineItems.item(i) as Element
            val idref = itemref.getAttribute("idref")
            idHrefMap[idref]?.let {
                htmlFiles.add(it)
                sectionTitles.add(idTitleMap[idref] ?: "Section ${i+1}")
            }
        }

        Log.d("EpubReader", "Parsed HTML Files: $htmlFiles")
        Log.d("EpubReader", "Section Titles: $sectionTitles")
        return Pair(htmlFiles, sectionTitles)
        //return htmlFiles
    }


    private fun loadHtmlIntoWebView(webView: WebView, htmlFile: File) {
        try {
            val htmlContent = htmlFile.readText()
            val viewportMeta = """
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        """

            val customStyle = """
            <style>
            @font-face {
                font-family: 'CustomFont';
                src: url('file:///android_asset/fonts/');
            }
            
            html, body {
                margin: 0;
                padding: 0;
                height: 100vh;
                width: 100vw;
               
                
                overflow-x: auto;
    overflow-y: hidden;
    overscroll-behavior-x: contain;
    scroll-behavior: smooth;
            }

            body {
                -webkit-column-width: 100vw;
                -webkit-column-gap: 0;
                column-width: 100vw;
                column-gap: 20px;
                height: 100vh;
                font-size: ${currentTextSize}px;
                line-height: ${currentLineHeight};
                font-family: 'CustomFont', sans-serif;
                
                scroll-snap-type: x mandatory;
                scroll-behavior: smooth;
                -webkit-overflow-scrolling: touch;
            }
            
            .page {
                scroll-snap-align: start;
            }

            p, div, span, h1, h2, h3, h4, h5, h6 {
                font-size: ${currentTextSize}px;
                line-height: ${currentLineHeight};
                font-family: 'CustomFont', sans-serif;
            }

            img, table {
                max-width: 100%;
                height: auto;
                break-inside: avoid;
                page-break-inside: avoid;
                -webkit-column-break-inside: avoid;
            }
            </style>
            <script>
            document.addEventListener("DOMContentLoaded", function() {
                document.body.style.fontSize = '${currentTextSize}px';
                document.querySelectorAll('p, div, span, h1, h2, h3, h4, h5, h6').forEach(function(element) {
                    element.style.fontSize = '${currentTextSize}px';
                    element.style.fontFamily = 'CustomFont, sans-serif';
                });
            });
        </script>
        """.trimIndent()

            val headInsert = "$viewportMeta\n$customStyle"
            val modifiedHtmlContent = htmlContent.replace("</head>", "$headInsert</head>")

            val baseUrl = "file://${htmlFile.parent}/"
            webView.loadDataWithBaseURL(baseUrl, modifiedHtmlContent, "text/html", "UTF-8", null)
        } catch (e: IOException) {
            Log.e("EpubReader", "Error reading HTML file", e)
        }
    }

    private fun changeBackgroundColor(color: String) {
        val js = """
        (function() {
            document.body.style.backgroundColor = '$color';
        })();
    """.trimIndent()

        binding.webView.evaluateJavascript(js, null)
    }
    private fun changeTextColor(color: String) {
        val js = """
        (function() {
            document.body.style.color = '$color';
            document.querySelectorAll('p, div, span, h1, h2, h3, h4, h5, h6').forEach(function(element) {
                element.style.color = '$color';
            });
        })();
    """.trimIndent()

        binding.webView.evaluateJavascript(js, null)
    }
    private fun changeFont(fontPath: String) {
        val js = """
        (function() {
            var style = document.createElement('style');
            style.type = 'text/css';
            var css = `
                @font-face {
                    font-family: 'CustomFont';
                    src: url('${fontPath}');
                }
                body, p, div, span, h1, h2, h3, h4, h5, h6 {
                    font-family: 'CustomFont', sans-serif !important;
                }
            `;
            style.appendChild(document.createTextNode(css));
            document.head.appendChild(style);
        })();
    """.trimIndent()

        binding.webView.evaluateJavascript(js, null)
    }
    private fun adjustTextAlignment(alignment: String) {
        val js = """
            document.body.style.textAlign = '$alignment';
            document.querySelectorAll('p, div, span, h1, h2, h3, h4, h5, h6').forEach(function(element) {
                element.style.textAlign = '$alignment';
            });
        """.trimIndent()
        binding.webView.evaluateJavascript(js, null)
    }
    private fun adjustTextSize(textSize: Int) {
        val js = """
        document.body.style.fontSize = '${textSize}px';
        document.querySelectorAll('p, div, span, h1, h2, h3, h4, h5, h6').forEach(function(element) {
            element.style.fontSize = '${textSize}px';
        });
    """.trimIndent()
        binding.webView.evaluateJavascript(js, null)
        calculatePagination()
    }
    private fun goToNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++
            scrollToPage(currentPage)
            binding.options.pageIndicator.progress = currentPage
        } else {
            if (currentPageIndex < htmlFiles.size - 1) {
                currentPageIndex++
                loadCurrentPage()
            } else {
                Toast.makeText(this, "End of book", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun goToPreviousPage() {
        if (currentPage > 0) {
            currentPage--
            scrollToPage(currentPage)
            binding.options.pageIndicator.progress = currentPage
        } else {
            if (currentPageIndex > 0) {
                currentPageIndex--
                loadCurrentPage()
            } else {
                Toast.makeText(this, "Already at first page", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun disableScrolling() {
        /*
         document.body.style.overflow = 'hidden';
            document.documentElement.style.overflow = 'hidden';*/
        val js = """
        (function() {
           
           
           
           document.body.style.overflowX = 'auto';
            document.body.style.overflowY = 'hidden';
            document.documentElement.style.overflowX = 'auto';
            document.documentElement.style.overflowY = 'hidden';
        })();
    """.trimIndent()
        binding.webView.evaluateJavascript(js, null)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId== R.id.brightness){
            Toast.makeText(this,"clicked",Toast.LENGTH_SHORT).show()
        }
        return true
    }
    private fun getScreenBrightness(): Int {
        try {
            return Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            return 125
        }
    }
    private fun setScreenBrightness(brightness: Int) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness / 255.0f
        window.attributes = layoutParams
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.reader_menu, menu)
        return true
    }
    private fun updateLineHeight(lineHeight: Float) {
        val js = """
        (function() {
            document.body.style.lineHeight = '$lineHeight';
            document.querySelectorAll('p, div, span, h1, h2, h3, h4, h5, h6').forEach(function(element) {
                element.style.lineHeight = '$lineHeight';
            });
        })();
    """.trimIndent()
        binding.webView.evaluateJavascript(js, null)
        calculatePagination()
    }

    private fun updateLetterSpacing(letterSpacing: Float) {
        val js = """
        (function() {
            document.body.style.letterSpacing = '${letterSpacing}px';
            document.querySelectorAll('p, div, span, h1, h2, h3, h4, h5, h6').forEach(function(element) {
                element.style.letterSpacing = '${letterSpacing}px';
            });
        })();
    """.trimIndent()

        binding.webView.evaluateJavascript(js, null)
        calculatePagination()
    }
    private fun updateWordSpacing(wordSpacing: Float) {
        val js = """
        (function() {
            document.body.style.wordSpacing = '${wordSpacing}px';
            document.querySelectorAll('p, div, span, h1, h2, h3, h4, h5, h6').forEach(function(element) {
                element.style.wordSpacing = '${wordSpacing}px';
            });
        })();
    """.trimIndent()

        binding.webView.evaluateJavascript(js, null)
        calculatePagination()
    }

    private fun showSectionsDialog() {
        if (!this::sectionTitles.isInitialized || sectionTitles.isEmpty()) return

        val dialog = AlertDialog.Builder(this)
            .setTitle("Table of Contents")
            .setItems(sectionTitles.toTypedArray()) { _, which ->
                currentPageIndex = which
                loadCurrentPage()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}

/*private fun calculatePagination() {
        val js = """
        (function() {
            let contentHeight = document.body.scrollHeight;
            let viewportHeight = window.innerHeight;
            let totalPages = Math.ceil(contentHeight / viewportHeight);
            return totalPages;
        })();
    """.trimIndent()

        binding.webView.evaluateJavascript(js) { result ->
            try {
                totalPages = result.toInt()
                currentPage = 0
                binding.options.pageIndicator.max = (totalPages-1)
                binding.options.pageTv.text = "1 / $totalPages ($sectionTitles)"
                Log.d("EpubReader", "Total Pages: $totalPages")
            } catch (e: Exception) {
                Log.e("EpubReader", "Error parsing page count", e)
            }
        }
    }*/

/*private fun loadHtmlIntoWebView(webView: WebView, htmlFile: File, assetsDir: File) {
        try {
            val htmlContent = htmlFile.readText()

            val fixedWidthCSS = """
        <style>
        @font-face {
                font-family: 'CustomFont';
                src: url('file:///android_asset/fonts/');
            }

            body {
                width: 100vw;
                overflow-x: hidden;
                margin: 0;
                padding: 0px;
                font-size: ${currentTextSize}px;
                font-family: 'CustomFont', sans-serif;
            }
            img, svg, table, div {
                max-width: 100%;
                height: auto;
            }
            p, div, span, h1, h2, h3, h4, h5, h6 {
                text-align: left;
                font-size: ${currentTextSize}px;
                font-family: 'CustomFont', sans-serif;
            }
        </style>
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                document.body.style.fontSize = '${currentTextSize}px';
                document.querySelectorAll('p, div, span, h1, h2, h3, h4, h5, h6').forEach(function(element) {
                    element.style.fontSize = '${currentTextSize}px';
                    element.style.fontFamily = 'CustomFont, sans-serif';
                });
            });
        </script>
        """.trimIndent()

            val modifiedHtmlContent = htmlContent.replace("</head>", "$fixedWidthCSS</head>")

            val baseUrl = "file://${htmlFile.parent}/"
            webView.loadDataWithBaseURL(baseUrl, modifiedHtmlContent, "text/html", "UTF-8", null)
        } catch (e: IOException) {
            Log.e("EpubReader", "Error reading HTML file", e)
        }
    }*/