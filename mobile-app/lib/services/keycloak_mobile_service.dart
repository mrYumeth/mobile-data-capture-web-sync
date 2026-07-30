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

  Future<TokenResponse?> login() async {
    return _appAuth.authorizeAndExchangeCode(
      AuthorizationTokenRequest(
        KeycloakConfig.clientId,
        KeycloakConfig.redirectUrl,
        serviceConfiguration: _serviceConfiguration,
        scopes: KeycloakConfig.scopes,
        promptValues: ['login'],
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
      ),
    );
  }
}
