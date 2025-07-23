# TODO - Feature: Búsqueda de ciudades

## Pendientes para completar la implementación

- [x] Usar el logo google_on_non_white en la atribución
- [x] Integrar debounce de 200ms en el SearchTextField (mock)
- [x] Crear SearchCityViewModel con estado único y lógica de debounce
- [x] Integrar SearchCityOverlay en la UI principal
- [x] Conectar botones de búsqueda (drawer y welcome) al overlay
- [x] Refactor para no pasar ViewModel por parámetros, solo funciones y estado
- [x] Refactor de previews para no usar ViewModel
- [ ] Integrar Google Places Autocomplete API (llamadas reales)
- [x] Mostrar resultados reales de la API en la lista (mock)
- [ ] Preparar modelo CityResult en la capa domain y agregarle coordenadas geográficas
- [ ] Al hacer clic en un resultado, obtener detalles con Places Details API
- [ ] Obtener coordenadas y llamar a OpenWeather API
- [ ] Cerrar overlay y actualizar UI con el nuevo pronóstico
- [ ] Manejo de errores de las APIs (Places, OpenWeather, red, límites, etc)
- [ ] Animaciones de entrada/salida en lista y overlay
- [ ] Testing con ComposeTestRule (UI, debounce, integración, accesibilidad, etc)
- [ ] Accesibilidad completa (TalkBack, content descriptions, navegación teclado)
- [x] Hacer el overlay invocable desde la UI principal
- [ ] Revisar cumplimiento de requisitos legales y de atribución 