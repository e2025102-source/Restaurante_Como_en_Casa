<?php

declare(strict_types=1);

require_once __DIR__ . '/../Database.php';
require_once __DIR__ . '/../Validacion.php';

final class Personal
{
    public static function listar(string $buscar = ''): array
    {
        $sql = 'SELECT p.*,
                       (SELECT COALESCE(SUM(pp.monto), 0) FROM pagos_personal pp
                         WHERE pp.personal_id = p.id) AS total_pagado
                  FROM personal p
                 WHERE 1 = 1';
        $params = [];

        if ($buscar !== '') {
            $sql .= ' AND (p.nombre LIKE :q OR p.cargo LIKE :q OR p.documento LIKE :q)';
            $params[':q'] = '%' . $buscar . '%';
        }

        $sql .= ' ORDER BY p.activo DESC, p.nombre ASC';
        $stmt = Database::conexion()->prepare($sql);
        $stmt->execute($params);

        return $stmt->fetchAll();
    }

    public static function obtener(int $id): ?array
    {
        $stmt = Database::conexion()->prepare('SELECT * FROM personal WHERE id = :id');
        $stmt->execute([':id' => $id]);
        $persona = $stmt->fetch();

        return $persona ?: null;
    }

    public static function guardar(array $datos): array
    {
        $id           = isset($datos['id']) && $datos['id'] !== '' ? (int) $datos['id'] : null;
        $nombre       = Validacion::texto($datos['nombre'] ?? '', 'nombre', 120);
        $documento    = Validacion::textoOpcional($datos['documento'] ?? '', 20);
        $cargo        = Validacion::texto($datos['cargo'] ?? '', 'cargo', 60);
        $telefono     = Validacion::textoOpcional($datos['telefono'] ?? '', 20);
        $sueldo       = Validacion::decimal($datos['sueldo'] ?? 0, 'sueldo', 0);
        $fechaIngreso = Validacion::fechaOpcional($datos['fecha_ingreso'] ?? '');
        $activo       = !empty($datos['activo']) ? 1 : 0;

        $pdo = Database::conexion();

        if ($id === null) {
            $stmt = $pdo->prepare(
                'INSERT INTO personal
                    (nombre, documento, cargo, telefono, sueldo, fecha_ingreso, activo)
                 VALUES
                    (:nombre, :documento, :cargo, :telefono, :sueldo, :fecha_ingreso, :activo)'
            );
            $stmt->execute([
                ':nombre'        => $nombre,
                ':documento'     => $documento,
                ':cargo'         => $cargo,
                ':telefono'      => $telefono,
                ':sueldo'        => $sueldo,
                ':fecha_ingreso' => $fechaIngreso,
                ':activo'        => $activo,
            ]);
            $id = (int) $pdo->lastInsertId();
        } else {
            if (self::obtener($id) === null) {
                throw new InvalidArgumentException('El trabajador indicado no existe.');
            }
            $stmt = $pdo->prepare(
                'UPDATE personal
                    SET nombre = :nombre, documento = :documento, cargo = :cargo,
                        telefono = :telefono, sueldo = :sueldo,
                        fecha_ingreso = :fecha_ingreso, activo = :activo
                  WHERE id = :id'
            );
            $stmt->execute([
                ':nombre'        => $nombre,
                ':documento'     => $documento,
                ':cargo'         => $cargo,
                ':telefono'      => $telefono,
                ':sueldo'        => $sueldo,
                ':fecha_ingreso' => $fechaIngreso,
                ':activo'        => $activo,
                ':id'            => $id,
            ]);
        }

        return self::obtener($id);
    }

    public static function eliminar(int $id): void
    {
        $pdo = Database::conexion();
        $stmt = $pdo->prepare('SELECT COUNT(*) FROM pagos_personal WHERE personal_id = :id');
        $stmt->execute([':id' => $id]);
        if ((int) $stmt->fetchColumn() > 0) {
            throw new InvalidArgumentException(
                'No se puede eliminar al trabajador porque tiene pagos registrados. Puede marcarlo como inactivo.'
            );
        }

        $stmt = $pdo->prepare('DELETE FROM personal WHERE id = :id');
        $stmt->execute([':id' => $id]);
    }

    public static function pagos(array $filtros = []): array
    {
        $sql = 'SELECT pp.*, pe.nombre AS personal, pe.cargo, u.nombres AS usuario
                  FROM pagos_personal pp
                  JOIN personal pe ON pe.id = pp.personal_id
                  LEFT JOIN usuarios u ON u.id = pp.usuario_id
                 WHERE 1 = 1';
        $params = [];

        if (!empty($filtros['personal_id'])) {
            $sql .= ' AND pp.personal_id = :personal_id';
            $params[':personal_id'] = (int) $filtros['personal_id'];
        }
        if (!empty($filtros['desde'])) {
            $sql .= ' AND pp.fecha >= :desde';
            $params[':desde'] = $filtros['desde'];
        }
        if (!empty($filtros['hasta'])) {
            $sql .= ' AND pp.fecha <= :hasta';
            $params[':hasta'] = $filtros['hasta'];
        }

        $sql .= ' ORDER BY pp.fecha DESC, pp.id DESC';
        $stmt = Database::conexion()->prepare($sql);
        $stmt->execute($params);

        return $stmt->fetchAll();
    }

    public static function registrarPago(array $datos, int $usuarioId): array
    {
        $personalId = Validacion::entero($datos['personal_id'] ?? 0, 'trabajador', 1);
        $fecha      = Validacion::fecha($datos['fecha'] ?? date('Y-m-d'), 'fecha');
        $monto      = Validacion::decimal($datos['monto'] ?? 0, 'monto', 0.01);
        $concepto   = Validacion::texto($datos['concepto'] ?? 'Sueldo', 'concepto', 120);

        if (self::obtener($personalId) === null) {
            throw new InvalidArgumentException('El trabajador indicado no existe.');
        }

        $pdo = Database::conexion();
        $stmt = $pdo->prepare(
            'INSERT INTO pagos_personal (personal_id, fecha, monto, concepto, usuario_id)
             VALUES (:personal_id, :fecha, :monto, :concepto, :usuario_id)'
        );
        $stmt->execute([
            ':personal_id' => $personalId,
            ':fecha'       => $fecha,
            ':monto'       => $monto,
            ':concepto'    => $concepto,
            ':usuario_id'  => $usuarioId,
        ]);
        $id = (int) $pdo->lastInsertId();

        $stmt = $pdo->prepare(
            'SELECT pp.*, pe.nombre AS personal, pe.cargo
               FROM pagos_personal pp
               JOIN personal pe ON pe.id = pp.personal_id
              WHERE pp.id = :id'
        );
        $stmt->execute([':id' => $id]);

        return $stmt->fetch() ?: [];
    }

    public static function eliminarPago(int $id): void
    {
        $stmt = Database::conexion()->prepare('DELETE FROM pagos_personal WHERE id = :id');
        $stmt->execute([':id' => $id]);
    }
}
