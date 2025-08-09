# TODO - Feature: Búsqueda de ciudades

## Pendientes para completar la implementación

- [x] Usar el logo google_on_non_white en la atribución
- [x] Integrar debounce de 200ms en el SearchTextField (mock)
- [x] Crear SearchCityViewModel con estado único y lógica de debounce
- [x] Integrar SearchCityOverlay en la UI principal
- [x] Conectar botones de búsqueda (drawer y welcome) al overlay
- [x] Refactor para no pasar ViewModel por parámetros, solo funciones y estado
- [x] Refactor de previews para no usar ViewModel
- [x] Integrar Google Places Autocomplete API (estructura base: repositorio, DI, UI state con token, overlay y predicciones)
- [x] Preparar modelo GeoCoordinate en la capa domain (lat, lon, name)
- [x] Al hacer clic en un resultado, obtener detalles con Places Details API (coordenadas + formatted address)
- [ ] Obtener coordenadas y llamar a OpenWeather API
- [x] Cerrar overlay y actualizar UI con LaunchedEffect (hasta exponer GeoCoordinate vía StateFlow)
- [ ] Manejo de errores de las APIs (Places, OpenWeather, red, límites, etc)
- [ ] Animaciones de entrada/salida en lista y overlay
- [ ] Testing con ComposeTestRule (UI, debounce, integración, accesibilidad, etc)
- [ ] Accesibilidad completa (TalkBack, content descriptions, navegación teclado)
- [x] Hacer el overlay invocable desde la UI principal
- [ ] Revisar cumplimiento de requisitos legales y de atribución 