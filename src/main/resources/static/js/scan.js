document.addEventListener('DOMContentLoaded', function () {
	cargarUnidades();
	ir('C:\\');
});

function cargarUnidades() {
	var cont = document.getElementById('unidades');
	fetch('/api/unidades')
		.then(function (r) { return r.json(); })
		.then(function (unidades) {
			cont.innerHTML = '';
			unidades.forEach(function (u) {
				var b = document.createElement('button');
				b.type = 'button';
				b.className = 'chip-unidad';
				b.textContent = u.nombre;
				b.addEventListener('click', function () { ir(u.ruta); });
				cont.appendChild(b);
			});
		})
		.catch(function () {
			cont.innerHTML = '<p class="error">No se pudieron cargar las unidades.</p>';
		});
}

function ir(ruta) {
	if (!ruta) {
		return;
	}
	document.getElementById('ruta').value = ruta;
	cargarBreadcrumb(ruta);
	cargarCarpetas(ruta);
}

function cargarCarpetas(ruta) {
	var cont = document.getElementById('carpetas');
	cont.innerHTML = '<p class="cargando">Cargando…</p>';
	fetch('/api/carpetas?ruta=' + encodeURIComponent(ruta))
		.then(function (r) { return r.json(); })
		.then(function (carpetas) {
			cont.innerHTML = '';
			if (carpetas.length === 0) {
				cont.innerHTML = '<p class="vacio">No hay subcarpetas.</p>';
				return;
			}
			carpetas.forEach(function (c) {
				var d = document.createElement('div');
				d.className = 'item-carpeta';
				var icono = document.createElement('span');
				icono.className = 'icono-carpeta';
				var nombre = document.createElement('span');
				nombre.className = 'nombre-carpeta';
				nombre.textContent = c.nombre;
				d.appendChild(icono);
				d.appendChild(nombre);
				d.addEventListener('click', function () { ir(c.ruta); });
				cont.appendChild(d);
			});
		})
		.catch(function () {
			cont.innerHTML = '<p class="error">No se pudo acceder a la carpeta (sin permisos).</p>';
		});
}

function cargarBreadcrumb(ruta) {
	var cont = document.getElementById('breadcrumb');
	cont.innerHTML = '';
	var norm = ruta;
	if (!norm.endsWith('\\') && !norm.endsWith('/')) {
		norm += '\\';
	}
	var partes = norm.split('\\').filter(function (p) { return p !== ''; });
	var acum = '';
	partes.forEach(function (p, i) {
		acum += p + '\\';
		var sp = document.createElement('span');
		sp.className = 'crumb';
		sp.textContent = p;
		sp.addEventListener('click', function () { ir(acum); });
		cont.appendChild(sp);
		if (i < partes.length - 1) {
			var sep = document.createElement('span');
			sep.className = 'sep';
			sep.textContent = '>';
			cont.appendChild(sep);
		}
	});
	if (partes.length === 0) {
		cont.innerHTML = '<span class="crumb">\\</span>';
	}
}
