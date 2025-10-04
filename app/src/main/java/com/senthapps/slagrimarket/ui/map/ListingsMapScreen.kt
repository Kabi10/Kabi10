package com.senthapps.slagrimarket.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsMapScreen(
    listings: List<Listing>,
    onNavigateBack: () -> Unit,
    onListingClick: (String) -> Unit,
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    
    // Default to Jaffna, Sri Lanka
    val defaultLocation = LatLng(9.6615, 80.0255)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Listings Map"
                            "ta" -> "பட்டியல் வரைபடம்"
                            "si" -> "ලැයිස්තු සිතියම"
                            else -> "Listings Map"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "${listings.size} listings shown on map"
                            "ta" -> "${listings.size} பட்டியல்கள் வரைபடத்தில் காட்டப்பட்டுள்ளன"
                            "si" -> "ලැයිස්තු ${listings.size} සිතියමේ පෙන්වා ඇත"
                            else -> "${listings.size} listings shown on map"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                // Add markers for each listing
                // Note: In a real app, listings would have lat/lng coordinates
                // For now, we'll show a placeholder message
                Marker(
                    state = MarkerState(position = defaultLocation),
                    title = when (currentLanguage) {
                        "en" -> "Jaffna Market Area"
                        "ta" -> "யாழ்ப்பாணம் சந்தை பகுதி"
                        "si" -> "යාපනය වෙළඳපොළ ප්‍රදේශය"
                        else -> "Jaffna Market Area"
                    },
                    snippet = when (currentLanguage) {
                        "en" -> "${listings.size} listings available"
                        "ta" -> "${listings.size} பட்டியல்கள் கிடைக்கின்றன"
                        "si" -> "ලැයිස්තු ${listings.size} ලබා ගත හැකිය"
                        else -> "${listings.size} listings available"
                    }
                )
            }
        }
    }
}
