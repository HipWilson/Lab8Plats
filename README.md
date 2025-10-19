# Lab: Integración de Room en la Galería

## Descripción General
Aplicación de galería de fotos con persistencia local usando Room, Paging 3, y API de Pexels. Incluye caché por consulta, favoritos, búsquedas recientes y soporte offline básico.

## Arquitectura

### Estructura de Capas

**Data Layer**
- `api/`: Retrofit interface y modelos de API (PexelsApi, PexelsResponse)
- `db/`: Room entities (PhotoEntity, QueryEntity, RemoteKeyEntity)
- `db/`: DAOs (PhotoDao, QueryDao, RemoteKeyDao)
- `db/`: AppDatabase singleton
- `repository/`: PhotoRepository + PhotoPagingSource

**UI Layer**
- `screens/home/`: HomeScreen + HomeViewModel
- `screens/details/`: DetailsScreen + DetailsViewModel
- `screens/profile/`: ProfileScreen
- `theme/`: Material 3 theme configuration

### Modelo de Datos

**PhotoEntity**
- Tabla: `photos`
- Índices: `(queryKey, pageIndex)`, `(isFavorite)`
- Campos: id, dimensiones, URLs, autor, isFavorite, queryKey, pageIndex, updatedAt

**QueryEntity**
- Tabla: `recent_queries`
- Campos: query (PK normalizada), lastUsedAt, useCount

**RemoteKeyEntity**
- Tabla: `remote_keys`
- Campos: photoId (PK), prevKey, nextKey, queryKey

## Características Implementadas

### 1. Cache por Consulta
- Las fotos se persisten en Room asociadas a `(queryKey normalizada, page)`
- Al buscar, primero carga de la red y persiste localmente
- En caso de error de red, intenta cargar del caché

### 2. Offline Básico
- Al perder conexión, `PhotoPagingSource` detecta la excepción `IOException`
- Intenta cargar del caché para esa página/query
- Si hay datos en caché, los muestra sin parpadeos

### 3. Favoritos
- Toggle en cada tarjeta y en details
- Se persiste en `isFavorite` en Room
- Pantalla de favoritos disponible

### 4. Búsquedas Recientes
- Cada búsqueda se guarda en `recent_queries` con timestamp y contador
- Al usar una reciente, actualiza `lastUsedAt` y `useCount`
- Se muestra como chips debajo de la barra de búsqueda
- Máximo 10 queries guardadas

### 5. Debounce de Búsqueda
- 500 ms de espera antes de ejecutar búsqueda
- Evita múltiples requests por cada carácter

### 6. Paginación Infinita
- Paging 3 con `PhotoPagingSource` personalizado
- LoadState para mostrar indicadores de carga
- Scroll fluido en grilla de 2 columnas

### 7. Tema Claro/Oscuro
- Toggle en ProfileScreen
- Se aplica globalmente vía `MyApplicationTheme`
- Material 3 dynamic colors en Android 12+

## Decisiones de Diseño

### ¿Por qué PhotoPagingSource manual vs RemoteMediator?
- **Elegida**: PhotoPagingSource manual (Opción B simplificada)
- **Razón**: Más directo para este lab. RemoteMediator añadiría complejidad sin beneficio palpable en este escenario
- **Trade-off**: Menos control sobre el ciclo de vida de la caché, pero más simple

### ¿Por qué sin ViewModel explícito para state management?
- Usamos `MutableStateFlow` en `HomeViewModel` para `_searchQuery`
- `DetailsViewModel` tiene funciones suspensas para no bloquear UI
- Los composables usan `rememberCoroutineScope` para lanzar corutinas sin ViewModel
- **Trade-off**: En apps grandes, un Repository + ViewModel + StateManagement separado sería mejor

### ¿Cache-first o Network-then-persist?
- **Implementado**: Network-then-persist con fallback offline
- Intenta siempre red primero
- Si falla, sirve caché
- Si no hay caché y no hay red: error graceful

### Índices en Room
```sql
CREATE INDEX idx_photos_query_page ON photos(queryKey, pageIndex);
CREATE INDEX idx_photos_favorite ON photos(isFavorite);
```
- Optimiza queries por query+page y por favoritos
- No hay PK compuesta para permitir inserciones sin conflicto

## Flujo de Usuarios

### Búsqueda y Cache
1. Usuario digita query → debounce 500ms
2. Se normaliza (lowercase, trim) → se guarda en `recent_queries`
3. `PhotoPagingSource` solicita a Pexels API
4. Respuesta se inserta en `photos` con `(queryKey, pageIndex)`
5. Al volver a misma query, Lee primero de Room sin flickering

