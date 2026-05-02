package com.homesense.android.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homesense.android.ui.theme.HomesenseBackground
import com.homesense.android.ui.theme.HomesenseChipPurple
import com.homesense.android.ui.theme.HomesenseCyan
import com.homesense.android.ui.theme.HomesenseHumidityAccent
import com.homesense.android.ui.theme.HomesenseSurface
import com.homesense.android.ui.theme.HomesenseTextSecondary

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = HomesenseBackground,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            HomesenseBottomBar(
                selected = ui.selectedTab,
                onSelect = viewModel::selectTab,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            HomesenseTopBar()
            Spacer(Modifier.height(16.dp))
            AssistChip(
                onClick = { },
                label = { Text("Select", color = MaterialTheme.colorScheme.onTertiary) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = HomesenseChipPurple,
                ),
                border = null,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Visão Geral do Sistema",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(20.dp))
            ClimateCardsRow(
                isLoading = ui.isLoading,
                temperatureC = ui.temperatureC,
                humidityPercent = ui.humidityPercent,
            )
            ui.errorMessage?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(28.dp))
            HistorySection(
                items = ui.energyHistory,
                onDismissRestored = viewModel::dismissFirstRestoredCard,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomesenseTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            imageVector = Icons.Default.GridView,
            contentDescription = null,
            tint = HomesenseCyan,
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = "HOME_SENSE",
            style = MaterialTheme.typography.titleLarge,
            color = HomesenseCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, HomesenseCyan, CircleShape)
                .background(HomesenseSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = HomesenseTextSecondary,
            )
        }
    }
}

@Composable
private fun ClimateCardsRow(
    isLoading: Boolean,
    temperatureC: Double?,
    humidityPercent: Double?,
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ClimateStatCard(
                modifier = Modifier.weight(1f),
                label = "CLIMA",
                value = if (isLoading && temperatureC == null) "…" else temperatureC?.let { "${it.toInt()}°C" } ?: "--",
                icon = { Icon(Icons.Default.Thermostat, null, tint = HomesenseCyan) },
                valueColor = HomesenseCyan,
            )
            ClimateStatCard(
                modifier = Modifier.weight(1f),
                label = "UMIDADE",
                value = if (isLoading && humidityPercent == null) "…" else humidityPercent?.let { "${it.toInt()}%" } ?: "--",
                icon = { Icon(Icons.Default.WaterDrop, null, tint = HomesenseHumidityAccent) },
                valueColor = HomesenseHumidityAccent,
            )
        }
        if (isLoading && temperatureC == null && humidityPercent == null) {
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = HomesenseCyan,
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
private fun ClimateStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: @Composable () -> Unit,
    valueColor: androidx.compose.ui.graphics.Color,
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HomesenseSurface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                icon()
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = HomesenseTextSecondary,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = valueColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun HistorySection(
    items: List<EnergyHistoryUiItem>,
    onDismissRestored: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Histórico",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Icon(Icons.Default.History, contentDescription = null, tint = HomesenseTextSecondary)
    }
    Spacer(Modifier.height(12.dp))
    items.forEach { item ->
        when (item) {
            is EnergyHistoryUiItem.EnergyRestoredCard -> EnergyRestoredCard(item, onDismissRestored)
            is EnergyHistoryUiItem.RecordedOutage -> {
                Text(
                    text = "Queda registrada: ${item.startedAtLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HomesenseTextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
    if (items.none { it is EnergyHistoryUiItem.EnergyRestoredCard } && items.isEmpty()) {
        Text(
            text = "Aguardando eventos de energia…",
            style = MaterialTheme.typography.bodyMedium,
            color = HomesenseTextSecondary,
        )
    }
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = HomesenseTextSecondary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = "Nenhuma outra interrupção detectada",
            style = MaterialTheme.typography.bodyMedium,
            color = HomesenseTextSecondary,
        )
    }
}

@Composable
private fun EnergyRestoredCard(
    item: EnergyHistoryUiItem.EnergyRestoredCard,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HomesenseSurface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(HomesenseCyan, HomesenseCyan.copy(alpha = 0.4f)),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = HomesenseCyan)
                    Text(
                        text = "ENERGIA RESTAURADA",
                        style = MaterialTheme.typography.labelLarge,
                        color = HomesenseCyan,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Houve uma queda de luz na data ")
                        withStyle(SpanStyle(color = HomesenseCyan, fontWeight = FontWeight.SemiBold)) {
                            append(item.outageDate)
                        }
                        append(", Hora ")
                        withStyle(SpanStyle(color = HomesenseCyan, fontWeight = FontWeight.SemiBold)) {
                            append(item.outageTime)
                        }
                        append(". Você ficou sem luz por ")
                        withStyle(SpanStyle(color = HomesenseCyan, fontWeight = FontWeight.SemiBold)) {
                            append(item.durationText)
                        }
                        append(".")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Ignorar", color = HomesenseTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomesenseBottomBar(
    selected: HomeNavTab,
    onSelect: (HomeNavTab) -> Unit,
) {
    NavigationBar(
        containerColor = HomesenseSurface,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = selected == HomeNavTab.Climate,
            onClick = { onSelect(HomeNavTab.Climate) },
            icon = { Icon(Icons.Default.Thermostat, contentDescription = null) },
            label = { Text("CLIMATE", style = MaterialTheme.typography.labelLarge) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HomesenseCyan,
                selectedTextColor = HomesenseCyan,
                unselectedIconColor = HomesenseTextSecondary,
                unselectedTextColor = HomesenseTextSecondary,
                indicatorColor = HomesenseCyan.copy(alpha = 0.12f),
            ),
        )
        NavigationBarItem(
            selected = selected == HomeNavTab.Energy,
            onClick = { onSelect(HomeNavTab.Energy) },
            icon = { Icon(Icons.Default.Bolt, contentDescription = null) },
            label = { Text("ENERGY", style = MaterialTheme.typography.labelLarge) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HomesenseCyan,
                selectedTextColor = HomesenseCyan,
                unselectedIconColor = HomesenseTextSecondary,
                unselectedTextColor = HomesenseTextSecondary,
                indicatorColor = HomesenseCyan.copy(alpha = 0.12f),
            ),
        )
        NavigationBarItem(
            selected = selected == HomeNavTab.Security,
            onClick = { onSelect(HomeNavTab.Security) },
            icon = { Icon(Icons.Default.Security, contentDescription = null) },
            label = { Text("SECURITY", style = MaterialTheme.typography.labelLarge) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HomesenseCyan,
                selectedTextColor = HomesenseCyan,
                unselectedIconColor = HomesenseTextSecondary,
                unselectedTextColor = HomesenseTextSecondary,
                indicatorColor = HomesenseCyan.copy(alpha = 0.12f),
            ),
        )
    }
}
