<?php

declare(strict_types=1);

require_once __DIR__ . '/../Database.php';

final class Reporte
{
    public static function dashboard(): array
    {
        $pdo = Database::conexion();

        $ventasHoy = $pdo->query(
            "SELECT COUNT(*) AS cantidad, COALESCE(SUM(total), 0) AS total
               FROM ventas
              WHERE DATE(fecha) = CURDATE() AND estado <> 'anulada'"
        )->fetch();

        $ingresosHoy = (float) $pdo->query(
            "SELECT COALESCE(SUM(p.monto), 0)
               FROM pagos_cliente p
               JOIN ventas v ON v.id = p.venta_id
              WHERE DATE(p.fecha) = CURDATE() AND v.estado <> 'anulada'"
        )->fetchColumn();

        $gastosHoy = (float) $pdo->query(
            'SELECT COALESCE(SUM(monto), 0) FROM gastos WHERE fecha = CURDATE()'
        )->fetchColumn();

        $pagosPersonalHoy = (float) $pdo->query(
            'SELECT COALESCE(SUM(monto), 0) FROM pagos_personal WHERE fecha = CURDATE()'
        )->fetchColumn();

        $comprasHoy = (float) $pdo->query(
            'SELECT COALESCE(SUM(total), 0) FROM compras WHERE fecha = CURDATE()'
        )->fetchColumn();

        $ventasPorEstado = $pdo->query(
            "SELECT estado, COUNT(*) AS cantidad
               FROM ventas
              WHERE DATE(fecha) = CURDATE() AND estado <> 'anulada'
              GROUP BY estado"
        )->fetchAll();

        $ingresosPorMedio = $pdo->query(
            "SELECT p.medio, COUNT(*) AS cantidad, SUM(p.monto) AS total
               FROM pagos_cliente p
               JOIN ventas v ON v.id = p.venta_id
              WHERE DATE(p.fecha) = CURDATE() AND v.estado <> 'anulada'
              GROUP BY p.medio
              ORDER BY total DESC"
        )->fetchAll();

        $topProductos = $pdo->query(
            "SELECT pr.nombre, pr.categoria,
                    SUM(d.cantidad) AS cantidad, SUM(d.subtotal) AS total
               FROM detalle_venta d
               JOIN ventas v ON v.id = d.venta_id
               JOIN productos pr ON pr.id = d.producto_id
              WHERE DATE(v.fecha) = CURDATE() AND v.estado <> 'anulada'
              GROUP BY pr.id
              ORDER BY cantidad DESC, total DESC
              LIMIT 5"
        )->fetchAll();

        $stockBajo = $pdo->query(
            'SELECT id, nombre, unidad, stock_actual, stock_minimo
               FROM v_stock_bajo
              ORDER BY (stock_minimo - stock_actual) DESC
              LIMIT 8'
        )->fetchAll();

        $ventasRecientes = $pdo->query(
            "SELECT v.id, v.numero, v.fecha, v.total, v.estado, v.tipo_servicio,
                    COALESCE(c.nombre, 'Consumidor final') AS cliente,
                    (SELECT COALESCE(SUM(p.monto), 0) FROM pagos_cliente p
                      WHERE p.venta_id = v.id) AS pagado
               FROM ventas v
               LEFT JOIN clientes c ON c.id = v.cliente_id
              ORDER BY v.fecha DESC, v.id DESC
              LIMIT 8"
        )->fetchAll();

        $serie = self::serieDiaria(
            (new DateTimeImmutable())->modify('-6 days')->format('Y-m-d'),
            (new DateTimeImmutable())->format('Y-m-d')
        );

        return [
            'fecha'              => date('Y-m-d'),
            'ventas_hoy'         => [
                'cantidad' => (int) $ventasHoy['cantidad'],
                'total'    => (float) $ventasHoy['total'],
            ],
            'ingresos_hoy'       => $ingresosHoy,
            'gastos_hoy'         => $gastosHoy,
            'pagos_personal_hoy' => $pagosPersonalHoy,
            'compras_hoy'        => $comprasHoy,
            'ganancia_hoy'       => round($ingresosHoy - $gastosHoy - $pagosPersonalHoy, 2),
            'ventas_por_estado'  => $ventasPorEstado,
            'ingresos_por_medio' => $ingresosPorMedio,
            'top_productos'      => $topProductos,
            'stock_bajo'         => $stockBajo,
            'ventas_recientes'   => $ventasRecientes,
            'serie'              => $serie,
        ];
    }

