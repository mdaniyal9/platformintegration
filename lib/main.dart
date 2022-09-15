import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key}) : super(key: key);

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  InteractingAnnotationChartController? controller = null;
  var isControllerCreated = false;

  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewTypeP = '<platform-view-type-p>';
    const String viewTypeL = '<platform-view-type-l>';
    // Pass parameters to the platform side.
    var x = [0.0,1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0];
    var y = [10.0,9.0,8.0,7.0,6.0,5.0,4.0,3.0,2.0,1.0,0.0];
    final Map<String, dynamic> creationParams = <String, dynamic>{'lable': 'Minutes', 'x': x, 'y': y};


    var orientation = MediaQuery.of(context).orientation;

    debugPrint('Build called-------> $orientation');
    if(controller!=null){
      controller!.setText('Minutes');
    }

    return SafeArea(
      child: Scaffold(
        body: OrientationBuilder(
          builder: (context, orientation){
            return Column(
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 5),
                  child: SizedBox(
                    height: 200,
                    child:
                    // AndroidView(
                    //   viewType: viewTypeP,
                    //   layoutDirection: TextDirection.ltr,
                    //   creationParams: creationParams,
                    //   creationParamsCodec: const StandardMessageCodec(),
                    //   onPlatformViewCreated: (id) {
                    //
                    //     controller = InteractingAnnotationChartController._(id);
                    //   },
                    // )
                    PlatformViewLink(
                      viewType: viewTypeP,
                      surfaceFactory:
                          (BuildContext context, PlatformViewController controller) {
                        return AndroidViewSurface(
                          controller: controller as AndroidViewController,
                          gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
                          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
                        );
                      },
                      onCreatePlatformView: (PlatformViewCreationParams params) {
                        controller = InteractingAnnotationChartController._(params.id);
                        return PlatformViewsService.initSurfaceAndroidView(
                          id: params.id,
                          viewType: viewTypeP,
                          layoutDirection: TextDirection.ltr,
                          creationParams: creationParams,
                          creationParamsCodec: StandardMessageCodec(),
                        )
                          ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
                          ..create();
                      },
                    )
                  ),
                ),
                ElevatedButton(
                  onPressed: () {
                    debugPrint('Button pressed');
                    if(controller != null) {
                      setState(() {
                        debugPrint('Button pressed 1');
                        controller!.setText('Minutes');
                      });
                    }
                  },
                  child: const Text('Set Text'),
                ),
              ],
            );
          },
        ),
      ),
    );
  }
}


class InteractingAnnotationChartController {
  InteractingAnnotationChartController._(int id)
      : _channel = new MethodChannel('platform-integration');

  final MethodChannel _channel;

  Future<void> setText(String text) async {
    assert(text != null);
    return _channel.invokeMethod('setText', text);
  }
}
