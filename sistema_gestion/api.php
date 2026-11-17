<?php

declare(strict_types=1);

require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/app/Database.php';
require_once __DIR__ . '/app/ApiException.php';
require_once __DIR__ . '/app/Auth.php';
require_once __DIR__ . '/app/Validacion.php';
require_once __DIR__ . '/app/modelos/Usuario.php';
require_once __DIR__ . '/app/modelos/Cliente.php';
require_once __DIR__ . '/app/modelos/Producto.php';
require_once __DIR__ . '/app/modelos/Insumo.php';
require_once __DIR__ . '/app/modelos/Venta.php';
require_once __DIR__ . '/app/modelos/Gasto.php';
require_once __DIR__ . '/app/modelos/Personal.php';
require_once __DIR__ . '/app/modelos/Reporte.php';

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'OPTIONS') {
    http_response_code(204);
    exit;
}

function entrada(): array
{
    $datos = $_POST;
    $crudo = file_get_contents('php://input');

    if ($crudo !== false && trim($crudo) !== '') {
        $json = json_decode($crudo, true);
        if (json_last_error() !== JSON_ERROR_NONE) {
            throw new ApiException('El cuerpo de la solicitud no es un JSON valido.', 400);
        }
        if (is_array($json)) {
            $datos = array_merge($datos, $json);
        }
    }

    return $datos;
}

function responder(array $datos, int $codigo = 200): void
{
    http_response_code($codigo);
    echo json_encode(['ok' => true, 'data' => $datos], JSON_UNESCAPED_UNICODE);
    exit;
}

