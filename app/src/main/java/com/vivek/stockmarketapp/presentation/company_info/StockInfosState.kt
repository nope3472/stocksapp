package com.vivek.stockmarketapp.presentation.company_info

import com.vivek.stockmarketapp.domain.model.IntradayInfo

data class StockInfosState(
    val stockInfos: List<IntradayInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)