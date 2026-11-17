<?php

declare(strict_types=1);

require_once __DIR__ . '/../Database.php';
require_once __DIR__ . '/../Validacion.php';

final class Insumo
{
    public static function listar(string $buscar = ''): array
    {
        $sql = 'SELECT i.*,
                       (i.stock_actual <= i.stock_minimo) AS bajo_stock
                  FROM insumos i';
        $params = [];

        if ($buscar !== '') {
            $sql .= ' WHERE i.nombre LIKE :q';
            $params[':q'] = '%' . $buscar . '%';
        }

        $sql .= ' ORDER BY i.nombre ASC';
        $stmt = Database::conexion()->prepare($sql);
        $stmt->execute($params);

        return $stmt->fetchAll();
    }

    public static function obtener(int $id): ?array
    {
        $stmt = Database::conexion()->prepare('SELECT * FROM insumos WHERE id = :id');
        $stmt->execute([':id' => $id]);
        $insumo = $stmt->fetch();

        return $insumo ?: null;
    }

    public static function guardar(array $datos): array
    {
        $id          = isset($datos['id']) && $datos['id'] !== '' ? (int) $datos['id'] : null;
        $nombre      = Validacion::texto($datos['nombre'] ?? '', 'nombre del insumo', 120);
        $unidad      = Validacion::texto($datos['unidad'] ?? 'unidad', 'unidad', 20);
        $costo       = Validacion::decimal($datos['costo'] ?? 0, 'costo', 0);
        $stockMinimo = Validacion::decimal($datos['stock_minimo'] ?? 0, 'stock minimo', 0);

        $pdo = Database::conexion();

        if ($id === null) {
            $stockActual = Validacion::decimal($datos['stock_actual'] ?? 0, 'stock inicial', 0);
            $stmt = $pdo->prepare(
                'INSERT INTO insumos (nombre, unidad, costo, stock_actual, stock_minimo)
                 VALUES (:nombre, :unidad, :costo, 0, :stock_minimo)'
            );
            $stmt->execute([
                ':nombre'       => $nombre,
                ':unidad'       => $unidad,
                ':costo'        => $costo,
                ':stock_minimo' => $stockMinimo,
            ]);
            $id = (int) $pdo->lastInsertId();

            if ($stockActual > 0) {
                self::registrarMovimiento([
                    'insumo_id'      => $id,
                    'tipo'           => 'entrada',
                    'cantidad'       => $stockActual,
                    'costo_unitario' => $costo,
                    'motivo'         => 'Stock inicial',
                ], null, true);
            }
        } else {
            if (self::obtener($id) === null) {
                throw new InvalidArgumentException('El insumo indicado no existe.');
            }
            $stmt = $pdo->prepare(
                'UPDATE insumos
                    SET nombre = :nombre, unidad = :unidad, costo = :costo,
                        stock_minimo = :stock_minimo
                  WHERE id = :id'
            );
            $stmt->execute([
                ':nombre'       => $nombre,
                ':unidad'       => $unidad,
                ':costo'        => $costo,
                ':stock_minimo' => $stockMinimo,
                ':id'           => $id,
            ]);
        }

        return self::obtener($id);
    }

    public static function eliminar(int $id): void
    {
        $pdo = Database::conexion();
        $stmt = $pdo->prepare('SELECT COUNT(*) FROM detalle_compra WHERE insumo_id = :id');
        $stmt->execute([':id' => $id]);
        if ((int) $stmt->fetchColumn() > 0) {
            throw new InvalidArgumentException(
                'No se puede eliminar el insumo porque tiene compras registradas.'
            );
        }

        $stmt = $pdo->prepare('DELETE FROM insumos WHERE id = :id');
        $stmt->execute([':id' => $id]);
    }

