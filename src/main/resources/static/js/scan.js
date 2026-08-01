document.addEventListener('DOMContentLoaded', function () {
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
});
