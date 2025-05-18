package com.vivek.stockmarketapp.data.mapper

import com.vivek.stockmarketapp.data.local.CompanyInfoEntity
import com.vivek.stockmarketapp.data.remote.dto.CompanyInfoDto
import com.vivek.stockmarketapp.domain.model.CompanyInfo

fun CompanyInfoDto.toCompanyInfoEntity(): CompanyInfoEntity {
    return CompanyInfoEntity(
        symbol = symbol ?: "",
        description = description,
        name = name,
        country = country,
        industry = industry,
        address = address
    )
}


fun CompanyInfoEntity.toCompanyInfo(): CompanyInfo {
    return CompanyInfo(
        symbol = symbol,
        description = description,
        name = name,
        country = country,
        industry = industry,
        address = address
    )
}