    /**
     * Registra entrada o salida de inventario. El stock lo actualiza un
     * trigger de MySQL (trg_movimiento_stock) para mantener consistencia.
     */
    public static function registrarMovimiento(array $datos, ?int $usuarioId, bool $interno = false): array
    {
        $insumoId = Validacion::entero($datos['insumo_id'] ?? 0, 'insumo', 1);
        $tipo     = Validacion::enLista($datos['tipo'] ?? '', ['entrada', 'salida'], 'tipo de movimiento');
        $cantidad = Validacion::decimal($datos['cantidad'] ?? 0, 'cantidad', 0.001);
        $costo    = Validacion::decimal($datos['costo_unitario'] ?? 0, 'costo unitario', 0);
        $motivo   = Validacion::textoOpcional($datos['motivo'] ?? '', 150);

        $pdo = Database::conexion();
        $insumo = self::obtener($insumoId);
        if ($insumo === null) {
            throw new InvalidArgumentException('El insumo indicado no existe.');
        }

        if ($tipo === 'salida' && (float) $insumo['stock_actual'] < $cantidad) {
            throw new InvalidArgumentException(
                'Stock insuficiente. Disponible: ' . $insumo['stock_actual'] . ' ' . $insumo['unidad'] . '.'
            );
        }

        $stmt = $pdo->prepare(
            'INSERT INTO movimientos_inventario
                (insumo_id, tipo, cantidad, costo_unitario, motivo, usuario_id)
             VALUES (:insumo_id, :tipo, :cantidad, :costo, :motivo, :usuario_id)'
        );
        $stmt->execute([
            ':insumo_id'  => $insumoId,
            ':tipo'       => $tipo,
            ':cantidad'   => $cantidad,
            ':costo'      => $costo,
            ':motivo'     => $motivo,
            ':usuario_id' => $usuarioId,
        ]);

        return [
            'id'         => (int) $pdo->lastInsertId(),
            'insumo'     => self::obtener($insumoId),
            'interno'    => $interno,
        ];
    }

    public static function movimientos(array $filtros = []): array
    {
        $sql = 'SELECT m.*, i.nombre AS insumo, i.unidad, u.nombres AS usuario
                  FROM movimientos_inventario m
                  JOIN insumos i ON i.id = m.insumo_id
                  LEFT JOIN usuarios u ON u.id = m.usuario_id
                 WHERE 1 = 1';
        $params = [];

        if (!empty($filtros['insumo_id'])) {
            $sql .= ' AND m.insumo_id = :insumo_id';
            $params[':insumo_id'] = (int) $filtros['insumo_id'];
        }
        if (!empty($filtros['desde'])) {
            $sql .= ' AND DATE(m.fecha) >= :desde';
            $params[':desde'] = $filtros['desde'];
        }
        if (!empty($filtros['hasta'])) {
            $sql .= ' AND DATE(m.fecha) <= :hasta';
            $params[':hasta'] = $filtros['hasta'];
        }

        $sql .= ' ORDER BY m.fecha DESC, m.id DESC LIMIT 300';
        $stmt = Database::conexion()->prepare($sql);
        $stmt->execute($params);

        return $stmt->fetchAll();
    }

    public static function stockBajo(): array
    {
        return Database::conexion()
            ->query('SELECT * FROM v_stock_bajo ORDER BY (stock_minimo - stock_actual) DESC')
            ->fetchAll();
    }

    public static function compras(array $filtros = []): array
    {
        $sql = 'SELECT c.*, u.nombres AS usuario,
                       (SELECT COUNT(*) FROM detalle_compra d WHERE d.compra_id = c.id) AS items
                  FROM compras c
                  LEFT JOIN usuarios u ON u.id = c.usuario_id
                 WHERE 1 = 1';
        $params = [];

        if (!empty($filtros['desde'])) {
            $sql .= ' AND c.fecha >= :desde';
            $params[':desde'] = $filtros['desde'];
        }
        if (!empty($filtros['hasta'])) {
            $sql .= ' AND c.fecha <= :hasta';
            $params[':hasta'] = $filtros['hasta'];
        }

        $sql .= ' ORDER BY c.fecha DESC, c.id DESC';
        $stmt = Database::conexion()->prepare($sql);
        $stmt->execute($params);

        return $stmt->fetchAll();
    }

