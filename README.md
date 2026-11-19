# Filtro Browser (protótipo Android)

Navegador Android baseado em WebView com permissões de câmera/microfone e efeitos experimentais na transmissão de `getUserMedia`.

## Filtros incluídos
Óculos, sardas, barba, careca e cobertura das sobrancelhas. São efeitos 2D de protótipo: não usam rastreamento facial/segmentação por IA, então o alinhamento pode variar conforme rosto, distância e enquadramento. A aparência não é comparável aos filtros avançados do TikTok.

## Compilar APK
Requer Android Studio ou JDK 17+ e Android SDK Platform 35.

```bash
gradle assembleDebug
```
APK esperado: `app/build/outputs/apk/debug/app-debug.apk`.

Também há workflow em `.github/workflows/android.yml`: envie o projeto a um repositório GitHub e execute Actions para compilar.

## Limitações importantes
- Alguns sites bloqueiam câmera em WebView, exigem navegador certificado, DRM, WebRTC específico ou políticas de origem.
- O script substitui `getUserMedia` no início do documento, mas páginas que guardem referências antes da injeção ou usem mecanismos próprios podem não receber o efeito.
- Os efeitos atuais são desenhos 2D aproximados, sem tracking facial real. Careca/barba/remoção de sobrancelha são demonstrações visuais e não máscaras de alta qualidade.
- Teste em dispositivo real antes de qualquer uso em chamadas importantes.
