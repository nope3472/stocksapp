package com.vivek.stockmarketapp.di

import com.vivek.stockmarketapp.data.csv.CSVParser
import com.vivek.stockmarketapp.data.csv.CompanyListingParser
import com.vivek.stockmarketapp.data.csv.IntradayInfoParser
import com.vivek.stockmarketapp.data.remote.dto.IntradayInfoDto
import com.vivek.stockmarketapp.data.repository.StockRepositoryImpl
import com.vivek.stockmarketapp.domain.model.CompanyListing
import com.vivek.stockmarketapp.domain.repository.StockRepository
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
    abstract fun bindCompanyListingParser(
        companyListingParser: CompanyListingParser
    ): CSVParser<CompanyListing>

    @Binds
    @Singleton
    abstract fun bindIntradayInfoParser(
        intradayInfoParser: IntradayInfoParser
    ): CSVParser<IntradayInfoDto>

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        stockRepositoryImpl: StockRepositoryImpl
    ): StockRepository
}