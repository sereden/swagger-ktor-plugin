package example.domain.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import example.model.MainScreenEvent
import example.model.MainScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent

@Factory
class MainViewModel : ViewModel(), KoinComponent {
    private val screenState = MutableStateFlow(MainScreenState())
    val screen = screenState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() = viewModelScope.launch {

    }

    fun onEvent(event: MainScreenEvent) {

    }
}