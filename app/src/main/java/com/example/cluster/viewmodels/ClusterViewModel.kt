package com.example.cluster.viewmodels

import android.app.Application
import android.icu.number.Notation
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.util.MeasureUnit
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cluster.ClusterApp
import com.example.cluster.utilities.SpeedometerCalculator
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.absoluteValue

class ClusterViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private val COLOR_NORMAL = Color(0xFF33CCFF)
        private val COLOR_CAUTION = Color(0xFFF5CB40)
        private val COLOR_WARNING = Color(0xFFD32F2F)

        private const val CAUTION_SPEED_THRESHOLD = 38f
        private const val WARNING_SPEED_THRESHOLD = 40f
    }

    private val repo = (application as ClusterApp).vehiclePropertyRepository
    val currentSpeed = repo.currentSpeed
    val currentSpeedUnit = repo.currentSpeedUnit

    // Derived states
    val speedLabel = mutableStateOf("")
    val unitLabel = mutableStateOf("")
    val textColor = mutableStateOf(Color.Unspecified)
    val backgroundColor = mutableStateOf(Color.Unspecified)
    val mainColor = mutableStateOf(Color.Unspecified)
    val secondaryColor = mutableStateOf(Color.Unspecified)

    private val calculator = SpeedometerCalculator()

    init {
        viewModelScope.launch {
            currentSpeed.collect {
                updateDerivedStates()
            }
        }
        viewModelScope.launch {
            currentSpeedUnit.collect {
                updateDerivedStates()
            }
        }
    }

    private fun updateDerivedStates() {
        val currentSpeed = currentSpeed.value
        val absoluteSpeed = currentSpeed.absoluteValue
        val (preferredSpeedUnit, speedInPreferredUnit) = calculator
            .getSpeedInfo(absoluteSpeed, currentSpeedUnit.value)

        getSpeedLabels(preferredSpeedUnit, speedInPreferredUnit).run {
            speedLabel.value = first
            unitLabel.value = second
        }
        getSpeedLabelColors(currentSpeed).run {
            textColor.value = first
            backgroundColor.value = second
        }
        getDialColors(absoluteSpeed).run {
            mainColor.value = first
            secondaryColor.value = second
        }
    }

    private fun getSpeedLabels(
        preferredSpeedUnit: MeasureUnit,
        speedInPreferredUnit: Float,
    ): Pair<String, String> {
        return NumberFormatter
            .withLocale(Locale.getDefault())
            .notation(Notation.compactShort())
            .precision(Precision.maxFraction(0))
            .unit(preferredSpeedUnit)
            .format(speedInPreferredUnit)
            .toString()
            .split("\\s+".toRegex())
            .let {
                when (it.size > 1) {
                    true -> Pair(it[0], it[1])
                    else -> Pair(it[0], "???")
                }
            }
    }

    private fun getSpeedLabelColors(speed: Float) = when {
        speed < 0f -> Color.Black to COLOR_CAUTION
        else -> Color.Unspecified to Color.Unspecified
    }

    private fun getDialColors(absoluteSpeed: Float): Pair<Color, Color> {
        return when {
            absoluteSpeed < CAUTION_SPEED_THRESHOLD -> COLOR_NORMAL
            absoluteSpeed < WARNING_SPEED_THRESHOLD -> COLOR_CAUTION
            else -> COLOR_WARNING
        }.let {
            it to it.copy(alpha = 0.2f)
        }
    }
}
