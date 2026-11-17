<?php

declare(strict_types=1);

require_once __DIR__ . '/../Database.php';
require_once __DIR__ . '/../Validacion.php';

final class Cliente
{
    public static function listar(string $buscar = ''): array
    {
        $pdo = Database::conexion();
        $sql = 'SELECT c.*,
                       (SELECT COUNT(*) FROM ventas v WHERE v.cliente_id = c.id) AS total_ventas,
                       (SELECT COALESCE(SUM(v.total), 0) FROM ventas v
                         WHERE v.cliente_id = c.id AND v.estado <> "anulada") AS consumo_total
                  FROM clientes c';
        $params = [];

        if ($buscar !== '') {
            $sql .= ' WHERE c.nombre LIKE :q OR c.documento LIKE :q OR c.telefono LIKE :q';
            $params[':q'] = '%' . $buscar . '%';
        }

        $sql .= ' ORDER BY c.nombre ASC';
        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);

        return $stmt->fetchAll();
    }

    public static function obtener(int $id): ?array
    {
        $stmt = Database::conexion()->prepare('SELECT * FROM clientes WHERE id = :id');
        $stmt->execute([':id' => $id]);
        $cliente = $stmt->fetch();

        return $cliente ?: null;
    }

    public static function guardar(array $datos): array
    {
        $id       = isset($datos['id']) && $datos['id'] !== '' ? (int) $datos['id'] : null;
        $nombre   = Validacion::texto($datos['nombre'] ?? '', 'nombre del cliente', 120);
        $documento = Validacion::textoOpcional($datos['documento'] ?? '', 20);
        $telefono = Validacion::textoOpcional($datos['telefono'] ?? '', 20);
        $direccion = Validacion::textoOpcional($datos['direccion'] ?? '', 150);

        $pdo = Database::conexion();

        if ($id === null) {
            $stmt = $pdo->prepare(
                'INSERT INTO clientes (nombre, documento, telefono, direccion)
                 VALUES (:nombre, :documento, :telefono, :direccion)'
            );
            $stmt->execute(compact('nombre', 'documento', 'telefono', 'direccion'));
            $id = (int) $pdo->lastInsertId();
        } else {
            if (self::obtener($id) === null) {
                throw new InvalidArgumentException('El cliente indicado no existe.');
            }
            $stmt = $pdo->prepare(
                'UPDATE clientes
                    SET nombre = :nombre, documento = :documento,
                        telefono = :telefono, direccion = :direccion
                  WHERE id = :id'
            );
            $stmt->execute(compact('nombre', 'documento', 'telefono', 'direccion', 'id'));
        }

        return self::obtener($id);
    }

    public static function eliminar(int $id): void
    {
        $stmt = Database::conexion()->prepare('DELETE FROM clientes WHERE id = :id');
        $stmt->execute([':id' => $id]);
    }
}
