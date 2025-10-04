package com.senthapps.slagrimarket.util

/**
 * Comprehensive translation utility for crop names, locations, and common terms
 * Supports English, Tamil, and Sinhala
 */
object TranslationUtil {
    
    /**
     * Crop name translations
     */
    private val cropTranslations = mapOf(
        // Vegetables
        "red_onion" to Triple("Red Onion", "வெங்காயம்", "රතු ළූණු"),
        "chili" to Triple("Chili", "மிளகாய்", "මිරිස්"),
        "tomato" to Triple("Tomato", "தக்காளி", "තක්කාලි"),
        "brinjal" to Triple("Brinjal", "கத்தரிக்காய்", "වම්බටු"),
        "okra" to Triple("Okra", "வெண்டைக்காய்", "බණ්ඩක්කා"),
        "carrot" to Triple("Carrot", "கேரட்", "කැරට්"),
        "beans" to Triple("Beans", "பீன்ஸ்", "බෝංචි"),
        "cabbage" to Triple("Cabbage", "முட்டைகோஸ்", "ගෝවා"),
        "cauliflower" to Triple("Cauliflower", "காலிஃப்ளவர்", "මල් ගෝවා"),
        "potato" to Triple("Potato", "உருளைக்கிழங்கு", "අල"),
        "pumpkin" to Triple("Pumpkin", "பூசணிக்காய்", "වට්ටක්කා"),
        "bitter_gourd" to Triple("Bitter Gourd", "பாகற்காய்", "කරවිල"),
        "snake_gourd" to Triple("Snake Gourd", "புடலங்காய்", "පතෝල"),
        "cucumber" to Triple("Cucumber", "வெள்ளரிக்காய்", "පිපිඤ්ඤා"),
        "radish" to Triple("Radish", "முள்ளங்கி", "රාබු"),
        "beetroot" to Triple("Beetroot", "பீட்ரூட்", "බීට්"),
        "leeks" to Triple("Leeks", "லீக்ஸ்", "ලීක්ස්"),
        "spinach" to Triple("Spinach", "கீரை", "නිවිති"),
        "green_chili" to Triple("Green Chili", "பச்சை மிளகாய்", "අබු මිරිස්"),
        
        // Fruits
        "mango" to Triple("Mango", "மாம்பழம்", "අඹ"),
        "banana" to Triple("Banana", "வாழைப்பழம்", "කෙසෙල්"),
        "papaya" to Triple("Papaya", "பப்பாளி", "පැපොල්"),
        "pineapple" to Triple("Pineapple", "அன்னாசி", "අන්නාසි"),
        "watermelon" to Triple("Watermelon", "தர்பூசணி", "කොමඩු"),
        "guava" to Triple("Guava", "கொய்யா", "පේර"),
        "lime" to Triple("Lime", "எலுமிச்சை", "දෙහි"),
        "coconut" to Triple("Coconut", "தேங்காய்", "පොල්"),
        "jackfruit" to Triple("Jackfruit", "பலாப்பழம்", "කොස්"),
        "passion_fruit" to Triple("Passion Fruit", "பேஷன் பழம்", "වැල් දොඩම්"),
        
        // Grains & Pulses
        "rice" to Triple("Rice", "அரிசி", "සහල්"),
        "lentils" to Triple("Lentils", "பருப்பு", "පරිප්පු"),
        "chickpeas" to Triple("Chickpeas", "கொண்டைக்கடலை", "කඩල"),
        "green_gram" to Triple("Green Gram", "பயறு", "මුං ඇට"),
        "black_gram" to Triple("Black Gram", "உளுந்து", "උඳු"),
        
        // Spices & Herbs
        "turmeric" to Triple("Turmeric", "மஞ்சள்", "කහ"),
        "ginger" to Triple("Ginger", "இஞ்சி", "ඉඟුරු"),
        "garlic" to Triple("Garlic", "பூண்டு", "සුදුළූණු"),
        "coriander" to Triple("Coriander", "கொத்தமல்லி", "කොත්තමල්ලි"),
        "curry_leaves" to Triple("Curry Leaves", "கறிவேப்பிலை", "කරපිංචා"),
        "pepper" to Triple("Pepper", "மிளகு", "ගම්මිරිස්"),
        "cinnamon" to Triple("Cinnamon", "பட்டை", "කුරුඳු"),
        "cardamom" to Triple("Cardamom", "ஏலக்காய்", "එනසාල්"),
        
        // Other
        "mushroom" to Triple("Mushroom", "காளான்", "හතු"),
        "corn" to Triple("Corn", "சோளம்", "ඉරිඟු"),
        "sweet_potato" to Triple("Sweet Potato", "சர்க்கரைவள்ளிக்கிழங்கு", "බතල"),
        "yam" to Triple("Yam", "சேனைக்கிழங்கு", "අල")
    )
    
