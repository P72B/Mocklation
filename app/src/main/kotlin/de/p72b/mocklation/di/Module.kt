package de.p72b.mocklation.di

import de.p72b.mocklation.service.ForegroundServiceInteractor
import de.p72b.mocklation.ui.model.simulation.SimulationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ForegroundServiceInteractor(get()) }
    viewModel { SimulationViewModel(get()) }
}