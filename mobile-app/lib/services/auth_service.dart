class AuthService {
  static const String _demoUsername = 'admin';
  static const String _demoPassword = 'admin123';

  Future<bool> login({
    required String username,
    required String password,
  }) async {
    await Future.delayed(const Duration(milliseconds: 500));

    return username.trim() == _demoUsername && password == _demoPassword;
  }
}