    /**
     * Location translations (Jaffna district)
     */
    private val locationTranslations = mapOf(
        "Jaffna" to Triple("Jaffna", "யாழ்ப்பாணம்", "යාපනය"),
        "Jaffna Central Market" to Triple("Jaffna Central Market", "யாழ்ப்பாணம் மத்திய சந்தை", "යාපනය මධ්‍යම වෙළඳපොළ"),
        "Chavakachcheri" to Triple("Chavakachcheri", "சாவகச்சேரி", "චාවකච්චේරි"),
        "Chavakachcheri Market" to Triple("Chavakachcheri Market", "சாவகச்சேரி சந்தை", "චාවකච්චේරි වෙළඳපොළ"),
        "Nallur" to Triple("Nallur", "நல்லூர்", "නල්ලූර්"),
        "Nallur Market" to Triple("Nallur Market", "நல்லூர் சந்தை", "නල්ලූර් වෙළඳපොළ"),
        "Point Pedro" to Triple("Point Pedro", "பருத்தித்துறை", "පොයින්ට් පේද්‍රෝ"),
        "Karainagar" to Triple("Karainagar", "காரைநகர்", "කරයිනගර්"),
        "Kayts" to Triple("Kayts", "காயத்தீவு", "කයිට්ස්"),
        "Velanai" to Triple("Velanai", "வேலணை", "වෙලනායි"),
        "Delft" to Triple("Delft", "நெடுந்தீவு", "ඩෙල්ෆ්ට්"),
        "Manipay" to Triple("Manipay", "மானிப்பாய்", "මනිපායි"),
        "Kopay" to Triple("Kopay", "கோப்பாய்", "කෝපායි"),
        "Tellippalai" to Triple("Tellippalai", "தெல்லிப்பழை", "තෙල්ලිප්පලායි"),
        "Chankanai" to Triple("Chankanai", "சங்கானை", "චන්කනායි"),
        "Uduvil" to Triple("Uduvil", "உடுவில்", "උඩුවිල්"),
        "Sandilipay" to Triple("Sandilipay", "சண்டிலிப்பாய்", "සන්දිලිපායි"),
        "Atchuvely" to Triple("Atchuvely", "அச்சுவேலி", "අච්චුවෙලි"),
        "Kokuvil" to Triple("Kokuvil", "கொக்குவில்", "කොකුවිල්"),
        "Thirunelvely" to Triple("Thirunelvely", "திருநெல்வேலி", "තිරුනෙල්වෙලි")
    )
    
    /**
     * Quality grade translations
     */
    private val qualityTranslations = mapOf(
        "PREMIUM" to Triple("Premium", "உயர்தரம்", "ප්‍රිමියම්"),
        "GRADE_A" to Triple("Grade A", "தரம் A", "ශ්‍රේණිය A"),
        "GRADE_B" to Triple("Grade B", "தரம் B", "ශ්‍රේණිය B"),
        "STANDARD" to Triple("Standard", "நிலையான", "සම්මත")
    )
    
    /**
     * Unit translations
     */
    private val unitTranslations = mapOf(
        "kg" to Triple("kg", "கிலோ", "කි.ග්‍රෑ"),
        "g" to Triple("g", "கிராம்", "ග්‍රෑ"),
        "lb" to Triple("lb", "பவுண்டு", "රාත්තල්"),
        "piece" to Triple("piece", "துண்டு", "කෑල්ල"),
        "bunch" to Triple("bunch", "கட்டு", "මිටියක්"),
        "bag" to Triple("bag", "பை", "බෑගය"),
        "box" to Triple("box", "பெட்டி", "පෙට්ටිය")
    )
    
    /**
     * Get crop name in specified language
     */
    fun getCropName(cropType: String, language: String): String {
        val translation = cropTranslations[cropType] ?: return cropType
        return when (language) {
            "en" -> translation.first
            "ta" -> translation.second
            "si" -> translation.third
            else -> translation.first
        }
    }
    
    /**
     * Get location name in specified language
     */
    fun getLocationName(location: String, language: String): String {
        val translation = locationTranslations[location] ?: return location
        return when (language) {
            "en" -> translation.first
            "ta" -> translation.second
            "si" -> translation.third
            else -> translation.first
        }
    }
    
    /**
     * Get quality grade in specified language
     */
    fun getQualityGrade(quality: String, language: String): String {
        val translation = qualityTranslations[quality] ?: return quality
        return when (language) {
            "en" -> translation.first
            "ta" -> translation.second
            "si" -> translation.third
            else -> translation.first
        }
    }
    
    /**
     * Get unit in specified language
     */
    fun getUnit(unit: String, language: String): String {
        val translation = unitTranslations[unit] ?: return unit
        return when (language) {
            "en" -> translation.first
            "ta" -> translation.second
            "si" -> translation.third
            else -> translation.first
        }
    }
    
    /**
     * Get all available crops
     */
    fun getAllCrops(): List<String> = cropTranslations.keys.toList()
    
    /**
     * Get all available locations
     */
    fun getAllLocations(): List<String> = locationTranslations.keys.toList()
    
    /**
     * Search crops by name in any language
     */
    fun searchCrops(query: String, language: String): List<String> {
        val lowerQuery = query.lowercase()
        return cropTranslations.filter { (key, translation) ->
            when (language) {
                "en" -> translation.first.lowercase().contains(lowerQuery)
                "ta" -> translation.second.contains(query)
                "si" -> translation.third.contains(query)
                else -> translation.first.lowercase().contains(lowerQuery)
            }
        }.keys.toList()
    }
}
