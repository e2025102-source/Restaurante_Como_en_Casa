(function () {
    'use strict';

    const API_URL = 'api.php';
    const TOKEN_KEY = 'cec_token';
    const USUARIO_KEY = 'cec_usuario';
    const ROLES_TODOS = ['administrador', 'cajero', 'cocina', 'inventario'];

    const qs = (sel, ctx) => (ctx || document).querySelector(sel);
    const qsa = (sel, ctx) => Array.prototype.slice.call((ctx || document).querySelectorAll(sel));

    let token = localStorage.getItem(TOKEN_KEY) || '';
    let usuario = null;
    try { usuario = JSON.parse(localStorage.getItem(USUARIO_KEY) || 'null'); } catch (e) { usuario = null; }

    let carrito = [];
    let productosCache = [];
    let clientesCache = [];
    let insumosCache = [];
    let personalCache = [];
    let modalSubmit = null;
    let rutaActual = '';

    const MONEDA = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    const vistaLogin = qs('#vista-login');
    const vistaApp = qs('#vista-app');
    const contenido = qs('#contenido');
    const sidebar = qs('#sidebar');
    const sidebarOverlay = qs('#sidebar-overlay');
    const modalOverlay = qs('#modal-overlay');
    const modalCaja = qs('#modal-caja');
    const modalTitulo = qs('#modal-titulo');
    const modalCuerpo = qs('#modal-cuerpo');
    const modalGuardar = qs('#modal-guardar');
    const modalCancelar = qs('#modal-cancelar');

    const ACCIONES = {};

    /* ------------------------------------------------------------------ */
    /* UTILIDADES                                                          */
    /* ------------------------------------------------------------------ */
    function money(valor) {
        return 'S/ ' + MONEDA.format(Number(valor) || 0);
    }

    function num(valor) {
        const n = Number(valor);
        return isFinite(n) ? n : 0;
    }

    function esc(valor) {
        return String(valor === null || valor === undefined ? '' : valor)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function fecha(valor) {
        if (!valor) return '—';
        const txt = String(valor);
        const m = txt.match(/^(\d{4})-(\d{2})-(\d{2})/);
        return m ? m[3] + '/' + m[2] + '/' + m[1] : txt;
    }

    function fechaHora(valor) {
        if (!valor) return '—';
        const txt = String(valor);
        return fecha(txt) + (txt.length >= 16 ? ' ' + txt.substring(11, 16) : '');
    }

    function hoyISO() {
        const d = new Date();
        const mes = String(d.getMonth() + 1).padStart(2, '0');
        const dia = String(d.getDate()).padStart(2, '0');
        return d.getFullYear() + '-' + mes + '-' + dia;
    }

    function primerDiaMes() {
        const d = new Date();
        const mes = String(d.getMonth() + 1).padStart(2, '0');
        return d.getFullYear() + '-' + mes + '-01';
    }

    function fechaLarga() {
        try {
            const txt = new Intl.DateTimeFormat('es-PE', {
                weekday: 'long', day: '2-digit', month: 'long', year: 'numeric'
            }).format(new Date());
            return txt.charAt(0).toUpperCase() + txt.slice(1);
        } catch (e) {
            return fecha(hoyISO());
        }
    }

    function tituloEstado(estado) {
        return String(estado || '').replace(/_/g, ' ').replace(/^\w/, c => c.toUpperCase());
    }

    function badge(valor, texto) {
        const clase = String(valor || '').replace(/[^a-z0-9_-]/gi, '');
        return '<span class="badge ' + clase + '">' + esc(texto || tituloEstado(valor)) + '</span>';
    }

    function sinDatos(mensaje) {
        return '<div class="estado-vacio"><strong>Sin informacion</strong><span>' + esc(mensaje || 'No hay registros para mostrar.') + '</span></div>';
    }

    function cargando() {
        return '<div class="cargando"><span class="spinner"></span> Cargando informacion...</div>';
    }

    function opciones(lista, seleccionado) {
        return lista.map(function (op) {
            const valor = typeof op === 'string' ? op : op.valor;
            const texto = typeof op === 'string' ? op : op.texto;
            const sel = String(valor) === String(seleccionado) ? ' selected' : '';
            return '<option value="' + esc(valor) + '"' + sel + '>' + esc(texto) + '</option>';
        }).join('');
    }

    const ICONOS = {
        panel: '<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>',
        ventas: '<path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/><path d="M3 6h18"/><path d="M16 10a4 4 0 0 1-8 0"/>',
        nueva: '<circle cx="12" cy="12" r="9"/><path d="M12 8v8M8 12h8"/>',
        productos: '<path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2Z"/>',
        clientes: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>',
        inventario: '<path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"/><path d="m3.3 7 8.7 5 8.7-5M12 22V12"/>',
        compras: '<path d="M10 17h4V5H2v12h3"/><path d="M20 17h2v-3.34a4 4 0 0 0-1.17-2.83L19 9h-5v8h1"/><circle cx="7.5" cy="17.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/>',
        gastos: '<path d="m22 17-8.5-8.5-5 5L2 7"/><path d="M16 17h6v-6"/>',
        personal: '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>',
        pagos: '<rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/>',
        reportes: '<path d="M3 3v18h18"/><path d="M18 17V9M13 17V5M8 17v-3"/>',
        usuarios: '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1Z"/>'
    };

    function icono(nombre) {
        return '<svg viewBox="0 0 24 24" style="fill:none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + (ICONOS[nombre] || '') + '</svg>';
    }

    /* ------------------------------------------------------------------ */
    /* API                                                                 */
    /* ------------------------------------------------------------------ */
    async function api(accion, datos, metodo) {
        datos = datos || {};
        metodo = metodo || 'GET';
        const params = new URLSearchParams();
        params.set('action', accion);

        const opcionesFetch = { method: metodo, headers: {} };
        if (token) opcionesFetch.headers['Authorization'] = 'Bearer ' + token;

        if (metodo === 'GET') {
            Object.keys(datos).forEach(function (clave) {
                const valor = datos[clave];
                if (valor !== undefined && valor !== null && valor !== '') {
                    params.set(clave, valor);
                }
            });
        } else {
            opcionesFetch.headers['Content-Type'] = 'application/json';
            opcionesFetch.body = JSON.stringify(datos);
        }

        let respuesta;
        try {
            respuesta = await fetch(API_URL + '?' + params.toString(), opcionesFetch);
        } catch (e) {
            throw new Error('No se pudo conectar con el servidor. Verifique que Apache y MySQL esten activos.');
        }

        let json;
        try {
            json = await respuesta.json();
        } catch (e) {
            throw new Error('El servidor devolvio una respuesta no valida.');
        }

        if (!json.ok) {
            if (respuesta.status === 401 && accion !== 'login' && accion !== 'me') {
                limpiarSesion();
                mostrarLogin('Su sesion expiro. Vuelva a ingresar.');
            }
            throw new Error(json.error || 'No se pudo completar la operacion.');
        }

        return json.data;
    }

    /* ------------------------------------------------------------------ */
    /* TOAST Y MODAL                                                       */
    /* ------------------------------------------------------------------ */
    function toast(mensaje, tipo) {
        const contenedor = qs('#toast-container');
        if (!contenedor) return;
        const el = document.createElement('div');
        el.className = 'toast ' + (tipo || 'info');
        el.textContent = mensaje;
        contenedor.appendChild(el);
        setTimeout(function () {
            el.style.opacity = '0';
            el.style.transform = 'translateX(20px)';
            el.style.transition = '0.2s ease';
            setTimeout(function () { el.remove(); }, 220);
        }, 4200);
    }

    function abrirModal(titulo, cuerpo, opciones) {
        opciones = opciones || {};
        modalTitulo.textContent = titulo;
        modalCuerpo.innerHTML = cuerpo;
        modalCaja.classList.toggle('ancho', !!opciones.ancho);
        modalGuardar.textContent = opciones.textoGuardar || 'Guardar';
        modalGuardar.classList.toggle('oculto', !!opciones.ocultarGuardar);
        modalCancelar.textContent = opciones.textoCancelar || 'Cancelar';
        modalSubmit = opciones.onSubmit || null;
        modalOverlay.classList.remove('oculto');
        setTimeout(function () {
            const primero = modalCuerpo.querySelector('input, select, textarea');
            if (primero) primero.focus();
        }, 40);
    }

    function cerrarModal() {
        modalOverlay.classList.add('oculto');
        modalCuerpo.innerHTML = '';
        modalSubmit = null;
    }

    function confirmar(mensaje) {
        return window.confirm(mensaje);
    }

    function mensajeVacioTabla(columnas, mensaje) {
        return '<tr><td colspan="' + columnas + '">' + sinDatos(mensaje) + '</td></tr>';
    }

    /* ------------------------------------------------------------------ */
    /* SESION Y LOGIN                                                      */
    /* ------------------------------------------------------------------ */
    function guardarSesion(datos) {
        token = datos.token;
        usuario = datos.usuario;
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario));
    }

    function limpiarSesion() {
        token = '';
        usuario = null;
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USUARIO_KEY);
    }

    function mostrarLogin(mensaje) {
        vistaApp.classList.add('oculto');
        vistaLogin.classList.remove('oculto');
        const error = qs('#login-error');
        if (mensaje) {
            error.textContent = mensaje;
            error.classList.remove('oculto');
        } else {
            error.classList.add('oculto');
        }
    }

    function entrarApp() {
        vistaLogin.classList.add('oculto');
        vistaApp.classList.remove('oculto');
        qs('#sidebar-nombre').textContent = usuario.nombres;
        qs('#sidebar-rol').textContent = tituloEstado(usuario.rol);
        qs('#sidebar-avatar').textContent = String(usuario.nombres || 'U').charAt(0).toUpperCase();
        qs('#topbar-usuario').textContent = usuario.nombres;
        qs('#topbar-fecha').textContent = fechaLarga();
        construirMenu();
        if (!location.hash || location.hash === '#') {
            location.hash = '#/dashboard';
        } else {
            atenderRuta();
        }
    }

    async function salir() {
        try { await api('logout', {}, 'POST'); } catch (e) { /* ignorar */ }
        limpiarSesion();
        location.hash = '';
        location.reload();
    }

    /* ------------------------------------------------------------------ */
    /* MENU Y RUTAS                                                        */
    /* ------------------------------------------------------------------ */
    const MENU = [
        {
            seccion: 'Operacion',
            items: [
                { ruta: 'dashboard', texto: 'Panel', icono: 'panel', roles: ROLES_TODOS },
                { ruta: 'ventas', texto: 'Ventas', icono: 'ventas', roles: ROLES_TODOS },
                { ruta: 'nueva-venta', texto: 'Nueva venta', icono: 'nueva', roles: ['administrador', 'cajero'] }
            ]
        },
        {
            seccion: 'Catalogo',
            items: [
                { ruta: 'productos', texto: 'Productos', icono: 'productos', roles: ROLES_TODOS },
                { ruta: 'clientes', texto: 'Clientes', icono: 'clientes', roles: ['administrador', 'cajero'] },
                { ruta: 'inventario', texto: 'Inventario', icono: 'inventario', roles: ['administrador', 'inventario', 'cocina'] },
                { ruta: 'compras', texto: 'Compras', icono: 'compras', roles: ['administrador', 'inventario'] }
            ]
        },
        {
            seccion: 'Administracion',
            items: [
                { ruta: 'gastos', texto: 'Gastos', icono: 'gastos', roles: ['administrador'] },
                { ruta: 'personal', texto: 'Personal', icono: 'personal', roles: ['administrador'] },
                { ruta: 'pagos', texto: 'Pagos de personal', icono: 'pagos', roles: ['administrador'] },
                { ruta: 'reportes', texto: 'Reportes', icono: 'reportes', roles: ['administrador', 'cajero'] },
                { ruta: 'usuarios', texto: 'Usuarios', icono: 'usuarios', roles: ['administrador'] }
            ]
        }
    ];

    const TITULOS = {
        dashboard: ['Panel principal', 'Resumen del dia y alertas del restaurante'],
        ventas: ['Ventas', 'Pedidos registrados y control de estados'],
        'nueva-venta': ['Nueva venta', 'Seleccione productos y confirme el pedido'],
        productos: ['Productos', 'Carta del restaurante y disponibilidad'],
        clientes: ['Clientes', 'Registro e historial de consumo'],
        inventario: ['Inventario', 'Stock de insumos y movimientos'],
        compras: ['Compras', 'Abastecimiento de insumos al almacen'],
        gastos: ['Gastos', 'Egresos operativos del restaurante'],
        personal: ['Personal', 'Trabajadores y pagos registrados'],
        pagos: ['Pagos de personal', 'Sueldos, adelantos y bonificaciones'],
        reportes: ['Reportes', 'Analisis de ventas, gastos y ganancias'],
        usuarios: ['Usuarios', 'Accesos y roles del sistema']
    };

    function rutaPermitida(ruta) {
        if (!usuario) return false;
        if (ruta === 'dashboard') return true;
        let permitida = false;
        MENU.forEach(function (grupo) {
            grupo.items.forEach(function (item) {
                if (item.ruta === ruta && item.roles.indexOf(usuario.rol) !== -1) permitida = true;
            });
        });
        return permitida;
    }

    function construirMenu() {
        const nav = qs('#nav-principal');
        let html = '';
        MENU.forEach(function (grupo) {
            const items = grupo.items.filter(function (item) {
                return item.roles.indexOf(usuario.rol) !== -1;
            });
            if (!items.length) return;
            html += '<span class="nav-seccion">' + esc(grupo.seccion) + '</span>';
            items.forEach(function (item) {
                html += '<a class="nav-enlace" data-ruta="' + item.ruta + '" href="#/' + item.ruta + '">' +
                    icono(item.icono) + '<span>' + esc(item.texto) + '</span></a>';
            });
        });
        nav.innerHTML = html;
    }

    function marcarMenu(ruta) {
        qsa('.nav-enlace').forEach(function (enlace) {
            enlace.classList.toggle('activo', enlace.dataset.ruta === ruta);
        });
    }

    function atenderRuta() {
        if (!usuario) return;
        let ruta = (location.hash || '').replace(/^#\/?/, '').trim();
        if (!ruta) ruta = 'dashboard';
        if (!rutaPermitida(ruta)) {
            ruta = 'dashboard';
            location.hash = '#/dashboard';
            return;
        }

        rutaActual = ruta;
        marcarMenu(ruta);
        const titulo = TITULOS[ruta] || ['Panel principal', ''];
        qs('#titulo-pagina').textContent = titulo[0];
        qs('#subtitulo-pagina').textContent = titulo[1];
        cerrarSidebar();

        const vistas = {
            dashboard: vistaDashboard,
            ventas: vistaVentas,
            'nueva-venta': vistaNuevaVenta,
            productos: vistaProductos,
            clientes: vistaClientes,
            inventario: vistaInventario,
            compras: vistaCompras,
            gastos: vistaGastos,
            personal: vistaPersonal,
            pagos: vistaPagosPersonal,
            reportes: vistaReportes,
            usuarios: vistaUsuarios
        };

        const funcion = vistas[ruta];
        if (!funcion) {
            contenido.innerHTML = sinDatos('La seccion solicitada no existe.');
            return;
        }
        Promise.resolve().then(funcion).catch(function (error) {
            contenido.innerHTML = '<div class="panel"><div class="panel-cuerpo">' + sinDatos(error.message) + '</div></div>';
            toast(error.message, 'error');
        });
    }

    function abrirSidebar() {
        sidebar.classList.add('abierto');
        sidebarOverlay.classList.add('visible');
    }

    function cerrarSidebar() {
        sidebar.classList.remove('abierto');
        sidebarOverlay.classList.remove('visible');
    }

    /* ------------------------------------------------------------------ */
    /* DASHBOARD                                                           */
    /* ------------------------------------------------------------------ */
    function grafico(serie) {
        if (!serie || !serie.length) return sinDatos('Aun no hay movimientos en el periodo.');
        let max = 1;
        serie.forEach(function (d) {
            max = Math.max(max, num(d.ingresos), num(d.gastos), num(d.compras));
        });
        const barras = serie.map(function (d) {
            const etiqueta = String(d.fecha).substring(5).split('-').reverse().join('/');
            const altura = function (v) { return Math.max(2, Math.round((num(v) / max) * 150)); };
            return '<div class="chart-col">' +
                '<div class="chart-bars">' +
                '<div class="chart-bar" style="height:' + altura(d.ingresos) + 'px" title="Ingresos: ' + money(d.ingresos) + '"></div>' +
                '<div class="chart-bar rojo" style="height:' + altura(d.gastos) + 'px" title="Gastos: ' + money(d.gastos) + '"></div>' +
                '<div class="chart-bar ambar" style="height:' + altura(d.compras) + 'px" title="Compras: ' + money(d.compras) + '"></div>' +
                '</div><span class="chart-label">' + etiqueta + '</span></div>';
        }).join('');
        return '<div class="chart">' + barras + '</div>';
    }

    async function vistaDashboard() {
        contenido.innerHTML = cargando();
        const d = await api('dashboard');

        const signoGanancia = num(d.ganancia_hoy) >= 0 ? 't-verde' : 't-rojo';
        const egresos = num(d.gastos_hoy) + num(d.pagos_personal_hoy);

        let stockHtml = '';
        if (d.stock_bajo.length) {
            d.stock_bajo.forEach(function (i) {
                stockHtml += '<div class="alerta-stock"><div><strong>' + esc(i.nombre) + '</strong><br>' +
                    '<span>Minimo: ' + esc(i.stock_minimo) + ' ' + esc(i.unidad) + '</span></div>' +
                    '<span class="stock-num">' + esc(i.stock_actual) + ' ' + esc(i.unidad) + '</span></div>';
            });
        } else {
            stockHtml = sinDatos('Todos los insumos estan por encima del minimo.');
        }

        let mediosHtml = '';
        if (d.ingresos_por_medio.length) {
            d.ingresos_por_medio.forEach(function (m) {
                mediosHtml += '<div class="lista-mini-item"><div><strong>' + esc(tituloEstado(m.medio)) + '</strong>' +
                    '<span>' + esc(m.cantidad) + ' pago(s)</span></div><span class="valor">' + money(m.total) + '</span></div>';
            });
        } else {
            mediosHtml = sinDatos('Sin ingresos registrados hoy.');
        }

        let topHtml = '';
        if (d.top_productos.length) {
            d.top_productos.forEach(function (p, idx) {
                topHtml += '<div class="lista-mini-item"><div><strong>' + (idx + 1) + '. ' + esc(p.nombre) + '</strong>' +
                    '<span>' + esc(tituloEstado(p.categoria)) + ' · ' + esc(p.cantidad) + ' vendido(s)</span></div>' +
                    '<span class="valor">' + money(p.total) + '</span></div>';
            });
        } else {
            topHtml = sinDatos('Aun no hay productos vendidos hoy.');
        }

        let filas = '';
        if (d.ventas_recientes.length) {
            d.ventas_recientes.forEach(function (v) {
                filas += '<tr>' +
                    '<td class="celda-principal"><strong>' + esc(v.numero) + '</strong><span>' + esc(v.cliente) + '</span></td>' +
                    '<td>' + esc(tituloEstado(v.tipo_servicio)) + '</td>' +
                    '<td>' + fechaHora(v.fecha) + '</td>' +
                    '<td class="num">' + money(v.total) + '</td>' +
                    '<td>' + badge(v.estado) + '</td>' +
                    '<td><button class="btn btn-sm btn-secundario" data-accion="verVenta" data-id="' + v.id + '">Ver</button></td>' +
                    '</tr>';
            });
        } else {
            filas = mensajeVacioTabla(6, 'No hay ventas registradas.');
        }

        contenido.innerHTML =
            '<div class="pagina-encabezado"><div><h1>Hola, ' + esc(String(usuario.nombres).split(' ')[0]) + '</h1>' +
            '<p>Resumen operativo del ' + fecha(d.fecha) + '.</p></div>' +
            '<div class="acciones"><a class="btn btn-primario" href="#/nueva-venta">' + icono('nueva') + ' Nueva venta</a></div></div>' +

            '<div class="tarjetas-grid">' +
            '<div class="stat-card t-ambar"><span class="stat-label">Ventas de hoy</span><div class="stat-valor">' + esc(d.ventas_hoy.cantidad) + '</div><div class="stat-pista">Total ' + money(d.ventas_hoy.total) + '</div></div>' +
            '<div class="stat-card t-verde"><span class="stat-label">Ingresos cobrados</span><div class="stat-valor">' + money(d.ingresos_hoy) + '</div><div class="stat-pista">Pagos del dia</div></div>' +
            '<div class="stat-card ' + signoGanancia + '"><span class="stat-label">Ganancia del dia</span><div class="stat-valor">' + money(d.ganancia_hoy) + '</div><div class="stat-pista">Ingresos - gastos - personal</div></div>' +
            '<div class="stat-card t-rojo"><span class="stat-label">Egresos</span><div class="stat-valor">' + money(egresos) + '</div><div class="stat-pista">Gastos ' + money(d.gastos_hoy) + ' + personal ' + money(d.pagos_personal_hoy) + '</div></div>' +
            '<div class="stat-card t-azul"><span class="stat-label">Compras de hoy</span><div class="stat-valor">' + money(d.compras_hoy) + '</div><div class="stat-pista">Abastecimiento de insumos</div></div>' +
            '</div>' +

            '<div class="dos-columnas">' +
            '<div class="panel"><div class="panel-cabecera"><div><h3>Ingresos y egresos</h3><span>Ultimos 7 dias</span></div>' +
            '<div class="leyenda"><span><i class="punto punto-verde"></i>Ingresos</span><span><i class="punto punto-rojo"></i>Gastos</span><span><i class="punto punto-ambar"></i>Compras</span></div></div>' +
            '<div class="panel-cuerpo">' + grafico(d.serie) + '</div></div>' +

            '<div class="panel"><div class="panel-cabecera"><div><h3>Stock bajo</h3><span>Insumos por reabastecer</span></div>' +
            '<a class="btn btn-sm btn-secundario" href="#/inventario">Ver inventario</a></div>' +
            '<div class="panel-cuerpo">' + stockHtml + '</div></div>' +
            '</div>' +

            '<div class="dos-columnas">' +
            '<div class="panel"><div class="panel-cabecera"><div><h3>Ventas recientes</h3><span>Ultimos movimientos</span></div>' +
            '<a class="btn btn-sm btn-secundario" href="#/ventas">Ver todas</a></div>' +
            '<div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
            '<th>Venta</th><th>Servicio</th><th>Fecha</th><th class="th-derecha">Total</th><th>Estado</th><th></th>' +
            '</tr></thead><tbody>' + filas + '</tbody></table></div></div></div>' +

            '<div>' +
            '<div class="panel"><div class="panel-cabecera"><h3>Ingresos por medio</h3></div><div class="panel-cuerpo"><div class="lista-mini">' + mediosHtml + '</div></div></div>' +
            '<div class="panel"><div class="panel-cabecera"><h3>Top productos de hoy</h3></div><div class="panel-cuerpo"><div class="lista-mini">' + topHtml + '</div></div></div>' +
            '</div></div>';
    }

    /* ------------------------------------------------------------------ */
    /* VENTAS                                                              */
    /* ------------------------------------------------------------------ */
    function puedeCobrar() {
        return usuario.rol === 'administrador' || usuario.rol === 'cajero';
    }

    function accionesEstadoVenta(venta) {
        const estado = venta.estado;
        const lista = [];
        if (estado === 'registrada') lista.push({ estado: 'en_preparacion', texto: 'Iniciar preparacion', clase: 'btn-naranja' });
        if (estado === 'en_preparacion') lista.push({ estado: 'lista', texto: 'Marcar lista', clase: 'btn-secundario' });
        if (estado === 'lista' && puedeCobrar()) lista.push({ estado: 'entregada', texto: 'Entregar pedido', clase: 'btn-secundario' });
        if (estado === 'pagada') lista.push({ estado: 'cerrada', texto: 'Cerrar venta', clase: 'btn-exito' });
        if (estado === 'entregada' && num(venta.saldo) <= 0.001) lista.push({ estado: 'cerrada', texto: 'Cerrar venta', clase: 'btn-exito' });
        return lista;
    }

    async function verVenta(id) {
        const venta = await api('venta', { id: id });

        let lineas = '';
        venta.detalles.forEach(function (d) {
            lineas += '<div class="detalle-lista-item"><div><strong>' + esc(d.producto) + '</strong><br>' +
                '<span class="cant">' + esc(d.cantidad) + ' x ' + money(d.precio) + '</span></div>' +
                '<strong>' + money(d.subtotal) + '</strong></div>';
        });

        let pagos = '';
        if (venta.pagos.length) {
            venta.pagos.forEach(function (p) {
                pagos += '<div class="detalle-lista-item"><div><strong>' + esc(tituloEstado(p.medio)) + '</strong>' +
                    '<br><span class="cant">' + fechaHora(p.fecha) + (p.referencia ? ' · ' + esc(p.referencia) : '') + '</span></div>' +
                    '<strong>' + money(p.monto) + '</strong></div>';
            });
        } else {
            pagos = '<div class="detalle-lista-item"><span class="cant">Sin pagos registrados</span><strong>' + money(0) + '</strong></div>';
        }

        let botones = '';
        if (puedeCobrar() && num(venta.saldo) > 0.001 && venta.estado !== 'anulada' && venta.estado !== 'cerrada') {
            botones += '<button type="button" class="btn btn-primario" data-accion="pagarVenta" data-id="' + venta.id + '">Registrar pago</button>';
        }
        accionesEstadoVenta(venta).forEach(function (a) {
            if (usuario.rol === 'cocina' && ['en_preparacion', 'lista'].indexOf(a.estado) === -1) return;
            botones += '<button type="button" class="btn ' + a.clase + '" data-accion="estadoVenta" data-id="' + venta.id + '" data-estado="' + a.estado + '">' + esc(a.texto) + '</button>';
        });
        if (usuario.rol === 'administrador' && venta.estado !== 'anulada' && venta.estado !== 'cerrada') {
            botones += '<button type="button" class="btn btn-peligro" data-accion="anularVenta" data-id="' + venta.id + '">Anular</button>';
        }

        const html =
            '<div class="ficha-venta">' +
            '<div class="ficha-item"><span>Numero</span><strong>' + esc(venta.numero) + '</strong></div>' +
            '<div class="ficha-item"><span>Estado</span><strong>' + badge(venta.estado) + '</strong></div>' +
            '<div class="ficha-item"><span>Cliente</span><strong>' + esc(venta.cliente || 'Consumidor final') + '</strong></div>' +
            '<div class="ficha-item"><span>Fecha</span><strong>' + fechaHora(venta.fecha) + '</strong></div>' +
            '<div class="ficha-item"><span>Servicio</span><strong>' + esc(tituloEstado(venta.tipo_servicio)) + (venta.mesa ? ' · Mesa ' + esc(venta.mesa) : '') + '</strong></div>' +
            '<div class="ficha-item"><span>Atendido por</span><strong>' + esc(venta.usuario || '—') + '</strong></div>' +
            '</div>' +
            (venta.observacion ? '<div class="mensaje-info">' + esc(venta.observacion) + '</div>' : '') +
            '<h4 style="margin-bottom:6px">Detalle del pedido</h4>' +
            '<div class="detalle-lista">' + lineas + '</div>' +
            '<div class="total-linea"><span>Subtotal</span><strong>' + money(venta.subtotal) + '</strong></div>' +
            '<div class="total-linea"><span>Descuento</span><strong>' + money(venta.descuento) + '</strong></div>' +
            '<div class="total-linea"><span>Total</span><strong>' + money(venta.total) + '</strong></div>' +
            '<div class="total-linea"><span>Pagado</span><strong>' + money(venta.pagado) + '</strong></div>' +
            '<div class="total-linea"><span>Saldo</span><strong>' + money(venta.saldo) + '</strong></div>' +
            '<h4 style="margin:16px 0 6px">Pagos</h4><div class="detalle-lista">' + pagos + '</div>' +
            (botones ? '<div class="acciones" style="margin-top:18px;flex-wrap:wrap">' + botones + '</div>' : '');

        abrirModal('Venta ' + venta.numero, html, { ocultarGuardar: true, textoCancelar: 'Cerrar' });
    }

    async function abrirPagoVenta(id) {
        const venta = await api('venta', { id: id });
        const saldo = num(venta.saldo);
        if (saldo <= 0.001) {
            toast('La venta ya esta pagada en su totalidad.', 'info');
            return;
        }
        const html =
            '<div class="mensaje-info">Saldo pendiente: <strong>' + money(saldo) + '</strong></div>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Monto</span><input type="number" step="0.01" min="0.01" max="' + saldo + '" id="pago-monto" value="' + saldo.toFixed(2) + '" required></label>' +
            '<label class="campo"><span>Medio de pago</span><select id="pago-medio">' + opciones(['efectivo', 'yape', 'tarjeta'], 'efectivo') + '</select></label>' +
            '</div>' +
            '<label class="campo"><span>Referencia (opcional)</span><input type="text" id="pago-referencia" maxlength="60" placeholder="Nro. de operacion"></label>';

        abrirModal('Registrar pago · ' + venta.numero, html, {
            textoGuardar: 'Registrar pago',
            onSubmit: async function () {
                const monto = num(qs('#pago-monto').value);
                if (monto <= 0) throw new Error('Ingrese un monto mayor a cero.');
                if (monto > saldo + 0.001) throw new Error('El monto supera el saldo pendiente.');
                await api('venta_pago', {
                    venta_id: id,
                    monto: monto,
                    medio: qs('#pago-medio').value,
                    referencia: qs('#pago-referencia').value.trim()
                }, 'POST');
                cerrarModal();
                toast('Pago registrado correctamente.', 'exito');
                atenderRuta();
            }
        });
    }

    async function vistaVentas() {
        contenido.innerHTML = cargando();
        const ventas = await api('ventas');
        const puedeNueva = puedeCobrar();

        let filas = '';
        ventas.forEach(function (v) {
            const saldo = num(v.total) - num(v.pagado);
            filas += '<tr>' +
                '<td class="celda-principal"><strong>' + esc(v.numero) + '</strong><span>' + esc(v.cliente || 'Consumidor final') + '</span></td>' +
                '<td>' + fechaHora(v.fecha) + '</td>' +
                '<td>' + esc(tituloEstado(v.tipo_servicio)) + (v.mesa ? ' · ' + esc(v.mesa) : '') + '</td>' +
                '<td class="num">' + esc(v.items) + '</td>' +
                '<td class="num">' + money(v.total) + '<br><span style="font-size:11px;color:var(--texto-suave)">Pagado ' + money(v.pagado) + '</span></td>' +
                '<td>' + badge(v.estado) + (saldo > 0.001 && v.estado !== 'anulada' ? '<br><span style="font-size:11px;color:var(--rojo-600)">Saldo ' + money(saldo) + '</span>' : '') + '</td>' +
                '<td><div class="acciones-tabla"><button class="btn btn-sm btn-secundario" data-accion="verVenta" data-id="' + v.id + '">Ver</button>' +
                (puedeCobrar() && saldo > 0.001 && v.estado !== 'anulada' && v.estado !== 'cerrada' ? '<button class="btn btn-sm btn-primario" data-accion="pagarVenta" data-id="' + v.id + '">Cobrar</button>' : '') +
                '</div></td></tr>';
        });
        if (!ventas.length) filas = mensajeVacioTabla(7, 'No hay ventas registradas con los filtros actuales.');

        contenido.innerHTML =
            '<div class="pagina-encabezado"><div><h1>Ventas</h1><p>Historial de pedidos y control de estados.</p></div>' +
            (puedeNueva ? '<div class="acciones"><a class="btn btn-primario" href="#/nueva-venta">' + icono('nueva') + ' Nueva venta</a></div>' : '') + '</div>' +
            '<div class="barra-filtros">' +
            '<label class="campo medio"><span>Buscar</span><input type="search" id="ventas-q" placeholder="Numero o cliente"></label>' +
            '<label class="campo corto"><span>Estado</span><select id="ventas-estado">' + opciones([{ valor: '', texto: 'Todos' }, 'registrada', 'en_preparacion', 'lista', 'entregada', 'pagada', 'cerrada', 'anulada'], '') + '</select></label>' +
            '<label class="campo corto"><span>Desde</span><input type="date" id="ventas-desde"></label>' +
            '<label class="campo corto"><span>Hasta</span><input type="date" id="ventas-hasta"></label>' +
            '<button type="button" class="btn btn-secundario" id="btn-filtrar-ventas">Filtrar</button>' +
            '</div>' +
            '<div class="panel"><div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
            '<th>Venta</th><th>Fecha</th><th>Servicio</th><th class="th-derecha">Items</th><th class="th-derecha">Total</th><th>Estado</th><th>Acciones</th>' +
            '</tr></thead><tbody>' + filas + '</tbody></table></div></div></div>';

        async function cargar(filtros) {
            contenido.innerHTML = cargando();
            try {
                const lista = await api('ventas', filtros);
                let cuerpo = '';
                lista.forEach(function (v) {
                    const saldo = num(v.total) - num(v.pagado);
                    cuerpo += '<tr>' +
                        '<td class="celda-principal"><strong>' + esc(v.numero) + '</strong><span>' + esc(v.cliente || 'Consumidor final') + '</span></td>' +
                        '<td>' + fechaHora(v.fecha) + '</td>' +
                        '<td>' + esc(tituloEstado(v.tipo_servicio)) + (v.mesa ? ' · ' + esc(v.mesa) : '') + '</td>' +
                        '<td class="num">' + esc(v.items) + '</td>' +
                        '<td class="num">' + money(v.total) + '<br><span style="font-size:11px;color:var(--texto-suave)">Pagado ' + money(v.pagado) + '</span></td>' +
                        '<td>' + badge(v.estado) + (saldo > 0.001 && v.estado !== 'anulada' ? '<br><span style="font-size:11px;color:var(--rojo-600)">Saldo ' + money(saldo) + '</span>' : '') + '</td>' +
                        '<td><div class="acciones-tabla"><button class="btn btn-sm btn-secundario" data-accion="verVenta" data-id="' + v.id + '">Ver</button>' +
                        (puedeCobrar() && saldo > 0.001 && v.estado !== 'anulada' && v.estado !== 'cerrada' ? '<button class="btn btn-sm btn-primario" data-accion="pagarVenta" data-id="' + v.id + '">Cobrar</button>' : '') +
                        '</div></td></tr>';
                });
                if (!cuerpo) cuerpo = mensajeVacioTabla(7, 'No hay ventas para los filtros seleccionados.');
                contenido.innerHTML =
                    '<div class="pagina-encabezado"><div><h1>Ventas</h1><p>Historial de pedidos y control de estados.</p></div>' +
                    (puedeNueva ? '<div class="acciones"><a class="btn btn-primario" href="#/nueva-venta">' + icono('nueva') + ' Nueva venta</a></div>' : '') + '</div>' +
                    '<div class="barra-filtros">' +
                    '<label class="campo medio"><span>Buscar</span><input type="search" id="ventas-q" value="' + esc(filtros.q || '') + '" placeholder="Numero o cliente"></label>' +
                    '<label class="campo corto"><span>Estado</span><select id="ventas-estado">' + opciones([{ valor: '', texto: 'Todos' }, 'registrada', 'en_preparacion', 'lista', 'entregada', 'pagada', 'cerrada', 'anulada'], filtros.estado || '') + '</select></label>' +
                    '<label class="campo corto"><span>Desde</span><input type="date" id="ventas-desde" value="' + esc(filtros.desde || '') + '"></label>' +
                    '<label class="campo corto"><span>Hasta</span><input type="date" id="ventas-hasta" value="' + esc(filtros.hasta || '') + '"></label>' +
                    '<button type="button" class="btn btn-secundario" id="btn-filtrar-ventas">Filtrar</button>' +
                    '</div>' +
                    '<div class="panel"><div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
                    '<th>Venta</th><th>Fecha</th><th>Servicio</th><th class="th-derecha">Items</th><th class="th-derecha">Total</th><th>Estado</th><th>Acciones</th>' +
                    '</tr></thead><tbody>' + cuerpo + '</tbody></table></div></div></div>';
                vincularFiltros();
            } catch (error) {
                contenido.innerHTML = '<div class="panel"><div class="panel-cuerpo">' + sinDatos(error.message) + '</div></div>';
                toast(error.message, 'error');
            }
        }

        function vincularFiltros() {
            const boton = qs('#btn-filtrar-ventas');
            if (!boton) return;
            boton.addEventListener('click', function () {
                cargar({
                    q: qs('#ventas-q').value.trim(),
                    estado: qs('#ventas-estado').value,
                    desde: qs('#ventas-desde').value,
                    hasta: qs('#ventas-hasta').value
                });
            });
        }

        vincularFiltros();
    }

    /* ------------------------------------------------------------------ */
    /* NUEVA VENTA                                                         */
    /* ------------------------------------------------------------------ */
    let pagoManual = false;

    function uuid() {
        if (window.crypto && typeof window.crypto.randomUUID === 'function') return window.crypto.randomUUID();
        return 'idem-' + Date.now() + '-' + Math.random().toString(16).slice(2, 10);
    }

    function totalCarrito() {
        return carrito.reduce(function (acc, item) { return acc + num(item.precio) * num(item.cantidad); }, 0);
    }

    function renderCatalogo() {
        const contenedor = qs('#catalogo');
        if (!contenedor) return;
        const texto = (qs('#nv-buscar') ? qs('#nv-buscar').value : '').toLowerCase().trim();
        const categoria = qs('#nv-categoria') ? qs('#nv-categoria').value : '';
        const lista = productosCache.filter(function (p) {
            if (categoria && p.categoria !== categoria) return false;
            if (texto && String(p.nombre).toLowerCase().indexOf(texto) === -1) return false;
            return true;
        });
        if (!lista.length) {
            contenedor.innerHTML = sinDatos('No se encontraron productos.');
            return;
        }
        contenedor.innerHTML = lista.map(function (p) {
            const deshabilitado = p.estado !== 'disponible';
            return '<div class="producto-card' + (deshabilitado ? ' deshabilitado' : '') + '" ' +
                (deshabilitado ? '' : 'data-accion="agregarProducto" data-id="' + p.id + '"') + '>' +
                '<span class="prod-categoria">' + esc(tituloEstado(p.categoria)) + '</span>' +
                '<strong>' + esc(p.nombre) + '</strong>' +
                '<span class="prod-precio">' + money(p.precio) + '</span>' +
                (deshabilitado ? '<span style="font-size:11px;color:var(--rojo-600)">No disponible</span>' : '<span class="prod-agregar">+</span>') +
                '</div>';
        }).join('');
    }

    function renderCarrito() {
        const contenedor = qs('#carrito-contenido');
        if (!contenedor) return;
        const contador = qs('#carrito-contador');
        const unidades = carrito.reduce(function (acc, i) { return acc + num(i.cantidad); }, 0);
        if (contador) contador.textContent = unidades + ' item(s)';

        if (!carrito.length) {
            contenedor.innerHTML = '<div class="carrito-vacio">El pedido esta vacio.<br>Haga clic en un producto para agregarlo.</div>';
        } else {
            let filas = '';
            carrito.forEach(function (item) {
                filas += '<tr><td><strong>' + esc(item.nombre) + '</strong><br><span style="font-size:11px;color:var(--texto-suave)">' + money(item.precio) + '</span></td>' +
                    '<td class="cantidad-celda"><div class="carrito-cantidad">' +
                    '<button type="button" data-accion="menosProducto" data-id="' + item.producto_id + '">-</button>' +
                    '<span>' + esc(item.cantidad) + '</span>' +
                    '<button type="button" data-accion="masProducto" data-id="' + item.producto_id + '">+</button>' +
                    '</div><br><button type="button" class="btn btn-sm btn-peligro" data-accion="quitarProducto" data-id="' + item.producto_id + '">Quitar</button></td></tr>';
            });
            contenedor.innerHTML = '<table class="carrito-tabla"><tbody>' + filas + '</tbody></table>';
        }

        const subtotal = totalCarrito();
        const descuento = Math.min(num(qs('#nv-descuento') ? qs('#nv-descuento').value : 0), subtotal);
        const total = subtotal - descuento;
        const resumen = qs('#nv-resumen');
        if (resumen) {
            resumen.innerHTML = '<div class="resumen-linea"><span>Subtotal</span><span>' + money(subtotal) + '</span></div>' +
                '<div class="resumen-linea"><span>Descuento</span><span>' + money(descuento) + '</span></div>' +
                '<div class="resumen-linea total"><span>Total</span><span>' + money(total) + '</span></div>';
        }
        const monto = qs('#nv-monto');
        if (monto && !pagoManual) monto.value = total > 0 ? total.toFixed(2) : '';
    }

    function agregarProducto(id) {
        const producto = productosCache.find(function (p) { return String(p.id) === String(id); });
        if (!producto) return;
        if (producto.estado !== 'disponible') {
            toast('El producto no esta disponible.', 'error');
            return;
        }
        const existente = carrito.find(function (i) { return String(i.producto_id) === String(id); });
        if (existente) existente.cantidad += 1;
        else carrito.push({ producto_id: producto.id, nombre: producto.nombre, precio: num(producto.precio), cantidad: 1 });
        pagoManual = false;
        renderCarrito();
    }

    function cambiarCantidad(id, delta) {
        const item = carrito.find(function (i) { return String(i.producto_id) === String(id); });
        if (!item) return;
        item.cantidad += delta;
        if (item.cantidad <= 0) carrito = carrito.filter(function (i) { return String(i.producto_id) !== String(id); });
        pagoManual = false;
        renderCarrito();
    }

    async function vistaNuevaVenta() {
        contenido.innerHTML = cargando();
        const datos = await Promise.all([api('productos'), api('clientes')]);
        productosCache = datos[0];
        clientesCache = datos[1];
        carrito = [];
        pagoManual = false;

        const opcionesCliente = [{ valor: '', texto: 'Consumidor final' }].concat(clientesCache.map(function (c) {
            return { valor: c.id, texto: c.nombre };
        }));

        contenido.innerHTML =
            '<div class="pagina-encabezado"><div><h1>Nueva venta</h1><p>Seleccione los productos y registre el pedido.</p></div></div>' +
            '<div class="venta-layout">' +
            '<div>' +
            '<div class="barra-filtros">' +
            '<label class="campo"><span>Buscar producto</span><input type="search" id="nv-buscar" placeholder="Nombre del plato o bebida"></label>' +
            '<label class="campo corto"><span>Categoria</span><select id="nv-categoria">' + opciones([{ valor: '', texto: 'Todas' }, 'entradas', 'platos', 'bebidas', 'postres'], '') + '</select></label>' +
            '</div>' +
            '<div id="catalogo" class="catalogo-grid"></div>' +
            '</div>' +
            '<div class="carrito"><div class="panel">' +
            '<div class="panel-cabecera"><h3>Pedido actual</h3><span id="carrito-contador">0 item(s)</span></div>' +
            '<div class="panel-cuerpo">' +
            '<div id="carrito-contenido"></div>' +
            '<div id="nv-resumen" class="carrito-resumen"></div>' +
            '<div class="carrito-form">' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Servicio</span><select id="nv-tipo">' + opciones([{ valor: 'mesa', texto: 'Mesa' }, { valor: 'llevar', texto: 'Para llevar' }, { valor: 'delivery', texto: 'Delivery' }], 'mesa') + '</select></label>' +
            '<label class="campo"><span>Mesa</span><input type="text" id="nv-mesa" maxlength="10" placeholder="Ej. 4"></label>' +
            '</div>' +
            '<label class="campo"><span>Cliente</span><select id="nv-cliente">' + opciones(opcionesCliente, '') + '</select></label>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Descuento</span><input type="number" id="nv-descuento" min="0" step="0.01" value="0"></label>' +
            '<label class="campo"><span>Observacion</span><input type="text" id="nv-observacion" maxlength="255" placeholder="Sin cebolla, etc."></label>' +
            '</div>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Pago inicial</span><input type="number" id="nv-monto" min="0" step="0.01" placeholder="0.00"></label>' +
            '<label class="campo"><span>Medio</span><select id="nv-medio">' + opciones(['efectivo', 'yape', 'tarjeta'], 'efectivo') + '</select></label>' +
            '</div>' +
            '<button type="button" class="btn btn-primario btn-bloque" id="btn-registrar-venta">Registrar pedido</button>' +
            '</div></div></div></div></div>';

        renderCatalogo();
        renderCarrito();

        qs('#nv-buscar').addEventListener('input', renderCatalogo);
        qs('#nv-categoria').addEventListener('change', renderCatalogo);
        qs('#nv-descuento').addEventListener('input', function () { pagoManual = false; renderCarrito(); });
        qs('#nv-monto').addEventListener('input', function () { pagoManual = true; });
        qs('#nv-tipo').addEventListener('change', function () {
            const esMesa = qs('#nv-tipo').value === 'mesa';
            qs('#nv-mesa').disabled = !esMesa;
            if (!esMesa) qs('#nv-mesa').value = '';
        });

        qs('#btn-registrar-venta').addEventListener('click', async function () {
            if (!carrito.length) {
                toast('Agregue al menos un producto al pedido.', 'error');
                return;
            }
            const subtotal = totalCarrito();
            const descuento = num(qs('#nv-descuento').value);
            if (descuento < 0 || descuento > subtotal) {
                toast('El descuento no puede superar el subtotal.', 'error');
                return;
            }
            const total = subtotal - descuento;
            const monto = num(qs('#nv-monto').value);
            if (monto < 0 || monto > total + 0.001) {
                toast('El pago inicial no puede superar el total.', 'error');
                return;
            }

            const boton = qs('#btn-registrar-venta');
            boton.disabled = true;
            boton.textContent = 'Registrando...';

            const payload = {
                idempotencia: uuid(),
                tipo_servicio: qs('#nv-tipo').value,
                mesa: qs('#nv-mesa').value.trim(),
                cliente_id: qs('#nv-cliente').value,
                descuento: descuento,
                observacion: qs('#nv-observacion').value.trim(),
                items: carrito.map(function (i) { return { producto_id: i.producto_id, cantidad: i.cantidad }; })
            };
            if (monto > 0) {
                payload.pagos = [{ monto: monto, medio: qs('#nv-medio').value, referencia: '' }];
            }

            try {
                const venta = await api('venta_crear', payload, 'POST');
                toast('Venta ' + venta.numero + ' registrada correctamente.', 'exito');
                carrito = [];
                location.hash = '#/ventas';
            } catch (error) {
                toast(error.message, 'error');
                boton.disabled = false;
                boton.textContent = 'Registrar pedido';
            }
        });
    }

    ACCIONES.verVenta = function (el) { verVenta(el.dataset.id); };
    ACCIONES.pagarVenta = function (el) { abrirPagoVenta(el.dataset.id); };
    ACCIONES.estadoVenta = async function (el) {
        try {
            await api('venta_estado', { id: el.dataset.id, estado: el.dataset.estado }, 'POST');
            cerrarModal();
            toast('Estado de la venta actualizado.', 'exito');
            atenderRuta();
        } catch (error) { toast(error.message, 'error'); }
    };
    ACCIONES.anularVenta = async function (el) {
        if (!confirmar('¿Confirma que desea anular esta venta?')) return;
        try {
            await api('venta_anular', { id: el.dataset.id }, 'POST');
            cerrarModal();
            toast('Venta anulada.', 'exito');
            atenderRuta();
        } catch (error) { toast(error.message, 'error'); }
    };
    ACCIONES.agregarProducto = function (el) { agregarProducto(el.dataset.id); };
    ACCIONES.masProducto = function (el) { cambiarCantidad(el.dataset.id, 1); };
    ACCIONES.menosProducto = function (el) { cambiarCantidad(el.dataset.id, -1); };
    ACCIONES.quitarProducto = function (el) {
        carrito = carrito.filter(function (i) { return String(i.producto_id) !== String(el.dataset.id); });
        pagoManual = false;
        renderCarrito();
    };

    /* ------------------------------------------------------------------ */
    /* PRODUCTOS                                                           */
    /* ------------------------------------------------------------------ */
    function abrirProducto(producto) {
        const esEdicion = !!producto;
        const p = producto || { nombre: '', descripcion: '', categoria: 'platos', precio: '', estado: 'disponible' };
        const html =
            '<div class="grid-2">' +
            '<label class="campo"><span>Nombre</span><input type="text" id="prod-nombre" maxlength="120" value="' + esc(p.nombre) + '" required></label>' +
            '<label class="campo"><span>Categoria</span><select id="prod-cat">' + opciones(['entradas', 'platos', 'bebidas', 'postres'], p.categoria) + '</select></label>' +
            '</div>' +
            '<label class="campo"><span>Descripcion</span><input type="text" id="prod-desc" maxlength="255" value="' + esc(p.descripcion) + '" placeholder="Ingredientes o presentacion"></label>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Precio (S/)</span><input type="number" id="prod-precio" min="0.01" step="0.01" value="' + esc(p.precio) + '" required></label>' +
            '<label class="campo"><span>Estado</span><select id="prod-estado">' + opciones(['disponible', 'agotado', 'inactivo'], p.estado) + '</select></label>' +
            '</div>';

        abrirModal(esEdicion ? 'Editar producto' : 'Nuevo producto', html, {
            onSubmit: async function () {
                const nombre = qs('#prod-nombre').value.trim();
                const precio = num(qs('#prod-precio').value);
                if (!nombre) throw new Error('Ingrese el nombre del producto.');
                if (precio <= 0) throw new Error('Ingrese un precio mayor a cero.');
                await api('producto_guardar', {
                    id: esEdicion ? p.id : '',
                    nombre: nombre,
                    descripcion: qs('#prod-desc').value.trim(),
                    categoria: qs('#prod-cat').value,
                    precio: precio,
                    estado: qs('#prod-estado').value
                }, 'POST');
                cerrarModal();
                toast('Producto guardado correctamente.', 'exito');
                atenderRuta();
            }
        });
    }

    async function vistaProductos() {
        contenido.innerHTML = cargando();
        const lista = await api('productos');
        const manage = usuario.rol === 'administrador' || usuario.rol === 'inventario';

        function filas(filtrada) {
            if (!filtrada.length) return mensajeVacioTabla(5, 'No se encontraron productos.');
            return filtrada.map(function (p) {
                let acciones = '<button class="btn btn-sm btn-secundario" data-accion="verProducto" data-id="' + p.id + '">Ver</button>';
                if (manage) {
                    acciones += '<button class="btn btn-sm btn-secundario" data-accion="editarProducto" data-id="' + p.id + '">Editar</button>';
                    acciones += '<button class="btn btn-sm ' + (p.estado === 'disponible' ? 'btn-peligro' : 'btn-exito') + '" data-accion="estadoProducto" data-id="' + p.id + '" data-estado="' + (p.estado === 'disponible' ? 'inactivo' : 'disponible') + '">' + (p.estado === 'disponible' ? 'Desactivar' : 'Activar') + '</button>';
                    acciones += '<button class="btn btn-sm btn-peligro" data-accion="eliminarProducto" data-id="' + p.id + '">Eliminar</button>';
                }
                return '<tr>' +
                    '<td class="celda-principal"><strong>' + esc(p.nombre) + '</strong><span>' + esc(p.descripcion || 'Sin descripcion') + '</span></td>' +
                    '<td>' + esc(tituloEstado(p.categoria)) + '</td>' +
                    '<td class="num">' + money(p.precio) + '</td>' +
                    '<td>' + badge(p.estado) + '</td>' +
                    '<td><div class="acciones-tabla">' + acciones + '</div></td></tr>';
            }).join('');
        }

        contenido.innerHTML =
            '<div class="pagina-encabezado"><div><h1>Productos</h1><p>Carta del restaurante y disponibilidad.</p></div>' +
            (manage ? '<div class="acciones"><button type="button" class="btn btn-primario" id="btn-nuevo-producto">' + icono('nueva') + ' Nuevo producto</button></div>' : '') + '</div>' +
            '<div class="barra-filtros">' +
            '<label class="campo"><span>Buscar</span><input type="search" id="prod-buscar" placeholder="Nombre del producto"></label>' +
            '<label class="campo corto"><span>Categoria</span><select id="prod-filtro-cat">' + opciones([{ valor: '', texto: 'Todas' }, 'entradas', 'platos', 'bebidas', 'postres'], '') + '</select></label>' +
            '<label class="campo corto"><span>Estado</span><select id="prod-filtro-estado">' + opciones([{ valor: '', texto: 'Todos' }, 'disponible', 'agotado', 'inactivo'], '') + '</select></label>' +
            '</div>' +
            '<div class="panel"><div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
            '<th>Producto</th><th>Categoria</th><th class="th-derecha">Precio</th><th>Estado</th><th>Acciones</th>' +
            '</tr></thead><tbody id="tbody-productos">' + filas(lista) + '</tbody></table></div></div></div>';

        function filtrar() {
            const q = qs('#prod-buscar').value.toLowerCase().trim();
            const cat = qs('#prod-filtro-cat').value;
            const est = qs('#prod-filtro-estado').value;
            const filtrada = lista.filter(function (p) {
                if (cat && p.categoria !== cat) return false;
                if (est && p.estado !== est) return false;
                if (q) {
                    const texto = (String(p.nombre) + ' ' + String(p.descripcion || '')).toLowerCase();
                    if (texto.indexOf(q) === -1) return false;
                }
                return true;
            });
            qs('#tbody-productos').innerHTML = filas(filtrada);
        }

        qs('#prod-buscar').addEventListener('input', filtrar);
        qs('#prod-filtro-cat').addEventListener('change', filtrar);
        qs('#prod-filtro-estado').addEventListener('change', filtrar);
        if (manage) {
            qs('#btn-nuevo-producto').addEventListener('click', function () { abrirProducto(null); });
        }

        ACCIONES.__productos = lista;
    }

    ACCIONES.verProducto = function (el) {
        const lista = ACCIONES.__productos || [];
        const p = lista.find(function (x) { return String(x.id) === String(el.dataset.id); });
        if (!p) return;
        abrirModal(p.nombre,
            '<div class="ficha-venta">' +
            '<div class="ficha-item"><span>Categoria</span><strong>' + esc(tituloEstado(p.categoria)) + '</strong></div>' +
            '<div class="ficha-item"><span>Precio</span><strong>' + money(p.precio) + '</strong></div>' +
            '<div class="ficha-item"><span>Estado</span><strong>' + badge(p.estado) + '</strong></div>' +
            '</div><p style="color:var(--texto-suave)">' + esc(p.descripcion || 'Sin descripcion registrada.') + '</p>',
            { ocultarGuardar: true, textoCancelar: 'Cerrar' });
    };

    ACCIONES.editarProducto = function (el) {
        const lista = ACCIONES.__productos || [];
        const p = lista.find(function (x) { return String(x.id) === String(el.dataset.id); });
        if (p) abrirProducto(p);
    };

    ACCIONES.estadoProducto = async function (el) {
        try {
            await api('producto_estado', { id: el.dataset.id, estado: el.dataset.estado }, 'POST');
            toast('Estado del producto actualizado.', 'exito');
            atenderRuta();
        } catch (error) { toast(error.message, 'error'); }
    };

    ACCIONES.eliminarProducto = async function (el) {
        if (!confirmar('¿Confirma que desea eliminar este producto?')) return;
        try {
            await api('producto_eliminar', { id: el.dataset.id }, 'POST');
            toast('Producto eliminado.', 'exito');
            atenderRuta();
        } catch (error) { toast(error.message, 'error'); }
    };

    /* ------------------------------------------------------------------ */
    /* CLIENTES                                                            */
    /* ------------------------------------------------------------------ */
    function abrirCliente(cliente) {
        const esEdicion = !!cliente;
        const c = cliente || { nombre: '', documento: '', telefono: '', direccion: '' };
        const html =
            '<label class="campo"><span>Nombre completo</span><input type="text" id="cli-nombre" maxlength="120" value="' + esc(c.nombre) + '" required></label>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Documento</span><input type="text" id="cli-documento" maxlength="20" value="' + esc(c.documento) + '" placeholder="DNI / RUC"></label>' +
            '<label class="campo"><span>Telefono</span><input type="text" id="cli-telefono" maxlength="20" value="' + esc(c.telefono) + '"></label>' +
            '</div>' +
            '<label class="campo"><span>Direccion</span><input type="text" id="cli-direccion" maxlength="150" value="' + esc(c.direccion) + '"></label>';

        abrirModal(esEdicion ? 'Editar cliente' : 'Nuevo cliente', html, {
            onSubmit: async function () {
                const nombre = qs('#cli-nombre').value.trim();
                if (!nombre) throw new Error('Ingrese el nombre del cliente.');
                await api('cliente_guardar', {
                    id: esEdicion ? c.id : '',
                    nombre: nombre,
                    documento: qs('#cli-documento').value.trim(),
                    telefono: qs('#cli-telefono').value.trim(),
                    direccion: qs('#cli-direccion').value.trim()
                }, 'POST');
                cerrarModal();
                toast('Cliente guardado correctamente.', 'exito');
                atenderRuta();
            }
        });
    }

    async function vistaClientes() {
        contenido.innerHTML = cargando();
        const lista = await api('clientes');

        function filas(filtrada) {
            if (!filtrada.length) return mensajeVacioTabla(6, 'No se encontraron clientes.');
            return filtrada.map(function (c) {
                return '<tr>' +
                    '<td class="celda-principal"><strong>' + esc(c.nombre) + '</strong><span>' + esc(c.documento || 'Sin documento') + '</span></td>' +
                    '<td>' + esc(c.telefono || '—') + '</td>' +
                    '<td>' + esc(c.direccion || '—') + '</td>' +
                    '<td class="num">' + esc(c.total_ventas) + '</td>' +
                    '<td class="num">' + money(c.consumo_total) + '</td>' +
                    '<td><div class="acciones-tabla">' +
                    '<button class="btn btn-sm btn-secundario" data-accion="editarCliente" data-id="' + c.id + '">Editar</button>' +
                    '<button class="btn btn-sm btn-peligro" data-accion="eliminarCliente" data-id="' + c.id + '">Eliminar</button>' +
                    '</div></td></tr>';
            }).join('');
        }

        contenido.innerHTML =
            '<div class="pagina-encabezado"><div><h1>Clientes</h1><p>Registro e historial de consumo.</p></div>' +
            '<div class="acciones"><button type="button" class="btn btn-primario" id="btn-nuevo-cliente">' + icono('nueva') + ' Nuevo cliente</button></div></div>' +
            '<div class="barra-filtros"><label class="campo"><span>Buscar</span><input type="search" id="cli-buscar" placeholder="Nombre, documento o telefono"></label></div>' +
            '<div class="panel"><div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
            '<th>Cliente</th><th>Telefono</th><th>Direccion</th><th class="th-derecha">Ventas</th><th class="th-derecha">Consumo</th><th>Acciones</th>' +
            '</tr></thead><tbody id="tbody-clientes">' + filas(lista) + '</tbody></table></div></div></div>';

        qs('#cli-buscar').addEventListener('input', function () {
            const q = qs('#cli-buscar').value.toLowerCase().trim();
            const filtrada = lista.filter(function (c) {
                if (!q) return true;
                return (String(c.nombre) + ' ' + String(c.documento || '') + ' ' + String(c.telefono || '')).toLowerCase().indexOf(q) !== -1;
            });
            qs('#tbody-clientes').innerHTML = filas(filtrada);
        });
        qs('#btn-nuevo-cliente').addEventListener('click', function () { abrirCliente(null); });

        ACCIONES.__clientes = lista;
    }

    ACCIONES.editarCliente = function (el) {
        const lista = ACCIONES.__clientes || [];
        const c = lista.find(function (x) { return String(x.id) === String(el.dataset.id); });
        if (c) abrirCliente(c);
    };

    ACCIONES.eliminarCliente = async function (el) {
        if (!confirmar('¿Confirma que desea eliminar este cliente? Sus ventas quedaran como consumidor final.')) return;
        try {
            await api('cliente_eliminar', { id: el.dataset.id }, 'POST');
            toast('Cliente eliminado.', 'exito');
            atenderRuta();
        } catch (error) { toast(error.message, 'error'); }
    };

    /* ------------------------------------------------------------------ */
    /* INVENTARIO                                                          */
    /* ------------------------------------------------------------------ */
    function cant(valor) {
        const n = num(valor);
        return Number.isInteger(n) ? String(n) : n.toFixed(2).replace(/0+$/, '').replace(/\.$/, '');
    }

    function abrirInsumo(insumo) {
        const esEdicion = !!insumo;
        const i = insumo || { nombre: '', unidad: 'unidad', costo: '', stock_minimo: '', stock_actual: '' };
        const html =
            '<div class="grid-2">' +
            '<label class="campo"><span>Nombre del insumo</span><input type="text" id="ins-nombre" maxlength="120" value="' + esc(i.nombre) + '" required></label>' +
            '<label class="campo"><span>Unidad</span><input type="text" id="ins-unidad" maxlength="20" value="' + esc(i.unidad) + '" placeholder="kg, lt, unidad" required></label>' +
            '</div>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Costo unitario (S/)</span><input type="number" id="ins-costo" min="0" step="0.01" value="' + esc(i.costo) + '"></label>' +
            '<label class="campo"><span>Stock minimo</span><input type="number" id="ins-minimo" min="0" step="0.001" value="' + esc(i.stock_minimo) + '"></label>' +
            '</div>' +
            (esEdicion
                ? '<div class="mensaje-info">El stock actual (' + esc(cant(i.stock_actual)) + ' ' + esc(i.unidad) + ') solo cambia registrando entradas o salidas de inventario.</div>'
                : '<label class="campo"><span>Stock inicial</span><input type="number" id="ins-inicial" min="0" step="0.001" value="0"></label>');

        abrirModal(esEdicion ? 'Editar insumo' : 'Nuevo insumo', html, {
            onSubmit: async function () {
                const nombre = qs('#ins-nombre').value.trim();
                if (!nombre) throw new Error('Ingrese el nombre del insumo.');
                const payload = {
                    id: esEdicion ? i.id : '',
                    nombre: nombre,
                    unidad: qs('#ins-unidad').value.trim(),
                    costo: num(qs('#ins-costo').value),
                    stock_minimo: num(qs('#ins-minimo').value)
                };
                if (!esEdicion) payload.stock_actual = num(qs('#ins-inicial').value);
                await api('insumo_guardar', payload, 'POST');
                cerrarModal();
                toast('Insumo guardado correctamente.', 'exito');
                atenderRuta();
            }
        });
    }

    function abrirMovimiento(insumo) {
        const lista = insumosCache || [];
        const opcionesInsumos = lista.map(function (i) {
            return { valor: i.id, texto: i.nombre + ' (' + cant(i.stock_actual) + ' ' + i.unidad + ')' };
        });
        const html =
            '<label class="campo"><span>Insumo</span><select id="mov-insumo">' + opciones(opcionesInsumos, insumo ? insumo.id : '') + '</select></label>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Tipo de movimiento</span><select id="mov-tipo">' + opciones([{ valor: 'entrada', texto: 'Entrada' }, { valor: 'salida', texto: 'Salida' }], 'entrada') + '</select></label>' +
            '<label class="campo"><span>Cantidad</span><input type="number" id="mov-cantidad" min="0.001" step="0.001" required></label>' +
            '</div>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Costo unitario (S/)</span><input type="number" id="mov-costo" min="0" step="0.01" value="' + esc(insumo ? insumo.costo : '') + '"></label>' +
            '<label class="campo"><span>Motivo</span><input type="text" id="mov-motivo" maxlength="150" placeholder="Compra, merma, consumo"></label>' +
            '</div>';

        abrirModal('Registrar movimiento de inventario', html, {
            onSubmit: async function () {
                const cantidad = num(qs('#mov-cantidad').value);
                if (cantidad <= 0) throw new Error('Ingrese una cantidad mayor a cero.');
                await api('movimiento_crear', {
                    insumo_id: qs('#mov-insumo').value,
                    tipo: qs('#mov-tipo').value,
                    cantidad: cantidad,
                    costo_unitario: num(qs('#mov-costo').value),
                    motivo: qs('#mov-motivo').value.trim()
                }, 'POST');
                cerrarModal();
                toast('Movimiento registrado. El stock fue actualizado.', 'exito');
                atenderRuta();
            }
        });
    }

    async function vistaInventario() {
        contenido.innerHTML = cargando();
        const manage = usuario.rol === 'administrador' || usuario.rol === 'inventario';
        const datos = await Promise.all([api('insumos'), manage ? api('movimientos') : Promise.resolve([])]);
        const insumos = datos[0];
        const movimientos = datos[1];
        insumosCache = insumos;

        function filas(filtrada) {
            if (!filtrada.length) return mensajeVacioTabla(6, 'No se encontraron insumos.');
            return filtrada.map(function (i) {
                const bajo = num(i.stock_actual) <= num(i.stock_minimo);
                let acciones = '';
                if (manage) {
                    acciones = '<div class="acciones-tabla">' +
                        '<button class="btn btn-sm btn-secundario" data-accion="editarInsumo" data-id="' + i.id + '">Editar</button>' +
                        '<button class="btn btn-sm btn-primario" data-accion="movimientoInsumo" data-id="' + i.id + '">Movimiento</button>' +
                        '<button class="btn btn-sm btn-peligro" data-accion="eliminarInsumo" data-id="' + i.id + '">Eliminar</button>' +
                        '</div>';
                }
                return '<tr>' +
                    '<td class="celda-principal"><strong>' + esc(i.nombre) + '</strong><span>Unidad: ' + esc(i.unidad) + '</span></td>' +
                    '<td class="num">' + money(i.costo) + '</td>' +
                    '<td class="num"><strong>' + cant(i.stock_actual) + '</strong> ' + esc(i.unidad) + '</td>' +
                    '<td class="num">' + cant(i.stock_minimo) + ' ' + esc(i.unidad) + '</td>' +
                    '<td>' + (bajo ? badge('agotado', 'Stock bajo') : badge('disponible', 'Suficiente')) + '</td>' +
                    '<td>' + (acciones || '—') + '</td></tr>';
            }).join('');
        }

        let filasMov = '';
        if (manage) {
            if (movimientos.length) {
                movimientos.slice(0, 40).forEach(function (m) {
                    filasMov += '<tr>' +
                        '<td>' + fechaHora(m.fecha) + '</td>' +
                        '<td class="celda-principal"><strong>' + esc(m.insumo) + '</strong><span>' + esc(m.usuario || 'Sistema') + '</span></td>' +
                        '<td>' + badge(m.tipo) + '</td>' +
                        '<td class="num">' + cant(m.cantidad) + ' ' + esc(m.unidad) + '</td>' +
                        '<td class="num">' + money(m.costo_unitario) + '</td>' +
                        '<td>' + esc(m.motivo || '—') + '</td></tr>';
                });
            } else {
                filasMov = mensajeVacioTabla(6, 'No hay movimientos registrados.');
            }
        }

        contenido.innerHTML =
            '<div class="pagina-encabezado"><div><h1>Inventario</h1><p>Control de stock de insumos y movimientos.</p></div>' +
            (manage ? '<div class="acciones"><button type="button" class="btn btn-primario" id="btn-nuevo-insumo">' + icono('nueva') + ' Nuevo insumo</button></div>' : '') + '</div>' +
            '<div class="barra-filtros"><label class="campo"><span>Buscar insumo</span><input type="search" id="ins-buscar" placeholder="Nombre del insumo"></label></div>' +
            '<div class="panel"><div class="panel-cabecera"><div><h3>Insumos</h3><span>Stock actual y minimo</span></div></div>' +
            '<div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
            '<th>Insumo</th><th class="th-derecha">Costo</th><th class="th-derecha">Stock actual</th><th class="th-derecha">Minimo</th><th>Estado</th><th>Acciones</th>' +
            '</tr></thead><tbody id="tbody-insumos">' + filas(insumos) + '</tbody></table></div></div></div>' +
            (manage ? '<div class="panel"><div class="panel-cabecera"><div><h3>Movimientos recientes</h3><span>Entradas y salidas de almacen</span></div></div>' +
                '<div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
                '<th>Fecha</th><th>Insumo</th><th>Tipo</th><th class="th-derecha">Cantidad</th><th class="th-derecha">Costo unit.</th><th>Motivo</th>' +
                '</tr></thead><tbody>' + filasMov + '</tbody></table></div></div></div>' : '');

        qs('#ins-buscar').addEventListener('input', function () {
            const q = qs('#ins-buscar').value.toLowerCase().trim();
            const filtrada = insumos.filter(function (i) {
                return !q || String(i.nombre).toLowerCase().indexOf(q) !== -1;
            });
            qs('#tbody-insumos').innerHTML = filas(filtrada);
        });
        if (manage) {
            qs('#btn-nuevo-insumo').addEventListener('click', function () { abrirInsumo(null); });
        }
        ACCIONES.__insumos = insumos;
    }

    ACCIONES.editarInsumo = function (el) {
        const lista = ACCIONES.__insumos || [];
        const i = lista.find(function (x) { return String(x.id) === String(el.dataset.id); });
        if (i) abrirInsumo(i);
    };

    ACCIONES.movimientoInsumo = function (el) {
        const lista = ACCIONES.__insumos || [];
        const i = lista.find(function (x) { return String(x.id) === String(el.dataset.id); });
        abrirMovimiento(i || null);
    };

    ACCIONES.eliminarInsumo = async function (el) {
        if (!confirmar('¿Confirma que desea eliminar este insumo? Tambien se borrara su historial de movimientos.')) return;
        try {
            await api('insumo_eliminar', { id: el.dataset.id }, 'POST');
            toast('Insumo eliminado.', 'exito');
            atenderRuta();
        } catch (error) { toast(error.message, 'error'); }
    };

    /* ------------------------------------------------------------------ */
    /* COMPRAS                                                             */
    /* ------------------------------------------------------------------ */
    function abrirCompra() {
        const opcionesInsumos = insumosCache.map(function (i) {
            return { valor: i.id, texto: i.nombre + ' (' + i.unidad + ')' };
        });
        if (!opcionesInsumos.length) {
            toast('Primero registre al menos un insumo.', 'error');
            return;
        }

        const html =
            '<div class="grid-3">' +
            '<label class="campo"><span>Fecha</span><input type="date" id="compra-fecha" value="' + hoyISO() + '" required></label>' +
            '<label class="campo"><span>Proveedor</span><input type="text" id="compra-proveedor" maxlength="120" required></label>' +
            '<label class="campo"><span>Documento</span><input type="text" id="compra-documento" maxlength="50" placeholder="Factura / boleta"></label>' +
            '</div>' +
            '<h4 style="margin:8px 0">Insumos comprados</h4>' +
            '<div id="compra-lineas"></div>' +
            '<button type="button" class="btn btn-sm btn-secundario" id="btn-add-linea">+ Agregar insumo</button>' +
            '<div class="total-linea" style="margin-top:12px"><span>Total de la compra</span><strong id="compra-total">' + money(0) + '</strong></div>';

        function crearLinea() {
            if (!opcionesInsumos.length) return;
            const div = document.createElement('div');
            div.className = 'grid-3 compra-linea';
            div.style.alignItems = 'end';
            div.style.marginBottom = '8px';
            div.innerHTML =
                '<label class="campo"><span>Insumo</span><select class="compra-insumo">' + opciones(opcionesInsumos, opcionesInsumos[0].valor) + '</select></label>' +
                '<label class="campo"><span>Cantidad</span><input type="number" class="compra-cantidad" min="0.001" step="0.001" value="1"></label>' +
                '<label class="campo"><span>Costo unit. (S/)</span><div style="display:flex;gap:6px"><input type="number" class="compra-costo" min="0" step="0.01" value="0" style="flex:1"><button type="button" class="btn btn-sm btn-peligro btn-quitar-linea" title="Quitar">X</button></div></label>';
            return div;
        }

        function recalcular() {
            let total = 0;
            qsa('.compra-linea').forEach(function (linea) {
                total += num(linea.querySelector('.compra-cantidad').value) * num(linea.querySelector('.compra-costo').value);
            });
            qs('#compra-total').textContent = money(total);
        }

        abrirModal('Registrar compra de insumos', html, {
            ancho: true,
            textoGuardar: 'Registrar compra',
            onSubmit: async function () {
                const proveedor = qs('#compra-proveedor').value.trim();
                if (!proveedor) throw new Error('Ingrese el proveedor.');
                const items = [];
                qsa('.compra-linea').forEach(function (linea) {
                    const cantidad = num(linea.querySelector('.compra-cantidad').value);
                    const costo = num(linea.querySelector('.compra-costo').value);
                    if (cantidad > 0) {
                        items.push({
                            insumo_id: linea.querySelector('.compra-insumo').value,
                            cantidad: cantidad,
                            costo_unitario: costo
                        });
                    }
                });
                if (!items.length) throw new Error('Agregue al menos un insumo con cantidad mayor a cero.');
                await api('compra_crear', {
                    fecha: qs('#compra-fecha').value,
                    proveedor: proveedor,
                    documento: qs('#compra-documento').value.trim(),
                    items: items
                }, 'POST');
                cerrarModal();
                toast('Compra registrada. El stock fue actualizado.', 'exito');
                atenderRuta();
            }
        });

        const contenedor = qs('#compra-lineas');
        contenedor.appendChild(crearLinea());
        qs('#btn-add-linea').addEventListener('click', function () {
            contenedor.appendChild(crearLinea());
        });
        contenedor.addEventListener('click', function (e) {
            const boton = e.target.closest('.btn-quitar-linea');
            if (!boton) return;
            if (qsa('.compra-linea').length === 1) {
                toast('Debe existir al menos una linea.', 'info');
                return;
            }
            boton.closest('.compra-linea').remove();
            recalcular();
        });
        contenedor.addEventListener('input', recalcular);
    }

    async function verCompra(id) {
        const compra = await api('compra', { id: id });
        let lineas = '';
        compra.detalles.forEach(function (d) {
            lineas += '<div class="detalle-lista-item"><div><strong>' + esc(d.insumo) + '</strong><br>' +
                '<span class="cant">' + cant(d.cantidad) + ' ' + esc(d.unidad) + ' x ' + money(d.costo_unitario) + '</span></div>' +
                '<strong>' + money(d.subtotal) + '</strong></div>';
        });
        const html =
            '<div class="ficha-venta">' +
            '<div class="ficha-item"><span>Fecha</span><strong>' + fecha(compra.fecha) + '</strong></div>' +
            '<div class="ficha-item"><span>Proveedor</span><strong>' + esc(compra.proveedor) + '</strong></div>' +
            '<div class="ficha-item"><span>Documento</span><strong>' + esc(compra.documento || '—') + '</strong></div>' +
            '</div>' +
            '<h4 style="margin-bottom:6px">Detalle</h4><div class="detalle-lista">' + lineas + '</div>' +
            '<div class="total-linea"><span>Total</span><strong>' + money(compra.total) + '</strong></div>' +
            '<p style="font-size:12px;color:var(--texto-suave);margin-top:10px">Registrado por ' + esc(compra.usuario || '—') + '</p>';
        abrirModal('Compra #' + compra.id, html, { ocultarGuardar: true, textoCancelar: 'Cerrar' });
    }

    async function vistaCompras() {
        contenido.innerHTML = cargando();
        const datos = await Promise.all([api('compras'), api('insumos')]);
        const compras = datos[0];
        insumosCache = datos[1];

        let filas = '';
        compras.forEach(function (c) {
            filas += '<tr>' +
                '<td>' + fecha(c.fecha) + '</td>' +
                '<td class="celda-principal"><strong>' + esc(c.proveedor) + '</strong><span>' + esc(c.documento || 'Sin documento') + '</span></td>' +
                '<td class="num">' + esc(c.items) + '</td>' +
                '<td class="num">' + money(c.total) + '</td>' +
                '<td>' + esc(c.usuario || '—') + '</td>' +
                '<td><button class="btn btn-sm btn-secundario" data-accion="verCompra" data-id="' + c.id + '">Ver</button></td></tr>';
        });
        if (!compras.length) filas = mensajeVacioTabla(6, 'No hay compras registradas.');

        contenido.innerHTML =
            '<div class="pagina-encabezado"><div><h1>Compras</h1><p>Abastecimiento de insumos al almacen.</p></div>' +
            '<div class="acciones"><button type="button" class="btn btn-primario" id="btn-nueva-compra">' + icono('nueva') + ' Nueva compra</button></div></div>' +
            '<div class="panel"><div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
            '<th>Fecha</th><th>Proveedor</th><th class="th-derecha">Items</th><th class="th-derecha">Total</th><th>Usuario</th><th></th>' +
            '</tr></thead><tbody>' + filas + '</tbody></table></div></div></div>';

        qs('#btn-nueva-compra').addEventListener('click', abrirCompra);
    }

    ACCIONES.verCompra = function (el) { verCompra(el.dataset.id); };

    /* ------------------------------------------------------------------ */
    /* GASTOS                                                              */
    /* ------------------------------------------------------------------ */
    function abrirGasto(gasto) {
        const esEdicion = !!gasto;
        const g = gasto || { fecha: hoyISO(), concepto: '', categoria: 'operativo', monto: '', comprobante: '' };
        const html =
            '<div class="grid-2">' +
            '<label class="campo"><span>Fecha</span><input type="date" id="gas-fecha" value="' + esc(String(g.fecha).substring(0, 10)) + '" required></label>' +
            '<label class="campo"><span>Monto (S/)</span><input type="number" id="gas-monto" min="0.01" step="0.01" value="' + esc(g.monto) + '" required></label>' +
            '</div>' +
            '<label class="campo"><span>Concepto</span><input type="text" id="gas-concepto" maxlength="150" value="' + esc(g.concepto) + '" required></label>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Categoria</span><input type="text" id="gas-categoria" list="lista-categorias-gasto" maxlength="60" value="' + esc(g.categoria) + '" required>' +
            '<datalist id="lista-categorias-gasto"><option value="operativo"><option value="insumos"><option value="servicios"><option value="mantenimiento"><option value="personal"><option value="otros"></datalist></label>' +
            '<label class="campo"><span>Comprobante</span><input type="text" id="gas-comprobante" maxlength="60" value="' + esc(g.comprobante) + '" placeholder="Boleta / factura"></label>' +
            '</div>';

        abrirModal(esEdicion ? 'Editar gasto' : 'Nuevo gasto', html, {
            onSubmit: async function () {
                const concepto = qs('#gas-concepto').value.trim();
                const monto = num(qs('#gas-monto').value);
                if (!concepto) throw new Error('Ingrese el concepto del gasto.');
                if (monto <= 0) throw new Error('Ingrese un monto mayor a cero.');
                await api('gasto_guardar', {
                    id: esEdicion ? g.id : '',
                    fecha: qs('#gas-fecha').value,
                    concepto: concepto,
                    categoria: qs('#gas-categoria').value.trim() || 'operativo',
                    monto: monto,
                    comprobante: qs('#gas-comprobante').value.trim()
                }, 'POST');
                cerrarModal();
                toast('Gasto guardado correctamente.', 'exito');
                atenderRuta();
            }
        });
    }

    async function vistaGastos() {
        contenido.innerHTML = cargando();
        const lista = await api('gastos');

        function filtrar() {
            const q = qs('#gas-buscar').value.toLowerCase().trim();
            const cat = qs('#gas-categoria').value;
            const desde = qs('#gas-desde').value;
            const hasta = qs('#gas-hasta').value;
            let total = 0;
            let filas = '';
            const filtrada = lista.filter(function (g) {
                if (q && (String(g.concepto) + ' ' + String(g.comprobante || '')).toLowerCase().indexOf(q) === -1) return false;
                if (cat && g.categoria !== cat) return false;
                const f = String(g.fecha).substring(0, 10);
                if (desde && f < desde) return false;
                if (hasta && f > hasta) return false;
                return true;
            });
            filtrada.forEach(function (g) {
                total += num(g.monto);
                filas += '<tr>' +
                    '<td>' + fecha(g.fecha) + '</td>' +
                    '<td class="celda-principal"><strong>' + esc(g.concepto) + '</strong><span>' + esc(g.comprobante || 'Sin comprobante') + '</span></td>' +
                    '<td>' + esc(tituloEstado(g.categoria)) + '</td>' +
                    '<td class="num">' + money(g.monto) + '</td>' +
                    '<td>' + esc(g.usuario || '—') + '</td>' +
                    '<td><div class="acciones-tabla">' +
                    '<button class="btn btn-sm btn-secundario" data-accion="editarGasto" data-id="' + g.id + '">Editar</button>' +
                    '<button class="btn btn-sm btn-peligro" data-accion="eliminarGasto" data-id="' + g.id + '">Eliminar</button>' +
                    '</div></td></tr>';
            });
            qs('#tbody-gastos').innerHTML = filas || mensajeVacioTabla(6, 'No hay gastos con los filtros actuales.');
            qs('#gastos-total').textContent = money(total);
        }

        const categorias = [];
        lista.forEach(function (g) { if (categorias.indexOf(g.categoria) === -1) categorias.push(g.categoria); });
        const opcionesCat = [{ valor: '', texto: 'Todas' }].concat(categorias.map(function (c) {
            return { valor: c, texto: tituloEstado(c) };
        }));

        contenido.innerHTML =
            '<div class="pagina-encabezado"><div><h1>Gastos</h1><p>Egresos operativos del restaurante.</p></div>' +
            '<div class="acciones"><button type="button" class="btn btn-primario" id="btn-nuevo-gasto">' + icono('nueva') + ' Nuevo gasto</button></div></div>' +
            '<div class="barra-filtros">' +
            '<label class="campo"><span>Buscar</span><input type="search" id="gas-buscar" placeholder="Concepto o comprobante"></label>' +
            '<label class="campo corto"><span>Categoria</span><select id="gas-categoria">' + opciones(opcionesCat, '') + '</select></label>' +
            '<label class="campo corto"><span>Desde</span><input type="date" id="gas-desde"></label>' +
            '<label class="campo corto"><span>Hasta</span><input type="date" id="gas-hasta"></label>' +
            '</div>' +
            '<div class="panel"><div class="panel-cabecera"><div><h3>Gastos registrados</h3><span>Total filtrado: <strong id="gastos-total">' + money(0) + '</strong></span></div></div>' +
            '<div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
            '<th>Fecha</th><th>Concepto</th><th>Categoria</th><th class="th-derecha">Monto</th><th>Usuario</th><th>Acciones</th>' +
            '</tr></thead><tbody id="tbody-gastos"></tbody></table></div></div></div>';

        qs('#gas-buscar').addEventListener('input', filtrar);
        qs('#gas-categoria').addEventListener('change', filtrar);
        qs('#gas-desde').addEventListener('change', filtrar);
        qs('#gas-hasta').addEventListener('change', filtrar);
        qs('#btn-nuevo-gasto').addEventListener('click', function () { abrirGasto(null); });

        ACCIONES.__gastos = lista;
        filtrar();
    }

    ACCIONES.editarGasto = function (el) {
        const lista = ACCIONES.__gastos || [];
        const g = lista.find(function (x) { return String(x.id) === String(el.dataset.id); });
        if (g) abrirGasto(g);
    };

    ACCIONES.eliminarGasto = async function (el) {
        if (!confirmar('¿Confirma que desea eliminar este gasto?')) return;
        try {
            await api('gasto_eliminar', { id: el.dataset.id }, 'POST');
            toast('Gasto eliminado.', 'exito');
            atenderRuta();
        } catch (error) { toast(error.message, 'error'); }
    };

    /* ------------------------------------------------------------------ */
    /* PERSONAL                                                            */
    /* ------------------------------------------------------------------ */
    function abrirPersonal(persona) {
        const esEdicion = !!persona;
        const p = persona || { nombre: '', documento: '', cargo: '', telefono: '', sueldo: '', fecha_ingreso: hoyISO(), activo: 1 };
        const html =
            '<label class="campo"><span>Nombre completo</span><input type="text" id="per-nombre" maxlength="120" value="' + esc(p.nombre) + '" required></label>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Documento</span><input type="text" id="per-documento" maxlength="20" value="' + esc(p.documento) + '"></label>' +
            '<label class="campo"><span>Cargo</span><input type="text" id="per-cargo" maxlength="60" value="' + esc(p.cargo) + '" placeholder="Cocinero, mesero..." required></label>' +
            '</div>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Telefono</span><input type="text" id="per-telefono" maxlength="20" value="' + esc(p.telefono) + '"></label>' +
            '<label class="campo"><span>Sueldo (S/)</span><input type="number" id="per-sueldo" min="0" step="0.01" value="' + esc(p.sueldo) + '"></label>' +
            '</div>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Fecha de ingreso</span><input type="date" id="per-ingreso" value="' + esc(p.fecha_ingreso ? String(p.fecha_ingreso).substring(0, 10) : '') + '"></label>' +
            '<label class="campo"><span>Estado</span><select id="per-activo">' + opciones([{ valor: '1', texto: 'Activo' }, { valor: '0', texto: 'Inactivo' }], String(p.activo) === '1' ? '1' : '0') + '</select></label>' +
            '</div>';

        abrirModal(esEdicion ? 'Editar trabajador' : 'Nuevo trabajador', html, {
            onSubmit: async function () {
                const nombre = qs('#per-nombre').value.trim();
                const cargo = qs('#per-cargo').value.trim();
                if (!nombre) throw new Error('Ingrese el nombre del trabajador.');
                if (!cargo) throw new Error('Ingrese el cargo.');
                await api('personal_guardar', {
                    id: esEdicion ? p.id : '',
                    nombre: nombre,
                    documento: qs('#per-documento').value.trim(),
                    cargo: cargo,
                    telefono: qs('#per-telefono').value.trim(),
                    sueldo: num(qs('#per-sueldo').value),
                    fecha_ingreso: qs('#per-ingreso').value,
                    activo: qs('#per-activo').value === '1'
                }, 'POST');
                cerrarModal();
                toast('Trabajador guardado correctamente.', 'exito');
                atenderRuta();
            }
        });
    }

    async function vistaPersonal() {
        contenido.innerHTML = cargando();
        const lista = await api('personal');
        personalCache = lista;

        let filas = '';
        lista.forEach(function (p) {
            filas += '<tr>' +
                '<td class="celda-principal"><strong>' + esc(p.nombre) + '</strong><span>' + esc(p.documento || 'Sin documento') + '</span></td>' +
                '<td>' + esc(p.cargo) + '</td>' +
                '<td>' + esc(p.telefono || '—') + '</td>' +
                '<td class="num">' + money(p.sueldo) + '</td>' +
                '<td>' + fecha(p.fecha_ingreso) + '</td>' +
                '<td class="num">' + money(p.total_pagado) + '</td>' +
                '<td>' + (Number(p.activo) === 1 ? badge('disponible', 'Activo') : badge('inactivo', 'Inactivo')) + '</td>' +
                '<td><div class="acciones-tabla">' +
                '<button class="btn btn-sm btn-secundario" data-accion="editarPersonal" data-id="' + p.id + '">Editar</button>' +
                '<button class="btn btn-sm btn-peligro" data-accion="eliminarPersonal" data-id="' + p.id + '">Eliminar</button>' +
                '</div></td></tr>';
        });
        if (!lista.length) filas = mensajeVacioTabla(8, 'No hay trabajadores registrados.');

        contenido.innerHTML =
            '<div class="pagina-encabezado"><div><h1>Personal</h1><p>Trabajadores del restaurante.</p></div>' +
            '<div class="acciones"><button type="button" class="btn btn-primario" id="btn-nuevo-personal">' + icono('nueva') + ' Nuevo trabajador</button></div></div>' +
            '<div class="panel"><div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
            '<th>Trabajador</th><th>Cargo</th><th>Telefono</th><th class="th-derecha">Sueldo</th><th>Ingreso</th><th class="th-derecha">Pagado</th><th>Estado</th><th>Acciones</th>' +
            '</tr></thead><tbody>' + filas + '</tbody></table></div></div></div>';

        qs('#btn-nuevo-personal').addEventListener('click', function () { abrirPersonal(null); });
    }

    ACCIONES.editarPersonal = function (el) {
        const p = (ACCIONES.__personalLista || personalCache).find(function (x) { return String(x.id) === String(el.dataset.id); });
        if (p) abrirPersonal(p);
    };

    ACCIONES.eliminarPersonal = async function (el) {
        if (!confirmar('¿Confirma que desea eliminar a este trabajador?')) return;
        try {
            await api('personal_eliminar', { id: el.dataset.id }, 'POST');
            toast('Trabajador eliminado.', 'exito');
            atenderRuta();
        } catch (error) { toast(error.message, 'error'); }
    };

    /* ------------------------------------------------------------------ */
    /* PAGOS DE PERSONAL                                                   */
    /* ------------------------------------------------------------------ */
    function abrirPagoPersonal() {
        if (!personalCache.length) {
            toast('Primero registre un trabajador.', 'error');
            return;
        }
        const opcionesPersonal = personalCache.filter(function (p) { return Number(p.activo) === 1; }).map(function (p) {
            return { valor: p.id, texto: p.nombre + ' · ' + p.cargo };
        });
        if (!opcionesPersonal.length) {
            toast('No hay trabajadores activos para pagar.', 'error');
            return;
        }
        const html =
            '<label class="campo"><span>Trabajador</span><select id="pp-personal">' + opciones(opcionesPersonal, '') + '</select></label>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Fecha</span><input type="date" id="pp-fecha" value="' + hoyISO() + '" required></label>' +
            '<label class="campo"><span>Monto (S/)</span><input type="number" id="pp-monto" min="0.01" step="0.01" required></label>' +
            '</div>' +
            '<label class="campo"><span>Concepto</span><input type="text" id="pp-concepto" maxlength="120" value="Sueldo" required></label>';

        abrirModal('Registrar pago de personal', html, {
            textoGuardar: 'Registrar pago',
            onSubmit: async function () {
                const monto = num(qs('#pp-monto').value);
                if (monto <= 0) throw new Error('Ingrese un monto mayor a cero.');
                await api('pago_personal_crear', {
                    personal_id: qs('#pp-personal').value,
                    fecha: qs('#pp-fecha').value,
                    monto: monto,
                    concepto: qs('#pp-concepto').value.trim() || 'Sueldo'
                }, 'POST');
                cerrarModal();
                toast('Pago registrado correctamente.', 'exito');
                atenderRuta();
            }
        });
    }

    async function vistaPagosPersonal() {
        contenido.innerHTML = cargando();
        const datos = await Promise.all([api('pagos_personal'), api('personal')]);
        const pagos = datos[0];
        personalCache = datos[1];

        const opcionesPersonal = [{ valor: '', texto: 'Todos' }].concat(personalCache.map(function (p) {
            return { valor: p.id, texto: p.nombre };
        }));

        function filtrar() {
            const persona = qs('#pp-filtro-personal').value;
            const desde = qs('#pp-desde').value;
            const hasta = qs('#pp-hasta').value;
            let total = 0;
            let filas = '';
            pagos.forEach(function (p) {
                const f = String(p.fecha).substring(0, 10);
                if (persona && String(p.personal_id) !== String(persona)) return;
                if (desde && f < desde) return;
                if (hasta && f > hasta) return;
                total += num(p.monto);
                filas += '<tr>' +
                    '<td>' + fecha(p.fecha) + '</td>' +
                    '<td class="celda-principal"><strong>' + esc(p.personal) + '</strong><span>' + esc(p.cargo) + '</span></td>' +
                    '<td>' + esc(p.concepto) + '</td>' +
                    '<td class="num">' + money(p.monto) + '</td>' +
                    '<td>' + esc(p.usuario || '—') + '</td>' +
                    '<td><button class="btn btn-sm btn-peligro" data-accion="eliminarPagoPersonal" data-id="' + p.id + '">Eliminar</button></td></tr>';
            });
            qs('#tbody-pagos').innerHTML = filas || mensajeVacioTabla(6, 'No hay pagos con los filtros actuales.');
            qs('#pagos-total').textContent = money(total);
        }

        contenido.innerHTML =
            '<div class="pagina-encabezado"><div><h1>Pagos de personal</h1><p>Sueldos, adelantos y bonificaciones.</p></div>' +
            '<div class="acciones"><button type="button" class="btn btn-primario" id="btn-nuevo-pago-personal">' + icono('nueva') + ' Registrar pago</button></div></div>' +
            '<div class="barra-filtros">' +
            '<label class="campo medio"><span>Trabajador</span><select id="pp-filtro-personal">' + opciones(opcionesPersonal, '') + '</select></label>' +
            '<label class="campo corto"><span>Desde</span><input type="date" id="pp-desde"></label>' +
            '<label class="campo corto"><span>Hasta</span><input type="date" id="pp-hasta"></label>' +
            '</div>' +
            '<div class="panel"><div class="panel-cabecera"><div><h3>Pagos registrados</h3><span>Total filtrado: <strong id="pagos-total">' + money(0) + '</strong></span></div></div>' +
            '<div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
            '<th>Fecha</th><th>Trabajador</th><th>Concepto</th><th class="th-derecha">Monto</th><th>Usuario</th><th>Acciones</th>' +
            '</tr></thead><tbody id="tbody-pagos"></tbody></table></div></div></div>';

        qs('#pp-filtro-personal').addEventListener('change', filtrar);
        qs('#pp-desde').addEventListener('change', filtrar);
        qs('#pp-hasta').addEventListener('change', filtrar);
        qs('#btn-nuevo-pago-personal').addEventListener('click', abrirPagoPersonal);
        filtrar();
    }

    ACCIONES.eliminarPagoPersonal = async function (el) {
        if (!confirmar('¿Confirma que desea eliminar este pago?')) return;
        try {
            await api('pago_personal_eliminar', { id: el.dataset.id }, 'POST');
            toast('Pago eliminado.', 'exito');
            atenderRuta();
        } catch (error) { toast(error.message, 'error'); }
    };

    /* ------------------------------------------------------------------ */
    /* REPORTES                                                            */
    /* ------------------------------------------------------------------ */
    function tablaHtml(cabeceras, filas) {
        return '<div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
            cabeceras.map(function (c) { return '<th>' + c + '</th>'; }).join('') +
            '</tr></thead><tbody>' + filas + '</tbody></table></div>';
    }

    async function vistaReportes() {
        contenido.innerHTML =
            '<div class="pagina-encabezado print-oculto"><div><h1>Reportes</h1><p>Analisis de ventas, gastos y ganancias.</p></div></div>' +
            '<div class="barra-filtros print-oculto">' +
            '<label class="campo corto"><span>Desde</span><input type="date" id="rep-desde" value="' + primerDiaMes() + '"></label>' +
            '<label class="campo corto"><span>Hasta</span><input type="date" id="rep-hasta" value="' + hoyISO() + '"></label>' +
            '<button type="button" class="btn btn-primario" id="btn-generar-reporte">Generar reporte</button>' +
            '<button type="button" class="btn btn-secundario" id="btn-imprimir-reporte">Imprimir</button>' +
            '</div>' +
            '<div id="rep-resultado">' + cargando() + '</div>';

        async function generar() {
            qs('#rep-resultado').innerHTML = cargando();
            try {
                const d = await api('reportes', { desde: qs('#rep-desde').value, hasta: qs('#rep-hasta').value });
                const r = d.resumen;

                let topFilas = '';
                d.top_productos.forEach(function (p) {
                    topFilas += '<tr><td class="celda-principal"><strong>' + esc(p.nombre) + '</strong><span>' + esc(tituloEstado(p.categoria)) + '</span></td>' +
                        '<td class="num">' + esc(p.cantidad) + '</td><td class="num">' + money(p.total) + '</td></tr>';
                });
                if (!d.top_productos.length) topFilas = mensajeVacioTabla(3, 'Sin productos vendidos en el periodo.');

                let diasFilas = '';
                d.ventas_por_dia.forEach(function (v) {
                    diasFilas += '<tr><td>' + fecha(v.fecha) + '</td><td class="num">' + esc(v.cantidad) + '</td><td class="num">' + money(v.total) + '</td></tr>';
                });
                if (!d.ventas_por_dia.length) diasFilas = mensajeVacioTabla(3, 'Sin ventas en el periodo.');

                let mediosFilas = '';
                d.ingresos_por_medio.forEach(function (m) {
                    mediosFilas += '<tr><td>' + badge(m.medio) + '</td><td class="num">' + esc(m.cantidad) + '</td><td class="num">' + money(m.total) + '</td></tr>';
                });
                if (!d.ingresos_por_medio.length) mediosFilas = mensajeVacioTabla(3, 'Sin ingresos en el periodo.');

                let gastosFilas = '';
                d.gastos_por_categoria.forEach(function (g) {
                    gastosFilas += '<tr><td>' + esc(tituloEstado(g.categoria)) + '</td><td class="num">' + esc(g.cantidad) + '</td><td class="num">' + money(g.total) + '</td></tr>';
                });
                if (!d.gastos_por_categoria.length) gastosFilas = mensajeVacioTabla(3, 'Sin gastos en el periodo.');

                let ventasFilas = '';
                d.ventas.forEach(function (v) {
                    ventasFilas += '<tr><td class="celda-principal"><strong>' + esc(v.numero) + '</strong><span>' + esc(v.cliente) + '</span></td>' +
                        '<td>' + fecha(v.fecha) + '</td><td>' + esc(tituloEstado(v.tipo_servicio)) + '</td>' +
                        '<td class="num">' + money(v.total) + '</td><td class="num">' + money(v.pagado) + '</td><td>' + badge(v.estado) + '</td></tr>';
                });
                if (!d.ventas.length) ventasFilas = mensajeVacioTabla(6, 'Sin ventas en el periodo.');

                let personalFilas = '';
                d.pagos_personal.forEach(function (p) {
                    personalFilas += '<tr><td>' + fecha(p.fecha) + '</td><td class="celda-principal"><strong>' + esc(p.personal) + '</strong><span>' + esc(p.cargo) + '</span></td>' +
                        '<td>' + esc(p.concepto) + '</td><td class="num">' + money(p.monto) + '</td></tr>';
                });
                if (!d.pagos_personal.length) personalFilas = mensajeVacioTabla(4, 'Sin pagos de personal en el periodo.');

                const signo = num(r.ganancia) >= 0 ? 't-verde' : 't-rojo';
                const signoNeto = num(r.resultado_neto) >= 0 ? 't-verde' : 't-rojo';

                qs('#rep-resultado').innerHTML =
                    '<div class="mensaje-info print-oculto">Periodo: <strong>' + fecha(d.desde) + '</strong> al <strong>' + fecha(d.hasta) + '</strong></div>' +
                    '<div class="tarjetas-grid">' +
                    '<div class="stat-card t-ambar"><span class="stat-label">Ventas</span><div class="stat-valor">' + esc(r.ventas_cantidad) + '</div><div class="stat-pista">Total ' + money(r.ventas_total) + '</div></div>' +
                    '<div class="stat-card t-verde"><span class="stat-label">Ingresos cobrados</span><div class="stat-valor">' + money(r.ingresos) + '</div><div class="stat-pista">Efectivo ' + money(r.efectivo) + ' · Yape ' + money(r.yape) + ' · Tarjeta ' + money(r.tarjeta) + '</div></div>' +
                    '<div class="stat-card t-rojo"><span class="stat-label">Gastos + personal</span><div class="stat-valor">' + money(num(r.gastos) + num(r.pagos_personal)) + '</div><div class="stat-pista">Gastos ' + money(r.gastos) + ' · Personal ' + money(r.pagos_personal) + '</div></div>' +
                    '<div class="stat-card t-azul"><span class="stat-label">Compras</span><div class="stat-valor">' + money(r.compras) + '</div><div class="stat-pista">Abastecimiento</div></div>' +
                    '<div class="stat-card ' + signo + '"><span class="stat-label">Ganancia</span><div class="stat-valor">' + money(r.ganancia) + '</div><div class="stat-pista">Ingresos - gastos - personal</div></div>' +
                    '<div class="stat-card ' + signoNeto + '"><span class="stat-label">Resultado neto</span><div class="stat-valor">' + money(r.resultado_neto) + '</div><div class="stat-pista">Ganancia - compras</div></div>' +
                    '<div class="stat-card t-gris"><span class="stat-label">Platos vendidos</span><div class="stat-valor">' + esc(r.platos_vendidos) + '</div><div class="stat-pista">Descuentos ' + money(r.descuentos) + '</div></div>' +
                    '</div>' +

                    '<div class="dos-columnas">' +
                    '<div class="panel"><div class="panel-cabecera"><div><h3>Ingresos y egresos</h3><span>Evolucion diaria del periodo</span></div>' +
                    '<div class="leyenda"><span><i class="punto punto-verde"></i>Ingresos</span><span><i class="punto punto-rojo"></i>Gastos</span><span><i class="punto punto-ambar"></i>Compras</span></div></div>' +
                    '<div class="panel-cuerpo">' + grafico(d.serie) + '</div></div>' +
                    '<div>' +
                    '<div class="panel"><div class="panel-cabecera"><h3>Ingresos por medio</h3></div><div class="panel-cuerpo sin-padding">' + tablaHtml(['Medio', 'Cant.', 'Total'], mediosFilas) + '</div></div>' +
                    '<div class="panel"><div class="panel-cabecera"><h3>Gastos por categoria</h3></div><div class="panel-cuerpo sin-padding">' + tablaHtml(['Categoria', 'Cant.', 'Total'], gastosFilas) + '</div></div>' +
                    '</div></div>' +

                    '<div class="dos-columnas">' +
                    '<div class="panel"><div class="panel-cabecera"><h3>Productos mas vendidos</h3></div><div class="panel-cuerpo sin-padding">' + tablaHtml(['Producto', 'Cant.', 'Total'], topFilas) + '</div></div>' +
                    '<div class="panel"><div class="panel-cabecera"><h3>Ventas por dia</h3></div><div class="panel-cuerpo sin-padding">' + tablaHtml(['Fecha', 'Ventas', 'Total'], diasFilas) + '</div></div>' +
                    '</div>' +

                    '<div class="panel"><div class="panel-cabecera"><div><h3>Detalle de ventas</h3><span>' + esc(d.ventas.length) + ' registro(s)</span></div></div>' +
                    '<div class="panel-cuerpo sin-padding">' + tablaHtml(['Venta', 'Fecha', 'Servicio', 'Total', 'Pagado', 'Estado'], ventasFilas) + '</div></div>' +

                    '<div class="panel"><div class="panel-cabecera"><h3>Pagos al personal</h3></div><div class="panel-cuerpo sin-padding">' + tablaHtml(['Fecha', 'Trabajador', 'Concepto', 'Monto'], personalFilas) + '</div></div>';
            } catch (error) {
                qs('#rep-resultado').innerHTML = '<div class="panel"><div class="panel-cuerpo">' + sinDatos(error.message) + '</div></div>';
                toast(error.message, 'error');
            }
        }

        qs('#btn-generar-reporte').addEventListener('click', generar);
        qs('#btn-imprimir-reporte').addEventListener('click', function () { window.print(); });
        generar();
    }

    /* ------------------------------------------------------------------ */
    /* USUARIOS                                                            */
    /* ------------------------------------------------------------------ */
    function abrirUsuario(usu) {
        const esEdicion = !!usu;
        const u = usu || { nombres: '', usuario: '', rol: 'cajero', activo: 1 };
        const html =
            '<div class="grid-2">' +
            '<label class="campo"><span>Nombres</span><input type="text" id="usu-nombres" maxlength="120" value="' + esc(u.nombres) + '" required></label>' +
            '<label class="campo"><span>Usuario de acceso</span><input type="text" id="usu-usuario" maxlength="50" value="' + esc(u.usuario) + '" required></label>' +
            '</div>' +
            '<div class="grid-2">' +
            '<label class="campo"><span>Rol</span><select id="usu-rol">' + opciones([{ valor: 'administrador', texto: 'Administrador' }, { valor: 'cajero', texto: 'Cajero' }, { valor: 'cocina', texto: 'Cocina' }, { valor: 'inventario', texto: 'Inventario' }], u.rol) + '</select></label>' +
            '<label class="campo"><span>Estado</span><select id="usu-activo">' + opciones([{ valor: '1', texto: 'Activo' }, { valor: '0', texto: 'Inactivo' }], String(u.activo) === '1' ? '1' : '0') + '</select></label>' +
            '</div>' +
            '<label class="campo"><span>Contrasena ' + (esEdicion ? '(dejar vacio para mantener)' : '') + '</span><input type="password" id="usu-clave" minlength="6" ' + (esEdicion ? '' : 'required') + ' autocomplete="new-password"></label>';

        abrirModal(esEdicion ? 'Editar usuario' : 'Nuevo usuario', html, {
            onSubmit: async function () {
                const nombres = qs('#usu-nombres').value.trim();
                const nombreUsuario = qs('#usu-usuario').value.trim().toLowerCase();
                const clave = qs('#usu-clave').value;
                if (!nombres) throw new Error('Ingrese los nombres.');
                if (!/^[a-z0-9._-]{3,50}$/.test(nombreUsuario)) throw new Error('El usuario debe tener entre 3 y 50 caracteres validos.');
                if (!esEdicion && clave.length < 6) throw new Error('La contrasena debe tener al menos 6 caracteres.');
                if (clave && clave.length < 6) throw new Error('La contrasena debe tener al menos 6 caracteres.');
                await api('usuario_guardar', {
                    id: esEdicion ? u.id : '',
                    nombres: nombres,
                    usuario: nombreUsuario,
                    rol: qs('#usu-rol').value,
                    clave: clave,
                    activo: qs('#usu-activo').value === '1'
                }, 'POST');
                cerrarModal();
                toast('Usuario guardado correctamente.', 'exito');
                atenderRuta();
            }
        });
    }

    async function vistaUsuarios() {
        contenido.innerHTML = cargando();
        const lista = await api('usuarios');

        let filas = '';
        lista.forEach(function (u) {
            filas += '<tr>' +
                '<td class="celda-principal"><strong>' + esc(u.nombres) + '</strong><span>' + esc(u.usuario) + '</span></td>' +
                '<td>' + esc(tituloEstado(u.rol)) + '</td>' +
                '<td>' + (Number(u.activo) === 1 ? badge('disponible', 'Activo') : badge('inactivo', 'Inactivo')) + '</td>' +
                '<td>' + fecha(u.creado_en) + '</td>' +
                '<td><div class="acciones-tabla">' +
                '<button class="btn btn-sm btn-secundario" data-accion="editarUsuario" data-id="' + u.id + '">Editar</button>' +
                '<button class="btn btn-sm ' + (Number(u.activo) === 1 ? 'btn-peligro' : 'btn-exito') + '" data-accion="estadoUsuario" data-id="' + u.id + '" data-activo="' + (Number(u.activo) === 1 ? '0' : '1') + '">' + (Number(u.activo) === 1 ? 'Desactivar' : 'Activar') + '</button>' +
                '</div></td></tr>';
        });
        if (!lista.length) filas = mensajeVacioTabla(5, 'No hay usuarios registrados.');

        contenido.innerHTML =
            '<div class="pagina-encabezado"><div><h1>Usuarios</h1><p>Accesos y roles del sistema.</p></div>' +
            '<div class="acciones"><button type="button" class="btn btn-primario" id="btn-nuevo-usuario">' + icono('nueva') + ' Nuevo usuario</button></div></div>' +
            '<div class="panel"><div class="panel-cuerpo sin-padding"><div class="tabla-wrapper"><table class="tabla"><thead><tr>' +
            '<th>Usuario</th><th>Rol</th><th>Estado</th><th>Creado</th><th>Acciones</th>' +
            '</tr></thead><tbody>' + filas + '</tbody></table></div></div></div>';

        qs('#btn-nuevo-usuario').addEventListener('click', function () { abrirUsuario(null); });
        ACCIONES.__usuarios = lista;
    }

    ACCIONES.editarUsuario = function (el) {
        const lista = ACCIONES.__usuarios || [];
        const u = lista.find(function (x) { return String(x.id) === String(el.dataset.id); });
        if (u) abrirUsuario(u);
    };

    ACCIONES.estadoUsuario = async function (el) {
        try {
            await api('usuario_estado', { id: el.dataset.id, activo: el.dataset.activo === '1' }, 'POST');
            toast('Estado del usuario actualizado.', 'exito');
            atenderRuta();
        } catch (error) { toast(error.message, 'error'); }
    };

    /* ------------------------------------------------------------------ */
    /* EVENTOS GLOBALES E INICIO                                           */
    /* ------------------------------------------------------------------ */
    function vincularLogin() {
        const form = qs('#form-login');
        form.addEventListener('submit', async function (e) {
            e.preventDefault();
            const error = qs('#login-error');
            const boton = qs('#btn-ingresar');
            error.classList.add('oculto');
            boton.disabled = true;
            boton.textContent = 'Ingresando...';
            try {
                const datos = await api('login', {
                    usuario: qs('#login-usuario').value.trim(),
                    clave: qs('#login-clave').value
                }, 'POST');
                guardarSesion(datos);
                qs('#login-clave').value = '';
                entrarApp();
            } catch (ex) {
                error.textContent = ex.message;
                error.classList.remove('oculto');
            } finally {
                boton.disabled = false;
                boton.textContent = 'Ingresar';
            }
        });

        qsa('.demo-chip').forEach(function (chip) {
            chip.addEventListener('click', function () {
                qs('#login-usuario').value = chip.dataset.usuario;
                qs('#login-clave').value = chip.dataset.clave;
                qs('#login-clave').focus();
            });
        });

        const ojo = qs('#btn-ver-clave');
        ojo.addEventListener('click', function () {
            const campo = qs('#login-clave');
            const mostrar = campo.type === 'password';
            campo.type = mostrar ? 'text' : 'password';
            ojo.classList.toggle('activo', mostrar);
            ojo.setAttribute('aria-label', mostrar ? 'Ocultar contrasena' : 'Mostrar contrasena');
        });
    }

    function vincularGlobales() {
        qs('#btn-salir').addEventListener('click', salir);
        qs('#btn-menu').addEventListener('click', abrirSidebar);
        sidebarOverlay.addEventListener('click', cerrarSidebar);
        qs('#modal-cerrar').addEventListener('click', cerrarModal);
        modalCancelar.addEventListener('click', cerrarModal);
        modalOverlay.addEventListener('click', function (e) {
            if (e.target === modalOverlay) cerrarModal();
        });
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && !modalOverlay.classList.contains('oculto')) cerrarModal();
        });

        qs('#modal-form').addEventListener('submit', async function (e) {
            e.preventDefault();
            if (!modalSubmit) return;
            modalGuardar.disabled = true;
            try {
                await modalSubmit();
            } catch (error) {
                toast(error.message, 'error');
            } finally {
                modalGuardar.disabled = false;
            }
        });

        document.addEventListener('click', function (e) {
            const el = e.target.closest('[data-accion]');
            if (!el) return;
            const funcion = ACCIONES[el.dataset.accion];
            if (typeof funcion === 'function') {
                e.preventDefault();
                funcion(el);
            }
        });

        window.addEventListener('hashchange', atenderRuta);
    }

    async function arrancar() {
        vincularLogin();
        vincularGlobales();
        if (token) {
            try {
                const actual = await api('me');
                usuario = actual;
                localStorage.setItem(USUARIO_KEY, JSON.stringify(actual));
                entrarApp();
                return;
            } catch (e) {
                limpiarSesion();
            }
        }
        mostrarLogin();
    }

    arrancar();
})();
