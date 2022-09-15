package com.example.platformintegration


import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.scichart.charting.model.ChartModifierCollection
import com.scichart.charting.model.dataSeries.XyDataSeries
import com.scichart.charting.modifiers.GestureModifierBase
import com.scichart.charting.modifiers.PinchZoomModifier
import com.scichart.charting.modifiers.RolloverModifier
import com.scichart.charting.modifiers.ZoomPanModifier
import com.scichart.charting.visuals.SciChartSurface
import com.scichart.charting.visuals.axes.AutoRange
import com.scichart.charting.visuals.axes.AxisAlignment
import com.scichart.charting.visuals.axes.AxisTitleOrientation
import com.scichart.charting.visuals.axes.NumericAxis
import com.scichart.charting.visuals.renderableSeries.FastLineRenderableSeries
import com.scichart.core.framework.UpdateSuspender
import com.scichart.data.model.DoubleRange
import com.scichart.drawing.canvas.RenderSurface
import com.scichart.drawing.common.SolidPenStyle
import com.scichart.drawing.common.Style
import com.scichart.drawing.utility.ColorUtil
import com.scichart.extensions.builders.SciChartBuilder
import java.util.*

class DiscreteEcgChart(context: Context, private val surface: SciChartSurface) : FrameLayout(context)
{
    private val mContext: Context
    private var sampleRate = 0
    private var numberOfSlots = 1.5
    private var seconds = 1.5
    private var totalPoints = 0
    private var lineThickness = 2.0
    private var yMin = 0.0
    private var yMax = 0.0
    private var iLineColor = ColorUtil.LimeGreen
    private val series1 = XyDataSeries(Double::class.javaObjectType, Double::class.javaObjectType)
    private var _currentIndex: Int = 0
    private var _totalIndex = 0.0
    private var MaxPoint: Double = 0.0
    private var MinPoint: Double = 0.0
    private var graphRange: Double = 0.0
    private var Diffrence: Double = 0.0
    private var calculatedRange: Double = 0.0
    private var maxRange: Double = 0.0
    private var minRange: Double = 0.0
    private var TAG: String = "DiscreteEcgChart"
    private var mType: String = "ECG"


    var dataSource : DoubleArray = DoubleArray(2500)

    //grids
    var tickStyle = SolidPenStyle(Color.parseColor("#545b66"), true, 2f, null)
    var tickStyle2 = SolidPenStyle(Color.parseColor("#363e4b"), true, 1f, null)

    private var config = HashMap<String, Double>()
    private var actualWidth: Int = 0
    private var actualHeight: Int = 0
    private var calculatedHeight: Double = 0.0
    private var newheight: Int = 0
    private var axisWidth: Int = 60
    private var axisHeight: Int = 20
    private var time: Double = 0.0
    val chartModifiers = ChartModifierCollection()
    var chartType: String? = null
    var x_axis: NumericAxis? =null
    var y_axis: NumericAxis? =null
    private var customModifier = CustomModifier()

    val viewOrientation = resources.configuration.orientation
    private val sciChartBuilder = SciChartBuilder.instance()
    private var rolloverModifier: RolloverModifier? = null

    init
    {
        this.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        series1.acceptsUnsortedData = true
        mContext = context
        configuration()

    }

    fun setupGraph(color: Int, type: String)
    {
        when (type)
        {
            "ECG" ->
            {
                dataSource =  DoubleArray(dataSource.size)
                 seconds = 10.0
                sampleRate = (dataSource.size/seconds).toInt()
                if (viewOrientation == Configuration.ORIENTATION_LANDSCAPE) numberOfSlots = 5.0 else numberOfSlots = 2.0
                totalPoints = dataSource.size
                yMin = -1.5
                yMax = 1.5
                lineThickness = 2.0
                mType = type
            }
        }
        chartType = type
        iLineColor = color
        getSize()
        calculations(numberOfSlots)
        drawAxis(type)
    }



    fun addData(data: DoubleArray)
    {
        onClearData()
        dataSource = data
        onCalculateDataRange()
        drawDataTo()
    }

