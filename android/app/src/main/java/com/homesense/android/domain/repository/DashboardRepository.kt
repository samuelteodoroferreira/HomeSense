package com.homesense.android.domain.repository

import com.homesense.android.domain.model.DashboardSnapshot

fun interface DashboardRepository {
    suspend fun loadDashboard(deviceId: String): DashboardSnapshot
}
