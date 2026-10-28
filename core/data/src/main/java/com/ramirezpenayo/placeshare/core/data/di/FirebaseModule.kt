package com.ramirezpenayo.placeshare.core.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramirezpenayo.placeshare.core.data.repository.AuthRepositoryImpl
import com.ramirezpenayo.placeshare.core.data.repository.ExploreRepositoryImpl
import com.ramirezpenayo.placeshare.core.data.repository.NotificationRepositoryImpl
import com.ramirezpenayo.placeshare.core.data.repository.PlaceRepositoryImpl
import com.ramirezpenayo.placeshare.core.data.repository.UserRepositoryImpl
import com.ramirezpenayo.placeshare.core.domain.repository.AuthRepository
import com.ramirezpenayo.placeshare.core.domain.repository.ExploreRepository
import com.ramirezpenayo.placeshare.core.domain.repository.NotificationRepository
import com.ramirezpenayo.placeshare.core.domain.repository.PlaceRepository
import com.ramirezpenayo.placeshare.core.domain.repository.UserRepository
import com.ramirezpenayo.placeshare.core.network.CloudinaryUploader
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCloudinaryUploader(): CloudinaryUploader {
        return CloudinaryUploader()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindExploreRepository(
        impl: ExploreRepositoryImpl
    ): ExploreRepository

    @Binds
    @Singleton
    abstract fun bindPlaceRepository(
        impl: PlaceRepositoryImpl
    ): PlaceRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository
}
