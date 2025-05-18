package com.aditya.stockmarketapp.presentation.company_listings

import com.aditya.stockmarketapp.domain.model.CompanyListing

data class CompanyListingsState(
    val companies: List<CompanyListing> = emptyList(),
    val topGainers: List<CompanyListing> = emptyList(),
    val topLosers: List<CompanyListing> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null
) 