package example.model

sealed interface MainScreenEvent {
    data object Request : MainScreenEvent
}