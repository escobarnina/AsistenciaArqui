#!/bin/bash

# Script para compilar, instalar y ejecutar la app en modo debug
# Uso: ./run-debug.sh

set -e

# Función para encontrar adb
find_adb() {
    # 1. Intentar desde local.properties
    if [ -f "local.properties" ]; then
        SDK_DIR=$(grep "sdk.dir" local.properties 2>/dev/null | cut -d'=' -f2 | tr -d ' ')
        if [ -n "$SDK_DIR" ] && [ -f "$SDK_DIR/platform-tools/adb" ]; then
            echo "$SDK_DIR/platform-tools/adb"
            return 0
        fi
    fi
    
    # 2. Intentar desde variable de entorno ANDROID_HOME
    if [ -n "$ANDROID_HOME" ] && [ -f "$ANDROID_HOME/platform-tools/adb" ]; then
        echo "$ANDROID_HOME/platform-tools/adb"
        return 0
    fi
    
    # 3. Buscar en ubicaciones comunes de macOS
    COMMON_PATHS=(
        "$HOME/Library/Android/sdk/platform-tools/adb"
        "$HOME/Android/Sdk/platform-tools/adb"
    )
    
    for path in "${COMMON_PATHS[@]}"; do
        if [ -f "$path" ]; then
            echo "$path"
            return 0
        fi
    done
    
    # 4. Buscar con find (más lento pero más completo)
    FOUND=$(find "$HOME/Library/Android" "$HOME/Android" -name "adb" -type f 2>/dev/null | head -1)
    if [ -n "$FOUND" ]; then
        echo "$FOUND"
        return 0
    fi
    
    return 1
}

# Encontrar adb
ADB=$(find_adb)

if [ -z "$ADB" ] || [ ! -f "$ADB" ]; then
    echo "❌ Error: No se encontró adb"
    echo ""
    echo "Opciones para solucionarlo:"
    echo "1. Crear/actualizar local.properties con: sdk.dir=/ruta/a/tu/sdk"
    echo "2. Configurar variable de entorno: export ANDROID_HOME=/ruta/a/tu/sdk"
    echo "3. Agregar adb a tu PATH: export PATH=\$PATH:/ruta/a/platform-tools"
    echo ""
    echo "Ubicaciones comunes en macOS:"
    echo "  ~/Library/Android/sdk/platform-tools/adb"
    echo "  ~/Android/Sdk/platform-tools/adb"
    exit 1
fi

echo "✅ ADB encontrado en: $ADB"

# Verificar dispositivo conectado
DEVICES=$($ADB devices | grep -v "List" | grep "device$" | wc -l | tr -d ' ')
if [ "$DEVICES" -eq 0 ]; then
    echo "❌ Error: No hay dispositivos conectados"
    echo "   Conecta un dispositivo o inicia un emulador"
    exit 1
fi

echo "🔨 Compilando aplicación..."
./gradlew assembleDebug

echo "📱 Instalando en dispositivo..."
./gradlew installDebug

echo "🚀 Iniciando aplicación..."
$ADB shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.bo.asistenciaapp/.MainActivity

echo "✅ Aplicación iniciada en modo debug!"
echo ""
echo "💡 Para ver los logs en tiempo real, ejecuta:"
echo "   $ADB logcat | grep -i asistenciaapp"

