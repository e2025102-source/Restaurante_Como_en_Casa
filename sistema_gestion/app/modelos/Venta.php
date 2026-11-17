<?php

declare(strict_types=1);

require_once __DIR__ . '/../Database.php';
require_once __DIR__ . '/../Validacion.php';

final class Venta
{
    public const ESTADOS = [
        'registrada',
        'en_preparacion',
        'lista',
        'entregada',
        'pagada',
        'cerrada',
        'anulada',
    ];

    private const TRANSICIONES = [
        'registrada'     => ['en_preparacion', 'anulada'],
        'en_preparacion' => ['lista', 'anulada'],
        'lista'          => ['entregada', 'anulada'],
        'entregada'      => ['pagada', 'cerrada', 'anulada'],
        'pagada'         => ['cerrada'],
        'cerrada'        => [],
        'anulada'        => [],
    ];

    public static function listar(array $filtros = []): array
    {
        $sql = 'SELECT v.*, c.nombre AS cliente, u.nombres AS usuario,
                       (SELECT COALESCE(SUM(p.monto), 0) FROM pagos_cliente p
                         WHERE p.venta_id = v.id) AS pagado,
                       (SELECT COUNT(*) FROM detalle_venta d
                         WHERE d.venta_id = v.id) AS items
                  FROM ventas v
                  LEFT JOIN clientes c ON c.id = v.cliente_id
                  LEFT JOIN usuarios u ON u.id = v.usuario_id
                 WHERE 1 = 1';
        $params = [];

        if (!empty($filtros['estado']) && in_array($filtros['estado'], self::ESTADOS, true)) {
            $sql .= ' AND v.estado = :estado';
            $params[':estado'] = $filtros['estado'];
        }
        if (!empty($filtros['desde'])) {
            $sql .= ' AND DATE(v.fecha) >= :desde';
            $params[':desde'] = $filtros['desde'];
        }
        if (!empty($filtros['hasta'])) {
            $sql .= ' AND DATE(v.fecha) <= :hasta';
            $params[':hasta'] = $filtros['hasta'];
        }
        if (!empty($filtros['buscar'])) {
            $sql .= ' AND (v.numero LIKE :q OR c.nombre LIKE :q)';
            $params[':q'] = '%' . $filtros['buscar'] . '%';
        }

        $sql .= ' ORDER BY v.fecha DESC, v.id DESC LIMIT 200';
        $stmt = Database::conexion()->prepare($sql);
        $stmt->execute($params);

        return $stmt->fetchAll();
    }

    public static function obtener(int $id): ?array
    {
        $pdo = Database::conexion();
        $stmt = $pdo->prepare(
            'SELECT v.*, c.nombre AS cliente, c.telefono AS cliente_telefono,
                    u.nombres AS usuario,
                    (SELECT COALESCE(SUM(p.monto), 0) FROM pagos_cliente p
                      WHERE p.venta_id = v.id) AS pagado
               FROM ventas v
               LEFT JOIN clientes c ON c.id = v.cliente_id
               LEFT JOIN usuarios u ON u.id = v.usuario_id
              WHERE v.id = :id'
        );
        $stmt->execute([':id' => $id]);
        $venta = $stmt->fetch();
        if (!$venta) {
            return null;
        }

        $stmt = $pdo->prepare(
            'SELECT d.*, p.nombre AS producto, p.categoria
               FROM detalle_venta d
               JOIN productos p ON p.id = d.producto_id
              WHERE d.venta_id = :id
              ORDER BY d.id ASC'
        );
        $stmt->execute([':id' => $id]);
        $venta['detalles'] = $stmt->fetchAll();

        $stmt = $pdo->prepare(
            'SELECT p.*, u.nombres AS usuario
               FROM pagos_cliente p
               LEFT JOIN usuarios u ON u.id = p.usuario_id
              WHERE p.venta_id = :id
              ORDER BY p.fecha ASC, p.id ASC'
        );
        $stmt->execute([':id' => $id]);
        $venta['pagos'] = $stmt->fetchAll();

        $venta['saldo'] = round((float) $venta['total'] - (float) $venta['pagado'], 2);

        return $venta;
    }

