    package com.example.lolapi// com.example.android.MainActivity.kt

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.android.Account
import com.example.android.Summoner
import com.example.lolapi.R
import com.google.firebase.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

    interface RiotGamesAPI {
        @GET("riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
        fun getAccountByRiotId(
            @Path("gameName") gameName: String,
            @Path("tagLine") tagLine: String,
            @Query("api_key") apiKey: String
        ): Call<Account>

        @GET("lol/summoner/v4/summoners/by-puuid/{puuid}")
        fun getSummonerByPuuid(
            @Path("puuid") puuid: String,
            @Query("api_key") apiKey: String
        ): Call<Summoner>
    }

    // Define más llamadas a la API según tus necesidades



class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 30
        }
        val fireBaseConfig = Firebase.remoteConfig
        fireBaseConfig.setConfigSettingsAsync(configSettings)
        fireBaseConfig.setDefaultsAsync(mapOf("show_error_button" to false, "error_button_text" to ""))

        setContentView(R.layout.activity_login)

        setContentView(R.layout.start_screen)


        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener{
            task ->
            if (task.isSuccessful){
                val showErrorButton = Firebase.remoteConfig.getBoolean("show_error_button")
                val errorButtonText = Firebase.remoteConfig.getString(("error_button_text"))
            }
        }

        Toast.makeText(this, "Toca la lupa para buscar perfiles", Toast.LENGTH_LONG).show()


        /*
                val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.start_toolbar)
                setSupportActionBar(toolbar)

                supportActionBar?.apply {
                    title = null // Hace que no aparezca el título del proyecto en la toolbar
                }

                val menu_button: ImageView = findViewById(R.id.btn_menu)
                menu_button.setOnClickListener { view ->
                    showPopupMenu(view)
                }
a
         */



        RestoreMenuButtons(this)
    }

    fun SetProfile(nombreCompleto: String, ma: MainActivity) {
        val parts = nombreCompleto.split("#")
        if (parts.size != 2) {
            Toast.makeText(ma, "Formato inválido. Usa Nombre#TAG", Toast.LENGTH_SHORT).show()
            return
        }

        val gameName = parts[0]
        val tagLine = parts[1]

        setContentView(R.layout.profile_layout)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://americas.api.riotgames.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val riotGamesAPI = retrofit.create(RiotGamesAPI::class.java)
        val apiKey = "RGAPI-b8de1bca-eb74-42bf-b650-298f4b2a5e4d"

        val userNameText: TextView = findViewById(R.id.user_name)
        val userLevelText: TextView = findViewById(R.id.user_level)
        val userIcon: ImageView = findViewById(R.id.user_icon)

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

        riotGamesAPI.getAccountByRiotId(gameName, tagLine, apiKey)
            .enqueue(object : Callback<Account> {
                override fun onResponse(call: Call<Account>, response: Response<Account>) {
                    if (response.isSuccessful) {
                        val account = response.body()
                        if (account != null) {
                            val puuid = account.puuid

                            // Segunda llamada: obtener ícono y nivel
                            val summonerRetrofit = Retrofit.Builder()
                                .baseUrl("https://euw1.api.riotgames.com/") // Usa la región correcta
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()

                            val summonerAPI = summonerRetrofit.create(RiotGamesAPI::class.java)

                            summonerAPI.getSummonerByPuuid(puuid, apiKey)
                                .enqueue(object : Callback<Summoner> {
                                    override fun onResponse(call: Call<Summoner>, response: Response<Summoner>) {
                                        if (response.isSuccessful) {
                                            val summoner = response.body()
                                            if (summoner != null) {
                                                userNameText.text = summoner.name
                                                userLevelText.text = "Nivel: ${summoner.summonerLevel}"
                                                val iconUrl = "https://ddragon.leagueoflegends.com/cdn/14.7.1/img/profileicon/${summoner.profileIconId}.png"
                                                Glide.with(ma).load(iconUrl).apply(requestOptions).into(userIcon)
                                            }
                                        } else {
                                            userLevelText.text = "No se pudo obtener el perfil"
                                        }
                                    }

                                    override fun onFailure(call: Call<Summoner>, t: Throwable) {
                                        userLevelText.text = "Error de conexión"
                                    }
                                })

                        } else {
                            userLevelText.text = "Cuenta no encontrada"
                        }
                    } else {
                        userLevelText.text = "Error al buscar cuenta"
                    }
                }

                override fun onFailure(call: Call<Account>, t: Throwable) {
                    userLevelText.text = "Error de red"
                }
            })
    }

    // Menu ------------------------------------------------------------------------------------------------------------------------
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu_settings)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    // Lógica cuando se selecciona la opción del menú
                    Toast.makeText(this, "Configuración seleccionada", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.action_home -> {
                    onBackPressed()
                    true
                }
                // Otras opciones
                else -> false
            }
        }

        popupMenu.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setContentView(R.layout.start_screen)
        RestoreMenuButtons(this)
    }

    private fun RestoreMenuButtons(ma: MainActivity){
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.start_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = null // Hace que no aparezca el título del proyecto en la toolbar
        }

        val menu_button: ImageView = findViewById(R.id.btn_menu)
        menu_button.setOnClickListener { view ->
            showPopupMenu(view)
        }

        val menu_search: SearchView = findViewById(R.id.search_view)
        menu_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Realizar la búsqueda cuando se presiona Enter o se confirma la búsqueda
                SetProfile(query, ma)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }
        })
    }
}