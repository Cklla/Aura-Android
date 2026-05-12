package com.aura.ui.transfer

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aura.databinding.ActivityTransferBinding
import kotlinx.coroutines.launch

class TransferActivity : AppCompatActivity() {

  private lateinit var binding: ActivityTransferBinding

  // On instancie le ViewModel via la Factory (comme pour LoginActivity)
  private val viewModel: TransferViewModel by viewModels { TransferViewModelFactory() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityTransferBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Le userId vient de HomeActivity via l'Intent
    val userId = intent.getStringExtra("USER_ID") ?: ""

    binding.recipient.addTextChangedListener { text ->
      viewModel.onRecipientChanged(text.toString())
    }

    binding.amount.addTextChangedListener { text ->
      viewModel.onAmountChanged(text.toString())
    }

    // Observer l'activation du bouton
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.isTransferButtonEnabled.collect { isEnabled ->
          binding.transfer.isEnabled = isEnabled
        }
      }
    }

    // Observer l'état du transfert (étape 11)
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
          when (state) {
            is TransferUiState.Idle -> { /* rien à faire */ }
            is TransferUiState.Loading -> {
              binding.loading.visibility = View.VISIBLE // affiche la ProgressBar
              binding.transfer.isEnabled = false // désactive le bouton pendant l'envoi
            }
            is TransferUiState.Success -> {
              binding.loading.visibility = View.GONE
              // On signale à HomeActivity que le transfert a réussi
              setResult(Activity.RESULT_OK)
              finish() // ferme TransferActivity -> retour automatique sur HomeActivity
            }
            is TransferUiState.Error -> {
              binding.loading.visibility = View.GONE
              binding.transfer.isEnabled = true // réactive le bouton
              Toast.makeText(this@TransferActivity, state.message, Toast.LENGTH_LONG).show()
            }
          }
        }
      }
    }

    // Au clic : déclencher le transfert
    binding.transfer.setOnClickListener {
      val recipient = binding.recipient.text.toString()
      val amount = binding.amount.text.toString().toDoubleOrNull() ?: 0.0
      viewModel.transfer(sender = userId, recipient = recipient, amount = amount)
    }
  }
}