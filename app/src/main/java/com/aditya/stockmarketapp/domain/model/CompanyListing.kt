package com.aditya.stockmarketapp.domain.model

data class CompanyListing(
    val name: String,
    val symbol: String,
    val exchange: String,
    val price: Double,
    val priceChange: Double,
    val priceChangePercent: Double
) 