package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SavedDestination
import com.example.ui.theme.CarbonMistElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.MutedGold
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.StateActiveGreen
import com.example.ui.theme.StateExitAmber
import com.example.ui.theme.SteelLineBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationsScreen(viewModel: MainViewModel) {
    val destinations by viewModel.savedDestinations.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Todas") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Swipe-to-Reload / Pull-to-Refresh state
    var isRefreshing by remember { mutableStateOf(false) }

    // Permission statuses
    var hasForegroundPermission by remember {
        mutableStateOf(viewModel.geofenceService.hasLocationPermission())
    }
    var hasBackgroundPermission by remember {
        mutableStateOf(viewModel.geofenceService.hasBackgroundLocationPermission())
    }

    val categories = listOf("Todas", "Obra", "Oficina", "Cliente", "Taller")

    val filteredDestinations = destinations.filter { dest ->
        val matchesSearch = searchQuery.isBlank() ||
                dest.alias.contains(searchQuery, ignoreCase = true) ||
                dest.address.contains(searchQuery, ignoreCase = true) ||
                dest.category.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategoryFilter == "Todas" ||
                dest.category.equals(selectedCategoryFilter, ignoreCase = true)

        matchesSearch && matchesCategory
    }

    Scaffold(
        containerColor = ObsidianBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MutedGold,
                contentColor = ObsidianBackground,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLocationAlt,
                        contentDescription = "Agregar ubicación de geocerca",
                        tint = ObsidianBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nueva Zona",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ObsidianBackground
                    )
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    delay(1000)
                    hasForegroundPermission = viewModel.geofenceService.hasLocationPermission()
                    hasBackgroundPermission = viewModel.geofenceService.hasBackgroundLocationPermission()
                    viewModel.showToast("Ubicaciones y geocercas re-sincronizadas ✓")
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBackground)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // --- HEADER ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "UBICACIONES Y GEOCERCAS",
                            color = MutedGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Destinos Guardados",
                            color = TextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = CarbonMistElevated,
                        border = BorderStroke(1.dp, SteelLineBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = StateActiveGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${destinations.size} Zonas",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- PERMISSIONS STATUS BANNER ---
                LocationPermissionsBanner(
                    hasForeground = hasForegroundPermission,
                    hasBackground = hasBackgroundPermission,
                    onVerifyPermissions = {
                        hasForegroundPermission = viewModel.geofenceService.hasLocationPermission()
                        hasBackgroundPermission = viewModel.geofenceService.hasBackgroundLocationPermission()
                        viewModel.showToast(
                            if (hasForegroundPermission) "Permiso de GPS Primer Plano Verificado ✓"
                            else "Asegúrate de permitir acceso a la ubicación en Ajustes de Android"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // --- SEARCH BAR ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar destino, dirección u obra...", color = TextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MutedGold
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar búsqueda",
                                    tint = TextMuted
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MutedGold,
                        unfocusedBorderColor = SteelLineBorder,
                        focusedContainerColor = GraphiteSurface,
                        unfocusedContainerColor = GraphiteSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- CATEGORY FILTER CHIPS ---
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategoryFilter == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MutedGold,
                                selectedLabelColor = ObsidianBackground,
                                containerColor = CarbonMistElevated,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = SteelLineBorder,
                                selectedBorderColor = MutedGold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- DESTINATIONS LAZYCOLUMN WITH SWIPE-TO-DELETE ---
                if (filteredDestinations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyDestinationsStateUi(
                            isFiltered = searchQuery.isNotEmpty() || selectedCategoryFilter != "Todas",
                            onResetFilter = {
                                searchQuery = ""
                                selectedCategoryFilter = "Todas"
                            },
                            onAddClick = { showAddDialog = true }
                        )
                    }
                } else {
                    Text(
                        text = "Desliza a la izquierda una zona para eliminar (Swipe-to-Delete)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredDestinations, key = { it.id }) { item ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteSavedDestination(item.id)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> StateExitAmber
                                        else -> Color.Transparent
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color, RoundedCornerShape(20.dp))
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Eliminar",
                                                color = ObsidianBackground,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = ObsidianBackground
                                            )
                                        }
                                    }
                                },
                                content = {
                                    SavedDestinationCard(
                                        destination = item,
                                        onStartWorkDay = {
                                            viewModel.startWorkDayWithSavedDestination(item)
                                        },
                                        onDelete = {
                                            viewModel.deleteSavedDestination(item.id)
                                        }
                                    )
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(90.dp))
                        }
                    }
                }
            }
        }
    }

    // --- ADD GEOFENCE LOCATION DIALOG ---
    if (showAddDialog) {
        AddGeofenceLocationDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onConfirm = { alias, address, lat, lng, radius, category ->
                viewModel.addSavedDestination(
                    alias = alias,
                    address = address,
                    latitude = lat,
                    longitude = lng,
                    radiusMeters = radius,
                    category = category
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun LocationPermissionsBanner(
    hasForeground: Boolean,
    hasBackground: Boolean,
    onVerifyPermissions: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CarbonMistElevated,
        border = BorderStroke(1.dp, if (hasForeground) MutedGold.copy(alpha = 0.3f) else StateExitAmber.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (hasForeground) StateActiveGreen.copy(alpha = 0.15f) else StateExitAmber.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (hasForeground) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                            contentDescription = null,
                            tint = if (hasForeground) StateActiveGreen else StateExitAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (hasForeground) "Permisos GPS Activos ✓" else "Permisos GPS Requeridos",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (hasForeground) "Geocercas de entrada/salida monitoreadas (Anti-bounce 10s)" else "Activa ubicación para detección automática de geocercas",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onVerifyPermissions) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Verificar Permisos",
                    tint = MutedGold
                )
            }
        }
    }
}