    public static function obtenerCompra(int $id): ?array
    {
        $pdo = Database::conexion();
        $stmt = $pdo->prepare(
            'SELECT c.*, u.nombres AS usuario
               FROM compras c LEFT JOIN usuarios u ON u.id = c.usuario_id
              WHERE c.id = :id'
        );
        $stmt->execute([':id' => $id]);
        $compra = $stmt->fetch();
        if (!$compra) {
            return null;
        }

        $stmt = $pdo->prepare(
            'SELECT d.*, i.nombre AS insumo, i.unidad
               FROM detalle_compra d
               JOIN insumos i ON i.id = d.insumo_id
              WHERE d.compra_id = :id
              ORDER BY d.id ASC'
        );
        $stmt->execute([':id' => $id]);
        $compra['detalles'] = $stmt->fetchAll();

        return $compra;
    }

    public static function registrarCompra(array $datos, int $usuarioId): array
    {
        $fecha     = Validacion::fecha($datos['fecha'] ?? date('Y-m-d'), 'fecha');
        $proveedor = Validacion::texto($datos['proveedor'] ?? '', 'proveedor', 120);
        $documento = Validacion::textoOpcional($datos['documento'] ?? '', 50);
        $items     = $datos['items'] ?? [];

        if (!is_array($items) || count($items) === 0) {
            throw new InvalidArgumentException('La compra debe tener al menos un insumo.');
        }

        $pdo = Database::conexion();
        $pdo->beginTransaction();

        try {
            $stmt = $pdo->prepare(
                'INSERT INTO compras (fecha, proveedor, documento, total, usuario_id)
                 VALUES (:fecha, :proveedor, :documento, 0, :usuario_id)'
            );
            $stmt->execute([
                ':fecha'      => $fecha,
                ':proveedor'  => $proveedor,
                ':documento'  => $documento,
                ':usuario_id' => $usuarioId,
            ]);
            $compraId = (int) $pdo->lastInsertId();

            $total = 0.0;
            $insertDetalle = $pdo->prepare(
                'INSERT INTO detalle_compra (compra_id, insumo_id, cantidad, costo_unitario, subtotal)
                 VALUES (:compra_id, :insumo_id, :cantidad, :costo, :subtotal)'
            );
            $insertMov = $pdo->prepare(
                'INSERT INTO movimientos_inventario
                    (insumo_id, tipo, cantidad, costo_unitario, motivo, usuario_id, fecha)
                 VALUES (:insumo_id, "entrada", :cantidad, :costo, :motivo, :usuario_id, :fecha)'
            );
            $updateCosto = $pdo->prepare('UPDATE insumos SET costo = :costo WHERE id = :id');

            foreach ($items as $item) {
                $insumoId  = Validacion::entero($item['insumo_id'] ?? 0, 'insumo', 1);
                $cantidad  = Validacion::decimal($item['cantidad'] ?? 0, 'cantidad', 0.001);
                $costo     = Validacion::decimal($item['costo_unitario'] ?? 0, 'costo unitario', 0);
                $subtotal  = round($cantidad * $costo, 2);

                if (self::obtener($insumoId) === null) {
                    throw new InvalidArgumentException("El insumo {$insumoId} no existe.");
                }

                $insertDetalle->execute([
                    ':compra_id' => $compraId,
                    ':insumo_id' => $insumoId,
                    ':cantidad'  => $cantidad,
                    ':costo'     => $costo,
                    ':subtotal'  => $subtotal,
                ]);

                $insertMov->execute([
                    ':insumo_id'  => $insumoId,
                    ':cantidad'   => $cantidad,
                    ':costo'      => $costo,
                    ':motivo'     => 'Compra a ' . $proveedor . ($documento ? ' (' . $documento . ')' : ''),
                    ':usuario_id' => $usuarioId,
                    ':fecha'      => $fecha . ' ' . date('H:i:s'),
                ]);

                $updateCosto->execute([':costo' => $costo, ':id' => $insumoId]);
                $total += $subtotal;
            }

            $total = round($total, 2);
            $stmt = $pdo->prepare('UPDATE compras SET total = :total WHERE id = :id');
            $stmt->execute([':total' => $total, ':id' => $compraId]);

            $pdo->commit();

            return self::obtenerCompra($compraId) ?? [];
        } catch (Throwable $e) {
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            throw $e;
        }
    }
}
