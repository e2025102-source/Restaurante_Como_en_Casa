<?php

declare(strict_types=1);

require_once __DIR__ . '/../Database.php';
require_once __DIR__ . '/../Validacion.php';

final class Gasto
{
    public static function listar(array $filtros = []): array
    {
        $sql = 'SELECT g.*, u.nombres AS usuario
                  FROM gastos g
                  LEFT JOIN usuarios u ON u.id = g.usuario_id
                 WHERE 1 = 1';
        $params = [];

        if (!empty($filtros['desde'])) {
            $sql .= ' AND g.fecha >= :desde';
            $params[':desde'] = $filtros['desde'];
        }
        if (!empty($filtros['hasta'])) {
            $sql .= ' AND g.fecha <= :hasta';
            $params[':hasta'] = $filtros['hasta'];
        }
        if (!empty($filtros['categoria'])) {
            $sql .= ' AND g.categoria = :categoria';
            $params[':categoria'] = $filtros['categoria'];
        }
        if (!empty($filtros['buscar'])) {
            $sql .= ' AND g.concepto LIKE :q';
            $params[':q'] = '%' . $filtros['buscar'] . '%';
        }

        $sql .= ' ORDER BY g.fecha DESC, g.id DESC';
        $stmt = Database::conexion()->prepare($sql);
        $stmt->execute($params);

        return $stmt->fetchAll();
    }

    public static function obtener(int $id): ?array
    {
        $stmt = Database::conexion()->prepare('SELECT * FROM gastos WHERE id = :id');
        $stmt->execute([':id' => $id]);
        $gasto = $stmt->fetch();

        return $gasto ?: null;
    }

    public static function guardar(array $datos, int $usuarioId): array
    {
        $id          = isset($datos['id']) && $datos['id'] !== '' ? (int) $datos['id'] : null;
        $fecha       = Validacion::fecha($datos['fecha'] ?? date('Y-m-d'), 'fecha');
        $concepto    = Validacion::texto($datos['concepto'] ?? '', 'concepto', 150);
        $categoria   = Validacion::texto($datos['categoria'] ?? 'operativo', 'categoria', 60);
        $monto       = Validacion::decimal($datos['monto'] ?? 0, 'monto', 0.01);
        $comprobante = Validacion::textoOpcional($datos['comprobante'] ?? '', 60);

        $pdo = Database::conexion();

        if ($id === null) {
            $stmt = $pdo->prepare(
                'INSERT INTO gastos (fecha, concepto, categoria, monto, comprobante, usuario_id)
                 VALUES (:fecha, :concepto, :categoria, :monto, :comprobante, :usuario_id)'
            );
            $stmt->execute([
                ':fecha'       => $fecha,
                ':concepto'    => $concepto,
                ':categoria'   => $categoria,
                ':monto'       => $monto,
                ':comprobante' => $comprobante,
                ':usuario_id'  => $usuarioId,
            ]);
            $id = (int) $pdo->lastInsertId();
        } else {
            if (self::obtener($id) === null) {
                throw new InvalidArgumentException('El gasto indicado no existe.');
            }
            $stmt = $pdo->prepare(
                'UPDATE gastos
                    SET fecha = :fecha, concepto = :concepto, categoria = :categoria,
                        monto = :monto, comprobante = :comprobante
                  WHERE id = :id'
            );
            $stmt->execute([
                ':fecha'       => $fecha,
                ':concepto'    => $concepto,
                ':categoria'   => $categoria,
                ':monto'       => $monto,
                ':comprobante' => $comprobante,
                ':id'          => $id,
            ]);
        }

        return self::obtener($id);
    }

    public static function eliminar(int $id): void
    {
        $stmt = Database::conexion()->prepare('DELETE FROM gastos WHERE id = :id');
        $stmt->execute([':id' => $id]);
    }
}
