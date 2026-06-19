import 'package:flutter_test/flutter_test.dart';
import 'package:mobile_app/app/app.dart';

void main() {
  testWidgets('App loads login screen', (WidgetTester tester) async {
    await tester.pumpWidget(const MobileDataCaptureApp());

    expect(find.text('Mobile Data Capture'), findsOneWidget);
    expect(find.text('Login'), findsOneWidget);
  });
}
