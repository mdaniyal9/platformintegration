package dev.flutter.example

//import android.content.Context
//import android.graphics.Color
//import android.view.View
//import android.widget.TextView
//import io.flutter.plugin.platform.PlatformView
//
//internal class NativeView(context: Context, id: Int, creationParams: Map<String?, Any?>?) : PlatformView {
//    private val textView: TextView
//
//    override fun getView(): View {
//        return textView
//    }
//
//    override fun dispose() {}
//
//    init {
//        textView = TextView(context)
//        textView.textSize = 72f
//        textView.setBackgroundColor(Color.rgb(255, 255, 255))
//        textView.text = "Rendered on a native Android view (id: $id)"
//    }
//}


import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.platformintegration.data.DataManager
import com.example.platformintegration.data.MarketDataService
import com.example.platformintegration.data.PriceSeries
import com.scichart.charting.model.dataSeries.OhlcDataSeries
import com.scichart.charting.model.dataSeries.XyDataSeries
import com.scichart.charting.modifiers.PinchZoomModifier
import com.scichart.charting.modifiers.RolloverModifier
import com.scichart.charting.modifiers.ZoomPanModifier
import com.scichart.charting.visuals.SciChartSurface
import com.scichart.charting.visuals.annotations.AnnotationCoordinateMode
import com.scichart.charting.visuals.annotations.AnnotationSurfaceEnum
import com.scichart.charting.visuals.annotations.HorizontalAnchorPoint
import com.scichart.charting.visuals.annotations.VerticalAnchorPoint
import com.scichart.charting.visuals.axes.AxisAlignment
import com.scichart.charting.visuals.axes.AxisTitleOrientation
import com.scichart.charting.visuals.renderableSeries.FastLineRenderableSeries
import com.scichart.core.framework.UpdateSuspender
import com.scichart.data.model.DoubleRange
import com.scichart.drawing.common.SolidPenStyle
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
    context: Context,
    messenger: BinaryMessenger?,
    id: Int,
    creationParams: Map<String?, Any?>?
) :
    PlatformView, MethodCallHandler {
    private val methodChannel: MethodChannel
    var surface: SciChartSurface
    private var lable = "secondssssss"
    private var xSeries: ArrayList<Double>? = arrayListOf()
    private var ySeries: ArrayList<Double>? = arrayListOf()
    override fun getView(): View? {
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
        result.success(null)
    }

    override fun dispose() {}


    private fun initExample(context: Context) {
        SciChartBuilder.init(context)
        val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()

        val fourierSeries = DataManager.getInstance().getFourierSeries(1.0, 0.1, 5000)

        val x_axis= sciChartBuilder.newNumericAxis()
            .withAxisTitle(lable)
            .build()

        val y_axis = sciChartBuilder.newNumericAxis()
            .withAxisTitle("mV")
            .withGrowBy(0.1, 0.1)
            .withAxisAlignment(AxisAlignment.Left)
            .withAxisTitleOrientation(AxisTitleOrientation.VerticalFlipped)
            .build()

        val rSeries1 = FastLineRenderableSeries()
        val dataSeries = XyDataSeries(Double::class.javaObjectType, Double::class.javaObjectType)
        rSeries1.dataSeries =  dataSeries

//        dataSeries.append(fourierSeries.xValues, fourierSeries.yValues)
        dataSeries.append(xSeries, ySeries)



        UpdateSuspender.using(surface)
        {
            surface.xAxes.add(x_axis)
            surface.yAxes.add(y_axis)
            surface.renderableSeries.add(rSeries1)
            Collections.addAll(surface.chartModifiers, ZoomPanModifier(), PinchZoomModifier(), RolloverModifier())
//            surface.chartModifiers.add()
        }
    }

    init {
        methodChannel = MethodChannel(
            messenger!!,
            "platform-integration_$id"
        )
        methodChannel.setMethodCallHandler(this)
        lable = creationParams!!["lable"].toString()
        xSeries = creationParams["x"] as ArrayList<Double>
        ySeries = creationParams["y"] as ArrayList<Double>
        surface = SciChartSurface(context)
        Log.e("TAG", ": INIT --> $id----> ${creationParams["lable"]}---> $xSeries")
        initExample(context)

    }
}