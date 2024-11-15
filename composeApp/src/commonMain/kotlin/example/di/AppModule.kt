package example.di

import org.koin.ksp.generated.module

fun appModule() = listOf(
    PresentationModule().module
)
