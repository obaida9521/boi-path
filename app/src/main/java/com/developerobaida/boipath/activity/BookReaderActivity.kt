package com.developerobaida.boipath.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.developerobaida.boipath.databinding.ActivityBookReaderBinding
import org.w3c.dom.Element
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class BookReaderActivity : AppCompatActivity() {
    lateinit var binding: ActivityBookReaderBinding
    private var currentPageIndex = 0
    private var currentTextSize = 30

    private lateinit var htmlFiles: List<String>
    private lateinit var targetDir: File
    private var opfDir: String = ""
    private val initialFontFamily = "serif"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        setEpub()
        loadCurrentPage()

        binding.nextPage.setOnClickListener { goToNextPage() }
        binding.prevPage.setOnClickListener { goToPreviousPage() }

        binding.increaseTextSize.setOnClickListener {
            currentTextSize += 2
            adjustTextSize(currentTextSize)
        }

        binding.decreaseTextSize.setOnClickListener {
            currentTextSize -= 2
            adjustTextSize(currentTextSize)
        }
        binding.alignLeft.setOnClickListener { adjustTextAlignment("left") }
        binding.alignCenter.setOnClickListener { adjustTextAlignment("center") }
        binding.alignRight.setOnClickListener { adjustTextAlignment("right") }
        binding.alignJustify.setOnClickListener { adjustTextAlignment("justify") }

        val htmlFile = File(targetDir, htmlFiles.first())



        //loadHtmlIntoWebView(binding.webView, htmlFile, targetDir)

    }
    private fun setEpub(){
        //val epubFileName = "famouspaintings.epub"
        val epubFileName = "mhdaifer.epub"
        val epubFile = File(filesDir, epubFileName)
        if (!epubFile.exists()) copyEpubFromAssets(this, epubFileName, epubFile)
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

        htmlFiles = try {
            parseOpf(opfFile)
        } catch (e: Exception) {
            Log.e("EpubReader", "Error parsing OPF file", e)
            return
        }
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
            if (htmlFile.exists()) loadHtmlIntoWebView(binding.webView, htmlFile, targetDir)
            else Log.e("EpubReader", "HTML file does not exist: ${htmlFile.absolutePath}")
        }
    }

    private fun goToNextPage() {
        if (currentPageIndex < htmlFiles.size - 1) {
            currentPageIndex++
            loadCurrentPage()
        } else Toast.makeText(this, "End of book", Toast.LENGTH_SHORT).show()
    }

    private fun goToPreviousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--
            loadCurrentPage()
        } else Toast.makeText(this, "Already at first page", Toast.LENGTH_SHORT).show()
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
    }

    private fun copyEpubFromAssets(context: Context, epubFileName: String, destination: File) {
        try {
            val inputStream = context.assets.open(epubFileName)
            val outputStream = FileOutputStream(destination)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            Log.e("EpubReader", "Error copying EPUB from assets", e)
        }
    }

    private fun unzipEpub(epubFile: File, targetDir: File) {
        val zipFile = ZipFile(epubFile)
        zipFile.entries().asSequence().forEach { entry ->
            val outFile = File(targetDir, entry.name)
            if (entry.isDirectory) outFile.mkdirs()
             else {
                try {
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

    private fun parseOpf(opfFile: File): List<String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(opfFile)
        val manifest = document.getElementsByTagName("manifest").item(0) as Element
        val spine = document.getElementsByTagName("spine").item(0) as Element
        val htmlFiles = mutableListOf<String>()

        val manifestItems = manifest.getElementsByTagName("item")
        val spineItems = spine.getElementsByTagName("itemref")

        val idHrefMap = mutableMapOf<String, String>()

        for (i in 0 until manifestItems.length) {
            val item = manifestItems.item(i) as Element
            val id = item.getAttribute("id")
            val href = item.getAttribute("href")
            idHrefMap[id] = href
        }

        for (i in 0 until spineItems.length) {
            val itemref = spineItems.item(i) as Element
            val idref = itemref.getAttribute("idref")
            idHrefMap[idref]?.let { htmlFiles.add(it) }
        }

        Log.d("EpubReader", "Parsed HTML Files: $htmlFiles")
        return htmlFiles
    }

    private fun loadHtmlIntoWebView(webView: WebView, htmlFile: File, assetsDir: File) {
        try {
            val htmlContent = htmlFile.readText()
            val fixedWidthCSS = """
            <style>
                body {
                    width: 100vw;
                    overflow-x: hidden;
                    margin: 0;
                    padding: 0;
                    font-size: ${currentTextSize}px;
                }
              
                img, svg, table, div {
                    max-width: 100%;
                    height: auto;
                }
                p, div, span, h1, h2, h3, h4, h5, h6 {
                    text-align: left;
                    font-size: ${currentTextSize}px;
                }
            </style>
        """.trimIndent()

            val modifiedHtmlContent = htmlContent.replace("</head>", "$fixedWidthCSS</head>")

            val baseUrl = "file://${htmlFile.parent}/"
            webView.loadDataWithBaseURL(baseUrl, modifiedHtmlContent, "text/html", "UTF-8", null)
        } catch (e: IOException) {
            Log.e("EpubReader", "Error reading HTML file", e)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        newConfig.screenWidthDp
        loadCurrentPage()
    }
}