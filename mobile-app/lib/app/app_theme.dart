import 'package:flutter/material.dart';

class AppTheme {
  static const Color pink = Color(0xFFEB5979);
  static const Color pinkDark = Color(0xFFD94368);

  static const Color graphite = Color(0xFF2A2B32);
  static const Color graphiteLight = Color(0xFF34353D);
  static const Color charcoal = Color(0xFF1F2026);

  static const Color lightBackground = Color(0xFFF5F5F7);
  static const Color lightCard = Color(0xFFFFFFFF);
  static const Color lightSurface = Color(0xFFF1F1F3);

  static const Color darkBackground = Color(0xFF1F2026);
  static const Color darkCard = Color(0xFF2A2B32);
  static const Color darkSurface = Color(0xFF34353D);

  static const Color text = Color(0xFF111827);
  static const Color mutedText = Color(0xFF6B7280);

  static const Color success = Color(0xFF16A34A);
  static const Color warning = Color(0xFFF59E0B);
  static const Color error = Color(0xFFDC2626);

  static ThemeData lightTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.light,
    scaffoldBackgroundColor: lightBackground,
    colorScheme: ColorScheme.fromSeed(
      seedColor: pink,
      brightness: Brightness.light,
      primary: pink,
      secondary: graphite,
      surface: lightCard,
      error: error,
    ),
    appBarTheme: const AppBarTheme(
      backgroundColor: graphite,
      foregroundColor: Colors.white,
      centerTitle: true,
      elevation: 0,
      surfaceTintColor: Colors.transparent,
      titleTextStyle: TextStyle(
        color: Colors.white,
        fontSize: 20,
        fontWeight: FontWeight.w800,
      ),
    ),
    cardTheme: CardThemeData(
      color: lightCard,
      elevation: 0,
      surfaceTintColor: Colors.transparent,
      shadowColor: Colors.black.withValues(alpha: 0.08),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(22),
        side: BorderSide(color: Colors.black.withValues(alpha: 0.06)),
      ),
      margin: const EdgeInsets.only(bottom: 14),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        backgroundColor: pink,
        foregroundColor: Colors.white,
        disabledBackgroundColor: pink.withValues(alpha: 0.45),
        disabledForegroundColor: Colors.white,
        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 15),
        textStyle: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: pink,
        side: const BorderSide(color: pink),
        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 15),
        textStyle: const TextStyle(fontWeight: FontWeight.w700),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      ),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: Colors.white,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      labelStyle: const TextStyle(color: mutedText),
      prefixIconColor: mutedText,
      suffixIconColor: mutedText,
      border: _inputBorder(Colors.black.withValues(alpha: 0.10)),
      enabledBorder: _inputBorder(Colors.black.withValues(alpha: 0.10)),
      focusedBorder: _inputBorder(pink, width: 1.6),
      errorBorder: _inputBorder(error),
      focusedErrorBorder: _inputBorder(error, width: 1.6),
    ),
    snackBarTheme: SnackBarThemeData(
      backgroundColor: graphite,
      contentTextStyle: const TextStyle(color: Colors.white),
      behavior: SnackBarBehavior.floating,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
    ),
    listTileTheme: const ListTileThemeData(
      iconColor: pink,
      titleTextStyle: TextStyle(
        color: text,
        fontWeight: FontWeight.w700,
        fontSize: 16,
      ),
      subtitleTextStyle: TextStyle(color: mutedText, fontSize: 13),
    ),
  );

  static ThemeData darkTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    scaffoldBackgroundColor: darkBackground,
    colorScheme: ColorScheme.fromSeed(
      seedColor: pink,
      brightness: Brightness.dark,
      primary: pink,
      secondary: Colors.white,
      surface: darkCard,
      error: error,
    ),
    appBarTheme: const AppBarTheme(
      backgroundColor: graphite,
      foregroundColor: Colors.white,
      centerTitle: true,
      elevation: 0,
      surfaceTintColor: Colors.transparent,
      titleTextStyle: TextStyle(
        color: Colors.white,
        fontSize: 20,
        fontWeight: FontWeight.w800,
      ),
    ),
    cardTheme: CardThemeData(
      color: darkCard,
      elevation: 0,
      surfaceTintColor: Colors.transparent,
      shadowColor: Colors.black.withValues(alpha: 0.25),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(22),
        side: BorderSide(color: Colors.white.withValues(alpha: 0.10)),
      ),
      margin: const EdgeInsets.only(bottom: 14),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        backgroundColor: pink,
        foregroundColor: Colors.white,
        disabledBackgroundColor: pink.withValues(alpha: 0.45),
        disabledForegroundColor: Colors.white,
        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 15),
        textStyle: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: Colors.white,
        side: BorderSide(color: Colors.white.withValues(alpha: 0.35)),
        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 15),
        textStyle: const TextStyle(fontWeight: FontWeight.w700),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      ),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: darkSurface,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      labelStyle: TextStyle(color: Colors.white.withValues(alpha: 0.72)),
      prefixIconColor: Colors.white.withValues(alpha: 0.72),
      suffixIconColor: Colors.white.withValues(alpha: 0.72),
      border: _inputBorder(Colors.white.withValues(alpha: 0.12)),
      enabledBorder: _inputBorder(Colors.white.withValues(alpha: 0.12)),
      focusedBorder: _inputBorder(pink, width: 1.6),
      errorBorder: _inputBorder(error),
      focusedErrorBorder: _inputBorder(error, width: 1.6),
    ),
    snackBarTheme: SnackBarThemeData(
      backgroundColor: darkSurface,
      contentTextStyle: const TextStyle(color: Colors.white),
      behavior: SnackBarBehavior.floating,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
    ),
    listTileTheme: ListTileThemeData(
      iconColor: pink,
      titleTextStyle: const TextStyle(
        color: Colors.white,
        fontWeight: FontWeight.w700,
        fontSize: 16,
      ),
      subtitleTextStyle: TextStyle(
        color: Colors.white.withValues(alpha: 0.70),
        fontSize: 13,
      ),
    ),
  );

  static OutlineInputBorder _inputBorder(Color color, {double width = 1}) {
    return OutlineInputBorder(
      borderRadius: BorderRadius.circular(16),
      borderSide: BorderSide(color: color, width: width),
    );
  }
}