    fun drawDataTo()
    {
        totalPoints = dataSource.size
        sampleRate = (dataSource.size/seconds).toInt()

        Log.e(TAG, "DeviceResults: Sample Rate 2 --------------- $sampleRate" )
        Log.e(TAG, "DeviceResults: Size 2 --------------- ${dataSource.size}" )

        for (point in 0 until totalPoints)
        {
//            if (mType == "Lepu-watch")
//            {
//                if (point > 500 && point < totalPoints-500)
//                    appendPointRESP()
//            }
//            else
                appendPointRESP()
        }

        android.os.Handler(Looper.getMainLooper()).postDelayed({
            y_axis!!.autoRange = AutoRange.Once
            x_axis!!.autoRange = AutoRange.Once
            //need to add in proper place after data has been added to graph(|)
            setVisibleRange(numberOfSlots, minRange, maxRange)
        },50)
    }

    private fun appendPointRESP()
    {
        if (_currentIndex >= dataSource.size)
        {
          return
        }

        val voltage = dataSource[_currentIndex]
        time = _totalIndex / sampleRate % seconds

        series1.append(time, voltage)

        _currentIndex++
        _totalIndex++
    }

    private fun drawAxis(type: String)
    {
        surface.xAxes.clear()
        surface.yAxes.clear()
        surface.renderableSeries.clear()
        surface.chartModifiers.clear()
        series1.clear()

        x_axis= sciChartBuilder.newNumericAxis()
            .withAxisTitle("seconds")
//            .withAxisTitleStyle(Constants.SCICHART_DEFAULT_FONT_STYLE)
            .build()

        y_axis = sciChartBuilder.newNumericAxis()
            .withAxisTitle("mV")
            .withGrowBy(0.1, 0.1)
            .withAxisAlignment(AxisAlignment.Left)
//            .withAxisTitleStyle(Constants.SCICHART_DEFAULT_FONT_STYLE)
            .withAxisTitleOrientation(AxisTitleOrientation.VerticalFlipped)
            .build()

        setVisibleRange(numberOfSlots, minRange, maxRange)
        x_axis!!.autoRange = AutoRange.Always
        x_axis!!.drawMajorGridLines = true
        x_axis!!.drawMinorGridLines = true
        x_axis!!.drawMajorBands = false
        x_axis!!.majorGridLineStyle = tickStyle
        x_axis!!.minorGridLineStyle = tickStyle2
        x_axis!!.autoTicks = false
        x_axis!!.majorDelta = 0.2
        x_axis!!.minorDelta = 0.04

        y_axis!!.autoRange = AutoRange.Always
        y_axis!!.drawMajorGridLines = true
        y_axis!!.drawMinorGridLines = true
        y_axis!!.drawMajorBands = false
        y_axis!!.majorGridLineStyle = tickStyle
        y_axis!!.minorGridLineStyle = tickStyle2
        y_axis!!.autoTicks = false
        y_axis!!.majorDelta = 0.5
        y_axis!!.minorDelta = 0.1

        val rSeries1 = FastLineRenderableSeries()
        rSeries1.strokeStyle = SolidPenStyle(iLineColor, true, lineThickness.toFloat(), null)
        rSeries1.dataSeries = series1

        val zoomPanModifier = ZoomPanModifier()

        val zoompinchModifier = PinchZoomModifier()
        zoompinchModifier.isUniformZoom = true

        rolloverModifier = RolloverModifier()

        UpdateSuspender.using(surface)
        {
            surface.xAxes.add(x_axis)
            surface.yAxes.add(y_axis)
            surface.renderableSeries.add(rSeries1)
            Collections.addAll(surface.chartModifiers, zoomPanModifier, zoompinchModifier, rolloverModifier)
            surface.chartModifiers.add(customModifier)
        }
    }

    fun configuration()
    {
        config.put("gXmin", 0.04)
        config.put("gXmaj", 0.2)
        config.put("gYmin", 0.1)
        config.put("gYmaj", 0.5)
    }

