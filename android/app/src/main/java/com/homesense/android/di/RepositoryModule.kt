package com.homesense.android.di

import com.homesense.android.data.repository.DashboardRepositoryImpl
import com.homesense.android.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun dashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}
