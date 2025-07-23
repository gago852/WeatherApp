# Descripción de la feature búsqueda de ciudades.

## Componente de Búsqueda Overlay
Se implementará un componente de búsqueda tipo overlay que se mostrará por encima de la pantalla principal con las siguientes características:

### SearchOverlay
- Componente que se muestra como overlay sobre la pantalla principal
- Fondo semi-transparente para el área fuera del componente de búsqueda
- Animación de entrada/salida suave
- TestTag: "search_overlay" para testing


### SearchTextField
- Componente tipo `OutlinedTextField` con las siguientes propiedades:
  - Label: "Buscar ciudad"
  - Icono de búsqueda al inicio (leadingIcon)
  - Botón de limpiar (trailingIcon) que aparece cuando hay texto
  - Modo singleLine activado
  - TestTag: "search_text_field" para testing
  - Posicionado en la parte superior del overlay

### Funcionalidad de Debounce
- Implementar un debounce de 200 milisegundos y este se guardara en una constante.
- El texto de búsqueda se almacenará en dos estados:
  1. `searchText`: Estado inmediato del texto
  2. `debouncedText`: Estado con el texto debounceado

### Integración con Google Places API
- Implementar autocomplete usando Google Places API
- Cuando el texto debounceado cambie:
  1. Llamar a Places Autocomplete API
  2. Mostrar resultados en una lista debajo del SearchTextField
  3. Cada resultado debe mostrar:
     - Nombre de la ciudad
     - País
     - TestTag: "search_result_item" para testing

### Lista de Resultados
- Componente `SearchResultsList` con las siguientes características:
  - Se muestra debajo del SearchTextField
  - Máximo 5 resultados visibles
  - Scroll vertical si hay más resultados
  - Animación de entrada/salida
  - TestTag: "search_results_list" para testing
  - Cada item es clickeable

### Flujo de Búsqueda
1. Usuario escribe en el SearchTextField
2. Después del debounce, se llama a Places Autocomplete API
3. Se muestran los resultados en la lista
4. Al hacer clic en un resultado:
   - Llamar a Places Details API para obtener coordenadas
   - Con las coordenadas, llamar a OpenWeather API
   - Cerrar el overlay
   - Actualizar la UI con el nuevo pronóstico

### Estados del Overlay
- `isVisible`: Controla la visibilidad del overlay
- `isLoading`: Indica cuando se están cargando resultados
- `error`: Manejo de errores de las APIs
- `searchResults`: Lista de resultados de autocomplete
- `selectedPlace`: Lugar seleccionado con sus detalles

### Atribución de Google Maps
- Implementar un componente `GoogleMapsAttribution` que muestre:
  - Logo de Google
  - El logo está ubicado en la carpeta res/drawable-(tamaño de pantalla)/google_on_non_white.png
  - Texto de atribución: "Powered by Google"
  - Enlace a los términos de servicio de Google Maps
  - Ubicación: Esquina inferior derecha del overlay
  - Estilo: Fondo semi-transparente con texto en color apropiado
  - TestTag: "google_maps_attribution" para testing
  - Clickable para abrir los términos de servicio

### Testing
- Implementar tests usando ComposeTestRule
- Verificar:
  - Renderizado correcto del overlay
  - Funcionamiento del debounce
  - Integración con Places API
  - Integración con OpenWeather API
  - Comportamiento del botón de limpiar
  - Interacción con el teclado
  - Accesibilidad
  - Visibilidad y funcionalidad de la atribución
  - Animaciones y transiciones
  - Manejo de errores
  - Estados de carga

### Accesibilidad
- ContentDescription para el icono de búsqueda
- ContentDescription para el botón de limpiar
- Label semántico para el campo de búsqueda
- ContentDescription para el logo y enlace de atribución
- Soporte para TalkBack en la lista de resultados
- Navegación por teclado

### Estilo
- Usar Material3 para todos los componentes
- Seguir el tema de la aplicación
- Mantener consistencia con otros componentes de la UI
- Asegurar que la atribución sea visible pero no intrusiva
- Implementar dark/light theme support

### Requisitos Legales
- Incluir la atribución de Google Maps según los términos de servicio
- Mantener la atribución visible en todo momento
- Asegurar que el enlace a los términos de servicio sea accesible
- Cumplir con las políticas de marca de Google Maps
- Cumplir con los términos de uso de OpenWeather API

### Manejo de Errores
- Mostrar mensajes de error apropiados para:
  - Fallos en la conexión
  - Errores de la API de Places
  - Errores de la API de OpenWeather
  - Tiempo de espera agotado
  - Límites de uso excedidos
