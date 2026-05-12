package com.aura.ui.home

import com.aura.data.repository.AuraRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockRepository: AuraRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk()
        viewModel = HomeViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `état initial est Idle`() = runTest {
        assertEquals(HomeUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `loadBalance réussi emet Success avec la balance`() = runTest {
        coEvery { mockRepository.getAccounts("user123") } returns 1500.50

        viewModel.loadBalance("user123")

        assertEquals(HomeUiState.Success(1500.50), viewModel.uiState.value)
    }

    @Test
    fun `loadBalance en erreur emet Error avec le message`() = runTest {
        coEvery { mockRepository.getAccounts("user123") } throws Exception("Pas de connexion")

        viewModel.loadBalance("user123")

        assertEquals(HomeUiState.Error("Pas de connexion"), viewModel.uiState.value)
    }
}