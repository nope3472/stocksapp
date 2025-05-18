package com.vivek.stockmarketapp.domain.repository

import com.vivek.stockmarketapp.domain.model.CompanyInfo
import com.vivek.stockmarketapp.domain.model.CompanyListing
import com.vivek.stockmarketapp.domain.model.IntradayInfo
import com.vivek.stockmarketapp.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>

    suspend fun getIntradayInfo(
        fetchFromRemote: String,
        symbol: String
    ): Flow<Resource<List<IntradayInfo>>>

    suspend fun getCompanyInfo(
        symbol: String
    ): Flow<Resource<CompanyInfo>>
}

