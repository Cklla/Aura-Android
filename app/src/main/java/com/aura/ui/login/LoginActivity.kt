package com.aura.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aura.databinding.ActivityLoginBinding
import com.aura.ui.home.HomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

  private lateinit var binding: ActivityLoginBinding
  private val viewModel: LoginViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.identifier.addTextChangedListener { text ->
      viewModel.onEmailChanged(text.toString())
    }
    binding.password.addTextChangedListener { text ->
      viewModel.onPasswordChanged(text.toString())
    }

    // Observer 1 : état du bouton
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.isLoginButtonEnabled.collect { isEnabled ->
          binding.login.isEnabled = isEnabled
        }
      }
    }

    // Observer 2 : état de l'UI (Loading / Success / Error)
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
          when (state) {
            is LoginUiState.Idle -> {
              binding.loading.visibility = View.GONE
              binding.login.isEnabled = true
            }
            is LoginUiState.Loading -> {
              // Pendant le chargement : spinner visible, bouton bloqué
              binding.loading.visibility = View.VISIBLE
              binding.login.isEnabled = false
            }
            is LoginUiState.Success -> {
              binding.loading.visibility = View.GONE
              // finish() empêche de revenir au login avec le bouton "retour"
              startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
              finish()
            }
            is LoginUiState.Error -> {
              binding.loading.visibility = View.GONE
              binding.login.isEnabled = true
              Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
            }
          }
        }
      }
    }

    // Le clic déclenche l'appel réseau
    binding.login.setOnClickListener {
      viewModel.login(
        identifier = binding.identifier.text.toString(),
        password = binding.password.text.toString()
      )
    }
  }
}
