package com.ipca.lojasocial.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.lojasocial.data.repository.ApplicationRepositoryImpl
import com.ipca.lojasocial.data.repository.AuthRepositoryImpl
import com.ipca.lojasocial.data.repository.BeneficiaryRepositoryImpl
import com.ipca.lojasocial.data.repository.CampaignRepositoryImpl
import com.ipca.lojasocial.data.repository.DeliveryRepositoryImpl
import com.ipca.lojasocial.data.repository.KitRepositoryImpl
import com.ipca.lojasocial.data.repository.ProductRepositoryImpl
import com.ipca.lojasocial.data.repository.ReportsRepositoryImpl
import com.ipca.lojasocial.domain.repository.ApplicationRepository
import com.ipca.lojasocial.domain.repository.AuthRepository
import com.ipca.lojasocial.domain.repository.BeneficiaryRepository
import com.ipca.lojasocial.domain.repository.CampaignRepository
import com.ipca.lojasocial.domain.repository.DeliveryRepository
import com.ipca.lojasocial.domain.repository.KitRepository
import com.ipca.lojasocial.domain.repository.ProductRepository
import com.ipca.lojasocial.domain.repository.ReportsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        firestore: FirebaseFirestore
    ): ProductRepository {
        return ProductRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideBeneficiaryRepository(
        firestore: FirebaseFirestore
    ): BeneficiaryRepository {
        return BeneficiaryRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideKitRepository(
        firestore: FirebaseFirestore
    ): KitRepository {
        return KitRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideDeliveryRepository(
        firestore: FirebaseFirestore
    ): DeliveryRepository {
        return DeliveryRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideCampaignRepository(
        firestore: FirebaseFirestore
    ): CampaignRepository {
        return CampaignRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideReportsRepository(
        firestore: FirebaseFirestore
    ): ReportsRepository {
        return ReportsRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideApplicationRepository(
        firestore: FirebaseFirestore
    ): ApplicationRepository {
        return ApplicationRepositoryImpl(firestore)
    }
}