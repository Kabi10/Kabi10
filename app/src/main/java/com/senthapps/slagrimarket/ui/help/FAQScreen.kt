package com.senthapps.slagrimarket.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

data class FAQItem(
    val questionEn: String,
    val questionTa: String,
    val questionSi: String,
    val answerEn: String,
    val answerTa: String,
    val answerSi: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    onNavigateBack: () -> Unit,
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    
    val faqItems = remember {
        listOf(
            FAQItem(
                "How do I create a listing?",
                "பட்டியலை எவ்வாறு உருவாக்குவது?",
                "ලැයිස්තුවක් නිර්මාණය කරන්නේ කෙසේද?",
                "Go to Home screen, tap the '+' button, fill in the details, add photos, and submit.",
                "முகப்பு திரைக்குச் செல்லவும், '+' பொத்தானைத் தட்டவும், விவரங்களை நிரப்பவும், புகைப்படங்களைச் சேர்க்கவும், சமர்ப்பிக்கவும்.",
                "මුල් පිටුවට යන්න, '+' බොත්තම ඔබන්න, විස්තර පුරවන්න, ඡායාරූප එක් කරන්න සහ ඉදිරිපත් කරන්න."
            ),
            FAQItem(
                "How do I place an order?",
                "ஆர்டரை எவ்வாறு வைப்பது?",
                "ඇණවුමක් කරන්නේ කෙසේද?",
                "Browse listings, select a product, tap 'Place Order', enter quantity, and confirm.",
                "பட்டியல்களை உலாவவும், ஒரு தயாரிப்பைத் தேர்ந்தெடுக்கவும், 'ஆர்டர் செய்யவும்' என்பதைத் தட்டவும், அளவை உள்ளிடவும், உறுதிப்படுத்தவும்.",
                "ලැයිස්තු බ්‍රවුස් කරන්න, නිෂ්පාදනයක් තෝරන්න, 'ඇණවුම් කරන්න' ඔබන්න, ප්‍රමාණය ඇතුළත් කරන්න සහ තහවුරු කරන්න."
            ),
            FAQItem(
                "How do I contact a seller?",
                "விற்பனையாளரை எவ்வாறு தொடர்பு கொள்வது?",
                "විකුණුම්කරුවෙකු අමතන්නේ කෙසේද?",
                "Open the listing, tap 'Contact Seller' to start a chat conversation.",
                "பட்டியலைத் திறக்கவும், அரட்டை உரையாடலைத் தொடங்க 'விற்பனையாளரைத் தொடர்பு கொள்ளவும்' என்பதைத் தட்டவும்.",
                "ලැයිස්තුව විවෘත කරන්න, කතාබස් සංවාදයක් ආරම්භ කිරීමට 'විකුණුම්කරු අමතන්න' ඔබන්න."
            ),
            FAQItem(
                "How do I track my order?",
                "எனது ஆர்டரை எவ்வாறு கண்காணிப்பது?",
                "මගේ ඇණවුම නිරීක්ෂණය කරන්නේ කෙසේද?",
                "Go to Transactions screen, select your order to view status and details.",
                "பரிவர்த்தனைகள் திரைக்குச் செல்லவும், நிலை மற்றும் விவரங்களைக் காண உங்கள் ஆர்டரைத் தேர்ந்தெடுக்கவும்.",
                "ගනුදෙනු තිරයට යන්න, තත්ත්වය සහ විස්තර බැලීමට ඔබේ ඇණවුම තෝරන්න."
            ),
            FAQItem(
                "What if I'm offline?",
                "நான் ஆஃப்லைனில் இருந்தால் என்ன?",
                "මම නොබැඳි නම් කුමක් කළ යුතුද?",
                "The app works offline. Your changes will sync automatically when you're back online.",
                "பயன்பாடு ஆஃப்லைனில் வேலை செய்கிறது. நீங்கள் மீண்டும் ஆன்லைனில் இருக்கும்போது உங்கள் மாற்றங்கள் தானாகவே ஒத்திசைக்கப்படும்.",
                "යෙදුම නොබැඳිව ක්‍රියා කරයි. ඔබ නැවත සබැඳි වූ විට ඔබේ වෙනස්කම් ස්වයංක්‍රීයව සමමුහුර්ත වේ."
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "FAQ"
                            "ta" -> "அடிக்கடி கேட்கப்படும் கேள்விகள்"
                            "si" -> "නිතර අසන ප්‍රශ්න"
                            else -> "FAQ"
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(faqItems) { item ->
                FAQCard(item = item, currentLanguage = currentLanguage)
            }
        }
    }
}

@Composable
private fun FAQCard(
    item: FAQItem,
    currentLanguage: String
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (currentLanguage) {
                        "ta" -> item.questionTa
                        "si" -> item.questionSi
                        else -> item.questionEn
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            
            if (expanded) {
                Text(
                    text = when (currentLanguage) {
                        "ta" -> item.answerTa
                        "si" -> item.answerSi
                        else -> item.answerEn
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
