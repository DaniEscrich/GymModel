package com.danielescrich.myapplication.mainmodule.ui.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danielescrich.myapplication.databinding.FragmentWeightDialogBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PesoDialogFragment(
    private val pesoActual: Float,
    private val pesoObjetivo: Float
) : BottomSheetDialogFragment() {

    private var _binding: FragmentWeightDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvPesoActualDialog.text = "Peso actual: $pesoActual kg"
        binding.tvPesoObjetivoDialog.text = "Peso objetivo: $pesoObjetivo kg"

        configurarGrafica(binding.barChart)
    }

    private fun configurarGrafica(barChart: BarChart) {
        val entries = listOf(
            BarEntry(0f, pesoActual),
            BarEntry(1f, pesoObjetivo)
        )

        val dataSet = BarDataSet(entries, "Peso (kg)")
        dataSet.colors = listOf(ColorTemplate.MATERIAL_COLORS[0], ColorTemplate.MATERIAL_COLORS[1])
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK

        val barData = BarData(dataSet)
        barData.barWidth = 0.4f

        barChart.data = barData
        barChart.description = Description().apply { text = "" }
        barChart.legend.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(800)
        barChart.xAxis.apply {
            granularity = 1f
            setDrawGridLines(false)
            setDrawLabels(true)
            setDrawAxisLine(false)
            valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                listOf("Actual", "Objetivo")
            )
            textColor = Color.BLACK
            textSize = 12f
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        }
        barChart.axisLeft.textColor = Color.BLACK
        barChart.axisRight.isEnabled = false
        barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
