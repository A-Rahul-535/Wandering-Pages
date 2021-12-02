package bits.pilani.hyd.wanderingpages

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import android.widget.TextView
import android.app.ProgressDialog
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.StorageReference
import com.google.firebase.database.DatabaseReference
import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseError
import com.google.firebase.storage.FirebaseStorage
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import bits.pilani.hyd.wanderingpages.Model.BookPost
import bits.pilani.hyd.wanderingpages.Model.User
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddBookActivity : AppCompatActivity() {
    val PICK_CODE = 1
    var currentDate = ""
    var currentTime = ""
    private var filepath // store the path of image we are uploading
            : Uri? = null
    var profile_url = ""
    var UserName = ""

    //firebase
    private var firebaseUser: FirebaseUser? = null
    private var mStorageRef: StorageReference? = null
    private var reference: DatabaseReference? = null
    var Sdate = ""
    var Stime = ""
    var latitude: String? = ""
    var longitude: String? = ""
    var address: String? = ""
    var city: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { //add this code
            //startActivity ( new Intent ( getApplicationContext (),MainActivity.class ).setFlags ( Intent.FLAG_ACTIVITY_CLEAR_TOP ) );
            finish()
        }

        getDateTime()
        var intent = intent
        address = intent.getStringExtra("address")
        city = intent.getStringExtra("city")
        latitude = intent.getStringExtra("latitude")
        longitude = intent.getStringExtra("longitude")
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val UID = firebaseUser!!.uid

        //firebase
        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.child(UID).getValue(
                        User::class.java
                    )!!
                    profile_url = user.imageURL.toString()
                    UserName = user.username.toString()
                    findViewById<TextView>(R.id.username).text = UserName
//                    username.setText(Objects.requireNonNull(user).username)
                    if (user.imageURL == "default") {
                        findViewById<CircleImageView>(R.id.profile_image).setImageResource(R.drawable.userphoto)
//                        profile_image.setImageResource(R.drawable.userphoto)
                    } else {
                        Glide.with(applicationContext).load(user.imageURL)
                            .placeholder(R.drawable.new_loader)
                            .into(findViewById(R.id.profile_image))
                    }
                } else {
                    //Toast.makeText ( getApplicationContext (), "Something went wrong", Toast.LENGTH_SHORT ).show ();
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        reference = FirebaseDatabase.getInstance().reference.child("BookPosts")
        mStorageRef = FirebaseStorage.getInstance().reference.child("Users").child(
            firebaseUser!!.email.toString()
        )

        // choose the image you want to upload to firebase
//        choooseImage.setOnClickListener(View.OnClickListener { showFileChooser() })
        findViewById<Button>(R.id.chooseImage).setOnClickListener { showFileChooser() }

        // upload the images into firebase
//        uploadPost.setOnClickListener(View.OnClickListener { uploadData() })
        findViewById<Button>(R.id.upload_post).setOnClickListener { uploadData() }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            filepath = data.data


            //shows image which you are uploading to firebase
            var bitmap: Bitmap? = null
            try {
                bitmap =
                    MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, filepath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            findViewById<ImageView>(R.id.prev_img).setImageBitmap(bitmap)
        }
    }

    fun showFileChooser() {
        val intent = Intent()
        intent.type = "image/*" // * means shows all types images from your phone (jpeg,png,etc)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_CODE)
    }

    fun uploadData() {
        //check filepath i.e image is selected or not
        if (filepath != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            //create the child from the storagereference path
            val store: StorageReference = mStorageRef!!.child(
                "BookPosts/" + System.currentTimeMillis() + "." + getFileExtension(filepath)
            )


            // putFile() method is used to add data to firebase storage
            store.putFile(filepath!!).addOnSuccessListener { taskSnapshot ->
                // disable the progress bar
                progressDialog.dismiss()

                // get the URL of image to store in database
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    val book_name = findViewById<TextInputLayout>(R.id.bookName).editText!!.text.toString()
                    val book_desc = findViewById<TextInputLayout>(R.id.bookDesc).editText!!.text.toString()
                    val book_author = findViewById<TextInputLayout>(R.id.bookAuthor).editText!!.text.toString()
                    val mDate = Sdate
                    val mTime = Stime
                    val book_image = uri.toString()
                    val mUID = firebaseUser!!.uid
                    val profileURL = profile_url
                    val userName = UserName
                    val mLocation = "$address, $city"
                    val mLatitude = latitude.toString()
                    val mLongitude = longitude.toString()
                    val bookPost = BookPost(
                        book_name,
                        book_desc,
                        mLocation,
                        mLatitude,
                        mLongitude,
                        book_author,
                        book_image,
                        profileURL,
                        userName,
                        mDate,
                        mTime,
                        mUID
                    )
                    //uploads the data using setValue() method
                    reference!!.push().setValue(bookPost).addOnSuccessListener {
                        Toast.makeText(
                            applicationContext,
                            "Uploaded Successfull :) ",
                            Toast.LENGTH_SHORT
                        ).show()
                        filepath = null
                        findViewById<ImageView>(R.id.prev_img)!!.setImageResource(R.drawable.ic_photo)
                        findViewById<TextInputLayout>(R.id.bookName).editText!!.setText("")
                        findViewById<TextInputLayout>(R.id.bookDesc).editText!!.setText("")
                        findViewById<TextInputLayout>(R.id.bookAuthor).editText!!.setText("")
                        findViewById<TextInputLayout>(R.id.location).editText!!.setText("")
                    }
                }
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Failed...try again", Toast.LENGTH_SHORT).show()
            }
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressDialog.setMessage("it takes few seconds...")
                }
        } else {
            Toast.makeText(
                applicationContext,
                "Image selected is invalid or no image is selected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getFileExtension(uri: Uri?): String? {
        val cR = applicationContext.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri!!))
    }

    fun getDateTime() {
            //time and date
            currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val date = currentDate.split("-").toTypedArray()
            val day = date[0]
            val temp = date[1]
            var month = ""
            val year = date[2]
            month = getMonth(temp)
            Sdate = "$day $month $year"
            currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
            val arr = currentTime.split(":").toTypedArray()
            val am_pm = arr[2].split(" ").toTypedArray()
            Stime = arr[0] + ":" + arr[1] + " " + am_pm[1].uppercase(Locale.getDefault())
        }

    fun getMonth(temp: String): String {
        var month = ""
        if (temp == "01") {
            month = "JAN"
        } else if (temp == "02") {
            month = "FEB"
        } else if (temp == "03") {
            month = "MAR"
        } else if (temp == "04") {
            month = "APL"
        } else if (temp == "05") {
            month = "MAY"
        } else if (temp == "06") {
            month = "JUN"
        } else if (temp == "07") {
            month = "JULY"
        } else if (temp == "08") {
            month = "AUG"
        } else if (temp == "09") {
            month = "SEPT"
        } else if (temp == "10") {
            month = "OCT"
        } else if (temp == "11") {
            month = "NOV"
        } else if (temp == "12") {
            month = "DEC"
        }
        return month
    }
}