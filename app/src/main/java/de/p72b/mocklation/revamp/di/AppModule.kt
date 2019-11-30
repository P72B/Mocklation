package de.p72b.mocklation.revamp.di

import de.p72b.mocklation.revamp.arch.LocationRepository
import org.koin.dsl.module

val appModule = module {
    single { LocationRepository() }
}