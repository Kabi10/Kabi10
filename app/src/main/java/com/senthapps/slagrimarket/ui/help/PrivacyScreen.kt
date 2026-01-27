package com.senthapps.slagrimarket.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
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
                            "en" -> "Privacy Policy"
                            "ta" -> "தனியுரிமைக் கொள்கை"
                            "si" -> "පෞද්ගලිකත්ව ප්‍රතිපත්තිය"
                            else -> "Privacy Policy"
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
                PrivacySection(
                    title = when (currentLanguage) {
                        "en" -> "Information We Collect"
                        "ta" -> "நாங்கள் சேகரிக்கும் தகவல்"
                        "si" -> "අපි එකතු කරන තොරතුරු"
                        else -> "Information We Collect"
                    },
                    content = when (currentLanguage) {
                        "en" -> "We collect information you provide directly, including your phone number, name, location, and listing details. We also collect usage data to improve our services."
                        "ta" -> "உங்கள் தொலைபேசி எண், பெயர், இருப்பிடம் மற்றும் பட்டியல் விவரங்கள் உள்ளிட்ட நேரடியாக வழங்கும் தகவல்களை நாங்கள் சேகரிக்கிறோம். எங்கள் சேவைகளை மேம்படுத்த பயன்பாட்டு தரவையும் சேகரிக்கிறோம்."
                        "si" -> "ඔබේ දුරකථන අංකය, නම, ස්ථානය සහ ලැයිස්තු විස්තර ඇතුළුව ඔබ සෘජුවම සපයන තොරතුරු අපි එකතු කරමු. අපගේ සේවාවන් වැඩිදියුණු කිරීමට භාවිත දත්ත ද එකතු කරමු."
                        else -> "We collect information you provide directly, including your phone number, name, location, and listing details. We also collect usage data to improve our services."
                    }
                )
            }

            item {
                PrivacySection(
                    title = when (currentLanguage) {
                        "en" -> "How We Use Your Information"
                        "ta" -> "உங்கள் தகவலை நாங்கள் எவ்வாறு பயன்படுத்துகிறோம்"
                        "si" -> "ඔබේ තොරතුරු අපි භාවිතා කරන ආකාරය"
                        else -> "How We Use Your Information"
                    },
                    content = when (currentLanguage) {
                        "en" -> "Your information is used to: provide and maintain our services, connect farmers with buyers, send notifications about transactions, and improve user experience."
                        "ta" -> "உங்கள் தகவல் பின்வருவனவற்றுக்கு பயன்படுத்தப்படுகிறது: எங்கள் சேவைகளை வழங்க மற்றும் பராமரிக்க, விவசாயிகளை வாங்குபவர்களுடன் இணைக்க, பரிவர்த்தனைகள் பற்றிய அறிவிப்புகளை அனுப்ப, பயனர் அனுபவத்தை மேம்படுத்த."
                        "si" -> "ඔබේ තොරතුරු භාවිතා කරන්නේ: අපගේ සේවාවන් සැපයීම සහ නඩත්තු කිරීම, ගොවීන් ගැනුම්කරුවන් සමඟ සම්බන්ධ කිරීම, ගනුදෙනු පිළිබඳ දැනුම්දීම් යැවීම සහ පරිශීලක අත්දැකීම වැඩිදියුණු කිරීම සඳහාය."
                        else -> "Your information is used to: provide and maintain our services, connect farmers with buyers, send notifications about transactions, and improve user experience."
                    }
                )
            }

            item {
                PrivacySection(
                    title = when (currentLanguage) {
                        "en" -> "Data Security"
                        "ta" -> "தரவு பாதுகாப்பு"
                        "si" -> "දත්ත ආරක්ෂාව"
                        else -> "Data Security"
                    },
                    content = when (currentLanguage) {
                        "en" -> "We implement industry-standard security measures to protect your data. This includes encryption of sensitive information and secure data storage practices."
                        "ta" -> "உங்கள் தரவைப் பாதுகாக்க தொழில்துறை-தரமான பாதுகாப்பு நடவடிக்கைகளை செயல்படுத்துகிறோம். இதில் முக்கியமான தகவல்களின் குறியாக்கம் மற்றும் பாதுகாப்பான தரவு சேமிப்பு நடைமுறைகள் அடங்கும்."
                        "si" -> "ඔබේ දත්ත ආරක්ෂා කිරීමට අපි කර්මාන්ත-ප්‍රමිතිගත ආරක්ෂක පියවර ක්‍රියාත්මක කරමු. මෙයට සංවේදී තොරතුරු සංකේතනය සහ ආරක්ෂිත දත්ත ගබඩා කිරීමේ පිළිවෙත් ඇතුළත් වේ."
                        else -> "We implement industry-standard security measures to protect your data. This includes encryption of sensitive information and secure data storage practices."
                    }
                )
            }

            item {
                PrivacySection(
                    title = when (currentLanguage) {
                        "en" -> "Data Sharing"
                        "ta" -> "தரவு பகிர்வு"
                        "si" -> "දත්ත බෙදාගැනීම"
                        else -> "Data Sharing"
                    },
                    content = when (currentLanguage) {
                        "en" -> "We do not sell your personal information. We may share your information with other users as necessary for transactions (e.g., sharing contact details with buyers/sellers)."
                        "ta" -> "உங்கள் தனிப்பட்ட தகவல்களை விற்க மாட்டோம். பரிவர்த்தனைகளுக்கு தேவையான அளவு மற்ற பயனர்களுடன் உங்கள் தகவலைப் பகிரலாம் (எ.கா., வாங்குபவர்கள்/விற்பனையாளர்களுடன் தொடர்பு விவரங்களைப் பகிர்வது)."
                        "si" -> "අපි ඔබේ පෞද්ගලික තොරතුරු විකුණන්නේ නැත. ගනුදෙනු සඳහා අවශ්‍ය පරිදි වෙනත් පරිශීලකයින් සමඟ ඔබේ තොරතුරු බෙදා ගත හැකිය (උදා: ගැනුම්කරුවන්/විකුණුම්කරුවන් සමඟ සම්බන්ධතා විස්තර බෙදාගැනීම)."
                        else -> "We do not sell your personal information. We may share your information with other users as necessary for transactions (e.g., sharing contact details with buyers/sellers)."
                    }
                )
            }

            item {
                PrivacySection(
                    title = when (currentLanguage) {
                        "en" -> "Your Rights"
                        "ta" -> "உங்கள் உரிமைகள்"
                        "si" -> "ඔබේ අයිතිවාසිකම්"
                        else -> "Your Rights"
                    },
                    content = when (currentLanguage) {
                        "en" -> "You have the right to access, update, or delete your personal information. You can manage your data through the app settings or by contacting our support team."
                        "ta" -> "உங்கள் தனிப்பட்ட தகவல்களை அணுக, புதுப்பிக்க அல்லது நீக்க உங்களுக்கு உரிமை உள்ளது. பயன்பாட்டு அமைப்புகள் மூலம் அல்லது எங்கள் ஆதரவு குழுவை தொடர்பு கொண்டு உங்கள் தரவை நிர்வகிக்கலாம்."
                        "si" -> "ඔබේ පෞද්ගලික තොරතුරු වෙත ප්‍රවේශ වීමට, යාවත්කාලීන කිරීමට හෝ මකා දැමීමට ඔබට අයිතිය ඇත. යෙදුම් සැකසීම් හරහා හෝ අපගේ සහාය කණ්ඩායම සම්බන්ධ කර ගැනීමෙන් ඔබට ඔබේ දත්ත කළමනාකරණය කළ හැකිය."
                        else -> "You have the right to access, update, or delete your personal information. You can manage your data through the app settings or by contacting our support team."
                    }
                )
            }

            item {
                PrivacySection(
                    title = when (currentLanguage) {
                        "en" -> "Contact Us"
                        "ta" -> "எங்களை தொடர்பு கொள்ளுங்கள்"
                        "si" -> "අප හා සම්බන්ධ වන්න"
                        else -> "Contact Us"
                    },
                    content = when (currentLanguage) {
                        "en" -> "For privacy-related questions or concerns, contact us at privacy@agrimarket.lk"
                        "ta" -> "தனியுரிமை தொடர்பான கேள்விகள் அல்லது கவலைகளுக்கு, privacy@agrimarket.lk இல் எங்களை தொடர்பு கொள்ளுங்கள்"
                        "si" -> "පෞද්ගලිකත්වය සම්බන්ධ ප්‍රශ්න හෝ ගැටළු සඳහා, privacy@agrimarket.lk හරහා අප හා සම්බන්ධ වන්න"
                        else -> "For privacy-related questions or concerns, contact us at privacy@agrimarket.lk"
                    }
                )
            }
        }
    }
}

@Composable
private fun PrivacySection(
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
