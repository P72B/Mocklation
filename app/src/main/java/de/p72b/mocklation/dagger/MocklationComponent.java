package de.p72b.mocklation.dagger;

import javax.inject.Singleton;

import dagger.Component;
import de.p72b.mocklation.main.MainPresenter;
import de.p72b.mocklation.map.MapsPresenter;
import de.p72b.mocklation.service.location.MockLocationService;

@Singleton
@Component(modules = MocklationModule.class)
public interface MocklationComponent {

}
