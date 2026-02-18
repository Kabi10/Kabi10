package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.industrialClickable

// ============================================================================
// DISTRICT PICKER DIALOG
// Scrollable list of all 25 Sri Lanka districts — no typing needed
// Used in EditProfile location field and QuickListingScreen location
// ============================================================================

/**
 * District entry with trilingual names
 */
data class SriLankaDistrict(
    val nameEnglish: String,
    val nameSinhala: String,
    val nameTamil: String,
    val province: String
)

/** All 25 Sri Lanka districts */
val ALL_DISTRICTS = listOf(
    // Northern Province
    SriLankaDistrict("Jaffna", "යාපනය", "யாழ்ப்பாணம்", "Northern"),
    SriLankaDistrict("Kilinochchi", "කිලිනොච්චිය", "கிளிநொச்சி", "Northern"),
    SriLankaDistrict("Mannar", "මන්නාරම", "மன்னார்", "Northern"),
    SriLankaDistrict("Mullaitivu", "මුලතිව්", "முல்லைத்தீவு", "Northern"),
    SriLankaDistrict("Vavuniya", "වව්නියාව", "வவுனியா", "Northern"),
    // North Western Province
    SriLankaDistrict("Kurunegala", "කුරුණෑගල", "குருநாகல்", "North Western"),
    SriLankaDistrict("Puttalam", "පුත්තලම", "புத்தளம்", "North Western"),
    // North Central Province
    SriLankaDistrict("Anuradhapura", "අනුරාධපුරය", "அனுராதபுரம்", "North Central"),
    SriLankaDistrict("Polonnaruwa", "පොළොන්නරුව", "பொலன்னறுவை", "North Central"),
    // Eastern Province
    SriLankaDistrict("Ampara", "අම්පාර", "அம்பாறை", "Eastern"),
    SriLankaDistrict("Batticaloa", "මඩකලපුව", "மட்டக்களப்பு", "Eastern"),
    SriLankaDistrict("Trincomalee", "ත්‍රිකුණාමලය", "திருகோணமலை", "Eastern"),
    // Central Province
    SriLankaDistrict("Kandy", "මහනුවර", "கண்டி", "Central"),
    SriLankaDistrict("Matale", "මාතලේ", "மாத்தளை", "Central"),
    SriLankaDistrict("Nuwara Eliya", "නුවරඑළිය", "நுவரெலியா", "Central"),
    // Western Province
    SriLankaDistrict("Colombo", "කොළඹ", "கொழும்பு", "Western"),
    SriLankaDistrict("Gampaha", "ගම්පහ", "கம்பஹா", "Western"),
    SriLankaDistrict("Kalutara", "කළුතර", "களுத்துறை", "Western"),
    // Sabaragamuwa Province
    SriLankaDistrict("Kegalle", "කෑගල්ල", "கேகாலை", "Sabaragamuwa"),
    SriLankaDistrict("Ratnapura", "රත්නපුර", "இரத்தினபுரி", "Sabaragamuwa"),
    // Southern Province
    SriLankaDistrict("Galle", "ගාල්ල", "காலி", "Southern"),
    SriLankaDistrict("Hambantota", "හම්බන්තොට", "அம்பாந்தோட்டை", "Southern"),
    SriLankaDistrict("Matara", "මාතර", "மாத்தறை", "Southern"),
    // Uva Province
    SriLankaDistrict("Badulla", "බදුල්ල", "பதுளை", "Uva"),
    SriLankaDistrict("Monaragala", "මොණරාගල", "மொணராகல", "Uva")
)

/**
 * Scrollable district selection dialog — no typing required
 *
 * Shows all 25 districts grouped by province. Farmer taps to select.
 *
 * @param language Display language: "en", "si", or "ta"
 * @param selectedDistrict Currently selected district (for highlight)
 * @param onDistrictSelected Called with the district's English name on tap
 * @param onDismiss Called when dialog is dismissed without selection
 */
@Composable
fun DistrictPickerDialog(
    language: String = "ta",
    selectedDistrict: String = "",
    onDistrictSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val title = when (language) {
        "si" -> "දිස්ත්‍රික්කය"
        "ta" -> "மாவட்டம்"
        else -> "SELECT DISTRICT"
    }
    val cancelText = when (language) {
        "si" -> "ආපසු"
        "ta" -> "பின்"
        else -> "BACK"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HumanIndustrial.Rice)
        ) {
            // ─── Title bar ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF5C3317))
                    .padding(horizontal = Spacing.lg.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .industrialClickable(onClick = onDismiss)
                            .padding(horizontal = Spacing.sm.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cancelText,
                            style = HumanIndustrialType.sectionLabel,
                            color = Color(0xFFD4A84B)
                        )
                    }
                    Text(
                        text = title,
                        style = HumanIndustrialType.screenTitle,
                        color = HumanIndustrial.Rice,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = Spacing.lg.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BorderWidth.Thick)
                    .background(HumanIndustrial.Earth)
            )

            // ─── District list ────────────────────────────────────────────
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                // Group by province
                val byProvince = ALL_DISTRICTS.groupBy { it.province }
                byProvince.forEach { (province, districts) ->
                    // Province header
                    item(key = "province_$province") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .background(HumanIndustrial.Earth)
                                .padding(horizontal = Spacing.lg.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = province.uppercase(),
                                style = HumanIndustrialType.sectionLabel,
                                color = HumanIndustrial.Rice
                            )
                        }
                    }
                    // District rows
                    items(districts, key = { it.nameEnglish }) { district ->
                        val isSelected = district.nameEnglish == selectedDistrict
                        val bgColor = if (isSelected) HumanIndustrial.Gold.copy(alpha = 0.18f) else HumanIndustrial.Rice
                        val displayName = when (language) {
                            "si" -> district.nameSinhala
                            "ta" -> district.nameTamil
                            else -> district.nameEnglish
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(bgColor)
                                .industrialClickable(onClick = {
                                    onDistrictSelected(district.nameEnglish)
                                })
                                .padding(horizontal = Spacing.lg.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayName,
                                    style = HumanIndustrialType.productName,
                                    color = HumanIndustrial.Ink,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Text(
                                        text = "✓",
                                        style = HumanIndustrialType.productName,
                                        color = HumanIndustrial.Green
                                    )
                                }
                            }
                        }

                        // Row divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(BorderWidth.Thin)
                                .background(HumanIndustrial.Dust)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// PREVIEW
// ============================================================================

@Preview(showBackground = true, widthDp = 360, heightDp = 700)
@Composable
private fun DistrictPickerPreview() {
    DistrictPickerDialog(
        language = "en",
        selectedDistrict = "Jaffna",
        onDistrictSelected = {},
        onDismiss = {}
    )
}
