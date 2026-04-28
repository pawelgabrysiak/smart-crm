package com.example.smartcrm.di

import android.content.Context
import androidx.room.Room
import com.example.smartcrm.data.AppDatabase
import com.example.smartcrm.data.ClientDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder( // tworzymy baze danych
            context,
            AppDatabase::class.java,
            "smart_crm_db"
        ).build()
    }

    @Provides
    fun provideClientDao(database: AppDatabase): ClientDao {
        return database.clientDao() // wywolanie funkcji z dao
    }
}