    public static function crear(array $datos, int $usuarioId): array
    {
        $items = $datos['items'] ?? [];
        if (!is_array($items) || count($items) === 0) {
            throw new InvalidArgumentException('La venta debe contener al menos un producto o plato.');
        }

        $idempotencia = Validacion::textoOpcional($datos['idempotencia'] ?? '', 64);
        $pdo = Database::conexion();

        if ($idempotencia !== null) {
            $stmt = $pdo->prepare('SELECT id FROM ventas WHERE idempotencia = :idem');
            $stmt->execute([':idem' => $idempotencia]);
            $existente = $stmt->fetchColumn();
            if ($existente !== false) {
                return self::obtener((int) $existente) ?? [];
            }
        }

        $clienteId = null;
        if (!empty($datos['cliente_id'])) {
            $clienteId = Validacion::entero($datos['cliente_id'], 'cliente', 1);
            $stmt = $pdo->prepare('SELECT COUNT(*) FROM clientes WHERE id = :id');
            $stmt->execute([':id' => $clienteId]);
            if ((int) $stmt->fetchColumn() === 0) {
                throw new InvalidArgumentException('El cliente indicado no existe.');
            }
        }

        $tipoServicio = Validacion::enLista(
            $datos['tipo_servicio'] ?? 'mesa',
            ['mesa', 'llevar', 'delivery'],
            'tipo de servicio'
        );
        $mesa        = $tipoServicio === 'mesa' ? Validacion::textoOpcional($datos['mesa'] ?? '', 10) : null;
        $observacion = Validacion::textoOpcional($datos['observacion'] ?? '', 255);

        // Consolidar lineas repetidas del mismo producto.
        $lineas = [];
        foreach ($items as $item) {
            $productoId = Validacion::entero($item['producto_id'] ?? 0, 'producto', 1);
            $cantidad   = Validacion::entero($item['cantidad'] ?? 0, 'cantidad', 1);
            if ($cantidad > 999) {
                throw new InvalidArgumentException('La cantidad por producto no debe superar 999.');
            }
            if (!isset($lineas[$productoId])) {
                $lineas[$productoId] = 0;
            }
            $lineas[$productoId] += $cantidad;
        }

        $stmtProducto = $pdo->prepare('SELECT * FROM productos WHERE id = :id');
        $detalles = [];
        $subtotal = 0.0;

        foreach ($lineas as $productoId => $cantidad) {
            $stmtProducto->execute([':id' => $productoId]);
            $producto = $stmtProducto->fetch();
            if (!$producto) {
                throw new InvalidArgumentException("El producto {$productoId} no existe.");
            }
            if ($producto['estado'] !== 'disponible') {
                throw new InvalidArgumentException(
                    'El producto "' . $producto['nombre'] . '" no esta disponible para la venta.'
                );
            }

            $precio    = (float) $producto['precio'];
            $subtotalLinea = round($precio * $cantidad, 2);
            $detalles[] = [
                'producto_id' => (int) $productoId,
                'cantidad'    => $cantidad,
                'precio'      => $precio,
                'subtotal'    => $subtotalLinea,
            ];
            $subtotal += $subtotalLinea;
        }

        $subtotal  = round($subtotal, 2);
        $descuento = Validacion::decimal($datos['descuento'] ?? 0, 'descuento', 0);
        if ($descuento > $subtotal) {
            throw new InvalidArgumentException('El descuento no puede superar el subtotal.');
        }
        $total = round($subtotal - $descuento, 2);

        // Validar pagos opcionales antes de abrir la transaccion.
        $pagos = $datos['pagos'] ?? [];
        if (!is_array($pagos)) {
            $pagos = [];
        }
        $totalPagos = 0.0;
        $pagosValidados = [];
        foreach ($pagos as $pago) {
            $monto = Validacion::decimal($pago['monto'] ?? 0, 'monto del pago', 0.01);
            $medio = Validacion::enLista(
                $pago['medio'] ?? 'efectivo',
                ['efectivo', 'yape', 'tarjeta'],
                'medio de pago'
            );
            $referencia = Validacion::textoOpcional($pago['referencia'] ?? '', 60);
            $pagosValidados[] = compact('monto', 'medio', 'referencia');
            $totalPagos += $monto;
        }
        if (round($totalPagos, 2) > $total + 0.001) {
            throw new InvalidArgumentException('La suma de los pagos no puede superar el total de la venta.');
        }

        $pdo->beginTransaction();

        try {
            $stmt = $pdo->prepare(
                'INSERT INTO ventas
                    (numero, idempotencia, cliente_id, usuario_id, fecha,
                     subtotal, descuento, total, estado, tipo_servicio, mesa, observacion)
                 VALUES
                    (NULL, :idempotencia, :cliente_id, :usuario_id, NOW(),
                     :subtotal, :descuento, :total, "registrada", :tipo_servicio, :mesa, :observacion)'
            );
            $stmt->execute([
                ':idempotencia' => $idempotencia,
                ':cliente_id'   => $clienteId,
                ':usuario_id'   => $usuarioId,
                ':subtotal'     => $subtotal,
                ':descuento'    => $descuento,
                ':total'        => $total,
                ':tipo_servicio' => $tipoServicio,
                ':mesa'         => $mesa,
                ':observacion'  => $observacion,
            ]);
            $ventaId = (int) $pdo->lastInsertId();

            $numero = 'V-' . str_pad((string) $ventaId, 6, '0', STR_PAD_LEFT);
            $stmt = $pdo->prepare('UPDATE ventas SET numero = :numero WHERE id = :id');
            $stmt->execute([':numero' => $numero, ':id' => $ventaId]);

            $stmtDetalle = $pdo->prepare(
                'INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio, subtotal)
                 VALUES (:venta_id, :producto_id, :cantidad, :precio, :subtotal)'
            );
            foreach ($detalles as $detalle) {
                $stmtDetalle->execute([
                    ':venta_id'    => $ventaId,
                    ':producto_id' => $detalle['producto_id'],
                    ':cantidad'    => $detalle['cantidad'],
                    ':precio'      => $detalle['precio'],
                    ':subtotal'    => $detalle['subtotal'],
                ]);
            }

            $stmtPago = $pdo->prepare(
                'INSERT INTO pagos_cliente (venta_id, fecha, monto, medio, referencia, usuario_id)
                 VALUES (:venta_id, NOW(), :monto, :medio, :referencia, :usuario_id)'
            );
            foreach ($pagosValidados as $pago) {
                $stmtPago->execute([
                    ':venta_id'   => $ventaId,
                    ':monto'      => $pago['monto'],
                    ':medio'      => $pago['medio'],
                    ':referencia' => $pago['referencia'],
                    ':usuario_id' => $usuarioId,
                ]);
            }

            $pdo->commit();

            return self::obtener($ventaId) ?? [];
        } catch (Throwable $e) {
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            throw $e;
        }
    }

