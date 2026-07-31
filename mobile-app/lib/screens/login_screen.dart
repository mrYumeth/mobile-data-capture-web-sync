import 'package:flutter/material.dart';

import '../app/app_theme.dart';
import '../services/auth_service.dart';
import 'home_screen.dart';
import '../services/master_data_sync_service.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  final _authService = AuthService();
  final _masterDataSyncService = MasterDataSyncService();

  bool _isLoading = false;
  bool _obscurePassword = true;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _completeLoginWithAutoSync() async {
    try {
      final syncResult = await _masterDataSyncService.syncMasterData();

      debugPrint(
        'Automatic master data sync completed: '
        '${syncResult.customerCount} customers, '
        '${syncResult.locationCount} locations, '
        '${syncResult.categoryCount} categories.',
      );

      if (!mounted) {
        return;
      }

      Navigator.of(
        context,
      ).pushReplacement(MaterialPageRoute(builder: (_) => const HomeScreen()));
    } catch (error) {
      debugPrint('Automatic master data sync failed: $error');

      // Prevent the newly logged-in tenant from seeing the previous
      // tenant's locally cached master data.
      await AuthService.clearSession();

      if (!mounted) {
        return;
      }

      setState(() {
        _isLoading = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Login succeeded, but your tenant data could not be loaded. '
            'Please check your internet connection and log in again.\n\n'
            'Details: $error',
          ),
          backgroundColor: Colors.red,
          duration: const Duration(seconds: 6),
        ),
      );
    }
  }

  Future<void> _login() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() {
      _isLoading = true;
    });

    final result = await _authService.login(
      username: _usernameController.text.trim(),
      password: _passwordController.text,
    );

    if (!mounted) {
      return;
    }

    if (result.isSuccess) {
      await _completeLoginWithAutoSync();
      return;
    }

    setState(() {
      _isLoading = false;
    });

    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(result.message)));
  }

  Future<void> _loginWithKeycloak() async {
    setState(() {
      _isLoading = true;
    });

    final result = await _authService.loginWithKeycloak();

    if (!mounted) {
      return;
    }

    if (result.isSuccess) {
      await _completeLoginWithAutoSync();
      return;
    }

    setState(() {
      _isLoading = false;
    });

    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(result.message)));
  }

  @override
  Widget build(BuildContext context) {
    final isDarkMode = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: isDarkMode
                ? const [
                    AppTheme.charcoal,
                    AppTheme.graphite,
                    AppTheme.graphiteLight,
                  ]
                : const [
                    Color(0xFFFFFFFF),
                    AppTheme.lightBackground,
                    AppTheme.lightSurface,
                  ],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(24),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 430),
                child: Form(
                  key: _formKey,
                  child: Container(
                    padding: const EdgeInsets.all(24),
                    decoration: BoxDecoration(
                      color: isDarkMode
                          ? Colors.white.withValues(alpha: 0.08)
                          : Colors.white.withValues(alpha: 0.82),
                      borderRadius: BorderRadius.circular(32),
                      border: Border.all(
                        color: isDarkMode
                            ? Colors.white.withValues(alpha: 0.12)
                            : Colors.black.withValues(alpha: 0.06),
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withValues(alpha: 0.16),
                          blurRadius: 40,
                          offset: const Offset(0, 22),
                        ),
                      ],
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        Center(
                          child: Container(
                            height: 96,
                            width: 96,
                            padding: const EdgeInsets.all(10),
                            decoration: BoxDecoration(
                              color: Colors.white,
                              shape: BoxShape.circle,
                              boxShadow: [
                                BoxShadow(
                                  color: AppTheme.pink.withValues(alpha: 0.25),
                                  blurRadius: 30,
                                  offset: const Offset(0, 12),
                                ),
                              ],
                            ),
                            child: Image.asset(
                              'assets/images/logo.png',
                              fit: BoxFit.contain,
                              errorBuilder: (_, __, ___) {
                                return const Icon(
                                  Icons.assignment_turned_in_outlined,
                                  color: AppTheme.pink,
                                  size: 48,
                                );
                              },
                            ),
                          ),
                        ),
                        const SizedBox(height: 24),
                        Text(
                          'FieldSync',
                          textAlign: TextAlign.center,
                          style: Theme.of(context).textTheme.headlineMedium
                              ?.copyWith(
                                fontWeight: FontWeight.w900,
                                color: isDarkMode
                                    ? Colors.white
                                    : AppTheme.text,
                              ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'Mobile Data Capture',
                          textAlign: TextAlign.center,
                          style: Theme.of(context).textTheme.bodyMedium
                              ?.copyWith(
                                color: isDarkMode
                                    ? Colors.white.withValues(alpha: 0.72)
                                    : AppTheme.mutedText,
                              ),
                        ),
                        const SizedBox(height: 28),
                        FilledButton.icon(
                          onPressed: _isLoading ? null : _loginWithKeycloak,
                          icon: _isLoading
                              ? const SizedBox(
                                  height: 18,
                                  width: 18,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: Colors.white,
                                  ),
                                )
                              : const Icon(Icons.security_outlined),
                          label: Text(_isLoading ? 'Signing in...' : 'Login'),
                        ),

                        const SizedBox(height: 12),

                        Container(
                          padding: const EdgeInsets.all(14),
                          decoration: BoxDecoration(
                            color: isDarkMode
                                ? Colors.white.withValues(alpha: 0.08)
                                : AppTheme.lightSurface,
                            borderRadius: BorderRadius.circular(18),
                          ),
                          child: Text(
                            'You will be redirected to the FieldSync IAM login page. Use the account provided by your administrator.',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              color: isDarkMode
                                  ? Colors.white.withValues(alpha: 0.72)
                                  : AppTheme.mutedText,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                        const SizedBox(height: 12),
                        Container(
                          padding: const EdgeInsets.all(14),
                          decoration: BoxDecoration(
                            color: isDarkMode
                                ? Colors.white.withValues(alpha: 0.08)
                                : AppTheme.lightSurface,
                            borderRadius: BorderRadius.circular(18),
                          ),
                          child: Text(
                            'Accounts are created by the administrator. Please use the username provided in your FieldSync invitation email.',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              color: isDarkMode
                                  ? Colors.white.withValues(alpha: 0.72)
                                  : AppTheme.mutedText,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                        const SizedBox(height: 18),
                        Container(
                          padding: const EdgeInsets.all(14),
                          decoration: BoxDecoration(
                            color: isDarkMode
                                ? Colors.white.withValues(alpha: 0.08)
                                : AppTheme.lightSurface,
                            borderRadius: BorderRadius.circular(18),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
