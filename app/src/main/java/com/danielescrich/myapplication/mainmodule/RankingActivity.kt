
package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ActivityRankingBinding
import com.danielescrich.myapplication.mainmodule.adapter.RankingAdapter
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.RankingUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Base64

class RankingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRankingBinding
    private lateinit var adapter: RankingAdapter
    private lateinit var toggle: ActionBarDrawerToggle
    private var rankingList = listOf<RankingUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbarAndDrawer()
        setupProfileMenu()
        setupBottomNavigation()
        adapter = RankingAdapter()
        binding.rvRanking.layoutManager = LinearLayoutManager(this)
        binding.rvRanking.adapter = adapter

        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.filtros_ranking,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFiltro.adapter = spinnerAdapter

        binding.spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filtrarRanking(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        obtenerRankingDesdeApi()
    }

    private fun obtenerRankingDesdeApi() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.rankingService.getRanking()
                }
                rankingList = response
                filtrarRanking(binding.spinnerFiltro.selectedItemPosition)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun filtrarRanking(opcion: Int) {
        val listaOrdenada = when (opcion) {
            0 -> rankingList.sortedByDescending { it.pesoPerdido ?: 0.0 }
            1 -> rankingList.sortedByDescending { it.diasDesdeRegistro }
            2 -> rankingList.sortedByDescending { it.progresosRegistrados }
            else -> rankingList
        }

        val top3 = listaOrdenada.take(3)
        val resto = listaOrdenada.drop(3)

        binding.tvTop1.text = top3.getOrNull(0)?.nombreUsuario ?: "-"
        binding.tvTop2.text = top3.getOrNull(1)?.nombreUsuario ?: "-"
        binding.tvTop3.text = top3.getOrNull(2)?.nombreUsuario ?: "-"

        val getMetrica: (RankingUser?) -> String = {
            when (opcion) {
                0 -> "${it?.pesoPerdido ?: 0} kg"
                1 -> "${it?.diasDesdeRegistro ?: 0} días"
                2 -> "${it?.progresosRegistrados ?: 0} progresos"
                else -> ""
            }
        }

        binding.tvTop1Valor.text = getMetrica(top3.getOrNull(0))
        binding.tvTop2Valor.text = getMetrica(top3.getOrNull(1))
        binding.tvTop3Valor.text = getMetrica(top3.getOrNull(2))

        adapter.submitList(resto, opcion)
    }

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val headerView = binding.navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val ivUserProfile = headerView.findViewById<ImageView>(R.id.ivProfileImage)
        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        tvUserName.text = prefs.getString("usuario_nombre", "Usuario")

        val imagenBase64 = prefs.getString("usuario_imagen", null)
        if (!imagenBase64.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.getDecoder().decode(imagenBase64)
                val decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ivUserProfile.setImageBitmap(decodedBitmap)
                Log.d("IMAGEN_RANKING", "Imagen decodificada correctamente")
            } catch (e: Exception) {
                Log.e("IMAGEN_RANKING", "Error al decodificar", e)
                ivUserProfile.setImageResource(R.drawable.ic_user)
            }
        } else {
            Log.d("IMAGEN_RANKING", "No hay imagen guardada en prefs")
            ivUserProfile.setImageResource(R.drawable.ic_user)
        }

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_preferencias -> startActivity(Intent(this, PreferencesActivity::class.java))
                R.id.nav_terminos -> startActivity(Intent(this, TermsActivity::class.java))
                R.id.nav_sobre -> startActivity(Intent(this, AboutActivity::class.java))
                R.id.nav_contacto -> startActivity(Intent(this, ContactActivity::class.java))
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupProfileMenu() {
        binding.ivProfile.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menuInflater.inflate(R.menu.menu_profile, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }
                    R.id.menu_logout -> {
                        getSharedPreferences("gymmodel_prefs", MODE_PRIVATE).edit().clear().apply()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_classes -> {
                    startActivity(Intent(this, ClassesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_progress -> {
                    startActivity(Intent(this, ProgressActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_ranking -> true
                R.id.nav_ia -> {
                    startActivity(Intent(this, IAHomeActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.menu.findItem(R.id.nav_ranking).isChecked = true
    }
}