    public static function cambiarEstado(int $id, string $nuevoEstado): array
    {
        $nuevoEstado = Validacion::enLista($nuevoEstado, self::ESTADOS, 'estado');
        $pdo = Database::conexion();
        $venta = self::obtener($id);
        if ($venta === null) {
            throw new InvalidArgumentException('La venta indicada no existe.');
        }

        $actual = $venta['estado'];
        if ($actual === $nuevoEstado) {
            return $venta;
        }

        if (!in_array($nuevoEstado, self::TRANSICIONES[$actual] ?? [], true)) {
            throw new InvalidArgumentException(
                "No se puede pasar de '{$actual}' a '{$nuevoEstado}'."
            );
        }

        // Si la venta ya esta pagada por completo y pasa a entregada, se marca pagada.
        if ($nuevoEstado === 'entregada'
            && (float) $venta['pagado'] + 0.001 >= (float) $venta['total']) {
            $nuevoEstado = 'pagada';
        }

        $stmt = $pdo->prepare('UPDATE ventas SET estado = :estado WHERE id = :id');
        $stmt->execute([':estado' => $nuevoEstado, ':id' => $id]);

        return self::obtener($id) ?? [];
    }

    public static function registrarPago(int $ventaId, array $datos, int $usuarioId): array
    {
        $monto      = Validacion::decimal($datos['monto'] ?? 0, 'monto', 0.01);
        $medio      = Validacion::enLista(
            $datos['medio'] ?? 'efectivo',
            ['efectivo', 'yape', 'tarjeta'],
            'medio de pago'
        );
        $referencia = Validacion::textoOpcional($datos['referencia'] ?? '', 60);

        $pdo = Database::conexion();
        $venta = self::obtener($ventaId);
        if ($venta === null) {
            throw new InvalidArgumentException('La venta indicada no existe.');
        }
        if ($venta['estado'] === 'anulada') {
            throw new InvalidArgumentException('No se puede registrar un pago de una venta anulada.');
        }
        if ((float) $venta['saldo'] <= 0.001) {
            throw new InvalidArgumentException('La venta ya se encuentra pagada en su totalidad.');
        }
        if ($monto > (float) $venta['saldo'] + 0.001) {
            throw new InvalidArgumentException(
                'El monto supera el saldo pendiente (' . number_format((float) $venta['saldo'], 2) . ').'
            );
        }

        $pdo->beginTransaction();

        try {
            $stmt = $pdo->prepare(
                'INSERT INTO pagos_cliente (venta_id, fecha, monto, medio, referencia, usuario_id)
                 VALUES (:venta_id, NOW(), :monto, :medio, :referencia, :usuario_id)'
            );
            $stmt->execute([
                ':venta_id'   => $ventaId,
                ':monto'      => $monto,
                ':medio'      => $medio,
                ':referencia' => $referencia,
                ':usuario_id' => $usuarioId,
            ]);

            $pagado = (float) $venta['pagado'] + $monto;
            if ($pagado + 0.001 >= (float) $venta['total']
                && in_array($venta['estado'], ['entregada'], true)) {
                $stmt = $pdo->prepare('UPDATE ventas SET estado = "pagada" WHERE id = :id');
                $stmt->execute([':id' => $ventaId]);
            }

            $pdo->commit();

            return self::obtener($ventaId) ?? [];
        } catch (Throwable $e) {
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            throw $e;
        }
    }

    public static function anular(int $id): array
    {
        $venta = self::obtener($id);
        if ($venta === null) {
            throw new InvalidArgumentException('La venta indicada no existe.');
        }
        if ($venta['estado'] === 'cerrada') {
            throw new InvalidArgumentException('No se puede anular una venta ya cerrada.');
        }
        if ($venta['estado'] === 'anulada') {
            return $venta;
        }

        $stmt = Database::conexion()->prepare('UPDATE ventas SET estado = "anulada" WHERE id = :id');
        $stmt->execute([':id' => $id]);

        return self::obtener($id) ?? [];
    }
}
