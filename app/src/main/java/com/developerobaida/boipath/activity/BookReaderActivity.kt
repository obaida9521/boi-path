package com.developerobaida.boipath.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.developerobaida.boipath.R
import com.developerobaida.boipath.data.local.database.AppDatabase
import com.developerobaida.boipath.data.local.entity.AnnotationEntity
import com.developerobaida.boipath.data.repository.AnnotationRepository
import com.developerobaida.boipath.data.repository.DownloadedBookRepository
import com.developerobaida.boipath.databinding.ActivityBookReaderBinding
import com.developerobaida.boipath.interfaces.ReaderOptionsListener
import com.developerobaida.boipath.utils.CustomBottomSheet
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class BookReaderActivity : AppCompatActivity(), ReaderOptionsListener {
    lateinit var binding: ActivityBookReaderBinding
    private var currentPageIndex = 0
    private var totalPages = 0
    private var currentPage = 0
    private var currentTextSize = 20
    private var selectedFont = ""
    private var currentLineHeight = 1.5f
    private var currentLetterSpacing = 1.0f
    private var currentWordSpacing = 5f
    private var currentTextPaddingLeft = 20
    private var currentTextPaddingRight = 20
    private var currentTextPaddingTop = 20
    private var currentTextPaddingBottom = 50


    private lateinit var gestureDetector: GestureDetectorCompat
    lateinit var bookRepository: DownloadedBookRepository
    lateinit var annotationRepository: AnnotationRepository

    private var isToolbarVisible = false
    private var isIndicatorVisible = false
    private var currentBookId =0

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

        val annotationDao = AppDatabase.getDatabase(this).annotationDao()
        annotationRepository = AnnotationRepository(annotationDao)

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
            binding.webView.overScrollMode = WebView.OVER_SCROLL_NEVER
        WebView.setWebContentsDebuggingEnabled(true)

        currentBookId = intent.getIntExtra("bookId",0)

        getDetails(currentBookId)

//        binding.options.chapters.setOnClickListener { showSectionsDialog() }
//        binding.options.pageIndicator.setOnSeekBarChangeListener(object :
//            SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                scrollToPage(progress)
//                currentPage = progress
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {}
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {}
//        })

        binding.webView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        binding.webView.addJavascriptInterface(TextSelectionInterface(this), "TextHandler")



        binding.nextPage.setOnClickListener {
            if (currentPage < totalPages - 1) {
                currentPage++
                scrollToPageViaJS(currentPage)
            } else {
                Toast.makeText(this, "End of section", Toast.LENGTH_SHORT).show()
            }
        }

        binding.prevPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                scrollToPageViaJS(currentPage)
            } else {
                Toast.makeText(this, "Already at first page", Toast.LENGTH_SHORT).show()
            }
        }

        binding.pageIndicator.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                currentPage = progress
                scrollToPageViaJS(currentPage)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

    }

    fun showBottomSheet() {
        val bottomSheet = CustomBottomSheet(this,totalPages, currentPage)
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }

    override fun goToNextPages() {
        goToNextPage()
    }
    override fun goToPreviousPages() = goToPreviousPage()
    override fun adjustTextSizes(size: Int) {
        currentTextSize = size
        adjustTextSize(currentTextSize)
    }
    override fun adjustTextAlignments(alignment: String) = adjustTextAlignment(alignment)
    override fun changeBackgroundColors(bgColor: String, textColor: String) {
        changeBackgroundColor(bgColor)
        changeTextColor(textColor)
    }
    override fun adjustBrightnesses(brightness: Int) = setScreenBrightness(brightness)
    override fun updateLineHeights(lineHeight: Float) = updateLineHeight(lineHeight)
    override fun updateLetterSpacings(letterSpacing: Float) = updateLetterSpacing(letterSpacing)
    override fun updateWordSpacings(wordSpacing: Float) = updateWordSpacing(wordSpacing)
    override fun changeFonts(fontPath: String) = changeFont(fontPath)
    override fun scrollToPages(page: Int) {
        currentPage = page
        scrollToPage(page)
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

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            toggleToolbar()
            toggleIndicator()
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            return super.onDoubleTapEvent(e)
        }

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

                        if (currentPage > 0) {
                            currentPage--
                            scrollToPageViaJS(currentPage)
                        } else {
                            if (currentPageIndex > 0) {
                                currentPageIndex--
                                loadCurrentPage()
                            } else {
                                Toast.makeText(this@BookReaderActivity, "Already at first page", Toast.LENGTH_SHORT).show()
                            }
                        }


                        //goToPreviousPage()
                    } else {

                        if (currentPage < totalPages - 1) {
                            currentPage++
                            scrollToPageViaJS(currentPage)
                        } else {
                            if (currentPageIndex < htmlFiles.size - 1) {
                                currentPageIndex++
                                loadCurrentPage()
                            } else {
                                Toast.makeText(this@BookReaderActivity, "End of book", Toast.LENGTH_SHORT).show()
                            }
                        }
                        //goToNextPage()
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
            const totalWidth = Math.max(document.body.scrollWidth, document.documentElement.scrollWidth);
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
                totalPages = result.toInt()
                currentPage = 0
                Toast.makeText(this,"$result ",Toast.LENGTH_SHORT).show()
                // binding.options.pageIndicator.max = totalPages - 1
                //binding.options.pageTv.text = "1 / $totalPages (${sectionTitles.getOrNull(currentPageIndex) ?: ""})"
            } catch (e: Exception) {
                Log.e("EpubReader", "Pagination parse error", e)
            }
        }
    }

    private fun scrollToPage(page: Int) {
        val js = """
    (function() {
        const pageWidth = window.innerWidth;
        window.scrollTo({
            left: pageWidth * $page,
            behavior: 'smooth'
        });
    })();
    """.trimIndent()

        binding.webView.evaluateJavascript(js, null)
        currentPage = page
    }

    fun scrollToPageViaJS(page: Int) {
        currentPage = page
        val js = """
        (function() {
            if (window.__pagination) {
                const pageWidth = window.__pagination.pageWidth;
                const scrollX = pageWidth * $page;
                document.documentElement.scrollLeft = scrollX;
                document.body.scrollLeft = scrollX;
            }
        })();
    """.trimIndent()

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

                        val js = """
        (function() {
            requestAnimationFrame(() => {
                setTimeout(() => {
                    const totalWidth = Math.max(document.body.scrollWidth, document.documentElement.scrollWidth);
                    const viewWidth = window.innerWidth;
                    const totalPages = Math.ceil(totalWidth / viewWidth);
                    window.__pagination = {
                        totalPages: totalPages,
                        pageWidth: viewWidth
                    };
                    if (typeof TextHandler !== "undefined" && TextHandler.onPaginationReady) {
                        TextHandler.onPaginationReady(totalPages.toString());
                    }
                }, 200); // ~1 frame delay
            });
        })();
    """.trimIndent()

                        binding.webView.evaluateJavascript(js, null)

                        disableScrolling()

                        restoreAnnotationsForCurrentPage()
                        injectTextSelectionListener()
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
                
                scroll-behavior: smooth;
        -webkit-overflow-scrolling: hidden;
         scroll-snap-type: x mandatory;
         touch-action: none;
               
            }

            body {
            overflow-x: scroll;
            overflow-y: hidden;
                -webkit-column-width: 100vw;
                -webkit-column-gap: 0;
                column-width: 100vw;
                column-gap: 0;
                column-padding: 0;
                height: 100vh;
                font-size: ${currentTextSize}px;
                line-height: ${currentLineHeight};
                font-family: 'CustomFont', sans-serif;
                
                padding-top: ${currentTextPaddingTop}px;
                padding-bottom: ${currentTextPaddingBottom}px;
            }
            
            
            .page {
              display: inline-block;
              vertical-align: top;
              width: 100vw;
              height: 100vh;
              box-sizing: border-box;
              padding: 20px;
            }
            
            
            
            * {
              user-select: none;
              -webkit-user-select: none;
            }
            
            ::selection {
               background-color: #8b1bf5; /* Highlight color */
               color: white;              /* Text color when selected */
            }
    

            p, div, span, h1, h2, h3, h4, h5, h6 {
                font-size: ${currentTextSize}px;
                line-height: ${currentLineHeight};
                font-family: 'CustomFont', sans-serif;
                
                user-select: text;
                -webkit-user-select: text;
                
                 
            }
            
             p, div, h1, h2, h3, h4, h5, h6 {
                 padding-left: ${currentTextPaddingLeft}px;
                 padding-right: ${currentTextPaddingRight}px;
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
                    //element.style.fontFamily = 'CustomFont, sans-serif';
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



    private fun disableScrolling() {
        val js = """
        (function() {
           
           document.body.style.overflowX = 'scroll';
            document.documentElement.style.overflowX = 'scroll';
            
            document.body.style.overflowY = 'hidden';
            document.documentElement.style.overflowY = 'hidden';
          
        })();
    """.trimIndent()
        binding.webView.evaluateJavascript(js, null)
    }

    private fun goToNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++
            scrollToPage(currentPage)
            //binding.options.pageIndicator.progress = currentPage
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
            //binding.options.pageIndicator.progress = currentPage
        } else {
            if (currentPageIndex > 0) {
                currentPageIndex--
                loadCurrentPage()
            } else {
                Toast.makeText(this, "Already at first page", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class TextSelectionInterface(private val context: Context) {

        @JavascriptInterface
        fun onTextSelected(data: String) {
            (context as? Activity)?.runOnUiThread {
                val parts = data.split("||")
                if (parts.size == 2) {
                    val text = parts[0]
                    val coords = parts[1].split(",").map { it.toFloat() }
                    if (coords.size == 4) {
                        val x = coords[0]
                        val y = coords[1]
                        val width = coords[2]
                        val height = coords[3]
                        (context as BookReaderActivity).showTextActionPopup(text, x, y, width, height)
                    }
                }
            }
        }


        @JavascriptInterface
        fun onPaginationReady(pages: String) {
            (context as? Activity)?.runOnUiThread {
                try {
                    val pageCount = pages.toInt()
                    (context as? BookReaderActivity)?.onPaginationCalculated(pageCount)
                } catch (e: Exception) {
                    Log.e("EpubReader", "Pagination parse error", e)
                }
            }
        }

        @JavascriptInterface
        fun goToPage(pageIndex: Int) {
            (context as? Activity)?.runOnUiThread {
                (context as? BookReaderActivity)?.scrollToPageViaJS(pageIndex)
            }
        }
    }

    fun onPaginationCalculated(pageCount: Int) {
        totalPages = pageCount
        currentPage = 0
        Log.d("Pagination", "Total pages = $totalPages")
        Toast.makeText(this, "Total pages: $totalPages", Toast.LENGTH_SHORT).show()

         binding.pageIndicator.max = totalPages - 1
         binding.pageTv.text = "1 / $totalPages"
    }

    fun showTextActionPopup(
        selectedText: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val overlay = findViewById<View>(R.id.overlayTextActions)
        val etNote = findViewById<EditText>(R.id.etNote)
        var selectedColor = "#FFFF00"

        etNote.visibility = View.GONE
        overlay.visibility = View.VISIBLE

        // Convert WebView coordinates to screen coordinates
        val location = IntArray(2)
        binding.webView.getLocationOnScreen(location)
        val webViewX = location[0]
        val webViewY = location[1]

        val overlayX = webViewX + x
        val overlayY = webViewY + y - overlay.height - 20

        overlay.translationX = overlayX
        overlay.translationY = overlayY

        // Action button listeners
        findViewById<Button>(R.id.btnBookmark).setOnClickListener {
            saveBookmark(selectedText)
            overlay.visibility = View.GONE
        }

        findViewById<Button>(R.id.btnHighlight).setOnClickListener {
            saveHighlight(selectedText, selectedColor)
            overlay.visibility = View.GONE
        }

        findViewById<Button>(R.id.btnNote).setOnClickListener {
            if (etNote.visibility == View.VISIBLE) {
                saveNote(selectedText, etNote.text.toString(), selectedColor)
                overlay.visibility = View.GONE
            } else {
                etNote.visibility = View.VISIBLE
            }
        }

        val colorPicker = findViewById<LinearLayout>(R.id.colorPicker)
        for (i in 0 until colorPicker.childCount) {
            val colorBtn = colorPicker.getChildAt(i)
            colorBtn.setOnClickListener {
                selectedColor = colorBtn.tag.toString()
            }
        }
    }


    fun saveBookmark(text: String) {
        lifecycleScope.launch {
            annotationRepository.insert(
                AnnotationEntity(
                    bookId = currentBookId, // you already use this
                    fileName = htmlFiles.getOrNull(currentPageIndex) ?: "",
                    pageIndex = currentPage,
                    type = "bookmark",
                    selectedText = text
                )
            )
            Toast.makeText(this@BookReaderActivity, "Bookmarked!", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveHighlight(text: String, color: String) {
        lifecycleScope.launch {
            annotationRepository.insert(
                AnnotationEntity(
                    bookId = currentBookId,
                    fileName = htmlFiles.getOrNull(currentPageIndex) ?: "",
                    pageIndex = currentPage,
                    type = "highlight",
                    selectedText = text,
                    highlightColor = color
                )
            )
            restoreAnnotationsForCurrentPage()
        }
    }

    fun saveNote(text: String, note: String, color: String) {
        lifecycleScope.launch {
            annotationRepository.insert(
                AnnotationEntity(
                    bookId = currentBookId,
                    fileName = htmlFiles.getOrNull(currentPageIndex) ?: "",
                    pageIndex = currentPage,
                    type = "note",
                    selectedText = text,
                    noteText = note,
                    highlightColor = color
                )
            )
        }
    }

    private fun injectTextSelectionListener() {
        val js = """
        (function() {
            document.addEventListener("selectionchange", function() {
                var selection = window.getSelection();
                var selectedText = selection.toString().trim();
                if (selectedText.length > 0) {
                    var range = selection.getRangeAt(0);
                    var rect = range.getBoundingClientRect();
                    if (typeof TextHandler !== "undefined" && TextHandler.onTextSelected) {
                        TextHandler.onTextSelected(selectedText + "||" + rect.left + "," + rect.top + "," + rect.width + "," + rect.height);
                    }
                }
            });
        })();
    """.trimIndent()

        binding.webView.evaluateJavascript(js, null)
    }


    private fun restoreAnnotationsForCurrentPage() {
        lifecycleScope.launch {
            val fileName = htmlFiles.getOrNull(currentPageIndex) ?: return@launch
            val annotations = annotationRepository.getAnnotations(currentBookId)
                .filter { it.fileName == fileName && it.pageIndex == currentPage }

            annotations.forEach {
                injectHighlightSpan(it.selectedText, it.highlightColor ?: "#FFFF00")
            }
        }
    }

    fun injectHighlightSpan(selectedText: String, color: String) {
        val js = """
        (function() {
            var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
            while(walker.nextNode()) {
                var node = walker.currentNode;
                if(node.nodeValue.includes("$selectedText")) {
                    var span = document.createElement("span");
                    span.style.backgroundColor = "$color";
                    span.className = "highlighted-text";
                    var text = node.nodeValue;
                    var index = text.indexOf("$selectedText");
                    if(index !== -1) {
                        var before = document.createTextNode(text.slice(0, index));
                        var middle = document.createTextNode("$selectedText");
                        var after = document.createTextNode(text.slice(index + "$selectedText".length));
                        span.appendChild(middle);
                        var parent = node.parentNode;
                        parent.replaceChild(after, node);
                        parent.insertBefore(span, after);
                        parent.insertBefore(before, span);
                        break;
                    }
                }
            }
        })();
    """.trimIndent()

        binding.webView.evaluateJavascript(js, null)
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
            var oldStyle = document.getElementById('customFontStyle');
            if (oldStyle) {
                oldStyle.parentNode.removeChild(oldStyle);
            }
            var style = document.createElement('style');
            style.type = 'text/css';
            style.id = 'customFontStyle';
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
        calculatePagination()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId== R.id.brightness){
            showBottomSheet()
        }
        return true
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

    private fun toggleToolbar() {
        if (isToolbarVisible) {
            binding.toolbar.animate()
                .alpha(0f)
                .withEndAction {
                    binding.toolbar.visibility = View.GONE
                }.start()
        } else {
            binding.toolbar.alpha = 0f
            binding.toolbar.visibility = View.VISIBLE
            binding.toolbar.animate().alpha(1f).start()
        }
        isToolbarVisible = !isToolbarVisible
    }

    private fun toggleIndicator() {
        if (isIndicatorVisible) {
            binding.indicatorLay.animate()
                .alpha(0f)
                .withEndAction {
                    binding.indicatorLay.visibility = View.GONE
                }.start()
        } else {
            binding.indicatorLay.alpha = 0f
            binding.indicatorLay.visibility = View.VISIBLE
            binding.indicatorLay.animate().alpha(1f).start()
        }
        isIndicatorVisible = !isIndicatorVisible
    }
}