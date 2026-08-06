document.addEventListener('DOMContentLoaded', function () {
	var carpetas = document.querySelectorAll('.categoria');

	function marcar(categoria, marcado) {
		categoria.querySelectorAll('input[name="ext"]').forEach(function (c) {
			c.checked = marcado;
		});
	}

	carpetas.forEach(function (cat) {
		var todas = cat.querySelector('.todas-cat');
		todas.addEventListener('change', function () {
			marcar(cat, todas.checked);
		});
	});

	document.getElementById('seleccionarTodas').addEventListener('click', function () {
		carpetas.forEach(function (cat) {
			marcar(cat, true);
			cat.querySelector('.todas-cat').checked = true;
		});
	});

	document.getElementById('desmarcarTodas').addEventListener('click', function () {
		carpetas.forEach(function (cat) {
			marcar(cat, false);
			cat.querySelector('.todas-cat').checked = false;
		});
	});

	buscarCarpetaConfig.alSeleccionar = function (ruta) {
		document.getElementById('ruta').value = ruta;
		var cont = document.getElementById('carpetaSeleccionada');
		cont.innerHTML = '';
		var span = document.createElement('span');
		span.className = 'ruta-elegida';
		span.textContent = ruta;
		cont.appendChild(span);
		document.getElementById('btnEscanear').disabled = false;
	};

	var rutaInicial = document.getElementById('ruta').value;
	if (rutaInicial) {
		var cont = document.getElementById('carpetaSeleccionada');
		cont.innerHTML = '';
		var span = document.createElement('span');
		span.className = 'ruta-elegida';
		span.textContent = rutaInicial;
		cont.appendChild(span);
		document.getElementById('btnEscanear').disabled = false;
	}
});