### Toggle Favorito
1. Usuario toca ❤️ en tarjeta
2. `toggleFavorite` ejecuta `photoDao.updatePhoto` con `isFavorite = !isFavorite`
3. UI se recompone (el Photo entity cambió)
4. Persiste automáticamente

### Offline
1. App tiene datos en caché de búsquedas previas
2. Usuario mata app y quita internet
3. Relanza app
4. HomeScreen intenta cargar "nature" (default)
5. `PhotoPagingSource.load()` obtiene `IOException`
6. Catch: intenta `photoDao.getPhotosByQueryAndPage(queryKey, 1)`
7. Si hay datos, los sirve; si no, error graceful

## Criterios de Aceptación

 **Cache funcional**: Al navegar y volver, elementos vienen de Room sin parpadeo
 **Offline básico**: Matar app sin red → relanzar → ver datos de última query
 **Favoritos persistentes**: Toggle persiste entre relanzamientos
 **Búsquedas recientes**: Ordenadas por uso; toque reejecuta search
 **Rendimiento**: Scroll fluido; cargas incrementales no bloquean UI
 **Tema**: Toggle claro/oscuro tiene efecto global

## Configuración Requerida

### API Key de Pexels
1. Ir a https://www.pexels.com/api
2. Generar API key
3. Reemplazar `PEXELS_API_KEY_HERE` en `PexelsApi.kt`:
   ```kotlin
   @Headers("Authorization: YOUR_KEY_HERE")
   ```

### Dependencias Agregadas
- Room 2.6.1
- Paging 3 Compose 3.3.0
- Retrofit 2.11.0 + Gson
- Coil 3 para imágenes
- Navigation Compose 2.8.7

## Testing

### Tests de DAO (en `androidTest/`)
```kotlin
// Ejemplo: testInsertAndRetrievePhotos
val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
val photos = listOf(PhotoEntity(...))
db.photoDao().insertPhotos(photos)
val retrieved = db.photoDao().getPhotosByQueryAndPage("test", 1)
assert(retrieved.size == 1)
```

### Escenarios Offline
1. Mock `IOException` en PhotoPagingSource
2. Verificar que carga del caché
3. Verificar que no crashea si caché vacío

## Extensiones Posibles (Extra Credit)

### 1. Migraciones
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE photos ADD COLUMN likes INTEGER DEFAULT 0")
    }
}
AppDatabase { addMigrations(MIGRATION_1_2) }
```

### 2. Filtros Locales
```kotlin
@Query("SELECT * FROM photos WHERE isFavorite = 1 AND photographer LIKE :author")
suspend fun getFavoritesByAuthor(author: String): List<PhotoEntity>
```

### 3. Caducidad de Caché
```kotlin
suspend fun deleteOldPhotos(maxAgeHours: Int) {
    val cutoff = System.currentTimeMillis() - (maxAgeHours * 3600 * 1000)
    photoDao.deleteOldPhotos(cutoff)
}
// Llamar periódicamente con WorkManager
```

## Preguntas de Reflexión Respondidas

### Consistencia: ¿Cómo invalidarías caché?
1. **Por timestamp**: Adicionar `updatedAt` y limpiar fotos > N horas antiguas
2. **Por parámetros**: Detectar cambios en query/filtros y deletear related photos
3. **Manual**: Botón "Refrescar" que borra caché de query actual

Implementado: Timestamp en PhotoEntity + método `deleteOldPhotos()`

### Offline-first: ¿Qué conflictos surgen?
1. **Caché parcial**: ¿Mostrar página 1 si no hay página 2?
   - Solución: Mostrar lo disponible, indicar que necesita red para más
2. **Sincronización**: ¿Si usuario edita favorito offline?
   - Solución: Guardar local, sincronizar al reconectar (no implementado en este lab)

Implementado: Caché parcial con fallback graceful

### Escalabilidad: ¿Cuándo introducir ViewModel formal?
- En este lab: Simple state management con Flows
- **Proyecto grande**: 
  - Agregar MVVM con StateManagement separado
  - Repository pattern formal con Clean Architecture
  - RemoteMediator para control más granular
  - Dependency injection (Hilt/Dagger)

## Notas de Implementación

1. **Coil vs Glide**: Coil es más moderna, async por defecto, integración perfecta con Compose
2. **Paging 3 vs manual**: Para este caso, ambas funcionan; Paging 3 escala mejor
3. **Room vs SharedPreferences**: Room para datos complejos; Prefs para booleans/strings
4. **Retrofit vs HttpUrlConnection**: Retrofit maneja retries, interceptors, deserialization automática