    public static function reporte(string $desde, string $hasta): array
    {
        $pdo = Database::conexion();
        $params = [':desde' => $desde, ':hasta' => $hasta];

        $stmt = $pdo->prepare(
            "SELECT COUNT(*) AS cantidad,
                    COALESCE(SUM(total), 0) AS total,
                    COALESCE(SUM(descuento), 0) AS descuentos
               FROM ventas
              WHERE DATE(fecha) BETWEEN :desde AND :hasta
                AND estado <> 'anulada'"
        );
        $stmt->execute($params);
        $ventas = $stmt->fetch();

        $stmt = $pdo->prepare(
            "SELECT COALESCE(SUM(d.cantidad), 0) AS platos
               FROM detalle_venta d
               JOIN ventas v ON v.id = d.venta_id
              WHERE DATE(v.fecha) BETWEEN :desde AND :hasta
                AND v.estado <> 'anulada'"
        );
        $stmt->execute($params);
        $platosVendidos = (int) $stmt->fetchColumn();

        $stmt = $pdo->prepare(
            "SELECT COALESCE(SUM(p.monto), 0) AS ingresos,
                    COALESCE(SUM(CASE WHEN p.medio = 'efectivo' THEN p.monto ELSE 0 END), 0) AS efectivo,
                    COALESCE(SUM(CASE WHEN p.medio = 'yape'     THEN p.monto ELSE 0 END), 0) AS yape,
                    COALESCE(SUM(CASE WHEN p.medio = 'tarjeta'  THEN p.monto ELSE 0 END), 0) AS tarjeta
               FROM pagos_cliente p
               JOIN ventas v ON v.id = p.venta_id
              WHERE DATE(p.fecha) BETWEEN :desde AND :hasta
                AND v.estado <> 'anulada'"
        );
        $stmt->execute($params);
        $ingresos = $stmt->fetch();

        $stmt = $pdo->prepare(
            'SELECT COALESCE(SUM(monto), 0) FROM gastos WHERE fecha BETWEEN :desde AND :hasta'
        );
        $stmt->execute($params);
        $gastos = (float) $stmt->fetchColumn();

        $stmt = $pdo->prepare(
            'SELECT COALESCE(SUM(monto), 0) FROM pagos_personal WHERE fecha BETWEEN :desde AND :hasta'
        );
        $stmt->execute($params);
        $pagosPersonal = (float) $stmt->fetchColumn();

        $stmt = $pdo->prepare(
            'SELECT COALESCE(SUM(total), 0) FROM compras WHERE fecha BETWEEN :desde AND :hasta'
        );
        $stmt->execute($params);
        $compras = (float) $stmt->fetchColumn();

        $stmt = $pdo->prepare(
            "SELECT DATE(v.fecha) AS fecha, COUNT(*) AS cantidad, SUM(v.total) AS total
               FROM ventas v
              WHERE DATE(v.fecha) BETWEEN :desde AND :hasta
                AND v.estado <> 'anulada'
              GROUP BY DATE(v.fecha)
              ORDER BY fecha ASC"
        );
        $stmt->execute($params);
        $ventasPorDia = $stmt->fetchAll();

        $stmt = $pdo->prepare(
            "SELECT p.medio, COUNT(*) AS cantidad, SUM(p.monto) AS total
               FROM pagos_cliente p
               JOIN ventas v ON v.id = p.venta_id
              WHERE DATE(p.fecha) BETWEEN :desde AND :hasta
                AND v.estado <> 'anulada'
              GROUP BY p.medio
              ORDER BY total DESC"
        );
        $stmt->execute($params);
        $ingresosPorMedio = $stmt->fetchAll();

        $stmt = $pdo->prepare(
            "SELECT pr.nombre, pr.categoria, SUM(d.cantidad) AS cantidad,
                    SUM(d.subtotal) AS total
               FROM detalle_venta d
               JOIN ventas v ON v.id = d.venta_id
               JOIN productos pr ON pr.id = d.producto_id
              WHERE DATE(v.fecha) BETWEEN :desde AND :hasta
                AND v.estado <> 'anulada'
              GROUP BY pr.id
              ORDER BY cantidad DESC, total DESC"
        );
        $stmt->execute($params);
        $topProductos = $stmt->fetchAll();

        $stmt = $pdo->prepare(
            'SELECT categoria, COUNT(*) AS cantidad, SUM(monto) AS total
               FROM gastos
              WHERE fecha BETWEEN :desde AND :hasta
              GROUP BY categoria
              ORDER BY total DESC'
        );
        $stmt->execute($params);
        $gastosPorCategoria = $stmt->fetchAll();

        $stmt = $pdo->prepare(
            "SELECT v.id, v.numero, DATE(v.fecha) AS fecha,
                    COALESCE(c.nombre, 'Consumidor final') AS cliente,
                    v.total, v.estado, v.tipo_servicio,
                    (SELECT COALESCE(SUM(p.monto), 0) FROM pagos_cliente p
                      WHERE p.venta_id = v.id) AS pagado
               FROM ventas v
               LEFT JOIN clientes c ON c.id = v.cliente_id
              WHERE DATE(v.fecha) BETWEEN :desde AND :hasta
              ORDER BY v.fecha DESC, v.id DESC
              LIMIT 500"
        );
        $stmt->execute($params);
        $listaVentas = $stmt->fetchAll();

        $stmt = $pdo->prepare(
            'SELECT pp.id, pp.fecha, pp.monto, pp.concepto, pe.nombre AS personal, pe.cargo
               FROM pagos_personal pp
               JOIN personal pe ON pe.id = pp.personal_id
              WHERE pp.fecha BETWEEN :desde AND :hasta
              ORDER BY pp.fecha DESC'
        );
        $stmt->execute($params);
        $pagosPersonalLista = $stmt->fetchAll();

        $serie = self::serieDiaria($desde, $hasta);

        $ingresosTotal = (float) $ingresos['ingresos'];
        $ganancia = round($ingresosTotal - $gastos - $pagosPersonal, 2);
        $resultadoNeto = round($ganancia - $compras, 2);

        return [
            'desde'               => $desde,
            'hasta'               => $hasta,
            'resumen'             => [
                'ventas_cantidad'  => (int) $ventas['cantidad'],
                'ventas_total'     => (float) $ventas['total'],
                'descuentos'       => (float) $ventas['descuentos'],
                'platos_vendidos'  => $platosVendidos,
                'ingresos'         => $ingresosTotal,
                'efectivo'         => (float) $ingresos['efectivo'],
                'yape'             => (float) $ingresos['yape'],
                'tarjeta'          => (float) $ingresos['tarjeta'],
                'gastos'           => $gastos,
                'pagos_personal'   => $pagosPersonal,
                'compras'          => $compras,
                'ganancia'         => $ganancia,
                'resultado_neto'   => $resultadoNeto,
            ],
            'ventas_por_dia'      => $ventasPorDia,
            'ingresos_por_medio'  => $ingresosPorMedio,
            'top_productos'       => $topProductos,
            'gastos_por_categoria' => $gastosPorCategoria,
            'ventas'              => $listaVentas,
            'pagos_personal'      => $pagosPersonalLista,
            'serie'               => $serie,
        ];
    }

