package de.p72b.mocklation.di

import de.p72b.mocklation.data.FeatureRepository
import de.p72b.mocklation.data.mapper.FeatureMapper
import de.p72b.mocklation.data.room.FeatureDatabase
import de.p72b.mocklation.usecase.GetCollectionUseCase
import de.p72b.mocklation.service.ForegroundServiceInteractor
import de.p72b.mocklation.service.RequirementsService
import de.p72b.mocklation.ui.Navigator
import de.p72b.mocklation.ui.model.collection.CollectionViewModel
import de.p72b.mocklation.ui.model.dashboard.DashboardViewModel
import de.p72b.mocklation.ui.model.map.MapViewModel
import de.p72b.mocklation.ui.model.requirements.RequirementsViewModel
import de.p72b.mocklation.ui.model.simulation.SimulationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Navigator() }
    single {
        FeatureDatabase.provide(
            context = get()
        )
        FeatureRepository(
            featureDatabase = FeatureDatabase.database,
            featureMapper = FeatureMapper()
        )
    }
    single { RequirementsService(get()) }
    single { ForegroundServiceInteractor(get()) }
    factory {
        GetCollectionUseCase(
            repository = get()
        )
    }
    single { RequirementsViewModel(get(), get()) }
    viewModel {
        CollectionViewModel(
            getCollectionUseCase = get()
        )
    }
    viewModel { DashboardViewModel() }
    viewModel { SimulationViewModel(get()) }
    viewModel { MapViewModel() }
}