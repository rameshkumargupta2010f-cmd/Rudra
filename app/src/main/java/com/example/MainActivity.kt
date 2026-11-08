package com.example

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Core Data Model for Wallpaper Items
data class WallpaperItem(
  val id: String,
  val title: String,
  val character: String,
  val category: String, // SAIYANS, BEASTS, CLASSIC
  val drawableResId: Int,
  val powerLevel: String,
  val formDescription: String,
  val accentColor: Color,
  val signatureMove: String
)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          WallpaperDashboardScreen()
        }
      }
    }
  }
}

// Persist user favorites locally offline using SharedPreferences
private fun getFavorites(context: Context): Set<String> {
  val prefs = context.getSharedPreferences("dbz_wallpapers_prefs", Context.MODE_PRIVATE)
  return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
}

private fun saveFavorite(context: Context, id: String, isFavorite: Boolean) {
  val prefs = context.getSharedPreferences("dbz_wallpapers_prefs", Context.MODE_PRIVATE)
  val current = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
  if (isFavorite) {
    current.add(id)
  } else {
    current.remove(id)
  }
  prefs.edit().putStringSet("favorites", current).apply()
}

// Selected digital aura filter settings for interactive wallpaper preview
enum class AuraFilter(val label: String, val colorMatrix: ColorMatrix) {
  NORMAL("NORMAL", ColorMatrix()),
  SUPER_SAIYAN("GOLD SAIYAN", ColorMatrix(floatArrayOf(
    1.4f, 0.2f, 0.0f, 0.0f, 30f,
    0.2f, 1.3f, 0.0f, 0.0f, 20f,
    0.0f, 0.0f, 0.7f, 0.0f, -20f,
    0.0f, 0.0f, 0.0f, 1.0f, 0f
  ))),
  KI_CYAN("GOD BLUE", ColorMatrix(floatArrayOf(
    0.6f, 0.0f, 0.2f, 0.0f, -20f,
    0.0f, 1.2f, 0.3f, 0.0f, 10f,
    0.1f, 0.2f, 1.6f, 0.0f, 40f,
    0.0f, 0.0f, 0.0f, 1.0f, 0f
  ))),
  DESTROYER_PURPLE("DESTROYER", ColorMatrix(floatArrayOf(
    1.1f, 0.0f, 0.3f, 0.0f, 25f,
    0.1f, 0.6f, 0.2f, 0.0f, -15f,
    0.3f, 0.0f, 1.4f, 0.0f, 30f,
    0.0f, 0.0f, 0.0f, 1.0f, 0f
  )))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WallpaperDashboardScreen() {
  val context = LocalContext.current
  val list = remember {
    listOf(
      WallpaperItem(
        id = "goku_ui",
        title = "Goku Ultra Instinct",
        character = "Son Goku",
        category = "SAIYANS",
        drawableResId = R.drawable.img_dbz_goku_1780861499566,
        powerLevel = "150,000,000",
        formDescription = "Godlike composure where bodies act independently of thought.",
        accentColor = DbzCyanAura,
        signatureMove = "Autonomous Kamehameha"
      ),
      WallpaperItem(
        id = "vegeta_blue",
        title = "Vegeta Saiyan Blue",
        character = "Prince Vegeta",
        category = "SAIYANS",
        drawableResId = R.drawable.img_dbz_vegeta_1780861516220,
        powerLevel = "135,000,000",
        formDescription = "Ascended state surpassing limits with unyielding loyalty to royalty.",
        accentColor = Color(0xFF00B0FF),
        signatureMove = "Final Flash"
      ),
      WallpaperItem(
        id = "gohan_beast",
        title = "Gohan Beast Unleashed",
        character = "Son Gohan",
        category = "BEASTS",
        drawableResId = R.drawable.img_dbz_gohan_1780861528832,
        powerLevel = "142,000,000",
        formDescription = "Untrapped savage instinct driven by unmasked inner righteousness.",
        accentColor = Color(0xFFD500F9),
        signatureMove = "Special Beam Cannon"
      ),
      WallpaperItem(
        id = "shenron_summon",
        title = "Shenron Sacred Dragon",
        character = "Shenron",
        category = "CLASSIC",
        drawableResId = R.drawable.img_dbz_shenron_1780861545276,
        powerLevel = "Infinite",
        formDescription = "Eternal magical beast that grants mortals their dearest desires.",
        accentColor = Color(0xFF4CAF50),
        signatureMove = "Wish Granting"
      )
    )
  }

  // UI State Managers
  var searchString by remember { mutableStateOf("") }
  var activeCategory by remember { mutableStateOf("ALL") }
  var favoritesSet by remember { mutableStateOf(getFavorites(context)) }
  var selectedWallpaper by remember { mutableStateOf<WallpaperItem?>(null) }
  var showWallpaperOptionDialog by remember { mutableStateOf(false) }
  var isApplyingWallpaper by remember { mutableStateOf(false) }

  // Background gradient shift simulation for interactive feel
  val infiniteTransition = rememberInfiniteTransition(label = "Background Gradient Pulse")
  val gradientOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 600f,
    animationSpec = infiniteRepeatable(
      animation = tween(8000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "Float offset"
  )

  // Filter the list dynamically based on active filters and search
  val filteredWallpapers = remember(searchString, activeCategory, favoritesSet) {
    list.filter { item ->
      val matchesSearch = item.title.contains(searchString, ignoreCase = true) ||
          item.character.contains(searchString, ignoreCase = true) ||
          item.signatureMove.contains(searchString, ignoreCase = true)

      val matchesCategory = when (activeCategory) {
        "ALL" -> true
        "FAVORITES" -> favoritesSet.contains(item.id)
        else -> item.category == activeCategory
      }

      matchesSearch && matchesCategory
    }
  }

  // Master Layout Scaffold
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.linearGradient(
              colors = listOf(DbzBlack, DbzDarkBg),
              start = androidx.compose.ui.geometry.Offset(0f, 0f),
              end = androidx.compose.ui.geometry.Offset(gradientOffset, gradientOffset)
            )
          )
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        // High-fidelity custom glowing branding header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(DbzOrangeLight)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "OFFLINE ENGINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DbzOrangeLight,
                letterSpacing = 1.5.sp
              )
            }
            Text(
              text = "DRAGON BALL",
              fontSize = 24.sp,
              fontWeight = FontWeight.ExtraBold,
              color = DbzTextLight,
              fontFamily = FontFamily.SansSerif,
              letterSpacing = 2.sp
            )
            Text(
              text = "STUNNING MOBILE WALLPAPERS",
              fontSize = 10.sp,
              fontWeight = FontWeight.Light,
              color = DbzTextMuted,
              letterSpacing = 3.sp
            )
          }

          // Top right small branding circular button
          Card(
            modifier = Modifier.size(44.dp),
            colors = CardDefaults.cardColors(containerColor = DbzCardBg),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DbzOrangePrimary.copy(alpha = 0.4f))
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_app_icon_1780861486230),
                contentDescription = "Core Star",
                modifier = Modifier.size(32.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Professional dark theme text search bar
        OutlinedTextField(
          value = searchString,
          onValueChange = { searchString = it },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_bar_input"),
          placeholder = {
            Text(
              "Search Fighter or Ki Attack...",
              fontSize = 14.sp,
              color = DbzTextMuted
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search icon",
              tint = DbzOrangePrimary
            )
          },
          trailingIcon = {
            if (searchString.isNotEmpty()) {
              IconButton(onClick = { searchString = "" }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear search",
                  tint = DbzTextMuted
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DbzCardBg,
            unfocusedContainerColor = DbzBlack.copy(alpha = 0.5f),
            focusedTextColor = DbzTextLight,
            unfocusedTextColor = DbzTextLight,
            focusedBorderColor = DbzOrangePrimary,
            unfocusedBorderColor = DbzCardBg
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Advanced filter horizontal scrolling Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val categories = listOf("ALL", "SAIYANS", "BEASTS", "CLASSIC", "FAVORITES")
          categories.forEach { cat ->
            val isActive = activeCategory == cat
            FilterChip(
              selected = isActive,
              onClick = { activeCategory = cat },
              label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (cat == "FAVORITES") {
                    Icon(
                      imageVector = if (isActive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                      contentDescription = "Favorites Tab icon",
                      modifier = Modifier.size(14.dp),
                      tint = if (isActive) DbzRedDestruction else DbzOrangePrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                  }
                  Text(
                    text = cat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                  )
                }
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = DbzOrangePrimary,
                selectedLabelColor = Color.White,
                containerColor = DbzCardBg,
                labelColor = DbzTextLight
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isActive,
                borderColor = DbzCardBg,
                selectedBorderColor = DbzOrangePrimary,
                borderWidth = 1.dp
              )
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(DbzDarkBg)
        .padding(innerPadding)
    ) {
      if (filteredWallpapers.isEmpty()) {
        // High fidelity styled Empty State
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Card(
            modifier = Modifier.size(80.dp),
            colors = CardDefaults.cardColors(containerColor = DbzCardBg),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, DbzOrangePrimary.copy(alpha = 0.3f))
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "No results",
                modifier = Modifier.size(40.dp),
                tint = DbzOrangeLight
              )
            }
          }
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            text = "No Offline Power Levels Found",
            color = DbzTextLight,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Try modifying your search or adjust the active category filter.",
            color = DbzTextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
          )
        }
      } else {
        // High-contrast, beautifully padded multi-column grid
        LazyVerticalGrid(
          columns = GridCells.Fixed(2),
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(filteredWallpapers, key = { it.id }) { item ->
            // Favorite status check
            val isFav = favoritesSet.contains(item.id)

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clickable { selectedWallpaper = item }
                .testTag("wallpaper_card_${item.id}"),
              colors = CardDefaults.cardColors(containerColor = DbzCardBg),
              shape = RoundedCornerShape(16.dp),
              elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
              border = BorderStroke(0.5.dp, DbzTextMuted.copy(alpha = 0.15f))
            ) {
              Box(modifier = Modifier.fillMaxSize()) {
                // High Quality Local Wallpaper Preview Image
                Image(
                  painter = painterResource(id = item.drawableResId),
                  contentDescription = item.title,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )

                // Elegant Top Edge Glowing Aura Gradient
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(
                      Brush.verticalGradient(
                        colors = listOf(
                          Color.Black.copy(alpha = 0.5f),
                          Color.Transparent,
                          Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 0f,
                        endY = 800f
                      )
                    )
                )

                // Interactive Heart Button on Top Right
                IconButton(
                  onClick = {
                    val nextFav = !isFav
                    saveFavorite(context, item.id, nextFav)
                    favoritesSet = getFavorites(context)
                  },
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(DbzBlack.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                ) {
                  Icon(
                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite Toggle",
                    tint = if (isFav) DbzRedDestruction else DbzTextLight,
                    modifier = Modifier.size(18.dp)
                  )
                }

                // Fighter / Character Banner on Bottom
                Column(
                  modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(item.accentColor)
                    )
                    Text(
                      text = item.character.uppercase(),
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = item.accentColor,
                      letterSpacing = 1.sp
                    )
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DbzTextLight,
                    maxLines = 1
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Box(
                    modifier = Modifier
                      .background(
                        DbzOrangePrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                      )
                      .border(0.5.dp, DbzOrangePrimary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "OFFLINE LOCAL",
                      fontSize = 8.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = DbzAccentGold,
                      letterSpacing = 0.5.sp
                    )
                  }
                }
              }
            }
          }
        }
      }

      // -------------------------------------------------------------
      // IMMERSIVE FULL-SCREEN PREVIEW AND ACTION PANEL
      // -------------------------------------------------------------
      AnimatedVisibility(
        visible = selectedWallpaper != null,
        enter = slideInVertically(
          initialOffsetY = { it },
          animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        ) + fadeIn(),
        exit = slideOutVertically(
          targetOffsetY = { it },
          animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        ) + fadeOut()
      ) {
        selectedWallpaper?.let { wallpaper ->
          var selectedAuraFilter by remember { mutableStateOf(AuraFilter.NORMAL) }
          val coroutineScope = rememberCoroutineScope()

          // Favorite state inside the overlay
          val isCurrentlyFav = favoritesSet.contains(wallpaper.id)

          // Custom wallpaper screen content
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(DbzBlack)
          ) {
            // Full Screen Image scaled beautifully
            Image(
              painter = painterResource(id = wallpaper.drawableResId),
              contentDescription = "Fullscreen preview of ${wallpaper.title}",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
              colorFilter = ColorFilter.colorMatrix(selectedAuraFilter.colorMatrix)
            )

            // Dynamic interactive energy gradient layout
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(
                      Color.Black.copy(alpha = 0.4f),
                      Color.Transparent,
                      DbzBlack.copy(alpha = 0.95f)
                    ),
                    startY = 0f,
                    endY = 1600f
                  )
                )
            )

            // Top control header icons overlay
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Back Button
              IconButton(
                onClick = { selectedWallpaper = null },
                modifier = Modifier
                  .size(44.dp)
                  .background(DbzBlack.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = "Back back arrow",
                  tint = DbzTextLight,
                  modifier = Modifier
                    .size(24.dp)
                    .scale(-1f, 1f) // Mirror horizontally as reverse play arrow
                )
              }

              Text(
                text = "OFFLINE PREVIEW",
                color = DbzTextLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier
                  .background(DbzBlack.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              )

              // Favorite toggle in core overlay
              IconButton(
                onClick = {
                  val nextFav = !isCurrentlyFav
                  saveFavorite(context, wallpaper.id, nextFav)
                  favoritesSet = getFavorites(context)
                },
                modifier = Modifier
                  .size(44.dp)
                  .background(DbzBlack.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
              ) {
                Icon(
                  imageVector = if (isCurrentlyFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = "Favorite in preview",
                  tint = if (isCurrentlyFav) DbzRedDestruction else DbzTextLight,
                  modifier = Modifier.size(22.dp)
                )
              }
            }

            // Bottom Information Overlay with interactive power meters and action controls
            Column(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(12.dp)
            ) {
              // Sliding stats panel
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DbzCardBg.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, wallpaper.accentColor.copy(alpha = 0.3f))
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                ) {
                  // Category & Power Rating Header
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .background(
                          wallpaper.accentColor.copy(alpha = 0.15f),
                          RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, wallpaper.accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                      Text(
                        text = wallpaper.category,
                        color = wallpaper.accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                      )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Power Rating",
                        tint = DbzAccentGold,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = "KI LEVEL: ${wallpaper.powerLevel}",
                        color = DbzAccentGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  // Fighter Name
                  Text(
                    text = wallpaper.title,
                    color = DbzTextLight,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                  )

                  Spacer(modifier = Modifier.height(6.dp))

                  // Short form history
                  Text(
                    text = wallpaper.formDescription,
                    color = DbzTextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                  )

                  Spacer(modifier = Modifier.height(14.dp))

                  // Signature attack label
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.PlayArrow,
                      contentDescription = "Signature Tag",
                      tint = DbzOrangePrimary,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "SIGNATURE KI ATTACK: ",
                      color = DbzTextMuted,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = wallpaper.signatureMove,
                      color = DbzTextLight,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.ExtraBold
                    )
                  }

                  Spacer(modifier = Modifier.height(16.dp))

                  // -------------------------------------------------------------
                  // BRAND NEW INTERACTIVE AURA FILTER SYSTEM
                  // -------------------------------------------------------------
                  Text(
                    text = "CUSTOM DIGITAL KI FILTER",
                    color = DbzTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    AuraFilter.values().forEach { filter ->
                      val isSelected = selectedAuraFilter == filter
                      Box(
                        modifier = Modifier
                          .weight(1f)
                          .background(
                            if (isSelected) DbzOrangePrimary else DbzBlack,
                            RoundedCornerShape(8.dp)
                          )
                          .border(
                            1.dp,
                            if (isSelected) DbzOrangePrimary else DbzTextMuted.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                          )
                          .clickable { selectedAuraFilter = filter }
                          .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = filter.label,
                          color = if (isSelected) Color.White else DbzTextMuted,
                          fontSize = 8.sp,
                          fontWeight = FontWeight.Bold,
                          textAlign = TextAlign.Center
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(20.dp))

                  // Action Buttons: Save/Download to Gallery & Set as Wallpaper
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    // Download to Gallery Button
                    OutlinedButton(
                      onClick = {
                        coroutineScope.launch {
                          isApplyingWallpaper = true
                          val result = withContext(Dispatchers.IO) {
                            saveWallpaperToGallery(context, wallpaper.drawableResId, wallpaper.title)
                          }
                          isApplyingWallpaper = false
                          Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                        }
                      },
                      modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("download_wallpaper_button"),
                      colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DbzAccentGold
                      ),
                      border = BorderStroke(1.5.dp, DbzAccentGold),
                      shape = RoundedCornerShape(12.dp)
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.Done,
                          contentDescription = "Save to Gallery Icon",
                          tint = DbzAccentGold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          "DOWNLOAD",
                          fontWeight = FontWeight.Bold,
                          fontSize = 13.sp,
                          letterSpacing = 0.5.sp
                        )
                      }
                    }

                    // Apply Wallpaper Action Button
                    Button(
                      onClick = { showWallpaperOptionDialog = true },
                      modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                        .testTag("apply_wallpaper_button"),
                      colors = ButtonDefaults.buttonColors(
                        containerColor = DbzOrangePrimary,
                        contentColor = Color.White
                      ),
                      shape = RoundedCornerShape(12.dp)
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.Star,
                          contentDescription = "Set Wallpaper Click"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          "SET WALLPAPER",
                          fontWeight = FontWeight.Bold,
                          fontSize = 13.sp,
                          letterSpacing = 0.5.sp
                        )
                      }
                    }
                  }
                }
              }
            }

            // Spinner loader when applying wallpaper is active
            if (isApplyingWallpaper) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
              ) {
                Card(
                  colors = CardDefaults.cardColors(containerColor = DbzCardBg),
                  shape = RoundedCornerShape(16.dp),
                  modifier = Modifier.padding(32.dp),
                  border = BorderStroke(1.dp, DbzOrangePrimary.copy(alpha = 0.4f))
                ) {
                  Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    CircularProgressIndicator(color = DbzOrangePrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                      text = "Gathering Ki...",
                      color = DbzTextLight,
                      fontWeight = FontWeight.Bold,
                      fontSize = 16.sp
                    )
                    Text(
                      text = "Applying secure offline wallpaper layout.",
                      color = DbzTextMuted,
                      fontSize = 12.sp,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(top = 4.dp)
                    )
                  }
                }
              }
            }
          }

          // BOTTOM SHEET DIALOG PREVIEW OPTIONS
          if (showWallpaperOptionDialog) {
            AlertDialog(
              onDismissRequest = { showWallpaperOptionDialog = false },
              modifier = Modifier.testTag("wallpaper_destination_dialog"),
              title = {
                Text(
                  text = "Set Destination",
                  color = DbzTextLight,
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                )
              },
              text = {
                Text(
                  text = "Where would you like to apply the ${wallpaper.character} offline wallpaper?",
                  color = DbzTextMuted,
                  fontSize = 14.sp
                )
              },
              confirmButton = {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  // Option 1: Home Screen
                  Button(
                    onClick = {
                      showWallpaperOptionDialog = false
                      isApplyingWallpaper = true
                      coroutineScope.launch {
                        val result = withContext(Dispatchers.IO) {
                          applyOfflineWallpaper(context, wallpaper.drawableResId, WallpaperManager.FLAG_SYSTEM)
                        }
                        isApplyingWallpaper = false
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                      }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DbzOrangePrimary),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(imageVector = Icons.Default.Build, contentDescription = "Home Device")
                      Spacer(modifier = Modifier.width(8.dp))
                      Text("HOME SCREEN ONLY", fontSize = 12.sp)
                    }
                  }

                  // Option 2: Lock Screen
                  Button(
                    onClick = {
                      showWallpaperOptionDialog = false
                      isApplyingWallpaper = true
                      coroutineScope.launch {
                        val result = withContext(Dispatchers.IO) {
                          applyOfflineWallpaper(context, wallpaper.drawableResId, WallpaperManager.FLAG_LOCK)
                        }
                        isApplyingWallpaper = false
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                      }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DbzOrangeLight),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(imageVector = Icons.Default.Info, contentDescription = "Lock Device")
                      Spacer(modifier = Modifier.width(8.dp))
                      Text("LOCK SCREEN ONLY", fontSize = 12.sp)
                    }
                  }

                  // Option 3: Both
                  Button(
                    onClick = {
                      showWallpaperOptionDialog = false
                      isApplyingWallpaper = true
                      coroutineScope.launch {
                        val result = withContext(Dispatchers.IO) {
                          applyOfflineWallpaper(context, wallpaper.drawableResId, 0) // 0 implies both
                        }
                        isApplyingWallpaper = false
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                      }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DbzCardBg),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DbzOrangePrimary.copy(alpha = 0.5f))
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(imageVector = Icons.Default.Done, contentDescription = "Both Devices", tint = DbzAccentGold)
                      Spacer(modifier = Modifier.width(8.dp))
                      Text("APPLY TO BOTH SCREENS", fontSize = 12.sp, color = DbzAccentGold)
                    }
                  }

                  // Option 4: Dismiss
                  TextButton(
                    onClick = { showWallpaperOptionDialog = false },
                    modifier = Modifier.align(Alignment.End)
                  ) {
                    Text("CANCEL", color = DbzTextMuted, fontWeight = FontWeight.Bold)
                  }
                }
              },
              dismissButton = {},
              containerColor = DbzCardBg,
              shape = RoundedCornerShape(20.dp)
            )
          }
        }
      }
    }
  }
}

