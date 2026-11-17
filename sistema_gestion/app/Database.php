<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/config.php';

/**
 * Conexion unica (patron Singleton) a la base de datos MySQL.
 */
final class Database
{
    private static ?PDO $conexion = null;

    private function __construct()
    {
    }

    public static function conexion(): PDO
    {
        if (self::$conexion === null) {
            $dsn = sprintf(
                'mysql:host=%s;port=%d;dbname=%s;charset=%s',
                DB_HOST,
                DB_PORT,
                DB_NAME,
                DB_CHARSET
            );

            try {
                self::$conexion = new PDO($dsn, DB_USER, DB_PASS, [
                    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    PDO::ATTR_EMULATE_PREPARES   => false,
                    PDO::ATTR_STRINGIFY_FETCHES  => false,
                ]);
                self::$conexion->exec("SET time_zone = '-05:00'");
            } catch (PDOException $e) {
                throw new RuntimeException(
                    'No se pudo conectar a MySQL. Verifique que el servicio este activo y la configuracion en config/config.php. Detalle: '
                    . $e->getMessage()
                );
            }
        }

        return self::$conexion;
    }

    public static function reiniciar(): void
    {
        self::$conexion = null;
    }
}
