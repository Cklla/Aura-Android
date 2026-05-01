package com.aura.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aura.databinding.ActivityLoginBinding
import com.aura.ui.home.HomeActivity
import kotlinx.coroutines.launch

/**
 * The login activity for the app.
 */
class LoginActivity : AppCompatActivity()
{

  /**
   * The binding for the login layout.
   */
  private lateinit var binding: ActivityLoginBinding

  // viewModels() crée ou récupère le ViewModel lié à cette Activity
  private val viewModel: LoginViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)

    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Notifie le ViewModel à chaque frappe dans les champs
    binding.identifier.addTextChangedListener { text ->
      viewModel.onEmailChanged(text.toString())
    }
    binding.password.addTextChangedListener { text ->
      viewModel.onPasswordChanged(text.toString())
    }

    // Observe l'état du bouton et met à jour l'UI en conséquence
    // repeatOnLifecycle évite les fuites mémoire : s'arrête quand l'écran n'est plus visible
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.isLoginButtonEnabled.collect { isEnabled ->
          binding.login.isEnabled = isEnabled
        }
      }
    }

    // Pour l'instant on garde la navigation directe (on l'améliorera après)
    binding.login.setOnClickListener {
      binding.loading.visibility = View.VISIBLE
      startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
      finish()
    }
  }
}