    /**
     * Serie diaria de ingresos, gastos, pagos al personal y compras.
     */
    private static function serieDiaria(string $desde, string $hasta): array
    {
        $pdo = Database::conexion();
        $params = [
            ':dp1' => $desde, ':hp1' => $hasta,
            ':dg1' => $desde, ':hg1' => $hasta,
            ':dpp1' => $desde, ':hpp1' => $hasta,
            ':dc1' => $desde, ':hc1' => $hasta,
        ];

        $sql = "SELECT fecha,
                       SUM(ingreso) AS ingresos,
                       SUM(gasto) AS gastos,
                       SUM(personal) AS pagos_personal,
                       SUM(compra) AS compras
                  FROM (
                        SELECT DATE(p.fecha) AS fecha, p.monto AS ingreso,
                               0 AS gasto, 0 AS personal, 0 AS compra
                          FROM pagos_cliente p
                          JOIN ventas v ON v.id = p.venta_id
                         WHERE DATE(p.fecha) BETWEEN :dp1 AND :hp1
                           AND v.estado <> 'anulada'
                        UNION ALL
                        SELECT fecha, 0, monto, 0, 0
                          FROM gastos
                         WHERE fecha BETWEEN :dg1 AND :hg1
                        UNION ALL
                        SELECT fecha, 0, 0, monto, 0
                          FROM pagos_personal
                         WHERE fecha BETWEEN :dpp1 AND :hpp1
                        UNION ALL
                        SELECT fecha, 0, 0, 0, total
                          FROM compras
                         WHERE fecha BETWEEN :dc1 AND :hc1
                  ) t
                 GROUP BY fecha
                 ORDER BY fecha ASC";
        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
        $filas = $stmt->fetchAll();

        $mapa = [];
        foreach ($filas as $fila) {
            $mapa[$fila['fecha']] = $fila;
        }

        $inicio = new DateTimeImmutable($desde);
        $fin    = new DateTimeImmutable($hasta);
        $dias   = [];
        for ($d = $inicio; $d <= $fin; $d = $d->modify('+1 day')) {
            $clave = $d->format('Y-m-d');
            $fila = $mapa[$clave] ?? null;
            $ingresos = $fila ? (float) $fila['ingresos'] : 0.0;
            $gastos = $fila ? (float) $fila['gastos'] : 0.0;
            $personal = $fila ? (float) $fila['pagos_personal'] : 0.0;
            $compras = $fila ? (float) $fila['compras'] : 0.0;

            $dias[] = [
                'fecha'          => $clave,
                'ingresos'       => $ingresos,
                'gastos'         => $gastos,
                'pagos_personal' => $personal,
                'compras'        => $compras,
                'ganancia'       => round($ingresos - $gastos - $personal, 2),
                'resultado_neto' => round($ingresos - $gastos - $personal - $compras, 2),
            ];
        }

        return $dias;
    }

    public static function menuPublico(): array
    {
        $productos = Database::conexion()->query(
            "SELECT id, nombre, descripcion, categoria, precio, estado
               FROM productos
              WHERE estado = 'disponible'
              ORDER BY categoria ASC, nombre ASC"
        )->fetchAll();

        $categorias = [];
        foreach ($productos as $producto) {
            $categorias[$producto['categoria']][] = $producto;
        }

        return [
            'productos'  => $productos,
            'categorias' => $categorias,
        ];
    }
}
