package bits.pilani.hyd.wanderingpages

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import java.util.*

class RegisterActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    var reference: DatabaseReference? = null
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        findViewById<Button>(R.id.btn_register).setOnClickListener {
            val txt_username = findViewById<MaterialEditText>(R.id.username).text.toString()
            val txt_email = findViewById<MaterialEditText>(R.id.email).text.toString()
            val txt_password = findViewById<MaterialEditText>(R.id.password).text.toString()
            if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(
                    txt_password
                )
            ) {
                Toast.makeText(this@RegisterActivity, "All fields are required", Toast.LENGTH_SHORT)
                    .show()
            } else if (txt_password.length < 6) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                register(txt_username, txt_email, txt_password)
            }
        }
    }

    private fun register(username: String, email: String, password: String) {
        progressDialog!!.setTitle("Register")
        progressDialog!!.setMessage("Wait a second")
        progressDialog!!.show()
        progressDialog!!.setCancelable(false)
        auth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog!!.dismiss()
                    val firebaseUser = auth!!.currentUser
                    val userid = firebaseUser!!.uid
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)
                    val hashMap = HashMap<String, String>()
                    hashMap["id"] = userid
                    hashMap["username"] = username
                    hashMap["imageURL"] = "default"
                    hashMap["is_online"] = "offline"
                    hashMap["search"] = username.lowercase(Locale.getDefault())
                    hashMap["address"] = ""
                    hashMap["city"] = ""
                    hashMap["latitude"] = ""
                    hashMap["longitude"] = ""
                    reference!!.setValue(hashMap).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    progressDialog!!.dismiss()
                    Toast.makeText(
                        this@RegisterActivity,
                        "You can't register with this email and password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}