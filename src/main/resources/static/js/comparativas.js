document.addEventListener('DOMContentLoaded', function () {
	buscarCarpetaConfig.alSeleccionar = function (ruta) {
		document.getElementById('ruta').value = ruta;
		var cont = document.getElementById('carpetaSeleccionada');
		cont.innerHTML = '';
		var span = document.createElement('span');
		span.className = 'ruta-elegida';
		span.textContent = ruta;
		cont.appendChild(span);
		document.getElementById('btnBuscar').disabled = false;
	};

	var estado = { campo: 'modificacion', dir: -1 };

	var tabla = document.getElementById('tablaResultados');
	var cabeceras = document.querySelectorAll('.th-ordenable');
	var filtro = document.getElementById('filtroTabla');
	var filtroExt = document.getElementById('filtroExt');
	var filtroDesde = document.getElementById('filtroFechaDesde');
	var filtroHasta = document.getElementById('filtroFechaHasta');
	var btnLimpiar = document.getElementById('btnLimpiarFiltros');
	var resaltar = document.getElementById('resaltarReciente');
	var contador = document.getElementById('contadorTabla');

	if (!tabla) {
		return;
	}

	cabeceras.forEach(function (th) {
		th.addEventListener('click', function () {
			ordenarPor(th.getAttribute('data-campo'));
		});
	});

	if (filtro) {
		filtro.addEventListener('input', aplicarFiltro);
	}
	if (filtroExt) {
		filtroExt.addEventListener('change', aplicarFiltro);
	}
	if (filtroDesde) {
		filtroDesde.addEventListener('change', aplicarFiltro);
	}
	if (filtroHasta) {
		filtroHasta.addEventListener('change', aplicarFiltro);
	}
	if (btnLimpiar) {
		btnLimpiar.addEventListener('click', function () {
			if (filtro) { filtro.value = ''; }
			if (filtroExt) { filtroExt.value = ''; }
			if (filtroDesde) { filtroDesde.value = ''; }
			if (filtroHasta) { filtroHasta.value = ''; }
			aplicarFiltro();
		});
	}
	if (resaltar) {
		resaltar.addEventListener('change', aplicarResaltado);
	}

	var EXTENSIONES_IMAGEN = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico', 'jfif'];

	poblarFiltroExt();
	agregarMiniaturas();
	actualizarContador();
	aplicarResaltado();

	function agregarMiniaturas() {
		tabla.querySelectorAll('tbody tr').forEach(function (f) {
			var celda = f.querySelector('.celda-miniatura');
			if (!celda) {
				return;
			}
			var ext = (f.getAttribute('data-extension') || '').toLowerCase();
			if (EXTENSIONES_IMAGEN.indexOf(ext) === -1) {
				var icono = document.createElement('span');
				icono.className = 'icono-ms';
				icono.textContent = 'insert_drive_file';
				celda.appendChild(icono);
				return;
			}
			var ruta = f.getAttribute('data-ruta');
			var img = document.createElement('img');
			img.className = 'miniatura';
			img.src = '/comparativas/miniatura?ruta=' + encodeURIComponent(ruta);
			img.alt = 'Vista previa';
			img.loading = 'lazy';
			img.addEventListener('click', function (e) {
				e.stopPropagation();
				abrirImagen(img.src);
			});
			celda.appendChild(img);
		});
	}

	function abrirImagen(src) {
		document.getElementById('imagenGrande').src = src;
		document.getElementById('modalImagen').style.display = 'flex';
	}

	function cerrarModalImagen() {
		document.getElementById('modalImagen').style.display = 'none';
		document.getElementById('imagenGrande').src = '';
	}

	var modalImagen = document.getElementById('modalImagen');
	if (modalImagen) {
		modalImagen.addEventListener('click', function (e) {
			if (e.target === this) {
				cerrarModalImagen();
			}
		});
	}
	document.addEventListener('keydown', function (e) {
		if (e.key === 'Escape') {
			cerrarModalImagen();
		}
	});
	window.cerrarModalImagen = cerrarModalImagen;

	var toastTimer = null;

	function mostrarToast(mensaje, esError) {
		var toast = document.getElementById('toast');
		if (!toast) {
			return;
		}
		toast.textContent = mensaje;
		toast.classList.toggle('error', !!esError);
		toast.classList.add('visible');
		if (toastTimer) {
			clearTimeout(toastTimer);
		}
		toastTimer = setTimeout(function () {
			toast.classList.remove('visible');
		}, 3200);
	}

	window.abrirCarpetaArchivo = function (boton) {
		var ruta = boton.getAttribute('data-ruta');
		fetch('/comparativas/abrir-carpeta?ruta=' + encodeURIComponent(ruta))
			.then(function (res) {
				return res.json().catch(function () {
					return {};
				});
			})
			.then(function (data) {
				var ok = data.ok === 'true';
				mostrarToast(data.mensaje || (ok ? 'Se abrió la carpeta en el Explorador' : 'No se pudo abrir la carpeta'), !ok);
			});
	};

	function poblarFiltroExt() {
		if (!filtroExt) {
			return;
		}
		var opciones = {};
		tabla.querySelectorAll('tbody tr').forEach(function (f) {
			var ext = f.getAttribute('data-extension') || '';
			if (ext) {
				opciones[ext] = true;
			}
		});
		Object.keys(opciones).sort(function (a, b) {
			return a.localeCompare(b, 'es', { sensitivity: 'base' });
		}).forEach(function (ext) {
			var op = document.createElement('option');
			op.value = ext;
			op.textContent = '.' + ext;
			filtroExt.appendChild(op);
		});
	}

	function fechaInicioMs(valor) {
		if (!valor) {
			return null;
		}
		return new Date(valor + 'T00:00:00').getTime();
	}

	function fechaFinMs(valor) {
		if (!valor) {
			return null;
		}
		return new Date(valor + 'T23:59:59.999').getTime();
	}

	function filasVisibles() {
		var cuerpo = tabla.querySelector('tbody');
		return Array.prototype.slice.call(cuerpo.querySelectorAll('tr'))
			.filter(function (f) { return f.style.display !== 'none'; });
	}

	function ordenarPor(campo) {
		var dir = (estado.campo === campo && estado.dir === -1) ? 1 : -1;
		var cuerpo = tabla.querySelector('tbody');
		var filas = filasVisibles();
		filas.sort(function (a, b) {
			var va = a.getAttribute('data-' + campo);
			var vb = b.getAttribute('data-' + campo);
			if (campo === 'tamanio' || campo === 'modificacion' || campo === 'creacion' || campo === 'acceso') {
				return ((parseFloat(va) || 0) - (parseFloat(vb) || 0)) * dir;
			}
			return String(va || '').localeCompare(String(vb || ''), 'es', { numeric: true, sensitivity: 'base' }) * dir;
		});
		filas.forEach(function (f) { cuerpo.appendChild(f); });
		estado.campo = campo;
		estado.dir = dir;
		marcarCabecera(campo, dir);
		aplicarResaltado();
	}

	function marcarCabecera(campo, dir) {
		cabeceras.forEach(function (th) {
			th.classList.remove('orden-asc', 'orden-desc');
			if (th.getAttribute('data-campo') === campo) {
				th.classList.add(dir === 1 ? 'orden-asc' : 'orden-desc');
			}
		});
	}

	function aplicarFiltro() {
		var texto = filtro ? (filtro.value || '').toLowerCase() : '';
		var ext = filtroExt ? filtroExt.value : '';
		var desde = fechaInicioMs(filtroDesde ? filtroDesde.value : '');
		var hasta = fechaFinMs(filtroHasta ? filtroHasta.value : '');
		tabla.querySelectorAll('tbody tr').forEach(function (f) {
			var coincideTexto = !texto
				|| (f.getAttribute('data-nombre') || '').toLowerCase().indexOf(texto) !== -1
				|| (f.getAttribute('data-carpeta') || '').toLowerCase().indexOf(texto) !== -1;
			var coincideExt = !ext || (f.getAttribute('data-extension') || '') === ext;
			var ms = parseFloat(f.getAttribute('data-modificacion')) || 0;
			var coincideFecha = (desde === null || ms >= desde) && (hasta === null || ms <= hasta);
			f.style.display = (coincideTexto && coincideExt && coincideFecha) ? '' : 'none';
		});
		actualizarContador();
		aplicarResaltado();
	}

	function actualizarContador() {
		if (!contador) {
			return;
		}
		var visibles = filasVisibles().length;
		contador.textContent = 'Mostrando ' + visibles + ' de ' + tabla.querySelectorAll('tbody tr').length + ' archivos';
	}

	function aplicarResaltado() {
		var visibles = filasVisibles();
		visibles.forEach(function (f) { f.classList.remove('fila-mas-reciente'); });
		if (!resaltar || !resaltar.checked || visibles.length === 0) {
			return;
		}
		var mejor = null;
		var mejorMs = -1;
		visibles.forEach(function (f) {
			var ms = parseFloat(f.getAttribute('data-modificacion')) || 0;
			if (ms > mejorMs) {
				mejorMs = ms;
				mejor = f;
			}
		});
		if (mejor) {
			mejor.classList.add('fila-mas-reciente');
		}
	}
});
