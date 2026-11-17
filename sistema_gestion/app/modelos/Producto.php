<?php

declare(strict_types=1);

require_once __DIR__ . '/../Database.php';
require_once __DIR__ . '/../Validacion.php';

final class Producto
{
    public const CATEGORIAS = ['entradas', 'platos', 'bebidas', 'postres'];
    public const ESTADOS = ['disponible', 'agotado', 'inactivo'];

    public static function listar(array $filtros = []): array
    {
        $pdo = Database::conexion();
        $sql = 'SELECT * FROM productos WHERE 1 = 1';
        $params = [];

        if (!empty($filtros['buscar'])) {
            $sql .= ' AND nombre LIKE :q';
            $params[':q'] = '%' . $filtros['buscar'] . '%';
        }
        if (!empty($filtros['categoria']) && in_array($filtros['categoria'], self::CATEGORIAS, true)) {
            $sql .= ' AND categoria = :categoria';
            $params[':categoria'] = $filtros['categoria'];
        }
        if (!empty($filtros['estado']) && in_array($filtros['estado'], self::ESTADOS, true)) {
            $sql .= ' AND estado = :estado';
            $params[':estado'] = $filtros['estado'];
        }

        $sql .= ' ORDER BY categoria ASC, nombre ASC';
        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);

        return $stmt->fetchAll();
    }

    public static function obtener(int $id): ?array
    {
        $stmt = Database::conexion()->prepare('SELECT * FROM productos WHERE id = :id');
        $stmt->execute([':id' => $id]);
        $producto = $stmt->fetch();

        return $producto ?: null;
    }

    public static function guardar(array $datos): array
    {
        $id          = isset($datos['id']) && $datos['id'] !== '' ? (int) $datos['id'] : null;
        $nombre      = Validacion::texto($datos['nombre'] ?? '', 'nombre del producto', 120);
        $descripcion = Validacion::textoOpcional($datos['descripcion'] ?? '', 255);
        $categoria   = Validacion::enLista($datos['categoria'] ?? 'platos', self::CATEGORIAS, 'categoria');
        $precio      = Validacion::decimal($datos['precio'] ?? 0, 'precio', 0.01);
        $estado      = Validacion::enLista($datos['estado'] ?? 'disponible', self::ESTADOS, 'estado');

        $pdo = Database::conexion();

        if ($id === null) {
            $stmt = $pdo->prepare(
                'INSERT INTO productos (nombre, descripcion, categoria, precio, estado)
                 VALUES (:nombre, :descripcion, :categoria, :precio, :estado)'
            );
            $stmt->execute(compact('nombre', 'descripcion', 'categoria', 'precio', 'estado'));
            $id = (int) $pdo->lastInsertId();
        } else {
            if (self::obtener($id) === null) {
                throw new InvalidArgumentException('El producto indicado no existe.');
            }
            $stmt = $pdo->prepare(
                'UPDATE productos
                    SET nombre = :nombre, descripcion = :descripcion,
                        categoria = :categoria, precio = :precio, estado = :estado
                  WHERE id = :id'
            );
            $stmt->execute(compact('nombre', 'descripcion', 'categoria', 'precio', 'estado', 'id'));
        }

        return self::obtener($id);
    }

    public static function cambiarEstado(int $id, string $estado): array
    {
        $estado = Validacion::enLista($estado, self::ESTADOS, 'estado');
        $stmt = Database::conexion()->prepare('UPDATE productos SET estado = :estado WHERE id = :id');
        $stmt->execute([':estado' => $estado, ':id' => $id]);

        $producto = self::obtener($id);
        if ($producto === null) {
            throw new InvalidArgumentException('El producto indicado no existe.');
        }

        return $producto;
    }

    public static function eliminar(int $id): void
    {
        $pdo = Database::conexion();
        $stmt = $pdo->prepare('SELECT COUNT(*) FROM detalle_venta WHERE producto_id = :id');
        $stmt->execute([':id' => $id]);

        if ((int) $stmt->fetchColumn() > 0) {
            throw new InvalidArgumentException(
                'No se puede eliminar el producto porque tiene ventas registradas. Puede marcarlo como inactivo.'
            );
        }

        $stmt = $pdo->prepare('DELETE FROM productos WHERE id = :id');
        $stmt->execute([':id' => $id]);
    }
}
