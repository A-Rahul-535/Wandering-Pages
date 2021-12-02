package bits.pilani.hyd.wanderingpages

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    var firebaseAuth: FirebaseAuth? = null
    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Reset Password"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        progressDialog = ProgressDialog(this)
        firebaseAuth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            val email = findViewById<EditText>(R.id.send_email).text.toString()
            if (email == "") {
                Toast.makeText(
                    this@ResetPasswordActivity,
                    "All fields are required!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                progressDialog!!.setMessage("Loading...")
                progressDialog!!.show()
                progressDialog!!.setCancelable(false)
                firebaseAuth!!.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        progressDialog!!.dismiss()
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Please check you Email",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                    } else {
                        progressDialog!!.dismiss()
                        val error = task.exception!!.message
                        Toast.makeText(this@ResetPasswordActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}