package de.p72b.mocklation.dagger;

import javax.inject.Singleton;

import dagger.Component;
import de.p72b.mocklation.map.MapsPresenter;
import de.p72b.mocklation.service.location.MockLocationService;

@Singleton
@Component(modules = MocklationModule.class)
public interface MocklationComponent {

    void inject(MockLocationService mockLocationService);

    void inject(MapsPresenter mapsPresenter);
}
