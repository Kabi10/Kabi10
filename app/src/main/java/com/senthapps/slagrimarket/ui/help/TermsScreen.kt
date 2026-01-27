package com.senthapps.slagrimarket.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    onNavigateBack: () -> Unit,
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Terms of Service"
                            "ta" -> "சேவை விதிமுறைகள்"
                            "si" -> "සේවා නියම"
                            else -> "Terms of Service"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = when (currentLanguage) {
                                "en" -> "Go back"
                                "ta" -> "பின் செல்"
                                "si" -> "ආපසු යන්න"
                                else -> "Go back"
                            }
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Last Updated: January 2026"
                        "ta" -> "கடைசியாக புதுப்பிக்கப்பட்டது: ஜனவரி 2026"
                        "si" -> "අවසන් යාවත්කාලීන: ජනවාරි 2026"
                        else -> "Last Updated: January 2026"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                TermsSection(
                    title = when (currentLanguage) {
                        "en" -> "1. Acceptance of Terms"
                        "ta" -> "1. விதிமுறைகளை ஏற்றுக்கொள்ளுதல்"
                        "si" -> "1. නියම පිළිගැනීම"
                        else -> "1. Acceptance of Terms"
                    },
                    content = when (currentLanguage) {
                        "en" -> "By using Agrimarket, you agree to these Terms of Service. If you do not agree, please do not use the application."
                        "ta" -> "Agrimarket ஐ பயன்படுத்துவதன் மூலம், இந்த சேவை விதிமுறைகளை ஏற்றுக்கொள்கிறீர்கள். நீங்கள் ஒப்புக்கொள்ளவில்லை என்றால், பயன்பாட்டை பயன்படுத்த வேண்டாம்."
                        "si" -> "Agrimarket භාවිතා කිරීමෙන්, ඔබ මෙම සේවා නියමවලට එකඟ වේ. ඔබ එකඟ නොවන්නේ නම්, කරුණාකර යෙදුම භාවිතා නොකරන්න."
                        else -> "By using Agrimarket, you agree to these Terms of Service. If you do not agree, please do not use the application."
                    }
                )
            }

            item {
                TermsSection(
                    title = when (currentLanguage) {
                        "en" -> "2. User Accounts"
                        "ta" -> "2. பயனர் கணக்குகள்"
                        "si" -> "2. පරිශීලක ගිණුම්"
                        else -> "2. User Accounts"
                    },
                    content = when (currentLanguage) {
                        "en" -> "You must provide accurate information when creating an account. You are responsible for maintaining the security of your account and for all activities that occur under your account."
                        "ta" -> "கணக்கை உருவாக்கும் போது நீங்கள் துல்லியமான தகவல்களை வழங்க வேண்டும். உங்கள் கணக்கின் பாதுகாப்பையும், உங்கள் கணக்கின் கீழ் நடக்கும் அனைத்து செயல்பாடுகளுக்கும் நீங்கள் பொறுப்பு."
                        "si" -> "ගිණුමක් සාදන විට ඔබ නිවැරදි තොරතුරු සැපයිය යුතුය. ඔබේ ගිණුමේ ආරක්ෂාව සහ ඔබේ ගිණුම යටතේ සිදුවන සියලුම ක්‍රියාකාරකම් සඳහා ඔබ වගකිව යුතුය."
                        else -> "You must provide accurate information when creating an account. You are responsible for maintaining the security of your account and for all activities that occur under your account."
                    }
                )
            }

            item {
                TermsSection(
                    title = when (currentLanguage) {
                        "en" -> "3. Marketplace Conduct"
                        "ta" -> "3. சந்தை நடத்தை"
                        "si" -> "3. වෙළඳපොළ හැසිරීම"
                        else -> "3. Marketplace Conduct"
                    },
                    content = when (currentLanguage) {
                        "en" -> "Users agree to list only agricultural products they have the right to sell. All listings must be accurate and not misleading. Fraudulent activity will result in account termination."
                        "ta" -> "பயனர்கள் விற்பனை செய்ய உரிமை உள்ள விவசாய தயாரிப்புகளை மட்டுமே பட்டியலிட ஒப்புக்கொள்கின்றனர். அனைத்து பட்டியல்களும் துல்லியமாகவும் தவறாக வழிநடத்தாததாகவும் இருக்க வேண்டும். மோசடி செயல்பாடு கணக்கு நிறுத்தத்தில் விளையும்."
                        "si" -> "භාවිතාකරුවන් ඔවුන්ට විකිණීමට අයිතිය ඇති කෘෂිකාර්මික නිෂ්පාදන පමණක් ලැයිස්තුගත කිරීමට එකඟ වේ. සියලුම ලැයිස්තු නිවැරදි විය යුතු අතර නොමඟ යවන සුළු නොවිය යුතුය. වංචනික ක්‍රියාකාරකම් ගිණුම අවසන් කිරීමට හේතු වේ."
                        else -> "Users agree to list only agricultural products they have the right to sell. All listings must be accurate and not misleading. Fraudulent activity will result in account termination."
                    }
                )
            }

            item {
                TermsSection(
                    title = when (currentLanguage) {
                        "en" -> "4. Transactions"
                        "ta" -> "4. பரிவர்த்தனைகள்"
                        "si" -> "4. ගනුදෙනු"
                        else -> "4. Transactions"
                    },
                    content = when (currentLanguage) {
                        "en" -> "Agrimarket facilitates connections between farmers and buyers but is not responsible for the actual transactions. Users transact at their own risk and should exercise due diligence."
                        "ta" -> "Agrimarket விவசாயிகள் மற்றும் வாங்குபவர்களுக்கு இடையே இணைப்புகளை எளிதாக்குகிறது ஆனால் உண்மையான பரிவர்த்தனைகளுக்கு பொறுப்பல்ல. பயனர்கள் தங்கள் சொந்த ஆபத்தில் பரிவர்த்தனை செய்கிறார்கள், கவனமாக இருக்க வேண்டும்."
                        "si" -> "Agrimarket ගොවීන් සහ ගැනුම්කරුවන් අතර සම්බන්ධතා පහසුකම් සපයයි නමුත් සත්‍ය ගනුදෙනු සඳහා වගකිව යුතු නොවේ. භාවිතාකරුවන් තමන්ගේම අවදානමෙන් ගනුදෙනු කරන අතර නිසි පරිශීලනය අනුගමනය කළ යුතුය."
                        else -> "Agrimarket facilitates connections between farmers and buyers but is not responsible for the actual transactions. Users transact at their own risk and should exercise due diligence."
                    }
                )
            }

            item {
                TermsSection(
                    title = when (currentLanguage) {
                        "en" -> "5. Limitation of Liability"
                        "ta" -> "5. பொறுப்பின் வரம்பு"
                        "si" -> "5. වගකීම් සීමාව"
                        else -> "5. Limitation of Liability"
                    },
                    content = when (currentLanguage) {
                        "en" -> "Agrimarket is provided \"as is\" without warranties. We are not liable for any indirect, incidental, or consequential damages arising from use of the platform."
                        "ta" -> "Agrimarket உத்தரவாதங்கள் இல்லாமல் \"அப்படியே\" வழங்கப்படுகிறது. தளத்தின் பயன்பாட்டிலிருந்து எழும் மறைமுக, தற்செயல் அல்லது விளைவு சேதங்களுக்கு நாங்கள் பொறுப்பல்ல."
                        "si" -> "Agrimarket වගකීම් නොමැතිව \"ඇති ලෙසම\" සපයනු ලැබේ. වේදිකාව භාවිතයෙන් පැන නගින වක්‍ර, අහඹු හෝ ප්‍රතිඵලයක් ලෙස සිදුවන හානි සඳහා අපි වගකිව යුතු නොවේ."
                        else -> "Agrimarket is provided \"as is\" without warranties. We are not liable for any indirect, incidental, or consequential damages arising from use of the platform."
                    }
                )
            }

            item {
                TermsSection(
                    title = when (currentLanguage) {
                        "en" -> "6. Contact"
                        "ta" -> "6. தொடர்பு"
                        "si" -> "6. සම්බන්ධතා"
                        else -> "6. Contact"
                    },
                    content = when (currentLanguage) {
                        "en" -> "For questions about these terms, contact us at support@agrimarket.lk"
                        "ta" -> "இந்த விதிமுறைகள் பற்றிய கேள்விகளுக்கு, எங்களை support@agrimarket.lk இல் தொடர்பு கொள்ளுங்கள்"
                        "si" -> "මෙම නියම පිළිබඳ ප්‍රශ්න සඳහා, support@agrimarket.lk හරහා අප හා සම්බන්ධ වන්න"
                        else -> "For questions about these terms, contact us at support@agrimarket.lk"
                    }
                )
            }
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
