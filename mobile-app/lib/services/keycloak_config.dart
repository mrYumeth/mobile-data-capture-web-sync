class KeycloakConfig {
  static const String issuer = 'http://10.0.2.2:8080/realms/fieldsync';
  static const String clientId = 'fieldsync-mobile';
  static const String redirectUrl = 'com.example.mobileapp:/oauthredirect';
  static const String discoveryUrl = '$issuer/.well-known/openid-configuration';

  static const List<String> scopes = [
    'openid',
    'profile',
    'email',
    'offline_access',
  ];
}
