package com.developerobaida.boipath.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.R
import com.developerobaida.boipath.adapter.BookAdapter
import com.developerobaida.boipath.adapter.ReviewAdapter
import com.developerobaida.boipath.api.ApiController
import com.developerobaida.boipath.data.local.entity.CartEntity
import com.developerobaida.boipath.data.local.entity.DownloadedBooks
import com.developerobaida.boipath.data.repository.CartRepository
import com.developerobaida.boipath.data.repository.DownloadedBookRepository
import com.developerobaida.boipath.databinding.ActivityBookDetailsBinding
import com.developerobaida.boipath.model.BookModel
import com.developerobaida.boipath.model.Constant.BASE_URL
import com.developerobaida.boipath.model.Constant.FREE_BOOK
import com.developerobaida.boipath.model.Purchase
import com.developerobaida.boipath.model.RatingModel
import com.developerobaida.boipath.model.ReviewModel
import com.developerobaida.boipath.model.WriterModel
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale

class BookDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookDetailsBinding
    private lateinit var repository: CartRepository
    private lateinit var booksRepository: DownloadedBookRepository
    private lateinit var file: File
    val apiController = ApiController.instance
    val apiInterface = apiController?.api
    var fileName = ""
    var bookId: Int =0
    var bookPrice: Int =0
    var fileUrl = ""
    val langFormat = NumberFormat.getInstance(Locale("bn", "BD"))
    var localBook: DownloadedBooks? = null
    var purchases = emptyList<Purchase>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = CartRepository(this)
        booksRepository = DownloadedBookRepository(this)

        bookId = intent.getIntExtra("bookId",0)
        fetchBookDetails(bookId)
        fetchReviews(bookId)

        binding.toolbar.setNavigationOnClickListener {
            super.onBackPressed()
        }
        binding.read.setOnClickListener{
            val intent = Intent(this,BookReaderActivity::class.java)
            intent.putExtra("bookId",bookId)
            startActivity(intent)
        }
        binding.submit.setOnClickListener{
            submitReview(bookId)
        }
        binding.rating.setOnClickListener{
            dialogRating(bookId)
        }

        binding.payment.setOnClickListener {
            startActivity(Intent(this,PaymentActivity::class.java))
        }
        binding.purchase.setOnClickListener {
            val request = Purchase(
                uid = "user123",
                bookId = 1,
                amount = 5.99,
                transactionId = "TXN2025041101",
                paymentStatus = "success"
            )

           apiInterface?.storePurchase(request)?.enqueue(object : Callback<Purchase> {
                override fun onResponse(call: Call<Purchase>, response: Response<Purchase>) {
                    if (response.isSuccessful) {
                        val purchase = response.body()
                        //val purchase = response.body()?.purchase
                        Toast.makeText(this@BookDetailsActivity, "Purchase successful: $purchase", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@BookDetailsActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Purchase>, t: Throwable) {
                    Toast.makeText(this@BookDetailsActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun downloadEbook(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            //Status: Downloading...
            binding.downloadProgress.visibility = View.VISIBLE
            binding.downloadProgress.progress = 0
            binding.buy.isEnabled = false

            try {
                withContext(Dispatchers.IO) {
                    downloadFile(url)
                }
                insertBookToLocalDB()
                updateDownloadStatus()
            } catch (e: Exception) {
                Toast.makeText(this@BookDetailsActivity,"Download failed: ${e.message}",Toast.LENGTH_SHORT).show()
                Log.e("DOWNLOAD_ERROR","Failed: ${e.message}")
            }finally {
                binding.downloadProgress.visibility = View.GONE
                binding.buy.isEnabled = true
            }
        }
    }
    private fun insertBookToLocalDB(){
        lifecycleScope.launch {
            if (localBook != null) {
                booksRepository.insertBook(localBook!!)
            }else Toast.makeText(this@BookDetailsActivity,"book can't be add to library!",Toast.LENGTH_SHORT).show()

        }
        Toast.makeText(this@BookDetailsActivity, "Book added to library!", Toast.LENGTH_SHORT).show()
    }

    private suspend fun downloadFile(url: String): File {
        return withContext(Dispatchers.IO) {
            // Download the file
            val response = apiInterface?.downloadEbook("storage/$url")

            if (response?.isSuccessful == true) {
                val body = response.body()
                body?.let {
                    val inputStream = it.byteStream()
                    val outputStream = FileOutputStream(file)
//                    inputStream.use { input ->
//                        outputStream.use { output ->
//                            input.copyTo(output)
//                        }
//                    }


                    // Get total file size if available
                    val contentLength = response.headers()["Content-Length"]?.toLong() ?: -1L
                    var totalBytesRead = 0L
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead: Int

                    inputStream.use { input ->
                        outputStream.use { output ->
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                if (contentLength > 0) {
                                    val progress = (totalBytesRead * 100 / contentLength).toInt()
                                    withContext(Dispatchers.Main) {
                                        binding.downloadProgress.progress = progress
                                    }
                                }
                            }
                            input.copyTo(output)
                        }
                    }
                }
            } else {
                Log.e("API_ERROR","Failed: ${response?.errorBody()?.string()}")
                throw IOException("Failed to download file: ${response?.errorBody()?.string()}")
            }
            file
        }
    }
    private fun updateDownloadStatus() {
        if (file.exists()) {
            binding.buy.text = "খুলুন"
            binding.buy.setOnClickListener {
                val intent = Intent(this,BookReaderActivity::class.java)
                intent.putExtra("bookId",bookId)
                startActivity(intent)
            }
        } else {
            isPurchased()
            purchases.forEach{
                if (it.bookId == bookId){
                    binding.buy.text = "ডাউনলোড"
                    binding.buy.setOnClickListener {
                        if (!file.exists()) {
                            downloadEbook(fileUrl)
                        } else{
                        }
                    }
                }else {
                    if (bookPrice == FREE_BOOK){
                        binding.buy.text = "ডাউনলোড"
                        binding.buy.setOnClickListener {
                            if (!file.exists()) {
                                downloadEbook(fileUrl)
                            } else{
                            }
                        }
                    }else{
                        binding.buy.text = "কিনুন"
                        binding.buy.setOnClickListener {
                            Toast.makeText(this@BookDetailsActivity,"Purchase",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }


        }
    }

    private fun fetchBookDetails(id: Int){
        apiInterface?.getBookById(id)?.enqueue(object : Callback<List<BookModel>>{
            override fun onResponse(call: Call<List<BookModel>>, response: Response<List<BookModel>>) {
                if (response.isSuccessful){
                    val book = response.body()
                    book?.let {
                        binding.bookName.text = book[0].bookName
                        binding.writer.text = book[0].author
                        binding.aboutBook.text = book[0].description
                        binding.page.text = langFormat.format(book[0].pages)
                        binding.price.text = langFormat.format(book[0].price)+"৳"
                        Picasso.get().load(book[0].bookCover).placeholder(R.drawable.dwewf).error(R.drawable.dwewf).into(binding.bookCover)

                        fileUrl = book[0].epubFile
                        bookId = book[0].id
                        bookPrice = book[0].price.toInt()

                        localBook = DownloadedBooks(
                            id = null,
                            book[0].bookCover,book[0].bookName,book[0].author,book[0].authorId,book[0].pages,book[0].description,book[0].categories.toString(),book[0].id
                        )
                        //fileName = Uri.parse(book[0].epubFile).lastPathSegment ?: "default.epub"
                        fileName = "book_${book[0].id}.epub"
                        file = File(getExternalFilesDir(null), fileName)
                        updateDownloadStatus()

                        val downloadedFiles = file.listFiles()
                        Log.d("DownloadedBooks", "==== Downloaded Books List ====")
                        val books = getDownloadedBooksList()

                        if (books.isEmpty()) {
                            Log.d("DownloadedBooks", "No books found in downloads directory")
                            return
                        }

                        Log.d("DownloadedBooks", "==== Downloaded Books (${books.size}) ====")
                        books.sortedBy { it.lastModified() }.forEachIndexed { index, file ->
                            Log.d("DownloadedBooks", """
            ${index + 1}. ${file.name}
               Path: ${file.absolutePath}
               
        """.trimIndent())
                        }
                        Log.d("DownloadedBooks", "==== End of List ====")


                        fetchAuthor(book[0].authorId)
                        fetchBooks(book[0].authorId)

                        binding.writerProfile.setOnClickListener {
                            val intent = Intent(this@BookDetailsActivity,WriterActivity::class.java)
                            intent.putExtra("author_id",book[0].authorId)
                            startActivity(intent)
                        }



                        binding.addToCart.setOnClickListener {

                            val cartItem = CartEntity(
                                id = null,
                                bookName = book[0].bookName,
                                bookCover = book[0].bookCover,
                                author = book[0].author,
                                pages = book[0].pages,
                                price = book[0].price,
                                categories = book[0].categories.toString(),
                                bookId = book[0].id
                            )

                            lifecycleScope.launch {
                                repository.insertItem(cartItem)
                            }

                            Toast.makeText(this@BookDetailsActivity, "Book added to cart!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }else {
                    Log.e("API_ERROR", "Book Response not successful: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Book Error body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(p0: Call<List<BookModel>>, p1: Throwable) {
                Log.e("API_ERROR", "Book Failed: ${p1.message}")
            }

        })
    }
    fun getDownloadedBooksList(): List<File> {
        val downloadsDir = getExternalFilesDir(null) ?: return emptyList()
        return downloadsDir.listFiles()?.toList() ?: emptyList()
    }

    private fun fetchBooks(id: Int) {

        apiInterface?.getBookByWriterId(id)?.enqueue(object : Callback<List<BookModel>> {
            override fun onResponse(call: Call<List<BookModel>>, response: Response<List<BookModel>>) {
                if (response.isSuccessful) {
                    val books = response.body()
                    books?.let {
                        Log.d("API_RESPONSE", "Books: ${Gson().toJson(books)}")

                        val adapter = BookAdapter(books)
                        binding.recBook.adapter =adapter
                        binding.recBook.layoutManager = LinearLayoutManager(this@BookDetailsActivity, LinearLayoutManager.HORIZONTAL,false)
                        binding.recBook.hasFixedSize()
                    }
                } else {
                    Log.e("API_ERROR", "Response not successful: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Error body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<BookModel>>, t: Throwable) {
                Log.e("API_ERROR", "Failed to fetch books: ${t.message}")
            }
        })
    }

    private fun fetchAuthor(id: Int){
        apiInterface?.getAuthorById(id)?.enqueue(object : Callback<WriterModel>{
            override fun onResponse(call: Call<WriterModel>, response: Response<WriterModel>) {
                if (response.isSuccessful){
                    val writer = response.body()
                    writer?.let {
                        binding.writerName2.text = writer.name
                        binding.writerFollower.text = langFormat.format(writer.followers)+" অনুসরণ কারী"
                        if (writer.image !=null){
                            Picasso.get().load(BASE_URL+writer.image).into(binding.writerImg)
                        }else binding.writerImg.setImageResource(R.drawable.dwewf)
                    }
                }else{

                }
            }

            override fun onFailure(p0: Call<WriterModel>, p1: Throwable) {
                Log.e("API_ERROR",p1.message.toString())
            }

        })
    }

    private fun fetchReviews(bookId: Int){
        apiInterface?.getReviewsByBookId(bookId)?.enqueue(object : Callback<List<ReviewModel>> {
            override fun onResponse(call: Call<List<ReviewModel>>, response: Response<List<ReviewModel>>) {
                if (response.isSuccessful) {
                    val reviews = response.body()
                    reviews?.let {
                        Log.d("API_RESPONSE", "reviews: ${Gson().toJson(reviews)}")

                        val adapter = ReviewAdapter(reviews)
                        binding.reviewRec.adapter =adapter
                        binding.reviewRec.layoutManager = LinearLayoutManager(this@BookDetailsActivity, LinearLayoutManager.HORIZONTAL,false)
                        binding.reviewRec.hasFixedSize()
                    }
                } else {
                    Log.e("API_ERROR", "Response not successful: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Error body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<ReviewModel>>, t: Throwable) {
                Log.e("API_ERROR", "Failed to fetch books: ${t.message}")
            }
        })
    }

    private fun submitReview(bookId: Int) {
        val bookName = "Test name"
        val reviewBody = binding.inputReview.text.toString()
        val reviewerName = "a reviewer"
        val reviewerId = 1

        if (bookName.isEmpty() || reviewBody.isEmpty() || reviewerName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val call = apiInterface?.addReview(bookId.toString(), bookName, reviewBody, reviewerName, reviewerId.toString())
        call?.enqueue(object : Callback<ReviewModel> {
            override fun onResponse(call: Call<ReviewModel>, response: Response<ReviewModel>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@BookDetailsActivity, "Review added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Failed to add review: $errorBody")
                    Toast.makeText(this@BookDetailsActivity, "Failed to add review: $errorBody", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReviewModel>, t: Throwable) {
                Toast.makeText(this@BookDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun addRating(bookId:Int,rating:Float){
        apiInterface?.addRatings(bookId.toString(),rating.toString())?.enqueue(object : Callback<RatingModel>{
            override fun onResponse(p0: Call<RatingModel>, p1: Response<RatingModel>) {
                if (p1.isSuccessful){
                    Toast.makeText(this@BookDetailsActivity,"Rating Successfully added!",Toast.LENGTH_SHORT).show()
                }else Log.e("API_ERROR", "Failed: ${p1.message()}")
            }

            override fun onFailure(p0: Call<RatingModel>, p1: Throwable) {
                Log.e("API_ERROR", "Failed: ${p1.message}")
            }

        })
    }

    private fun dialogRating(bookId: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_rating)
        val yes = dialog.findViewById<MaterialButton>(R.id.yes)
        val ratingBar = dialog.findViewById<RatingBar>(R.id.ratingBar)


        yes.setOnClickListener { view: View? ->
            val rating = ratingBar.rating
            if (rating>0){
                addRating(bookId,rating)
            }else Toast.makeText(this,"Select the star",Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setWindowAnimations(R.style.dialogAnimation)
        dialog.create()
        dialog.show()
    }

    private fun isPurchased(){
        apiInterface?.getPurchasedBooks("user123")?.enqueue(object : Callback<List<Purchase>> {
            override fun onResponse(call: Call<List<Purchase>>, response: Response<List<Purchase>>) {
                if (response.isSuccessful) {
                    purchases = response.body()!!
                    purchases.forEach {
                        Log.d("PurchasedBook", it.bookId.toString())
                    }
                } else {
                    Log.d("PURCHASE","Error: ${response.message()}" )
                }
            }

            override fun onFailure(call: Call<List<Purchase>>, t: Throwable) {
                Toast.makeText(this@BookDetailsActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}