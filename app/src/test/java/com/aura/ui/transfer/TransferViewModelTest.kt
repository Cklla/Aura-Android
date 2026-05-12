package com.aura.ui.transfer

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
class TransferViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockRepository: AuraRepository
    private lateinit var viewModel: TransferViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk()
        viewModel = TransferViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Tests du bouton de virement

    @Test
    fun `bouton désactivé quand les deux champs sont vides`() = runTest {
        val job = launch { viewModel.isTransferButtonEnabled.collect {} }
        assertFalse(viewModel.isTransferButtonEnabled.value)
        job.cancel()
    }

    @Test
    fun `bouton désactivé quand seul le bénéficiaire est rempli`() = runTest {
        val job = launch { viewModel.isTransferButtonEnabled.collect {} }
        viewModel.onRecipientChanged("Jean Dupont")
        assertFalse(viewModel.isTransferButtonEnabled.value)
        job.cancel()
    }

    @Test
    fun `bouton désactivé quand montant est zéro`() = runTest {
        val job = launch { viewModel.isTransferButtonEnabled.collect {} }
        viewModel.onRecipientChanged("Jean Dupont")
        viewModel.onAmountChanged("0")
        assertFalse(viewModel.isTransferButtonEnabled.value)
        job.cancel()
    }

    @Test
    fun `bouton désactivé quand montant est négatif`() = runTest {
        val job = launch { viewModel.isTransferButtonEnabled.collect {} }
        viewModel.onRecipientChanged("Jean Dupont")
        viewModel.onAmountChanged("-50")
        assertFalse(viewModel.isTransferButtonEnabled.value)
        job.cancel()
    }

    @Test
    fun `bouton activé quand bénéficiaire et montant valide`() = runTest {
        val job = launch { viewModel.isTransferButtonEnabled.collect {} }
        viewModel.onRecipientChanged("Jean Dupont")
        viewModel.onAmountChanged("100.0")
        advanceUntilIdle()
        assertTrue(viewModel.isTransferButtonEnabled.value)
        job.cancel()
    }

    // Tests de la fonction transfer()

    @Test
    fun `transfert réussi emet Success`() = runTest {
        coEvery { mockRepository.transfer(any(), any(), any()) } returns true

        viewModel.transfer("sender123", "recipient456", 100.0)

        assertEquals(TransferUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `transfert refusé par le serveur emet Error`() = runTest {
        coEvery { mockRepository.transfer(any(), any(), any()) } returns false

        viewModel.transfer("sender123", "recipient456", 100.0)

        assertEquals(TransferUiState.Error("Transfert refusé par le serveur"), viewModel.uiState.value)
    }

    @Test
    fun `exception réseau pendant transfert emet Error`() = runTest {
        coEvery { mockRepository.transfer(any(), any(), any()) } throws Exception("Network error")

        viewModel.transfer("sender123", "recipient456", 100.0)

        assertEquals(TransferUiState.Error("Network error"), viewModel.uiState.value)
    }
}