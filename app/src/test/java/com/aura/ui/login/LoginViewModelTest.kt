package com.aura.ui.login

import com.aura.data.repository.AuraRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    // Le dispatcher de test remplace Dispatchers.Main (inexistant en JVM)
    private val testDispatcher = UnconfinedTestDispatcher()

    // mockk() crée un faux Repository qui ne fait aucun vrai appel réseau
    private lateinit var mockRepository: AuraRepository
    private lateinit var viewModel: LoginViewModel

    @Before // Appelé avant chaque test
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // On installe notre faux Main
        mockRepository = mockk()
        viewModel = LoginViewModel(mockRepository)
    }

    @After // Appelé après CHAQUE test
    fun tearDown() {
        Dispatchers.resetMain() // On nettoie après chaque test
    }

    // Tests du bouton de connexion

    @Test
    fun `bouton désactivé quand les deux champs sont vides`() = runTest {
        // On s'abonne au Flow pour l'activer (WhileSubscribed)
        val job = launch { viewModel.isLoginButtonEnabled.collect {} }
        assertFalse(viewModel.isLoginButtonEnabled.value)
        job.cancel()
    }

    @Test
    fun `bouton désactivé quand seul l'email est rempli`() = runTest {
        val job = launch { viewModel.isLoginButtonEnabled.collect {} }
        viewModel.onEmailChanged("test@test.com")
        assertFalse(viewModel.isLoginButtonEnabled.value)
        job.cancel()
    }

    @Test
    fun `bouton désactivé quand seul le mot de passe est rempli`() = runTest {
        val job = launch { viewModel.isLoginButtonEnabled.collect {} }
        viewModel.onPasswordChanged("secret123")
        assertFalse(viewModel.isLoginButtonEnabled.value)
        job.cancel()
    }

    @Test
    fun `bouton activé quand les deux champs sont remplis`() = runTest {
        val job = launch { viewModel.isLoginButtonEnabled.collect {} }
        viewModel.onEmailChanged("test@test.com")
        viewModel.onPasswordChanged("secret123")
        advanceUntilIdle() // attendre que combine + stateIn aient recalculé
        assertTrue(viewModel.isLoginButtonEnabled.value)
        job.cancel()
    }

    // Tests de la fonction login()

    @Test
    fun `login réussi emet Success`() = runTest {
        // coEvery = "quand on appelle cette méthode suspend, retourne ceci"
        coEvery { mockRepository.login(any(), any()) } returns true

        viewModel.login("user@test.com", "password")

        // Grâce à UnconfinedTestDispatcher, la coroutine s'est terminée
        // immédiatement, on peut vérifier l'état final
        assertEquals(LoginUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `mauvais identifiants emet Error`() = runTest {
        coEvery { mockRepository.login(any(), any()) } returns false

        viewModel.login("user@test.com", "mauvais_mdp")

        assertEquals(LoginUiState.Error("Identifiants incorrects"), viewModel.uiState.value)
    }

    @Test
    fun `exception réseau emet Error avec message`() = runTest {
        // On simule une panne réseau
        coEvery { mockRepository.login(any(), any()) } throws Exception("Timeout")

        viewModel.login("user@test.com", "password")

        assertEquals(LoginUiState.Error("Erreur réseau : Timeout"), viewModel.uiState.value)
    }
}