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
	if (resaltar) {
		resaltar.addEventListener('change', aplicarResaltado);
	}

	actualizarContador();
	aplicarResaltado();

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
		var texto = (filtro.value || '').toLowerCase();
		tabla.querySelectorAll('tbody tr').forEach(function (f) {
			var coincide = !texto
				|| (f.getAttribute('data-nombre') || '').toLowerCase().indexOf(texto) !== -1
				|| (f.getAttribute('data-carpeta') || '').toLowerCase().indexOf(texto) !== -1;
			f.style.display = coincide ? '' : 'none';
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
