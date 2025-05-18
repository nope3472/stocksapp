package com.vivek.stockmarketapp.data.repository

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import com.vivek.stockmarketapp.data.csv.CSVParser
import com.vivek.stockmarketapp.data.local.StockDatabase
import com.vivek.stockmarketapp.data.mapper.toCompanyInfo
import com.vivek.stockmarketapp.data.mapper.toCompanyInfoEntity
import com.vivek.stockmarketapp.data.mapper.toCompanyListing
import com.vivek.stockmarketapp.data.mapper.toCompanyListingEntity
import com.vivek.stockmarketapp.data.mapper.toIntradayEntity
import com.vivek.stockmarketapp.data.mapper.toIntradayInfoList
import com.vivek.stockmarketapp.data.remote.StockApi
import com.vivek.stockmarketapp.data.remote.dto.IntradayInfoDto
import com.vivek.stockmarketapp.domain.model.CompanyInfo
import com.vivek.stockmarketapp.domain.model.CompanyListing
import com.vivek.stockmarketapp.domain.model.IntradayInfo
import com.vivek.stockmarketapp.domain.repository.StockRepository
import com.vivek.stockmarketapp.core.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingParser: CSVParser<CompanyListing>,
    private val intradayInfoParser: CSVParser<IntradayInfoDto>
) : StockRepository {

    private val companyListingDao = db.companyListingDao
    private val companyInfoDao = db.companyInfoDao
    private val intradayInfoDao = db.intradayInfoDao

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(true))
            val localListings = companyListingDao.searchCompanyListing(query)
            emit(Resource.Success(data = localListings.map { it.toCompanyListing() }))

            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            if (shouldJustLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListings = try {
                val response = api.getListings()
                companyListingParser.parse(response.byteStream())
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }

            remoteListings?.let { listings ->
                companyListingDao.clearCompanyListings()
                companyListingDao.insertCompanyListings(
                    listings.map { it.toCompanyListingEntity() }
                )
                emit(Resource.Success(
                    data = companyListingDao
                        .searchCompanyListing("")
                        .map { it.toCompanyListing() }
                ))
                emit(Resource.Loading(false))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getIntradayInfo(
        fetchFromRemote: String,
        symbol: String
    ): Flow<Resource<List<IntradayInfo>>> {
        return flow {
            Log.d("StockRepo", "getIntradayInfo called for symbol: $symbol, fetchFromRemote: $fetchFromRemote")
            val today = LocalDate.now()
            val targetDay =
                if (today.dayOfWeek == java.time.DayOfWeek.MONDAY) today.minusDays(2)
                else today.minusDays(1)

            emit(Resource.Loading(true))
            val localIntradayInfo = intradayInfoDao.getIntradayInfo(symbol)
            Log.d("StockRepo", "Local DB IntradayInfoEntity: $localIntradayInfo")
            
            val isDbEmpty = localIntradayInfo == null || localIntradayInfo.stockInfos.isNullOrEmpty()
            val shouldJustLoadFromCache = !isDbEmpty && fetchFromRemote != "true"
            
            if (!isDbEmpty) {
                val intradayList = localIntradayInfo.toIntradayInfoList()
                    .sortedBy { it.date }
                Log.d("StockRepo", "Parsed local intradayList: $intradayList")
                emit(Resource.Success(data = intradayList))
            }

            if (shouldJustLoadFromCache) {
                Log.d("StockRepo", "Should just load from cache, stopping fetch.")
                emit(Resource.Loading(false))
                return@flow
            }

            Log.d("StockRepo", "Fetching from remote API...")
            val remoteIntradayInfo = try {
                val response = api.getIntradayInfo(symbol)
                val responseString = response.string()
                Log.d("StockRepo", "Raw API response for $symbol: $responseString")
                val inputStream = responseString.byteInputStream()
                intradayInfoParser.parse(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("StockRepo", "IOException: ${e.message}")
                emit(Resource.Error("Couldn't load data."))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                Log.e("StockRepo", "HttpException: ${e.message}")
                emit(Resource.Error("Couldn't load data"))
                null
            }

            Log.d("StockRepo", "Parsed remoteIntradayInfo: $remoteIntradayInfo")
            remoteIntradayInfo?.let {
                intradayInfoDao.deleteIntradayInfo(symbol)
                intradayInfoDao.insertIntradayInfo(it.toIntradayEntity(symbol))
                val newIntradayInfo = intradayInfoDao.getIntradayInfo(symbol).toIntradayInfoList()
                    .sortedBy { it.date }
                Log.d("StockRepo", "Newly saved intraday info from remote: $newIntradayInfo")
                emit(Resource.Success(data = newIntradayInfo))
                emit(Resource.Loading(false))
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getCompanyInfo(symbol: String): Flow<Resource<CompanyInfo>> {
        return flow {
            try {
                emit(Resource.Loading(true))
                val localCompanyInfo = companyInfoDao.getCompanyInfo(symbol)

                if (localCompanyInfo != null) {
                    emit(Resource.Success(data = localCompanyInfo.toCompanyInfo()))
                } else {
                    val result = api.getCompanyInfo(symbol)
                    result?.let {
                        companyInfoDao.deleteCompanyInfo(symbol)
                        companyInfoDao.insertCompanyInfo(result.toCompanyInfoEntity())
                        val newCompanyInfo = companyInfoDao.getCompanyInfo(symbol).toCompanyInfo()
                        emit(Resource.Success(newCompanyInfo))
                    }
                }
            } catch (e: retrofit2.HttpException) {
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            } catch (e: IOException) {
                emit(Resource.Error("Couldn't reach server. Check your internet connection."))
            } catch (e: Exception) {
                emit(Resource.Error("Data not available"))
            }
        }
    }
}