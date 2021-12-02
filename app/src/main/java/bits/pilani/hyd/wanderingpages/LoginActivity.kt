package bits.pilani.hyd.wanderingpages

import androidx.appcompat.app.AppCompatActivity
import com.rengwuxian.materialedittext.MaterialEditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import android.app.ProgressDialog
import android.os.Bundle
import android.content.Intent
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Login"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        auth = FirebaseAuth.getInstance()
        findViewById<TextView>(R.id.forgot_password).setOnClickListener {
            startActivity(
                Intent(
                    applicationContext, ResetPasswordActivity::class.java
                )
            )
        }
        progressDialog = ProgressDialog(this)
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            val txt_email = findViewById<MaterialEditText>(R.id.email).text.toString()
            val txt_password = findViewById<MaterialEditText>(R.id.password).text.toString()
            if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                Toast.makeText(this@LoginActivity, "All fields are required", Toast.LENGTH_SHORT)
                    .show()
            } else {
                progressDialog!!.setTitle("Log in")
                progressDialog!!.setMessage("Wait a second")
                progressDialog!!.show()
                progressDialog!!.setCancelable(false)
                auth!!.signInWithEmailAndPassword(txt_email, txt_password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            progressDialog!!.dismiss()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            progressDialog!!.dismiss()
                            Toast.makeText(
                                this@LoginActivity,
                                "Authentication Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}