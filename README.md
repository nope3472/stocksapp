
# Stock Market App

A modern Android application built with Jetpack Compose that provides real-time stock market information with a clean, intuitive interface.

## Features

### 1. Market Overview
- **Top Gainers Section**
  - Displays top 4 performing stocks
  - Shows company name, symbol, and price change
  - "View All" option to see complete list
  - Bottom sheet with full list of gainers

- **Top Losers Section**
  - Displays bottom 4 performing stocks
  - Shows company name, symbol, and price change
  - "View All" option to see complete list
  - Bottom sheet with full list of losers

### 2. Company Details
- **Price Information**
  - Current stock price
  - Price change (absolute and percentage)
  - Visual indicators for positive/negative changes

- **Interactive Chart**
  - Multiple time ranges (1D, 1W, 1M, 1Y, MAX)
  - Color-coded based on performance
  - Smooth animations

- **Company Information**
  - Company name and symbol
  - Industry and country
  - Detailed description (expandable)
  - Headquarters location

### 3. Search Functionality
- Real-time search across all companies
- Debounced search to prevent excessive API calls
- Clear visual feedback for search results

## Technical Architecture

### Clean Architecture
The app follows Clean Architecture principles with three main layers:

1. **Presentation Layer**
   - UI components using Jetpack Compose
   - ViewModels for state management
   - Navigation using Compose Destinations

2. **Domain Layer**
   - Business logic and use cases
   - Repository interfaces
   - Domain models

3. **Data Layer**
   - Repository implementations
   - Remote data sources
   - Local caching

### Key Technologies
- **Jetpack Compose** for modern UI
- **Material 3** for design system
- **Hilt** for dependency injection
- **Coroutines** for asynchronous operations
- **Flow** for reactive programming
- **Retrofit** for network calls
- **Room** for local storage

## Data Flow

### 1. Stock Listings Flow
```
UI (CompanyListingScreen)
    ↓
ViewModel (CompanyListingViewModel)
    ↓
Repository (StockRepository)
    ↓
Data Sources:
├── Remote (Alpha Vantage API)
│   ├── GET /query?function=LISTING_STATUS
│   └── GET /query?function=GLOBAL_QUOTE
└── Local (Room Database)
    ├── Cache stock listings
    └── Cache company details
```

### 2. Company Details Flow
```
UI (CompanyInfoScreen)
    ↓
ViewModel (CompanyInfoViewModel)
    ↓
Repository (StockRepository)
    ↓
Data Sources:
├── Remote (Alpha Vantage API)
│   ├── GET /query?function=OVERVIEW
│   └── GET /query?function=TIME_SERIES_INTRADAY
└── Local (Room Database)
    ├── Cache company overview
    └── Cache intraday data
```

### 3. Search Flow
```
User Input
    ↓
Debounce (500ms)
    ↓
ViewModel
    ↓
Repository
    ↓
Filter Results:
├── Local Cache (Primary)
└── Remote API (Fallback)
```

### 4. Data Caching Strategy
```
Remote API Call
    ↓
Success → Update Local Cache
    ↓
Error → Use Cached Data
    ↓
Cache Expiry (24 hours)
    ↓
Force Refresh on Pull-to-Refresh
```

### 5. State Management
```
UI State (CompanyListingsState)
    ↓
├── Loading State
├── Error State
├── Success State
│   ├── Top Gainers
│   ├── Top Losers
│   └── Search Results
└── Empty State
```

### 6. Error Handling Flow
```
API Error
    ↓
Repository
    ↓
├── Network Error → Show Retry
├── Server Error → Show Error Message
└── Cache Miss → Show Loading
    ↓
ViewModel
    ↓
UI (Error States)
```

### 7. Real-time Updates
```
Pull-to-Refresh
    ↓
Force Remote Fetch
    ↓
Update Local Cache
    ↓
Update UI State
    ↓
Refresh All Sections
```

## App Flow

1. **Launch**
   - App starts with CompanyListingScreen
   - Fetches initial stock data
   - Displays loading indicator

2. **Main Screen (CompanyListingScreen)**
   - Shows search bar at top
   - Displays Top Gainers section (4 cards)
   - Shows Top Losers section (4 cards)
   - Each section has "View All" option
   - Pull-to-refresh functionality

3. **View All Bottom Sheet**
   - Opens when "View All" is clicked
   - Takes 80% of screen height
   - Shows complete list of stocks
   - Maintains search functionality
   - Smooth animations for opening/closing

4. **Company Details (CompanyInfoScreen)**
   - Opens when a stock is selected
   - Shows detailed company information
   - Interactive price chart
   - Expandable company description
   - Company details in cards

5. **Search Flow**
   - Real-time search as user types
   - 500ms debounce to prevent API spam
   - Updates both main list and sections
   - Clear error handling

## UI/UX Features

### Light Theme
- Clean white background
- Green accent for positive changes
- Red accent for negative changes
- High contrast for readability
- Consistent card design

### Animations
- Smooth transitions between screens
- Bottom sheet animations
- Loading indicators
- Price change animations
- Chart interactions

### Error Handling
- Clear error messages
- Retry options
- Offline support
- Loading states

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run the app

## Dependencies

```gradle
// Core
implementation 'androidx.core:core-ktx:1.12.0'
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
implementation 'androidx.activity:activity-compose:1.8.2'

// Compose
implementation platform('androidx.compose:compose-bom:2024.02.00')
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.ui:ui-graphics'
implementation 'androidx.compose.ui:ui-tooling-preview'
implementation 'androidx.compose.material3:material3'

// Navigation
implementation 'com.ramcosta.composedestinations:core:1.9.55'
implementation 'com.ramcosta.composedestinations:animations-core:1.9.55'

// Hilt
implementation 'com.google.dagger:hilt-android:2.50'
kapt 'com.google.dagger:hilt-android-compiler:2.50'

// Retrofit
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

// Room
implementation 'androidx.room:room-runtime:2.6.1'
implementation 'androidx.room:room-ktx:2.6.1'
kapt 'androidx.room:room-compiler:2.6.1'
```

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.


## Technologies and Architecture Used:

- Clean Architecture with MVVM
- Kotlin
- Jetpack Compose for UI
- Dagger Hilt for Dependency Injection
- Kotlin Coroutines & Flows
- Retrofit for Rest API integration
- Room Database for Local Caching
- Compose Destinations for Navigation
- Lottie animation

## Acknowledgments
- [Alpha Vantage API](https://www.alphavantage.co/) for providing an extensive array of stocks information.
- [Compose Destinations](https://github.com/raamcosta/compose-destinations) for providing a wonderful library which makes the navigation in jetpack compose much smoother.



