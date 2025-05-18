package com.vivek.stockmarketapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vivek.stockmarketapp.data.remote.dto.IntradayInfoDto

@Entity
data class IntradayInfoEntity(
    @PrimaryKey val symbol: String,
    val stockInfos: List<IntradayInfoDto>?
)