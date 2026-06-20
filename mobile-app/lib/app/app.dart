import 'package:flutter/material.dart';

import '../screens/login_screen.dart';

import 'app_theme.dart';

class MobileDataCaptureApp extends StatelessWidget {
  const MobileDataCaptureApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'FieldSync',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      home: const LoginScreen(),
    );
  }
}
