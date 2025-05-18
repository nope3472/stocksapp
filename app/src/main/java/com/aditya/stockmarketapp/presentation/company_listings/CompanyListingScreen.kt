package com.aditya.stockmarketapp.presentation.company_listings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.stockmarketapp.presentation.destinations.CompanyInfoScreenDestination
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight

@RootNavGraph(start = true)
@Destination
@Composable
fun CompanyListingScreen(
    navigator: DestinationsNavigator,
    viewModel: CompanyListingViewModel = hiltViewModel()
) {
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = viewModel.state.isRefreshing
    )
    val state = viewModel.state
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf<MarketSection?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = {
                    viewModel.onEvent(
                        CompanyListingsEvent.OnSearchQueryChange(it)
                    )
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                placeholder = {
                    Text(text = "Search...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                },
                maxLines = 1,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
            )

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    viewModel.onEvent(CompanyListingsEvent.Refresh)
                }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Gainers Section
                    item {
                        MarketSectionHeader(
                            title = "Top Gainers",
                            onViewAllClick = {
                                selectedSection = MarketSection.GAINERS
                                showBottomSheet = true
                            }
                        )
                    }
                    items(state.topGainers.take(4)) { company ->
                        CompanyItem(
                            company = company,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navigator.navigate(
                                        CompanyInfoScreenDestination(
                                            symbol = company.symbol
                                        )
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Top Losers Section
                    item {
                        MarketSectionHeader(
                            title = "Top Losers",
                            onViewAllClick = {
                                selectedSection = MarketSection.LOSERS
                                showBottomSheet = true
                            }
                        )
                    }
                    items(state.topLosers.take(4)) { company ->
                        CompanyItem(
                            company = company,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navigator.navigate(
                                        CompanyInfoScreenDestination(
                                            symbol = company.symbol
                                        )
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (state.error.isNotBlank()) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .align(Alignment.Center)
            )
        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Bottom Sheet
        if (showBottomSheet) {
            MarketSectionBottomSheet(
                section = selectedSection,
                companies = when (selectedSection) {
                    MarketSection.GAINERS -> state.topGainers
                    MarketSection.LOSERS -> state.topLosers
                    null -> emptyList()
                },
                onDismiss = { showBottomSheet = false },
                onCompanyClick = { company ->
                    navigator.navigate(
                        CompanyInfoScreenDestination(
                            symbol = company.symbol
                        )
                    )
                    showBottomSheet = false
                }
            )
        }
    }
}

@Composable
private fun MarketSectionHeader(
    title: String,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onViewAllClick) {
            Text(
                text = "View All",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MarketSectionBottomSheet(
    section: MarketSection?,
    companies: List<CompanyListing>,
    onDismiss: () -> Unit,
    onCompanyClick: (CompanyListing) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = when (section) {
                    MarketSection.GAINERS -> "Top Gainers"
                    MarketSection.LOSERS -> "Top Losers"
                    null -> ""
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyColumn {
                items(companies) { company ->
                    CompanyItem(
                        company = company,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCompanyClick(company) }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

enum class MarketSection {
    GAINERS,
    LOSERS
}