function responderError(string $mensaje, int $codigo = 400): void
{
    http_response_code($codigo);
    echo json_encode(['ok' => false, 'error' => $mensaje], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    $accion = strtolower(trim((string) ($_GET['action'] ?? $_POST['action'] ?? '')));
    $datos  = entrada();

    if (isset($datos['action']) && $accion === '') {
        $accion = strtolower(trim((string) $datos['action']));
    }

    if ($accion === '') {
        throw new ApiException('Debe indicar la accion a ejecutar (?action=...).', 400);
    }

    switch ($accion) {
        case 'login':
            $resultado = Auth::iniciarSesion(
                (string) ($datos['usuario'] ?? ''),
                (string) ($datos['clave'] ?? '')
            );
            responder($resultado);

        case 'logout':
            Auth::cerrarSesion(Auth::tokenDeSolicitud());
            responder(['mensaje' => 'Sesion finalizada.']);

        case 'me':
            responder(Auth::requerir());

        case 'menu_publico':
            responder(Reporte::menuPublico());

        case 'dashboard':
            Auth::requerir();
            responder(Reporte::dashboard());

        // -------------------------------------------------------------
        // CLIENTES
        // -------------------------------------------------------------
        case 'clientes':
            Auth::requerirRol(['administrador', 'cajero']);
            responder(Cliente::listar(trim((string) ($_GET['q'] ?? ''))));

        case 'cliente_guardar':
            Auth::requerirRol(['administrador', 'cajero']);
            responder(Cliente::guardar($datos), 201);

        case 'cliente_eliminar':
            Auth::requerirRol(['administrador', 'cajero']);
            Cliente::eliminar((int) ($datos['id'] ?? 0));
            responder(['mensaje' => 'Cliente eliminado.']);

        // -------------------------------------------------------------
        // PRODUCTOS
        // -------------------------------------------------------------
        case 'productos':
            Auth::requerir();
            responder(Producto::listar([
                'buscar'    => trim((string) ($_GET['q'] ?? '')),
                'categoria' => trim((string) ($_GET['categoria'] ?? '')),
                'estado'    => trim((string) ($_GET['estado'] ?? '')),
            ]));

        case 'producto_guardar':
            Auth::requerirRol(['administrador', 'inventario']);
            responder(Producto::guardar($datos), 201);

        case 'producto_estado':
            Auth::requerirRol(['administrador', 'inventario']);
            responder(Producto::cambiarEstado(
                (int) ($datos['id'] ?? 0),
                (string) ($datos['estado'] ?? '')
            ));

        case 'producto_eliminar':
            Auth::requerirRol(['administrador', 'inventario']);
            Producto::eliminar((int) ($datos['id'] ?? 0));
            responder(['mensaje' => 'Producto eliminado.']);

        // -------------------------------------------------------------
        // INVENTARIO
        // -------------------------------------------------------------
        case 'insumos':
            Auth::requerirRol(['administrador', 'inventario', 'cocina']);
            responder(Insumo::listar(trim((string) ($_GET['q'] ?? ''))));

        case 'insumo_guardar':
            Auth::requerirRol(['administrador', 'inventario']);
            responder(Insumo::guardar($datos), 201);

        case 'insumo_eliminar':
            Auth::requerirRol(['administrador', 'inventario']);
            Insumo::eliminar((int) ($datos['id'] ?? 0));
            responder(['mensaje' => 'Insumo eliminado.']);

        case 'movimientos':
            Auth::requerirRol(['administrador', 'inventario']);
            responder(Insumo::movimientos([
                'insumo_id' => (int) ($_GET['insumo_id'] ?? 0),
                'desde'     => trim((string) ($_GET['desde'] ?? '')),
                'hasta'     => trim((string) ($_GET['hasta'] ?? '')),
            ]));

        case 'movimiento_crear':
            $usuario = Auth::requerirRol(['administrador', 'inventario']);
            responder(Insumo::registrarMovimiento($datos, $usuario['id']), 201);

        case 'stock_bajo':
            Auth::requerirRol(['administrador', 'inventario', 'cocina']);
            responder(Insumo::stockBajo());

        case 'compras':
            Auth::requerirRol(['administrador', 'inventario']);
            responder(Insumo::compras([
                'desde' => trim((string) ($_GET['desde'] ?? '')),
                'hasta' => trim((string) ($_GET['hasta'] ?? '')),
            ]));

        case 'compra':
            Auth::requerirRol(['administrador', 'inventario']);
            $compra = Insumo::obtenerCompra((int) ($_GET['id'] ?? 0));
            if ($compra === null) {
                throw new ApiException('La compra indicada no existe.', 404);
            }
            responder($compra);

        case 'compra_crear':
            $usuario = Auth::requerirRol(['administrador', 'inventario']);
            responder(Insumo::registrarCompra($datos, $usuario['id']), 201);

        // -------------------------------------------------------------
        // VENTAS
        // -------------------------------------------------------------
        case 'ventas':
            Auth::requerir();
            responder(Venta::listar([
                'estado' => trim((string) ($_GET['estado'] ?? '')),
                'desde'  => trim((string) ($_GET['desde'] ?? '')),
                'hasta'  => trim((string) ($_GET['hasta'] ?? '')),
                'buscar' => trim((string) ($_GET['q'] ?? '')),
            ]));

        case 'venta':
            Auth::requerir();
            $venta = Venta::obtener((int) ($_GET['id'] ?? 0));
            if ($venta === null) {
                throw new ApiException('La venta indicada no existe.', 404);
            }
            responder($venta);

        case 'venta_crear':
            $usuario = Auth::requerirRol(['administrador', 'cajero']);
            responder(Venta::crear($datos, $usuario['id']), 201);

        case 'venta_estado':
            $usuario = Auth::requerirRol(['administrador', 'cajero', 'cocina']);
            $nuevoEstado = (string) ($datos['estado'] ?? '');
            if ($usuario['rol'] === 'cocina'
                && !in_array($nuevoEstado, ['en_preparacion', 'lista'], true)) {
                throw new ApiException('Cocina solo puede iniciar la preparacion o marcar el pedido como listo.', 403);
            }
            responder(Venta::cambiarEstado((int) ($datos['id'] ?? 0), $nuevoEstado));

        case 'venta_pago':
            $usuario = Auth::requerirRol(['administrador', 'cajero']);
            responder(Venta::registrarPago((int) ($datos['venta_id'] ?? 0), $datos, $usuario['id']));

        case 'venta_anular':
            Auth::requerirRol(['administrador']);
            responder(Venta::anular((int) ($datos['id'] ?? 0)));

        // -------------------------------------------------------------
        // GASTOS
        // -------------------------------------------------------------
        case 'gastos':
            Auth::requerirRol(['administrador']);
            responder(Gasto::listar([
                'desde'     => trim((string) ($_GET['desde'] ?? '')),
                'hasta'     => trim((string) ($_GET['hasta'] ?? '')),
                'categoria' => trim((string) ($_GET['categoria'] ?? '')),
                'buscar'    => trim((string) ($_GET['q'] ?? '')),
            ]));

        case 'gasto_guardar':
            $usuario = Auth::requerirRol(['administrador']);
            responder(Gasto::guardar($datos, $usuario['id']), 201);

        case 'gasto_eliminar':
            Auth::requerirRol(['administrador']);
            Gasto::eliminar((int) ($datos['id'] ?? 0));
            responder(['mensaje' => 'Gasto eliminado.']);

        // -------------------------------------------------------------
        // PERSONAL
        // -------------------------------------------------------------
        case 'personal':
            Auth::requerirRol(['administrador']);
            responder(Personal::listar(trim((string) ($_GET['q'] ?? ''))));

        case 'personal_guardar':
            Auth::requerirRol(['administrador']);
            responder(Personal::guardar($datos), 201);

        case 'personal_eliminar':
            Auth::requerirRol(['administrador']);
            Personal::eliminar((int) ($datos['id'] ?? 0));
            responder(['mensaje' => 'Trabajador eliminado.']);

        case 'pagos_personal':
            Auth::requerirRol(['administrador']);
            responder(Personal::pagos([
                'personal_id' => (int) ($_GET['personal_id'] ?? 0),
                'desde'       => trim((string) ($_GET['desde'] ?? '')),
                'hasta'       => trim((string) ($_GET['hasta'] ?? '')),
            ]));

        case 'pago_personal_crear':
            $usuario = Auth::requerirRol(['administrador']);
            responder(Personal::registrarPago($datos, $usuario['id']), 201);

        case 'pago_personal_eliminar':
            Auth::requerirRol(['administrador']);
            Personal::eliminarPago((int) ($datos['id'] ?? 0));
            responder(['mensaje' => 'Pago eliminado.']);

        // -------------------------------------------------------------
        // REPORTES
        // -------------------------------------------------------------
        case 'reportes':
            Auth::requerirRol(['administrador', 'cajero']);
            $desde = trim((string) ($_GET['desde'] ?? ''));
            $hasta = trim((string) ($_GET['hasta'] ?? ''));
            if ($desde === '') {
                $desde = date('Y-m-01');
            }
            if ($hasta === '') {
                $hasta = date('Y-m-d');
            }
            $desde = Validacion::fecha($desde, 'desde');
            $hasta = Validacion::fecha($hasta, 'hasta');
            if ($desde > $hasta) {
                throw new ApiException('La fecha inicial no puede ser posterior a la fecha final.', 422);
            }
            responder(Reporte::reporte($desde, $hasta));

        // -------------------------------------------------------------
        // USUARIOS
        // -------------------------------------------------------------
        case 'usuarios':
            Auth::requerirRol(['administrador']);
            responder(Usuario::listar());

        case 'usuario_guardar':
            Auth::requerirRol(['administrador']);
            responder(Usuario::guardar($datos), 201);

        case 'usuario_estado':
            Auth::requerirRol(['administrador']);
            responder(Usuario::cambiarEstado(
                (int) ($datos['id'] ?? 0),
                !empty($datos['activo'])
            ));

        default:
            throw new ApiException('Accion no reconocida: ' . $accion, 404);
    }
} catch (ApiException $e) {
    responderError($e->getMessage(), $e->httpCode());
} catch (InvalidArgumentException $e) {
    responderError($e->getMessage(), 422);
} catch (PDOException $e) {
    responderError('Error de base de datos: ' . $e->getMessage(), 500);
} catch (Throwable $e) {
    responderError('Error interno: ' . $e->getMessage(), 500);
}
