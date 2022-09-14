package com.example.platformintegration

import com.scichart.charting.visuals.SciChartSurface
import dev.flutter.example.NativeViewFactory
import io.flutter.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine


class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        setLicense()
        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory("<platform-view-type>", NativeViewFactory(flutterEngine.dartExecutor.binaryMessenger))
    }

    private fun setLicense() {
        try {
            SciChartSurface.setRuntimeLicenseKey("b+jmezHqlkFVsmZ+4tNLrsl1lc9vo83X50qzNhEOkAs5xB8O8HG/apqpSX2llK8k8MbHWoKKz6ZkniOknnoO7q0ED63eFKu8I0kEO/NHHgZWpGvi/XQUROILALv9niR7tAO8QmhCKFGTcunEjmY04iCH+f4ZLGvnSSrOgKjfDKnBD2H69puM01MA8CRKNvA5vnrh68h6VaMkzrZ5zLnXfjZp0g9zm6HP1TefwFVBZyoK4MWhDlvH3AANnbQSguX6PaL4mRafNKpufrw5pASO7mV+XB0ooZRlExtC/kKZqqfs1Sxm1+zsjMQq0SJGsyuvuEPMUnsVTyZmhydQFbOpH7PEk80i+ZGaBZt56A7Mf7ShdZvwMqH9tytIUWl0aao3bmq4vPyXPUlsrMzUsgBOWkDv2O2up1GKuPP7M8wiO1CjQFsTbF+QcgrdE/GO15Y/nwDzNtirwULbjaGRqNdbQ5e+c6m7Qc8iv/LrMkWcsxAQKpzaKQz5")
        } catch (e: Exception) {
            Log.e("SciChart", "Error when setting the license", e)
        }
    }
}
