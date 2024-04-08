package de.p72b.mocklation.di

import de.p72b.mocklation.data.FeatureRepository
import de.p72b.mocklation.data.PreferencesRepository
import de.p72b.mocklation.data.mapper.FeatureEntityMapper
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
import de.p72b.mocklation.usecase.DeleteFeatureUseCase
import de.p72b.mocklation.usecase.GetFeatureUseCase
import de.p72b.mocklation.usecase.SetFeatureUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { PreferencesRepository(get()) }
    single { Navigator() }
    single {
        FeatureDatabase.provide(
            context = get()
        )
        FeatureRepository(
            featureDatabase = FeatureDatabase.database,
            featureMapper = FeatureMapper(),
            featureEntityMapper = FeatureEntityMapper(),
        )
    }
    single { RequirementsService(get()) }
    single { ForegroundServiceInteractor(get()) }
    factory {
        GetCollectionUseCase(
            repository = get(),
        )
    }
    factory {
        DeleteFeatureUseCase(
            repository = get(),
        )
    }
    factory {
        SetFeatureUseCase(
            repository = get(),
        )
    }
    factory {
        GetFeatureUseCase(
            repository = get(),
        )
    }
    single { RequirementsViewModel(get(), get()) }
    single {
        CollectionViewModel(
            getCollectionUseCase = get(),
            preferencesRepository = get(),
            deleteFeatureUseCase = get(),
            simulationService = get(),
        )
    }
    viewModel { DashboardViewModel() }
    viewModel {
        SimulationViewModel(
            get(),
            get(),
            get()
        )
    }
    viewModel {
        MapViewModel(
            get(),
        )
    }
}