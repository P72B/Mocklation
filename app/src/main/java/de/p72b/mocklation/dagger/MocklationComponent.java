package de.p72b.mocklation.dagger;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = MocklationModule.class)
public interface MocklationComponent {

}
