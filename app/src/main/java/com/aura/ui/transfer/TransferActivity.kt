package com.aura.ui.transfer

import android.app.Activity
import android.os.Bundle
import android.view.View
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

    // Chaque fois que l'utilisateur tape dans le champ bénéficiaire, on notifie le ViewModel
    binding.recipient.addTextChangedListener { text ->
      viewModel.onRecipientChanged(text.toString())
    }

    // Idem pour le montant
    binding.amount.addTextChangedListener { text ->
      viewModel.onAmountChanged(text.toString())
    }

    // On observe le ViewModel : est-ce que le bouton doit être actif ?
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.isTransferButtonEnabled.collect { isEnabled ->
          binding.transfer.isEnabled = isEnabled
        }
      }
    }

    // Pour l'instant, le clic ne fait rien — ce sera l'étape 11
    binding.transfer.setOnClickListener {
      // TODO étape 11 : appel réseau
    }
  }
}