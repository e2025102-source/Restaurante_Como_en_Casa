<?php

declare(strict_types=1);

/**
 * Instalador de la base de datos "como_en_casa".
 *
 * Uso por consola:
 *   C:\xampp\php\php.exe database\instalar.php
 *
 * Uso desde el navegador:
 *   http://localhost/como_en_casa/database/instalar.php
 *
 * ADVERTENCIA: vuelve a crear todas las tablas y carga los datos demo.
 */

require_once __DIR__ . '/../config/config.php';

$porConsola = PHP_SAPI === 'cli';

function ejecutarArchivo(PDO $pdo, string $ruta): int
{
    if (!is_file($ruta)) {
        throw new RuntimeException('No se encontro el archivo: ' . $ruta);
    }

    $contenido = (string) file_get_contents($ruta);
    $sentencias = preg_split('/;\s*(?:\r?\n|$)/', $contenido);
    $total = 0;

    foreach ($sentencias as $sentencia) {
        $sentencia = trim($sentencia);
        if ($sentencia === '' || preg_match('/^--[^\n]*$/', $sentencia)) {
            continue;
        }
        $pdo->exec($sentencia);
        $total++;
    }

    return $total;
}

try {
    $dsn = sprintf('mysql:host=%s;port=%d;charset=%s', DB_HOST, DB_PORT, DB_CHARSET);
    $pdo = new PDO($dsn, DB_USER, DB_PASS, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    ]);

    $n1 = ejecutarArchivo($pdo, __DIR__ . '/schema.sql');
    $n2 = ejecutarArchivo($pdo, __DIR__ . '/seed.sql');

    $mensaje = "Base de datos '" . DB_NAME . "' instalada correctamente. "
        . "{$n1} sentencias de estructura y {$n2} de datos ejecutadas.";

    if ($porConsola) {
        echo $mensaje . PHP_EOL;
        echo "Usuarios demo: admin/admin123, cajero/cajero123, cocina/cocina123, inventario/inventario123" . PHP_EOL;
    } else {
        header('Content-Type: text/html; charset=utf-8');
        echo '<!DOCTYPE html><html lang="es"><head><meta charset="utf-8">'
            . '<title>Instalacion - Como en Casa</title>'
            . '<style>body{font-family:Segoe UI,Arial,sans-serif;background:#fdf7f0;color:#2c221e;'
            . 'display:flex;min-height:100vh;align-items:center;justify-content:center;margin:0}'
            . '.c{background:#fff;padding:40px;border-radius:16px;box-shadow:0 10px 40px rgba(0,0,0,.08);'
            . 'max-width:620px;border-top:6px solid #8b0000}h1{color:#8b0000;margin-top:0}'
            . 'code{background:#f3ede6;padding:2px 6px;border-radius:4px}</style></head><body>'
            . '<div class="c"><h1>Instalacion completada</h1>'
            . '<p>' . htmlspecialchars($mensaje, ENT_QUOTES, 'UTF-8') . '</p>'
            . '<p><strong>Usuarios demo:</strong></p><ul>'
            . '<li><code>admin</code> / <code>admin123</code> (administrador)</li>'
            . '<li><code>cajero</code> / <code>cajero123</code> (cajero)</li>'
            . '<li><code>cocina</code> / <code>cocina123</code> (cocina)</li>'
            . '<li><code>inventario</code> / <code>inventario123</code> (inventario)</li>'
            . '</ul><p><a href="../index.php" style="display:inline-block;background:#8b0000;color:#fff;'
            . 'padding:12px 22px;border-radius:8px;text-decoration:none;font-weight:600">Ir al sistema</a></p>'
            . '</div></body></html>';
    }
} catch (Throwable $e) {
    $error = 'Error al instalar la base de datos: ' . $e->getMessage();
    if ($porConsola) {
        fwrite(STDERR, $error . PHP_EOL);
        exit(1);
    }
    header('Content-Type: text/html; charset=utf-8', true, 500);
    echo '<pre style="padding:20px;color:#8b0000">' . htmlspecialchars($error, ENT_QUOTES, 'UTF-8') . '</pre>';
}
