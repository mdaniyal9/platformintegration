package dev.flutter.example


import android.content.Context
import android.util.Log
import android.view.View
import com.example.platformintegration.DiscreteEcgChart

import com.scichart.charting.model.dataSeries.XyDataSeries
import com.scichart.charting.modifiers.PinchZoomModifier
import com.scichart.charting.modifiers.RolloverModifier
import com.scichart.charting.modifiers.ZoomPanModifier
import com.scichart.charting.visuals.SciChartSurface
import com.scichart.charting.visuals.axes.AxisAlignment
import com.scichart.charting.visuals.axes.AxisTitleOrientation
import com.scichart.charting.visuals.axes.NumericAxis
import com.scichart.charting.visuals.renderableSeries.FastLineRenderableSeries
import com.scichart.core.framework.UpdateSuspender
import com.scichart.drawing.utility.ColorUtil
import com.scichart.extensions.builders.SciChartBuilder
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.platform.PlatformView
import java.util.*
import kotlin.collections.ArrayList


class NativeView internal constructor(
    private val context: Context,
    private val messenger: BinaryMessenger?,
    private val id: Int,
    private val creationParams: Map<String?, Any?>?
) :
    PlatformView, MethodCallHandler {
    private val methodChannel: MethodChannel
    var surface: SciChartSurface
    private var lable = "seconds"
    private var xSeries: ArrayList<Double>? = arrayListOf()
    private var ySeries: ArrayList<Double>? = arrayListOf()

    private var xS: DoubleArray? = null

    var discreteChart: DiscreteEcgChart? = null


    override fun getView(): View {
        return surface
    }

    override fun onMethodCall(methodCall: MethodCall, result: MethodChannel.Result) {
        when (methodCall.method) {
            "setText" -> setText(methodCall, result)
            else -> result.notImplemented()
        }
    }

    private fun setText(methodCall: MethodCall, result: MethodChannel.Result) {
        val text = methodCall.arguments as String
        Log.e("TAG", "setText: Text received $text")
        lable = text
        setGraph()
        result.success(null)
    }

    override fun dispose() {
        Log.e("TAG", "dispose: ")
    }

    init {
        methodChannel = MethodChannel(
            messenger!!,
            "platform-integration"
        )
        methodChannel.setMethodCallHandler(this)
        surface = SciChartSurface(context)

        setGraph()
    }

    private fun setGraph() {
        lable = creationParams!!["lable"].toString()
        xSeries = creationParams["x"] as ArrayList<Double>
        xS = DoubleArray(xSeries!!.size)
        for (i in 0 until xSeries!!.size){
            xS!![i] = xSeries!![i]
        }
        ySeries = creationParams["y"] as ArrayList<Double>

        discreteChart = DiscreteEcgChart(context, surface)
        discreteChart!!.setupGraph(ColorUtil.LimeGreen,"ECG")

        discreteChart!!.addData(xS!!)
        Log.e("TAG", ": INIT --> $id----> ${creationParams["lable"]}---> $xSeries")
    }
}