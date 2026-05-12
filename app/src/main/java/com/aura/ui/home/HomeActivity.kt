package com.aura.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aura.R
import com.aura.databinding.ActivityHomeBinding
import com.aura.ui.login.LoginActivity
import com.aura.ui.transfer.TransferActivity
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

  private lateinit var binding: ActivityHomeBinding
  private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory() }
  private var currentUserId: String = ""

  private val startTransferActivityForResult =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
      if (result.resultCode == Activity.RESULT_OK) {
        viewModel.loadBalance(currentUserId) // on recharge la balance depuis l'API
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Récupère l'userId envoyé par LoginActivity
    val userId = intent.getStringExtra("USER_ID") ?: ""

    // Observer : réagit à chaque changement d'état
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
          when (state) {
            is HomeUiState.Idle -> {
              binding.loading.visibility = View.GONE
            }
            is HomeUiState.Loading -> {
              binding.loading.visibility = View.VISIBLE
              binding.errorMessage.visibility = View.GONE // cache l'erreur pendant le chargement
              binding.retryButton.visibility = View.GONE
            }
            is HomeUiState.Success -> {
              binding.loading.visibility = View.GONE
              binding.errorMessage.visibility = View.GONE
              binding.retryButton.visibility = View.GONE
              // Affiche la balance formatée (ex: "2 331,31 €")
              binding.balance.text = String.format("%,.2f €", state.balance)
            }
            is HomeUiState.Error -> {
              binding.loading.visibility = View.GONE
              binding.errorMessage.visibility = View.VISIBLE // affiche le message
              binding.errorMessage.text = state.message // écrit le message
              binding.retryButton.visibility = View.VISIBLE // affiche le bouton
            }
          }
        }
      }
    }

    // Déclenche le chargement de la balance dès l'arrivée sur l'écran
    currentUserId = userId // on mémorise le userID
    viewModel.loadBalance(userId)

    // Quand on clique sur Réessayer, on relance le même chargement
    binding.retryButton.setOnClickListener {
      viewModel.loadBalance(currentUserId)
    }

    binding.transfer.setOnClickListener {
      startTransferActivityForResult.launch(
        Intent(this@HomeActivity, TransferActivity::class.java).apply {
          putExtra("USER_ID", currentUserId) // on passe l'ID au suivant
        }
      )
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.home_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.disconnect -> {
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }
}
