package com.spiritual.hub.viewmodel;

import android.app.Application;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class ChalisaViewModel_Factory implements Factory<ChalisaViewModel> {
  private final Provider<Application> appProvider;

  public ChalisaViewModel_Factory(Provider<Application> appProvider) {
    this.appProvider = appProvider;
  }

  @Override
  public ChalisaViewModel get() {
    return newInstance(appProvider.get());
  }

  public static ChalisaViewModel_Factory create(Provider<Application> appProvider) {
    return new ChalisaViewModel_Factory(appProvider);
  }

  public static ChalisaViewModel newInstance(Application app) {
    return new ChalisaViewModel(app);
  }
}
