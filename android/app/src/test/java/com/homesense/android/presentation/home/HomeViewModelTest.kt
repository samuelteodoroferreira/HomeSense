package com.homesense.android.presentation.home

import com.homesense.android.domain.model.DashboardSnapshot
import com.homesense.android.domain.model.EnergyRestoredNotification
import com.homesense.android.domain.repository.DashboardRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `notificacao Energia Restaurada insere card no historico`() = runTest {
        val repo = mockk<DashboardRepository>()
        coEvery { repo.loadDashboard(HomeViewModel.DEFAULT_DEVICE_ID) } returns DashboardSnapshot(
            temperatureC = 24.0,
            humidityPercent = 55.0,
            climateRecordedAt = "2024-10-12T14:00:00Z",
            outageStartsIso = emptyList(),
        )
        val vm = HomeViewModel(repo)
        assertTrue(vm.uiState.value.energyHistory.isEmpty())

        vm.onEnergyRestoredNotification(
            EnergyRestoredNotification(
                outageDate = "12/10",
                outageTime = "14:30",
                durationText = "2 horas, 15 min.",
            ),
        )

        val history = vm.uiState.value.energyHistory
        assertEquals(1, history.size)
        val card = history.first() as EnergyHistoryUiItem.EnergyRestoredCard
        assertEquals("12/10", card.outageDate)
        assertEquals("14:30", card.outageTime)
        assertEquals("2 horas, 15 min.", card.durationText)
    }

    @Test
    fun `refresh mantem card de Energia Restaurada e acrescenta quedas da API`() = runTest {
        val repo = mockk<DashboardRepository>()
        coEvery { repo.loadDashboard(HomeViewModel.DEFAULT_DEVICE_ID) } returnsMany listOf(
            DashboardSnapshot(22.0, 40.0, "t1", emptyList()),
            DashboardSnapshot(22.0, 40.0, "t1", listOf("2024-10-11T08:00:00Z")),
        )
        val vm = HomeViewModel(repo)
        vm.onEnergyRestoredNotification(
            EnergyRestoredNotification("10/10", "09:00", "30 min."),
        )
        assertEquals(1, vm.uiState.value.energyHistory.size)

        vm.refresh(HomeViewModel.DEFAULT_DEVICE_ID)

        val merged = vm.uiState.value.energyHistory
        assertEquals(2, merged.size)
        assertTrue(merged.first() is EnergyHistoryUiItem.EnergyRestoredCard)
        assertTrue(merged[1] is EnergyHistoryUiItem.RecordedOutage)
    }
}
