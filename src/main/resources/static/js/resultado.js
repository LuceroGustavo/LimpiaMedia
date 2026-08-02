document.addEventListener('DOMContentLoaded', function () {
	buscarCarpetaConfig.campoDestino = document.getElementById('destino');

	var EXTENSIONES_IMAGEN = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico', 'jfif'];

	var listaItems = document.querySelectorAll('.lista-archivos li');
	var grupos = document.querySelectorAll('.panel.grupo');
	var buscar = document.getElementById('buscarGrupo');
	var filtrarExt = document.getElementById('filtrarExtension');
	var contador = document.getElementById('contadorGrupos');

	agregarMiniaturas();
	llenarExtensiones();
	aplicarFiltros();

	buscar.addEventListener('input', aplicarFiltros);
	filtrarExt.addEventListener('change', aplicarFiltros);

	function agregarMiniaturas() {
		listaItems.forEach(function (li) {
			var ext = (li.getAttribute('data-ext') || '').toLowerCase();
			if (EXTENSIONES_IMAGEN.indexOf(ext) === -1) {
				return;
			}
			var id = li.getAttribute('data-id');
			var img = document.createElement('img');
			img.className = 'miniatura';
			img.src = '/archivo/' + id;
			img.alt = 'Vista previa';
			img.loading = 'lazy';
			img.addEventListener('click', function (e) {
				e.stopPropagation();
				abrirImagen(img.src);
			});
			li.insertBefore(img, li.firstChild);
		});
	}

	function llenarExtensiones() {
		var presentes = {};
		listaItems.forEach(function (li) {
			var ext = (li.getAttribute('data-ext') || '').toLowerCase();
			if (ext) {
				presentes[ext] = true;
			}
		});
		Object.keys(presentes).sort().forEach(function (ext) {
			var op = document.createElement('option');
			op.value = ext;
			op.textContent = '.' + ext;
			filtrarExt.appendChild(op);
		});
	}

	function aplicarFiltros() {
		var texto = (buscar.value || '').toLowerCase();
		var ext = filtrarExt.value;
		var visibles = 0;
		grupos.forEach(function (grupo) {
			var coincide = false;
			grupo.querySelectorAll('.lista-archivos li').forEach(function (li) {
				var nombre = li.getAttribute('data-ext') || '';
				var ruta = li.querySelector('.ruta');
				if (ruta) {
					nombre = ruta.textContent;
				}
				if (ext && (li.getAttribute('data-ext') || '').toLowerCase() !== ext) {
					return;
				}
				if (!texto || nombre.toLowerCase().indexOf(texto) !== -1) {
					coincide = true;
				}
			});
			grupo.style.display = coincide ? '' : 'none';
			if (coincide) {
				visibles++;
			}
		});
		if (contador) {
			contador.textContent = 'Mostrando ' + visibles + ' de ' + grupos.length + ' grupos';
		}
	}

	function abrirImagen(src) {
		document.getElementById('imagenGrande').src = src;
		document.getElementById('modalImagen').style.display = 'flex';
	}

	function cerrarModalImagen() {
		document.getElementById('modalImagen').style.display = 'none';
		document.getElementById('imagenGrande').src = '';
	}

	document.getElementById('modalImagen').addEventListener('click', function (e) {
		if (e.target === this) {
			cerrarModalImagen();
		}
	});
	document.addEventListener('keydown', function (e) {
		if (e.key === 'Escape') {
			cerrarModalImagen();
		}
	});
	window.cerrarModalImagen = cerrarModalImagen;

	var moverForm = document.querySelector('.mover-form');
	if (moverForm) {
		moverForm.addEventListener('submit', function () {
			mostrarProcesando();
		});
	}

	function mostrarProcesando() {
		var overlay = document.getElementById('procesando-overlay');
		var texto = document.getElementById('procesando-texto');
		if (!overlay) {
			return;
		}
		overlay.classList.add('visible');
		var p = 0;
		var t = setInterval(function () {
			p = Math.min(100, p + 7);
			texto.textContent = p + '%';
			if (p >= 100) {
				clearInterval(t);
			}
		}, 80);
	}
});
