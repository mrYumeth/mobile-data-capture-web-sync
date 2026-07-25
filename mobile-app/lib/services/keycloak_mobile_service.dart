import 'package:flutter_appauth/flutter_appauth.dart';

import 'keycloak_config.dart';

class KeycloakMobileService {
  static const FlutterAppAuth _appAuth = FlutterAppAuth();

  Future<TokenResponse?> login() async {
    return _appAuth.authorizeAndExchangeCode(
      AuthorizationTokenRequest(
        KeycloakConfig.clientId,
        KeycloakConfig.redirectUrl,
        discoveryUrl: KeycloakConfig.discoveryUrl,
        scopes: KeycloakConfig.scopes,
        promptValues: ['login'],

        // Development only: allows local HTTP Keycloak
        allowInsecureConnections: true,
      ),
    );
  }

  Future<TokenResponse?> refreshToken(String refreshToken) async {
    return _appAuth.token(
      TokenRequest(
        KeycloakConfig.clientId,
        KeycloakConfig.redirectUrl,
        discoveryUrl: KeycloakConfig.discoveryUrl,
        refreshToken: refreshToken,
        scopes: KeycloakConfig.scopes,

        // Development only: allows local HTTP Keycloak
        allowInsecureConnections: true,
      ),
    );
  }
}
