package com.senthapps.slagrimarket.util

import androidx.annotation.DrawableRes
import com.senthapps.slagrimarket.R
import com.senthapps.slagrimarket.data.model.CropTypes

/**
 * Provides generic crop images for listings
 * These are stock/placeholder images shown when farmers haven't uploaded their own photos
 */
object CropImageProvider {
    
    /**
     * Get generic drawable resource ID for a crop type
     * Returns a placeholder image representing the crop category
     * 
     * @param cropType The crop type identifier (e.g., "red_onion", "tomato")
     * @return Drawable resource ID for the generic crop image
     */
    @DrawableRes
    fun getGenericCropImage(cropType: String): Int {
        return when (cropType) {
            CropTypes.RED_ONION -> R.drawable.crop_red_onion
            CropTypes.CHILI -> R.drawable.crop_chili
            CropTypes.TOMATO -> R.drawable.crop_tomato
            CropTypes.BRINJAL -> R.drawable.crop_brinjal
            CropTypes.OKRA -> R.drawable.crop_okra
            CropTypes.COCONUT -> R.drawable.crop_coconut
            CropTypes.PALMYRA -> R.drawable.crop_palmyra
            CropTypes.MANGO -> R.drawable.crop_mango
            CropTypes.BANANA -> R.drawable.crop_banana
            CropTypes.RICE -> R.drawable.crop_rice
            else -> R.drawable.crop_generic // Fallback for unknown crops
        }
    }
    
    /**
     * Check if a listing has farmer-uploaded photos
     * 
     * @param images List of image URLs from the listing
     * @return true if the listing has actual farmer photos, false if empty
     */
    fun hasFarmerPhotos(images: List<String>): Boolean {
        return images.isNotEmpty()
    }
    
    /**
     * Get all images for a listing, including fallback to generic image
     * 
     * @param cropType The crop type
     * @param farmerImages List of farmer-uploaded image URLs
     * @return List of image sources (URLs or resource IDs)
     */
    fun getListingImages(cropType: String, farmerImages: List<String>): List<Any> {
        return if (farmerImages.isNotEmpty()) {
            farmerImages
        } else {
            listOf(getGenericCropImage(cropType))
        }
    }
}

