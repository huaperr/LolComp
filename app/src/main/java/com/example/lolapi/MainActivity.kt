    package com.example.lolapi// com.example.android.MainActivity.kt

import android.os.Bundle
import android.util.Log
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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.android.Maestry
import com.example.android.Rank
import kotlin.math.log

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

        @GET("lol/league/v4/entries/by-puuid/{encryptedPUUID}")
        fun getRankByPuuid(
            @Path("encryptedPUUID") Rpuuid: String,
            @Query("api_key") apiKey: String
        ): Call<List<Rank>>

        @GET("lol/champion-mastery/v4/champion-masteries/by-puuid/{puuid}/top")
        fun getTopChampionMasteries(
            @Path("puuid") puuid: String,
            @Query("api_key") apiKey: String
        ): Call<List<Maestry>>
    }




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

    fun SetProfile(nickName: String, ma: MainActivity) {
        val parts = nickName.split("#")
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
        val userRankText: TextView = findViewById(R.id.user_textRank)
        val userRank: ImageView = findViewById(R.id.user_rank)
        val userIcon: ImageView = findViewById(R.id.user_icon)

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

        riotGamesAPI.getAccountByRiotId(gameName, tagLine, apiKey)
            .enqueue(object : Callback<Account> {
                override fun onResponse(call: Call<Account>, response: Response<Account>) {
                    if (response.isSuccessful) {
                        val account = response.body()
                        if (account != null) {
                            val puuid = account.puuid

                            // segunda call, icono y nivel
                            val summonerRetrofit = Retrofit.Builder()
                                .baseUrl("https://euw1.api.riotgames.com/") // Usa la región correcta
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()

                            val summonerAPI = summonerRetrofit.create(RiotGamesAPI::class.java)

                            summonerAPI.getSummonerByPuuid(puuid, apiKey)
                                .enqueue(object : Callback<Summoner> {
                                    override fun onResponse(
                                        call: Call<Summoner>,
                                        response: Response<Summoner>
                                    ) {
                                        if (response.isSuccessful) {
                                            val summoner = response.body()
                                            if (summoner != null) {
                                                userNameText.text = account.gameName
                                                Log.d("API_RESPONSE", "Nombre: ${summoner.name}")
                                                userLevelText.text =
                                                    "Nivel: ${summoner.summonerLevel}"
                                                val iconUrl =
                                                    "https://ddragon.leagueoflegends.com/cdn/14.7.1/img/profileicon/${summoner.profileIconId}.png"
                                                Glide.with(ma).load(iconUrl).apply(requestOptions)
                                                    .into(userIcon)
                                            }
                                        } else {
                                            userLevelText.text = "No se pudo obtener el perfil"
                                        }
                                    }



                                    override fun onFailure(call: Call<Summoner>, t: Throwable) {
                                        userLevelText.text = "Error de conexión"
                                    }
                                })

                            val rankAPI = summonerRetrofit.create(RiotGamesAPI::class.java)

                            rankAPI.getRankByPuuid(puuid, apiKey)
                                .enqueue(object : Callback<List<Rank>> {
                                    override fun onResponse(call: Call<List<Rank>>, response: Response<List<Rank>>) {
                                        if (response.isSuccessful) {
                                            val ranks = response.body()
                                            if (!ranks.isNullOrEmpty()) {
                                                val soloQ = ranks.find { it.queueType == "RANKED_SOLO_5x5" }
                                                if (soloQ != null) {
                                                    val rankText = "Rango: ${soloQ.tier} ${soloQ.rank} - ${soloQ.leaguePoints}LP"
                                                    userRankText.text = "${userRankText.text}\n$rankText"

                                                    val rankImage = when (soloQ.tier.lowercase()) {
                                                        "iron" -> R.drawable.iron
                                                        "bronze" -> R.drawable.bronze
                                                        "silver" -> R.drawable.silver
                                                        "gold" -> R.drawable.gold
                                                        "platinum" -> R.drawable.platinum
                                                        "diamond" -> R.drawable.diamond
                                                        "master" -> R.drawable.master
                                                        "grandmaster" -> R.drawable.grandmaster
                                                        "challenger" -> R.drawable.challenger
                                                        else -> R.drawable.unranked // Imagen predeterminada en caso de que no se encuentre el rango
                                                    }

                                                    userRank.setImageResource(rankImage)
                                                }
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<List<Rank>>, t: Throwable) {
                                        Log.e("RankFetch", "Error al obtener el rango", t)
                                    }
                                })

                                val riotGamesAPI = retrofit.create(RiotGamesAPI::class.java)

                                riotGamesAPI.getTopChampionMasteries(puuid, apiKey)
                                    .enqueue(object : Callback<List<Maestry>> {
                                        override fun onResponse(
                                            call: Call<List<Maestry>>,
                                            response: Response<List<Maestry>>
                                        ) {
                                            if (response.isSuccessful) {
                                                val championMasteries = response.body()
                                                if (championMasteries != null && championMasteries.isNotEmpty()) {

                                                    for (champion in championMasteries) {
                                                        Log.d(
                                                            "Champion Mastery",
                                                            "Champion ID: ${champion.championId}"
                                                        )

                                                    }
                                                }
                                            }

                                        }

                                        override fun onFailure(
                                            call: Call<List<Maestry>>,
                                            t: Throwable
                                        ) {
                                            Log.e(
                                                "Champion Mastery",
                                                "Error en la llamada a la API",
                                                t
                                            )
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
        popupMenu.inflate(R.menu.menu_settings)  // Asegúrate de que este archivo de menú exista

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    // Lógica cuando se selecciona la opción del menú
                    Toast.makeText(this, "Configuración seleccionada", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.action_home -> {
                    onBackPressed()  // Volver a la pantalla anterior
                    true
                }
                else -> false
            }
        }

        // Mostrar el menú en pantalla
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
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrEmpty()) {
                    Toast.makeText(this@MainActivity, "Por favor ingrese un nombre de jugador", Toast.LENGTH_SHORT).show()
                    return false
                }

                // Realizar la búsqueda cuando se presiona Enter o se confirma la búsqueda
                SetProfile(query, ma)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })
    }
}