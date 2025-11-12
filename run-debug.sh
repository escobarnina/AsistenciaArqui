#!/bin/bash

# Script para compilar, instalar y ejecutar la app en modo debug
# Uso: ./run-debug.sh

set -e

# Obtener ruta del SDK desde local.properties
SDK_DIR=$(grep "sdk.dir" local.properties | cut -d'=' -f2)
ADB="$SDK_DIR/platform-tools/adb"

# Verificar que adb existe
if [ ! -f "$ADB" ]; then
    echo "❌ Error: No se encontró adb en $ADB"
    echo "   Verifica que local.properties tenga la ruta correcta del SDK"
    exit 1
fi

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

