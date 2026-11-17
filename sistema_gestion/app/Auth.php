<?php

declare(strict_types=1);

require_once __DIR__ . '/Database.php';
require_once __DIR__ . '/ApiException.php';

final class Auth
{
    public static function iniciarSesion(string $usuario, string $clave): array
    {
        $usuario = trim($usuario);
        if ($usuario === '' || $clave === '') {
            throw new ApiException('Ingrese su usuario y contrasena.', 422);
        }

        $pdo = Database::conexion();
        $stmt = $pdo->prepare('SELECT * FROM usuarios WHERE usuario = :usuario');
        $stmt->execute([':usuario' => $usuario]);
        $fila = $stmt->fetch();

        if (!$fila || !password_verify($clave, $fila['clave_hash'])) {
            throw new ApiException('Usuario o contrasena incorrectos.', 401);
        }
        if ((int) $fila['activo'] !== 1) {
            throw new ApiException('Su usuario se encuentra inactivo. Contacte al administrador.', 403);
        }

        $pdo->exec('DELETE FROM tokens WHERE expira < NOW()');

        $token  = bin2hex(random_bytes(32));
        $expira = (new DateTimeImmutable())->modify('+' . TOKEN_VIGENCIA_HORAS . ' hours');

        $stmt = $pdo->prepare(
            'INSERT INTO tokens (usuario_id, token, expira) VALUES (:usuario_id, :token, :expira)'
        );
        $stmt->execute([
            ':usuario_id' => $fila['id'],
            ':token'      => $token,
            ':expira'     => $expira->format('Y-m-d H:i:s'),
        ]);

        return [
            'token'   => $token,
            'expira'  => $expira->format('Y-m-d H:i:s'),
            'usuario' => self::formatearUsuario($fila),
        ];
    }

    public static function cerrarSesion(?string $token): void
    {
        if ($token === null || $token === '') {
            return;
        }
        $stmt = Database::conexion()->prepare('DELETE FROM tokens WHERE token = :token');
        $stmt->execute([':token' => $token]);
    }

    public static function tokenDeSolicitud(): ?string
    {
        $cabecera = $_SERVER['HTTP_AUTHORIZATION']
            ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION']
            ?? '';

        if ($cabecera === '' && function_exists('getallheaders')) {
            $headers = getallheaders();
            foreach ($headers as $nombre => $valor) {
                if (strcasecmp($nombre, 'Authorization') === 0) {
                    $cabecera = $valor;
                    break;
                }
            }
        }

        if (preg_match('/Bearer\s+([A-Za-z0-9]+)/', $cabecera, $coincidencias)) {
            return $coincidencias[1];
        }

        if (!empty($_GET['token'])) {
            return preg_replace('/[^A-Za-z0-9]/', '', (string) $_GET['token']);
        }

        return null;
    }

    public static function usuarioActual(): ?array
    {
        $token = self::tokenDeSolicitud();
        if ($token === null || $token === '') {
            return null;
        }

        $stmt = Database::conexion()->prepare(
            'SELECT u.*
               FROM tokens t
               JOIN usuarios u ON u.id = t.usuario_id
              WHERE t.token = :token
                AND t.expira > NOW()
                AND u.activo = 1'
        );
        $stmt->execute([':token' => $token]);
        $fila = $stmt->fetch();

        return $fila ? self::formatearUsuario($fila) + ['token' => $token] : null;
    }

    public static function requerir(): array
    {
        $usuario = self::usuarioActual();
        if ($usuario === null) {
            throw new ApiException('Sesion expirada o no iniciada. Vuelva a ingresar.', 401);
        }

        return $usuario;
    }

    public static function requerirRol(array $roles): array
    {
        $usuario = self::requerir();
        if (!in_array($usuario['rol'], $roles, true)) {
            throw new ApiException('No tiene permisos para realizar esta operacion.', 403);
        }

        return $usuario;
    }

    private static function formatearUsuario(array $fila): array
    {
        return [
            'id'      => (int) $fila['id'],
            'nombres' => $fila['nombres'],
            'usuario' => $fila['usuario'],
            'rol'     => $fila['rol'],
            'activo'  => (int) $fila['activo'],
        ];
    }
}
