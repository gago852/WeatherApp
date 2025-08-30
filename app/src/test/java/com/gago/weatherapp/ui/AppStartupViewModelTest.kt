package com.gago.weatherapp.ui

import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.fakes.FakeDataStore
import com.gago.weatherapp.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppStartupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `when initialized, isReady becomes true after datastore emits and delay`() = runTest {
        val dataStore = FakeDataStore(Settings())
        val vm = AppStartupViewModel(dataStore)

        // Initially false
        assertFalse(vm.isReady.first())

        // Let coroutine run and delay(300) pass
        advanceUntilIdle()
        advanceTimeBy(300)
        advanceUntilIdle()

        assertTrue(vm.isReady.first())
    }
}
