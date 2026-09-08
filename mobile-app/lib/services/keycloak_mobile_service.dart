import 'package:flutter_appauth/flutter_appauth.dart';

import 'keycloak_config.dart';

class KeycloakMobileService {
  static const FlutterAppAuth _appAuth = FlutterAppAuth();

  static final AuthorizationServiceConfiguration _serviceConfiguration =
      AuthorizationServiceConfiguration(
        authorizationEndpoint:
            '${KeycloakConfig.issuer}/protocol/openid-connect/auth',

        tokenEndpoint: '${KeycloakConfig.issuer}/protocol/openid-connect/token',

        endSessionEndpoint:
            '${KeycloakConfig.issuer}/protocol/openid-connect/logout',
      );

  static bool get _allowInsecureConnections {
    final uri = Uri.parse(KeycloakConfig.issuer);

    if (uri.scheme.toLowerCase() != 'http') {
      return false;
    }

    final host = uri.host.toLowerCase();

    return host == 'localhost' || host == '127.0.0.1' || host == '::1';
  }

  Future<TokenResponse?> login() async {
    return _appAuth.authorizeAndExchangeCode(
      AuthorizationTokenRequest(
        KeycloakConfig.clientId,
        KeycloakConfig.redirectUrl,

        serviceConfiguration: _serviceConfiguration,

        scopes: KeycloakConfig.scopes,

        promptValues: ['login'],

        allowInsecureConnections: _allowInsecureConnections,
      ),
    );
  }

  Future<TokenResponse?> refreshToken(String refreshToken) async {
    return _appAuth.token(
      TokenRequest(
        KeycloakConfig.clientId,
        KeycloakConfig.redirectUrl,

        serviceConfiguration: _serviceConfiguration,

        refreshToken: refreshToken,

        scopes: KeycloakConfig.scopes,

        allowInsecureConnections: _allowInsecureConnections,
      ),
    );
  }
}
