<?php
/**
 * Configuracion general del sistema "Como en Casa".
 * Ajuste los datos de conexion si su instalacion de MySQL es diferente.
 */

declare(strict_types=1);

date_default_timezone_set('America/Lima');

define('APP_NOMBRE', 'Como en Casa');
define('APP_VERSION', '1.0.0');

define('DB_HOST', '127.0.0.1');
define('DB_PORT', 3306);
define('DB_NAME', 'como_en_casa');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_CHARSET', 'utf8mb4');

define('TOKEN_VIGENCIA_HORAS', 12);
define('BASE_URL', '/como_en_casa');