@Composable
fun EmptyDestinationsStateUi(
    isFiltered: Boolean,
    onResetFilter: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = CarbonMistElevated,
            border = BorderStroke(1.dp, SteelLineBorder),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isFiltered) Icons.Default.FilterList else Icons.Default.Place,
                    contentDescription = null,
                    tint = MutedGold,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isFiltered) "No se encontraron zonas con los filtros actuales"
            else "No tienes ubicaciones o salas de trabajo guardadas",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isFiltered)
                "Intenta modificar el término de búsqueda o selecciona otra categoría."
            else
                "Añade lugares como Obras, Oficinas o Clientes para activar la automatización de geocercas al entrar o salir.",
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 12.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isFiltered) {
            OutlinedButton(
                onClick = onResetFilter,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MutedGold)
            ) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = MutedGold)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Limpiar Filtros", color = MutedGold, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = MutedGold),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = ObsidianBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Agregar Primera Zona", color = ObsidianBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SavedDestinationCard(
    destination: SavedDestination,
    onStartWorkDay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
        border = BorderStroke(1.dp, SteelLineBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Category badge & Delete action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MutedGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MutedGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = destination.category.uppercase(),
                        color = MutedGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = CarbonMistElevated,
                        border = BorderStroke(0.5.dp, SteelLineBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = StateActiveGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${destination.radiusMeters}m radio",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar destino",
                            tint = StateExitAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alias Title & Address
            Row(verticalAlignment = Alignment.Top) {
                val categoryIcon: ImageVector = when (destination.category.lowercase()) {
                    "oficina" -> Icons.Default.Business
                    "cliente" -> Icons.Default.Store
                    else -> Icons.Default.Place
                }

                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = MutedGold,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = destination.alias,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = destination.address,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coordinates & Action Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonMistElevated, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GPS: ${String.format("%.4f", destination.latitude)}, ${String.format("%.4f", destination.longitude)}",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Button(
                    onClick = onStartWorkDay,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MutedGold),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = ObsidianBackground,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Iniciar Jornada",
                        color = ObsidianBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AddGeofenceLocationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onConfirm: (alias: String, address: String, lat: Double, lng: Double, radius: Int, category: String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var aliasInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Obra") }
    var radiusInput by remember { mutableFloatStateOf(150f) }
    var latInput by remember { mutableStateOf("14.5880") }
    var lngInput by remember { mutableStateOf("-90.5280") }
    var isGeocoding by remember { mutableStateOf(false) }

    val categoryOptions = listOf("Obra", "Oficina", "Cliente", "Taller")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonMistElevated,
        titleContentColor = TextPrimary,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddLocationAlt,
                    contentDescription = null,
                    tint = MutedGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nueva Zona de Geocerca",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name / Alias Input
                OutlinedTextField(
                    value = aliasInput,
                    onValueChange = { aliasInput = it },
                    label = { Text("Nombre o Alias de Obra / Cliente", color = TextMuted) },
                    placeholder = { Text("Ej: Obra Central Zona 10", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MutedGold,
                        unfocusedBorderColor = SteelLineBorder,
                        focusedContainerColor = GraphiteSurface,
                        unfocusedContainerColor = GraphiteSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                // Address Input
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Dirección de la Ubicación", color = TextMuted) },
                    placeholder = { Text("Ej: 10a Calle 5-20, Zona 10", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MutedGold,
                        unfocusedBorderColor = SteelLineBorder,
                        focusedContainerColor = GraphiteSurface,
                        unfocusedContainerColor = GraphiteSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isGeocoding = true
                                    val coords = viewModel.geofenceService.geocodeAddress(addressInput)
                                    if (coords != null) {
                                        latInput = coords.lat.toString()
                                        lngInput = coords.lng.toString()
                                        viewModel.showToast("Coordenadas detectadas por Geocoder")
                                    }
                                    isGeocoding = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Obtener Coordenadas GPS",
                                tint = MutedGold
                            )
                        }
                    }
                )

                // Category Selection Chips
                Text("Categoría:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categoryOptions) { cat ->
                        val isSelected = categoryInput == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { categoryInput = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MutedGold,
                                selectedLabelColor = ObsidianBackground,
                                containerColor = GraphiteSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                // Radius Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Radio de Geocerca:", color = TextSecondary, fontSize = 12.sp)
                        Text("${radiusInput.toInt()} metros", color = MutedGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = radiusInput,
                        onValueChange = { radiusInput = it },
                        valueRange = 50f..500f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MutedGold,
                            activeTrackColor = MutedGold,
                            inactiveTrackColor = SteelLineBorder
                        )
                    )
                }

                // Coordinates Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latInput,
                        onValueChange = { latInput = it },
                        label = { Text("Latitud", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MutedGold,
                            unfocusedBorderColor = SteelLineBorder,
                            focusedContainerColor = GraphiteSurface,
                            unfocusedContainerColor = GraphiteSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lngInput,
                        onValueChange = { lngInput = it },
                        label = { Text("Longitud", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MutedGold,
                            unfocusedBorderColor = SteelLineBorder,
                            focusedContainerColor = GraphiteSurface,
                            unfocusedContainerColor = GraphiteSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latInput.toDoubleOrNull() ?: 14.5880
                    val lng = lngInput.toDoubleOrNull() ?: -90.5280
                    val alias = aliasInput.ifBlank { "Zona ${addressInput.take(15)}" }
                    val address = addressInput.ifBlank { "Ubicación personalizada" }
                    onConfirm(alias, address, lat, lng, radiusInput.toInt(), categoryInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MutedGold),
                shape = RoundedCornerShape(16.dp),
                enabled = aliasInput.isNotBlank() || addressInput.isNotBlank()
            ) {
                Text("Guardar Zona", color = ObsidianBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}
