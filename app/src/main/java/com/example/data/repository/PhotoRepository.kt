package com.example.data.repository

import com.example.data.database.CachedMediaDao
import com.example.data.database.CachedMediaEntity
import com.example.data.database.AiMemoryDao
import com.example.data.database.AiMemoryAlbumEntity
import com.example.data.database.PhotoDao
import com.example.data.database.PhotoEntity
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class PhotoRepository(
    private val photoDao: PhotoDao,
    private val cachedMediaDao: CachedMediaDao,
    private val aiMemoryDao: AiMemoryDao
) {

    val allPhotos: Flow<List<PhotoEntity>> = photoDao.getAllPhotos()
    val publicPhotos: Flow<List<PhotoEntity>> = photoDao.getPublicPhotos()
    val vaultPhotos: Flow<List<PhotoEntity>> = photoDao.getVaultPhotos()
    val favoritePhotos: Flow<List<PhotoEntity>> = photoDao.getFavoritePhotos()

    // --- AI Memories Database Operations ---
    val allAiMemories: Flow<List<AiMemoryAlbumEntity>> = aiMemoryDao.getAllMemoriesFlow()

    suspend fun getAllAiMemoriesList(): List<AiMemoryAlbumEntity> = aiMemoryDao.getAllMemories()
    suspend fun saveAiMemory(memory: AiMemoryAlbumEntity): Long = aiMemoryDao.insertMemory(memory)
    suspend fun deleteAiMemory(id: Int) = aiMemoryDao.deleteMemoryById(id)
    suspend fun clearAllAiMemories() = aiMemoryDao.clearAllMemories()
    suspend fun getAiMemoriesCount(): Int = aiMemoryDao.getMemoriesCount()

    // --- Cache Database Operations ---
    val allCachedMedia: Flow<List<CachedMediaEntity>> = cachedMediaDao.getAllCachedMediaFlow()
    val totalCachedBytes: Flow<Long?> = cachedMediaDao.getTotalCachedBytesFlow()

    suspend fun getCacheForPhoto(photoId: Int): CachedMediaEntity? =
        cachedMediaDao.getCacheByPhotoId(photoId)

    suspend fun saveCacheMetadata(cache: CachedMediaEntity): Long =
        cachedMediaDao.insertCache(cache)

    suspend fun deleteCache(photoId: Int) =
        cachedMediaDao.deleteCacheByPhotoId(photoId)

    suspend fun clearAllCache() =
        cachedMediaDao.clearAllCache()

    suspend fun getCachedCount(): Int =
        cachedMediaDao.getCachedCount()

    suspend fun getAllCachedMediaList(): List<CachedMediaEntity> =
        cachedMediaDao.getAllCachedMedia()

    suspend fun insert(photo: PhotoEntity) = photoDao.insertPhoto(photo)
    suspend fun insertAll(photos: List<PhotoEntity>) = photoDao.insertPhotos(photos)
    suspend fun update(photo: PhotoEntity) = photoDao.updatePhoto(photo)
    suspend fun delete(id: Int) = photoDao.deletePhotoById(id)
    suspend fun setBackup(id: Int, isBackedUp: Boolean) = photoDao.updateBackupStatus(id, isBackedUp)
    suspend fun setVault(id: Int, isLockedInVault: Boolean) = photoDao.updateVaultStatus(id, isLockedInVault)
    suspend fun setFavorite(id: Int, isFavorite: Boolean) = photoDao.updateFavoriteStatus(id, isFavorite)

    suspend fun prepopulateIfEmpty() {
        if (photoDao.getPhotoCount() == 0) {
            val now = System.currentTimeMillis()
            val defaultPhotos = listOf(
                // Nature / Mountains
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                    title = "Yosemite Majesty",
                    timestamp = now - TimeUnit.DAYS.toMillis(1),
                    location = "Yosemite, California",
                    isVideo = false,
                    fileSizeString = "4.2 MB",
                    tags = "mountain, Yosemite, valley, nature, sunset, river, national park",
                    faces = "",
                    objects = "river, mountains, pine trees",
                    isBackedUp = true,
                    isFavorite = true,
                    aiCaption = "A magnificent view of the vertical granite cliff with the river valley under safe sunset rays.",
                    aiRating = 4.9f,
                    fileHash = "SHA256_e99a18d1"
                ),
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                    title = "Yosemite Majesty (Copy)",
                    timestamp = now - TimeUnit.MINUTES.toMillis(45),
                    location = "Yosemite, California",
                    isVideo = false,
                    fileSizeString = "4.2 MB",
                    tags = "mountain, Yosemite, duplicate, clutter",
                    faces = "",
                    objects = "river, mountains",
                    isBackedUp = false,
                    isFavorite = false,
                    aiCaption = "Duplicate image detected via exact SHA-256 hash match.",
                    aiRating = 4.1f,
                    fileHash = "SHA256_e99a18d1"
                ),
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1454496522488-7a8e488e8606?w=800",
                    title = "Himalayan Snow Trek",
                    timestamp = now - TimeUnit.DAYS.toMillis(5),
                    location = "Ladakh, India",
                    isVideo = false,
                    fileSizeString = "5.6 MB",
                    tags = "mountain, Ladakh, snow, trekking, vacation, clean air, hills",
                    faces = "",
                    objects = "snow peaks, clouds, walking trails",
                    isBackedUp = false,
                    isFavorite = false,
                    aiCaption = "Expansive snow-capped peaks rising into a sharp sapphire sky during mid-day ascent.",
                    aiRating = 4.8f,
                    fileHash = "SHA256_3f1a2b8e"
                ),
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800",
                    title = "Valley Fog Sunrise",
                    timestamp = now - TimeUnit.DAYS.toMillis(10),
                    location = "Flachau, Austria",
                    isVideo = true,
                    durationString = "0:15",
                    fileSizeString = "18.4 MB",
                    tags = "video, nature, sunrise, fog, green field, hills",
                    faces = "",
                    objects = "fog valley, cottages, forest lines",
                    isBackedUp = true,
                    isFavorite = false,
                    aiCaption = "Cinematic video reel capturing mystical morning fog rolling over dynamic alpine pastures.",
                    aiRating = 4.7f,
                    fileHash = "SHA256_7c4d5a1f"
                ),

                // Portraits / Faces
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=800",
                    title = "Sophia Portrait session",
                    timestamp = now - TimeUnit.DAYS.toMillis(2),
                    location = "Studio Milan",
                    isVideo = false,
                    fileSizeString = "2.8 MB",
                    tags = "portrait, Sophia, expression, headshot, close-up, Milan",
                    faces = "Sophia",
                    objects = "studio background, brown coat",
                    isBackedUp = true,
                    isFavorite = true,
                    aiCaption = "High dynamic range portrait focusing on soft, natural expressions and sharp shallow depth of field.",
                    aiRating = 4.9f,
                    fileHash = "SHA256_b8a9c2bd"
                ),
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800",
                    title = "Rahul Outdoor smile",
                    timestamp = now - TimeUnit.DAYS.toMillis(4),
                    location = "Goa, India",
                    isVideo = false,
                    fileSizeString = "3.1 MB",
                    tags = "portrait, Rahul, smiling, summer, beach house, Goa",
                    faces = "Rahul",
                    objects = "beach lounger, linen shirt",
                    isBackedUp = false,
                    isFavorite = false,
                    aiCaption = "Candid daylight portrait emphasizing organic smiles and bright, golden beach-bound color tones.",
                    aiRating = 4.5f,
                    fileHash = "SHA256_f4d1e2ac"
                ),
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800",
                    title = "Emily Golden Hour",
                    timestamp = now - TimeUnit.DAYS.toMillis(12),
                    location = "Brooklyn, New York",
                    isVideo = false,
                    fileSizeString = "3.4 MB",
                    tags = "portrait, Emily, street portrait, sundown, New York",
                    faces = "Emily",
                    objects = "city street railing, black jacket",
                    isBackedUp = true,
                    isFavorite = true,
                    aiCaption = "Glowy backlighting accentuating auburn hair with the iconic Brooklyn cityscape subtly blurred behind.",
                    aiRating = 4.8f,
                    fileHash = "SHA256_a1b2c3ef"
                ),

                // Events / Festivals
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800",
                    title = "Cosmic Music Night",
                    timestamp = now - TimeUnit.DAYS.toMillis(3),
                    location = "Mumbai, India",
                    isVideo = true,
                    durationString = "0:30",
                    fileSizeString = "45.0 MB",
                    tags = "video, music, lights, crowd, festival, party, Mumbai",
                    faces = "Sophia, Rahul",
                    objects = "glowing panels, stage rig, lasers",
                    isBackedUp = false,
                    isFavorite = false,
                    aiCaption = "An 8K scale ultra-smooth dynamic concert recording highlighting lasers piercing mist above the crowd.",
                    aiRating = 4.6f,
                    fileHash = "SHA256_9e8d7c1a"
                ),
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1504196606672-aef5c9cefc92?w=800",
                    title = "My Birthday Party",
                    timestamp = now - TimeUnit.DAYS.toMillis(8),
                    location = "Delhi, India",
                    isVideo = false,
                    fileSizeString = "3.9 MB",
                    tags = "event, birthday, cake, sweet, celebrations, family",
                    faces = "Rahul, Emily",
                    objects = "birthday cake, lit sparklers, balloons",
                    isBackedUp = true,
                    isFavorite = false,
                    aiCaption = "Warm indoor lighting preserving the intimate glow of sparkler candles reflecting off laughing faces.",
                    aiRating = 4.4f,
                    fileHash = "SHA256_6b5a4d9e"
                ),
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1467810564000-014a49161f01?w=800",
                    title = "Firework Celebration Sky",
                    timestamp = now - TimeUnit.DAYS.toMillis(15),
                    location = "Tokyo, Japan",
                    isVideo = false,
                    fileSizeString = "5.1 MB",
                    tags = "event, Tokyo, fireworks, festival, summer sky, night",
                    faces = "",
                    objects = "sky fireworks, silhouette crowd",
                    isBackedUp = true,
                    isFavorite = true,
                    aiCaption = "Double-exposure scale capture highlighting a massive radial gold chrysantheme explosive pattern.",
                    aiRating = 4.8f,
                    fileHash = "SHA256_1a2b3c4d"
                ),

                // Cities / Objects / Documents
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800",
                    title = "Eiffel Evening Lines",
                    timestamp = now - TimeUnit.DAYS.toMillis(6),
                    location = "Paris, France",
                    isVideo = false,
                    fileSizeString = "3.2 MB",
                    tags = "place, Paris, architecture, landmark, street, sunset",
                    faces = "",
                    objects = "Eiffel tower, Parisian streetlamp",
                    isBackedUp = false,
                    isFavorite = false,
                    aiCaption = "Symmetrical, low-angle viewpoint framing the majestic tower with deep violet sky coordinates.",
                    aiRating = 4.6f,
                    fileHash = "SHA256_d4e5f6fd"
                ),
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1512151172458-15c697ebe5c0?w=800",
                    title = "Weekend Fusion Burger",
                    timestamp = now - TimeUnit.DAYS.toMillis(11),
                    location = "Smokehouse Delhi",
                    isVideo = false,
                    fileSizeString = "2.5 MB",
                    tags = "object, food, burger, street-food, weekend, Delhi",
                    faces = "Sophia",
                    objects = "crisp burger plate, potato wedges",
                    isBackedUp = true,
                    isFavorite = false,
                    aiCaption = "Succulent macro food photography with vivid saturation showcasing crispy golden exterior textures.",
                    aiRating = 4.3f,
                    fileHash = "SHA256_6c7b8a5d"
                ),
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1512151172458-15c697ebe5c0?w=800",
                    title = "Weekend Fusion Burger (Duplicate)",
                    timestamp = now - TimeUnit.MINUTES.toMillis(12),
                    location = "Smokehouse Delhi",
                    isVideo = false,
                    fileSizeString = "2.5 MB",
                    tags = "object, food, duplicate, clutter",
                    faces = "Sophia",
                    objects = "crisp burger plate",
                    isBackedUp = false,
                    isFavorite = false,
                    aiCaption = "Exact duplicate file with matching SHA-256 checksum.",
                    aiRating = 3.9f,
                    fileHash = "SHA256_6c7b8a5d"
                ),
                PhotoEntity(
                    uri = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
                    title = "Abstract Core Wallpaper",
                    timestamp = now - TimeUnit.DAYS.toMillis(20),
                    location = "Digital Studio",
                    isVideo = false,
                    fileSizeString = "1.5 MB",
                    tags = "document, abstract, graphics, purple, pink, glassmorphism",
                    faces = "",
                    objects = "virtual geometric spheres, frosted card",
                    isBackedUp = true,
                    isFavorite = false,
                    aiCaption = "Ultra HD graphic rendering simulating multi-layered glass panels refraction and gradients.",
                    aiRating = 4.7f,
                    fileHash = "SHA256_5e4d2c8b"
                )
            )
            photoDao.insertPhotos(defaultPhotos)
        }
    }
}
