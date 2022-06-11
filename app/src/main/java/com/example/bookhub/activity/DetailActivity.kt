package com.example.bookhub.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookhub.R
import com.example.bookhub.database.BookDatabase
import com.example.bookhub.database.BookEntity
import com.example.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_details.*
import org.json.JSONObject
import java.lang.Exception


class DetailActivity : AppCompatActivity() {
    lateinit var txtBookName:TextView
    lateinit var txtBookAuthor:TextView
    lateinit var txtBookPrice:TextView
    lateinit var txtBookRating:TextView
    lateinit var imgBookImage:ImageView
    lateinit var btnAddFav:Button
    lateinit var txtBookDesc:TextView
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var toolbar: Toolbar
    var bookId : String? = "100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor = findViewById(R.id.txtBookAuthor)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        txtBookDesc = findViewById(R.id.txtBookDesc)
        imgBookImage = findViewById(R.id.imgBookImage)
        btnAddFav = findViewById(R.id.btnAddFav)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Description of the Book"

        if(intent!=null){
           bookId = intent.getStringExtra("book_id")
        }
        else{
            finish()
            Toast.makeText(this@DetailActivity,"Unexpected Error1",Toast.LENGTH_LONG).show()
        }
if (bookId=="100"){
    finish()
    Toast.makeText(this@DetailActivity,"Unexpected Error2",Toast.LENGTH_LONG).show()
}
        val queue = Volley.newRequestQueue(this@DetailActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"

        val jsonParams = JSONObject()
        jsonParams.put("book_id",bookId)

       if(ConnectionManager().checkConnectivity(this@DetailActivity)){
           val jsonRequest = object: JsonObjectRequest(Request.Method.POST, url, jsonParams,Response.Listener {

               try{
                   val success = it.getBoolean("success")
                   if(success){
                       val bookJsonObject = it.getJSONObject("book_data")
                       progressLayout.visibility = View.GONE
                       val bookImageUrl = bookJsonObject.getString("image")
                       Picasso.get().load(bookJsonObject.getString("image")).error(R.drawable.book_app_icon_web).into(imgBookImage)
                       txtBookName.text = bookJsonObject.getString("name")
                       txtBookAuthor.text = bookJsonObject.getString("author")
                       txtBookPrice.text = bookJsonObject.getString("price")
                       txtBookRating.text = bookJsonObject.getString("rating")
                       txtBookDesc.text = bookJsonObject.getString("description")

                       val bookEntity = BookEntity(
                           bookId?.toInt() as Int,
                           txtBookName.text.toString(),
                           txtBookAuthor.text.toString(),
                           txtBookPrice.text.toString(),
                           txtBookRating.text.toString(),
                           txtBookDesc.text.toString(),
                           bookImageUrl
                       )
                       val checkFav = DBAsyncTask(applicationContext,bookEntity,1).execute()
                       val isFav = checkFav.get()
                       if(isFav){
                           btnAddFav.text ="Remove from Favourites"
                           val favColor = ContextCompat.getColor(applicationContext,R.color.colorFavorites)
                           btnAddFav.setBackgroundColor(favColor)
                       }else{
                           btnAddFav.text="Add to Favorites"
                           val noFavColor = ContextCompat.getColor(applicationContext,R.color.colorPrimary)
                           btnAddFav.setBackgroundColor(noFavColor)
                       }
                       btnAddFav.setOnClickListener{
                           if(!DBAsyncTask(applicationContext,bookEntity,1).execute().get()){
                               val async = DBAsyncTask(applicationContext,bookEntity,2).execute()
                               val result = async.get()
                               if(result){
                                   Toast.makeText(applicationContext,"Book Added To favorites",Toast.LENGTH_SHORT).show()
                                   btnAddFav.text = "Remove from favorites"
                                   val favcolor = ContextCompat.getColor(applicationContext,R.color.colorFavorites)
                                   btnAddFav.setBackgroundColor(favcolor)
                               }else{
                                   Toast.makeText(this@DetailActivity,"error in favorites",Toast.LENGTH_SHORT).show()
                               }
                           }else{
                               val async = DBAsyncTask(applicationContext,bookEntity,3).execute()
                               val result = async.get()
                               if(result){
                               Toast.makeText(this@DetailActivity,"Book removed grom Favorites",Toast.LENGTH_SHORT).show()
                               btnAddFav.text = "Add to Favorites"
                               val noFavColor = ContextCompat.getColor(applicationContext,R.color.colorPrimary)
                               btnAddFav.setBackgroundColor(noFavColor)
                               }else{
                                   Toast.makeText(this@DetailActivity,"Some Error Occured",Toast.LENGTH_SHORT).show()

                               }
                           }
                       }

                   } else{
                       Toast.makeText(this@DetailActivity,"Unexpected Error3",Toast.LENGTH_LONG).show()
                   }
               }catch (e:Exception){
                   Toast.makeText(this@DetailActivity,"Unexpected Error4",Toast.LENGTH_LONG).show()
               }

           },Response.ErrorListener {
               Toast.makeText(this@DetailActivity,"Volley Error $it",Toast.LENGTH_LONG).show()
           }
           ){
               override fun getHeaders(): MutableMap<String, String> {
                   val headers = HashMap<String,String>()
                   headers["Content-type"] = "application/json"
                   headers["token"] = "b880cf3f40e3dd"
                   return headers
               }
           }
           queue.add(jsonRequest)

       }else{
           val dialog = AlertDialog.Builder(this@DetailActivity)
           dialog.setTitle("Error")
           dialog.setMessage("Connection not Found")
           dialog.setPositiveButton("Open Settings"){text, listner ->
               val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
               startActivity(settingsIntent)
               finish()
           }
           dialog.setNegativeButton("Exit"){text,listner->
               ActivityCompat.finishAffinity(this@DetailActivity)

           }
           dialog.create()
           dialog.show()
       }
        }
    class DBAsyncTask(val context:Context,val bookEntity: BookEntity,val mode :Int): AsyncTask<Void, Void, Boolean>(){
        /*Mode1->check the DB if book is fovourite or not
        * mode2 ->save the book in to the favorites
        * mode3 ->Remove the book from favorites
        * */
        val db = Room.databaseBuilder(context,BookDatabase::class.java,"books-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            when(mode){
                1->{
                    //Mode1->check the DB if book is fovourite or not
                    val book:BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }
                2->{
                       //mode2 ->save the book in to the favorites
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3->{
                    //Mode 3-> remove the book from favorites
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }
            return false
        }

    }
    }

