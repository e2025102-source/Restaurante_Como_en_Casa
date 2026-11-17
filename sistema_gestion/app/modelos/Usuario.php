<?php

declare(strict_types=1);

require_once __DIR__ . '/../Database.php';
require_once __DIR__ . '/../Validacion.php';

final class Usuario
{
    public const ROLES = ['administrador', 'cajero', 'cocina', 'inventario'];

    public static function listar(): array
    {
        return Database::conexion()
            ->query('SELECT id, nombres, usuario, rol, activo, creado_en FROM usuarios ORDER BY id ASC')
            ->fetchAll();
    }

    public static function obtener(int $id): ?array
    {
        $stmt = Database::conexion()->prepare(
            'SELECT id, nombres, usuario, rol, activo, creado_en FROM usuarios WHERE id = :id'
        );
        $stmt->execute([':id' => $id]);
        $usuario = $stmt->fetch();

        return $usuario ?: null;
    }

    public static function porNombreUsuario(string $usuario): ?array
    {
        $stmt = Database::conexion()->prepare('SELECT * FROM usuarios WHERE usuario = :usuario');
        $stmt->execute([':usuario' => $usuario]);
        $fila = $stmt->fetch();

        return $fila ?: null;
    }

    public static function guardar(array $datos): array
    {
        $id      = isset($datos['id']) && $datos['id'] !== '' ? (int) $datos['id'] : null;
        $nombres = Validacion::texto($datos['nombres'] ?? '', 'nombres', 120);
        $usuario = strtolower(Validacion::texto($datos['usuario'] ?? '', 'usuario', 50));
        $rol     = Validacion::enLista($datos['rol'] ?? 'cajero', self::ROLES, 'rol');
        $clave   = trim((string) ($datos['clave'] ?? ''));
        $activo  = !empty($datos['activo']) ? 1 : 0;

        if (!preg_match('/^[a-z0-9._-]{3,50}$/', $usuario)) {
            throw new InvalidArgumentException(
                'El usuario debe tener entre 3 y 50 caracteres y solo letras, numeros, punto, guion o guion bajo.'
            );
        }

        $pdo = Database::conexion();

        $stmt = $pdo->prepare('SELECT id FROM usuarios WHERE usuario = :usuario AND id <> :id');
        $stmt->execute([':usuario' => $usuario, ':id' => $id ?? 0]);
        if ($stmt->fetchColumn() !== false) {
            throw new InvalidArgumentException('Ya existe otro usuario con ese nombre de acceso.');
        }

        if ($id === null) {
            if (mb_strlen($clave) < 6) {
                throw new InvalidArgumentException('La contrasena debe tener al menos 6 caracteres.');
            }
            $hash = password_hash($clave, PASSWORD_BCRYPT);
            $stmt = $pdo->prepare(
                'INSERT INTO usuarios (nombres, usuario, clave_hash, rol, activo)
                 VALUES (:nombres, :usuario, :hash, :rol, :activo)'
            );
            $stmt->execute([
                ':nombres' => $nombres,
                ':usuario' => $usuario,
                ':hash'    => $hash,
                ':rol'     => $rol,
                ':activo'  => $activo,
            ]);
            $id = (int) $pdo->lastInsertId();
        } else {
            if (self::obtener($id) === null) {
                throw new InvalidArgumentException('El usuario indicado no existe.');
            }

            if ($clave !== '') {
                if (mb_strlen($clave) < 6) {
                    throw new InvalidArgumentException('La contrasena debe tener al menos 6 caracteres.');
                }
                $hash = password_hash($clave, PASSWORD_BCRYPT);
                $stmt = $pdo->prepare(
                    'UPDATE usuarios
                        SET nombres = :nombres, usuario = :usuario, clave_hash = :hash,
                            rol = :rol, activo = :activo
                      WHERE id = :id'
                );
                $stmt->execute([
                    ':nombres' => $nombres,
                    ':usuario' => $usuario,
                    ':hash'    => $hash,
                    ':rol'     => $rol,
                    ':activo'  => $activo,
                    ':id'      => $id,
                ]);
            } else {
                $stmt = $pdo->prepare(
                    'UPDATE usuarios
                        SET nombres = :nombres, usuario = :usuario, rol = :rol, activo = :activo
                      WHERE id = :id'
                );
                $stmt->execute([
                    ':nombres' => $nombres,
                    ':usuario' => $usuario,
                    ':rol'     => $rol,
                    ':activo'  => $activo,
                    ':id'      => $id,
                ]);
            }
        }

        return self::obtener($id);
    }

    public static function cambiarEstado(int $id, bool $activo): array
    {
        $stmt = Database::conexion()->prepare('UPDATE usuarios SET activo = :activo WHERE id = :id');
        $stmt->execute([':activo' => $activo ? 1 : 0, ':id' => $id]);

        $usuario = self::obtener($id);
        if ($usuario === null) {
            throw new InvalidArgumentException('El usuario indicado no existe.');
        }

        return $usuario;
    }
}
