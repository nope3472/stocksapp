package com.vivek.stockmarketapp.presentation.company_info

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.clickable
import com.vivek.stockmarketapp.domain.model.IntradayInfo

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Destination
fun CompanyInfoScreen(
    symbol: String,
    viewModel: CompanyInfoViewModel = hiltViewModel()
) {
    val companyInfoState = viewModel.companyInfoState
    val stockInfosState = viewModel.stockInfosState
    val scrollState = rememberScrollState()
    
    // UI State
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var isSectionExpanded by remember { mutableStateOf(true) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.MONTH) }
    
    // Animation states
    val arrowRotation by animateFloatAsState(
        targetValue = if (isDescriptionExpanded) 180f else 0f,
        animationSpec = tween(300)
    )
    
    // LOGGING: Print stockInfosState and chartData
    LaunchedEffect(stockInfosState.stockInfos) {
        Log.d("StockUI", "Composable received stockInfos: ${stockInfosState.stockInfos}")
    }
    
    val HEADER_HEIGHT = 100.dp // Adjust as needed for your header height
    Box(modifier = Modifier.fillMaxSize()) {
        // Header (fixed at the top)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = companyInfoState.company?.name ?: "-",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFF1565C0),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = companyInfoState.company?.symbol ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
        // Main content (scrollable)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = HEADER_HEIGHT, start = 0.dp, end = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            // Floating Price Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 10.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (stockInfosState.stockInfos.isNotEmpty()) {
                        val latestData = stockInfosState.stockInfos.lastOrNull()
                        val highPrice = latestData?.high ?: 0.0
                        val lowPrice = latestData?.low ?: 0.0
                        val priceChange = highPrice - lowPrice
                        val priceChangePercent = if (lowPrice != 0.0) {
                            ((highPrice - lowPrice) / lowPrice) * 100
                        } else 0.0
                        val isPositive = priceChange >= 0

                        Text(
                            text = "$${String.format("%.2f", highPrice)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isPositive) "Price Up" else "Price Down",
                                tint = if (isPositive) Color(0xFF0DA678) else Color(0xFFE91E63),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "${String.format("%.2f", priceChangePercent)}%",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = if (isPositive) Color(0xFF0DA678) else Color(0xFFE91E63)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Chart Time Range Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeRange.values().forEach { range ->
                    TimeRangeChip(
                        range = range,
                        isSelected = selectedTimeRange == range,
                        onSelect = { selectedTimeRange = range }
                    )
                }
            }
            // Chart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(300.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (stockInfosState.stockInfos.isNotEmpty()) {
                        val latestData = stockInfosState.stockInfos.lastOrNull()
                        val highPrice = latestData?.high ?: 0.0
                        val lowPrice = latestData?.low ?: 0.0
                        val priceChange = highPrice - lowPrice
                        val priceChangePercent = if (lowPrice != 0.0) {
                            ((highPrice - lowPrice) / lowPrice) * 100
                        } else 0.0

                        StockChart(
                            infos = stockInfosState.stockInfos,
                            modifier = Modifier.fillMaxSize(),
                            graphColor = if (priceChangePercent >= 0) Color.Green else Color.Red
                        )
                    } else {
                        Text(
                            text = "No chart data available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            // Quick Stats Row (pills)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!companyInfoState.company?.industry.isNullOrBlank()) {
                        StatPill(
                            icon = Icons.Outlined.TrendingUp,
                            label = companyInfoState.company?.industry ?: ""
                        )
                    }
                    if (!companyInfoState.company?.country.isNullOrBlank()) {
                        StatPill(
                            icon = Icons.Outlined.LocationOn,
                            label = companyInfoState.company?.country ?: ""
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            // Company Details Section
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    // Section header with gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
                                        ),
                                        shape = CircleShape
                                    )
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Company Info",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Company Details",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Overview and key information",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Company description card with expandable text
                    val maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 4
                    val isOverflowing = remember { mutableStateOf(false) }
                    
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "About",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "About ${companyInfoState.company?.name ?: "Company"}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = companyInfoState.company?.description 
                                            ?: "No description available for this company.",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = maxLines,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 20.sp,
                                        onTextLayout = { textLayoutResult ->
                                            isOverflowing.value = textLayoutResult.hasVisualOverflow
                                        }
                                    )
                                }
                            }
                            
                            if (isOverflowing.value || isDescriptionExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { isDescriptionExpanded = !isDescriptionExpanded },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        text = if (isDescriptionExpanded) "Read Less" else "Read More",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Icon(
                                        imageVector = if (isDescriptionExpanded) 
                                            Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isDescriptionExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .rotate(arrowRotation)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Company facts in a table-like format with alternating backgrounds
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            CompanyInfoRowEnhanced(
                                icon = Icons.Outlined.TrendingUp,
                                label = "Industry",
                                value = companyInfoState.company?.industry ?: "N/A",
                                isAlternate = false
                            )
                            CompanyInfoRowEnhanced(
                                icon = Icons.Outlined.LocationOn,
                                label = "Country",
                                value = companyInfoState.company?.country ?: "N/A",
                                isAlternate = true
                            )
                            if (!companyInfoState.company?.address.isNullOrBlank()) {
                                CompanyInfoRowEnhanced(
                                    icon = Icons.Outlined.LocationOn,
                                    label = "Headquarters",
                                    value = companyInfoState.company?.address ?: "N/A",
                                    isAlternate = false
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Loading and Error states
        if (companyInfoState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else if (companyInfoState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = companyInfoState.error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeRangeChip(
    range: TimeRange,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .height(32.dp)
            .shadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        onClick = onSelect
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = range.displayName,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .padding(bottom = 4.dp)
        )
        
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatPill(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
        modifier = Modifier
            .heightIn(min = 36.dp)
            .padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Clip,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CompanyInfoRowEnhanced(
    icon: ImageVector,
    label: String,
    value: String,
    isAlternate: Boolean
) {
    Surface(
        color = if (isAlternate) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
               else MaterialTheme.colorScheme.background
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.width(120.dp)
            )
            Text(
                text = value,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

enum class TimeRange(val displayName: String) {
    DAY("1D"),
    WEEK("1W"),
    MONTH("1M"),
    YEAR("1Y"),
    MAX("MAX")
}