// True Android WallpaperManager Application Service (Executing Offline via IO Scope)
private fun applyOfflineWallpaper(context: Context, resId: Int, flag: Int): String {
  val wallpaperManager = WallpaperManager.getInstance(context)
  return try {
    val inputStream = context.resources.openRawResource(resId)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && flag != 0) {
      wallpaperManager.setStream(inputStream, null, true, flag)
    } else {
      wallpaperManager.setStream(inputStream)
    }
    "Success! Dragon Ball power applied to your device background."
  } catch (e: Exception) {
    "Failed to apply wallpaper: ${e.localizedMessage ?: "Unknown Error"}"
  }
}

// Save Full-Res Wallpaper directly to Gallery / Pictures folder offline
private fun saveWallpaperToGallery(context: Context, resId: Int, title: String): String {
  return try {
    val bitmap = BitmapFactory.decodeResource(context.resources, resId)
    val filename = "DBZ_${title.replace(" ", "_")}_${System.currentTimeMillis()}.png"
    val contentValues = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
      put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DragonBallWallpapers")
        put(MediaStore.MediaColumns.IS_PENDING, 1)
      }
    }

    val resolver = context.contentResolver
    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
      ?: return "Failed to access storage."

    resolver.openOutputStream(imageUri)?.use { out ->
      bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      contentValues.clear()
      contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
      resolver.update(imageUri, contentValues, null, null)
    }

    "Downloaded! Saved to Pictures/DragonBallWallpapers"
  } catch (e: Exception) {
    "Download failed: ${e.localizedMessage ?: "Error saving file"}"
  }
}

