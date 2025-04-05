package com.example.lolapi

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.server.response.SafeParcelResponse.from
import com.google.firebase.auth.FirebaseAuth
import java.util.Date.from

import android.util.Log
import android.widget.Button
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var txtUser: EditText
    private lateinit var txtPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var auth:FirebaseAuth

    private var canAuthenticate = false

    private val RC_SIGN_IN = 9001
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        txtUser=findViewById(R.id.txtUser)
        txtPassword=findViewById(R.id.txtPassword)
        progressBar= findViewById(R.id.progressBar)
        auth= FirebaseAuth.getInstance()

        mAuth = FirebaseAuth.getInstance()
        // Configura el click listener del botón de inicio de sesión con Google
        val googleSignInButton: Button = findViewById<Button>(R.id.googleSignInButton)
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Reemplaza con tu ID de cliente web
            .requestEmail()
            .build()

        val googleClient : GoogleSignInClient = GoogleSignIn.getClient(this, gso)

        startActivityForResult(googleClient.signInIntent, RC_SIGN_IN)

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                firebaseAuthWithGoogle(account?.idToken)
            } catch (e: ApiException) {
                Toast.makeText(this, "No se pudo iniciar sesión con Google", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        Toast.makeText(this, ".-.-.-.-.--", Toast.LENGTH_LONG).show()

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso, el usuario está autenticado
                    val user = mAuth.currentUser
                    action()
                } else {
                    // Fallo en el inicio de sesión
                    Log.w("Login", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Error en la autentificacion", Toast.LENGTH_LONG).show()

                }
            }
    }
    fun forgotPassword(view: View)
    {
       // startActivity(Intent(this,ForgotPassActivity::class.java))
    }

    fun register(view: View)
    {
        startActivity(Intent(this,RegisterActivity::class.java))
    }
    fun login(view: View)
    {
        loginUser()
    }

    private fun loginUser()
    {
        val user:String=txtUser.text.toString()
        val password:String=txtPassword.text.toString()

        //Comprobando que todos los campos esten llenos
        if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(password))
        {

            progressBar.visibility=View.VISIBLE

            //Inicio de sesion
            auth.signInWithEmailAndPassword(user,password).addOnCompleteListener(this)
            {
                task ->

                //Verificando si se han puesto bien las credenciales
                if(task.isSuccessful)
                {
                    action()
                }
                else
                {
                    Toast.makeText(this, "Error en la autentificacion", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun action()
    {
        startActivity(Intent(this,MainActivity::class.java))
    }
}