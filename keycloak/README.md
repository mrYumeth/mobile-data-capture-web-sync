# FieldSync Keycloak Setup

## Included files

- Dockerfile: builds a custom Keycloak image with the FieldSync login theme.
- realm-export/fieldsync-realm-template.json: exported realm configuration template.

## Local custom image build

```powershell
docker build -f keycloak/Dockerfile -t fieldsync-keycloak-custom .