    fun getSize()
    {
        actualWidth = Resources.getSystem().displayMetrics.widthPixels
        actualHeight = Resources.getSystem().displayMetrics.heightPixels
    }

    fun calculations(seconds: Double)
    {
        val delta = pointToPixel(0.0, seconds, 0.0, actualWidth.toDouble() - axisWidth, config.get("gXmin"))
        calculatedHeight = findHeight(delta, config.get("gYmin"), Math.abs(yMax) + Math.abs(yMin))


        Log.e(TAG, "calculations: Delta $delta  Height $calculatedHeight +++++++++++++++++++++++++++++++++++++++++++++++++++ ")
        newheight = calculatedHeight.toInt()
    }

    fun pointToPixel(x1: Double?, x2: Double?, y1: Double?, y2: Double?, value: Double?): Double?
    {
        val slope = (y2!! - y1!!) / (x2!! - x1!!)
        return y1 + slope * (value!! - x1)
    }

    fun findHeight(delta: Double?, grid_y: Double?, range_sum: Double?): Double
    {
        Log.e(TAG, "findHeight: Grid Y $grid_y  Range Sum $range_sum +++++++++++++++++++++++++++++++++++++++++++++++++++ ")
        return delta!! / grid_y!! * range_sum!!
    }

    fun onOrientationChange(landscape: Boolean)
    {
        when(chartType)
        {
            "ECG" ->
            {
                if (landscape)
                {
                    numberOfSlots = 6.0
                    getSize()
                    calculations(numberOfSlots)
                    setVisibleRange(numberOfSlots, minRange, maxRange)
                }
                else
                {
                    numberOfSlots = 2.0
                    getSize()
                    calculations(numberOfSlots)
                    setVisibleRange(numberOfSlots, minRange, maxRange)
                }
            }
        }
    }

    fun setVisibleRange(x_Max: Double, y_Min: Double, y_Max: Double)
    {
        x_axis!!.visibleRange = DoubleRange(0.0,x_Max)
        if (mType == "Lepu-Watch")
            y_axis!!.visibleRange = DoubleRange(yMin,yMax)
        else
            y_axis!!.visibleRange = DoubleRange(y_Min,y_Max)
    }

    fun onClearData()
    {
        series1.clear()
        _currentIndex = 0
        _totalIndex = 0.0
        time = 0.0
    }

    fun onCalculateDataRange()
    {
        graphRange = Math.abs(yMax) + Math.abs(yMin)
        MinPoint = dataSource.minOrNull()!!
        MaxPoint = dataSource.maxOrNull()!!
        if (MinPoint < yMin && MaxPoint > yMax)
        {
            MinPoint = yMin
            MaxPoint = yMax
        }
        else if (MinPoint < yMin)
        {
            MinPoint = yMin
        }
        else if (MaxPoint > yMax)
        {
            MaxPoint = yMax
        }
        calculatedRange = MaxPoint - MinPoint
        Diffrence = (graphRange - calculatedRange) / 2
        maxRange = MaxPoint + Diffrence
        minRange = MinPoint - Diffrence
    }

    inner class CustomModifier : GestureModifierBase()
    {
        override fun onDoubleTap(e: MotionEvent): Boolean
        {
            Log.e(TAG, "onDoubleTap: numberOfSlots $numberOfSlots minRange $minRange maxRange $maxRange +++++++++++++++++++++++++++ ")
            setVisibleRange(numberOfSlots, minRange, maxRange)
            return true
        }
        override fun onLongPress(e: MotionEvent?)
        {
            rolloverModifier!!.isEnabled = true
            rolloverModifier!!.drawVerticalLine = true
            rolloverModifier!!.showTooltip = true
            rolloverModifier!!.showAxisLabels = true
        }

        override fun onUp(e: MotionEvent?)
        {
            rolloverModifier!!.isEnabled = false
            rolloverModifier!!.drawVerticalLine = false
            rolloverModifier!!.showTooltip = false
            rolloverModifier!!.showAxisLabels = false
        }
    }
}
