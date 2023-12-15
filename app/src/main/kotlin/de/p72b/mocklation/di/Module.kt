package de.p72b.mocklation.di

import de.p72b.mocklation.service.ForegroundServiceInteractor
import de.p72b.mocklation.service.RequirementsService
import de.p72b.mocklation.ui.Navigator
import de.p72b.mocklation.ui.model.collection.CollectionViewModel
import de.p72b.mocklation.ui.model.dashboard.DashboardViewModel
import de.p72b.mocklation.ui.model.requirements.RequirementsViewModel
import de.p72b.mocklation.ui.model.simulation.SimulationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { RequirementsService(get()) }
    single { ForegroundServiceInteractor(get()) }
    single { Navigator() }
    single { RequirementsViewModel(get(), get()) }
    viewModel { CollectionViewModel() }
    viewModel { DashboardViewModel() }
    viewModel { SimulationViewModel(get()) }
}