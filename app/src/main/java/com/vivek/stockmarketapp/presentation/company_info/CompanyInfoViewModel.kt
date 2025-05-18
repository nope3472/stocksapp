package com.vivek.stockmarketapp.presentation.company_info

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vivek.stockmarketapp.domain.repository.StockRepository
import com.vivek.stockmarketapp.core.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: StockRepository
) : ViewModel() {

    private lateinit var stockSymbol: String
    var companyInfoState by mutableStateOf(CompanyInfoState())
    var stockInfosState by mutableStateOf(StockInfosState())

    init {
        viewModelScope.launch {
            val symbol = savedStateHandle.get<String>("symbol") ?: return@launch
            stockSymbol = symbol
            Log.d("StockVM", "Initializing view model for symbol: $symbol")
            getCompanyInfo(stockSymbol)
            getStockInfos(symbol = stockSymbol)
        }
    }

    private fun getCompanyInfo(symbol: String) {
        viewModelScope.launch {
            Log.d("StockVM", "Fetching company info for symbol: $symbol")
            repository.getCompanyInfo(symbol).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                        Log.d("StockVM", "Successfully fetched company info: ${result.data}")
                            companyInfoState = companyInfoState.copy(
                                company = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                        is Resource.Error -> {
                        Log.e("StockVM", "Error fetching company info: ${result.message}")
                            companyInfoState = companyInfoState.copy(
                                isLoading = false,
                            error = result.message
                            )
                        }
                        is Resource.Loading -> {
                        Log.d("StockVM", "Loading company info...")
                            companyInfoState = companyInfoState.copy(
                            isLoading = result.isLoading
                            )
                        }
                    }
                }
        }
    }

    private fun getStockInfos(fetchFromRemote: String = "false", symbol: String) {
        viewModelScope.launch {
            Log.d("StockVM", "Fetching stock infos for symbol: $symbol, fetchFromRemote: $fetchFromRemote")
            repository.getIntradayInfo(fetchFromRemote, symbol)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("StockVM", "Successfully fetched stock infos: ${result.data?.size} entries")
                            stockInfosState = stockInfosState.copy(
                                stockInfos = result.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            Log.e("StockVM", "Error fetching stock infos: ${result.message}")
                            stockInfosState = stockInfosState.copy(
                                isLoading = false,
                                error = result.message,
                                stockInfos = emptyList()
                            )
                        }
                        is Resource.Loading -> {
                            Log.d("StockVM", "Loading stock infos...")
                            stockInfosState = stockInfosState.copy(
                                isLoading = result.isLoading
                            )
                        }
                    }
                }
        }
    }
}