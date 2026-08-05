var buscarCarpetaConfig = {
	campoDestino: null,
	rutaInicial: 'C:\\',
	alSeleccionar: null
};

document.addEventListener('DOMContentLoaded', function () {
	cargarRecientes();
	cargarUnidades();
	ir('C:\\');

	document.getElementById('modal').addEventListener('click', function (e) {
		if (e.target === this) {
			cerrarModal();
		}
	});

	document.getElementById('rutaModal').addEventListener('keydown', function (e) {
		if (e.key === 'Enter') {
			e.preventDefault();
			ir(this.value);
		}
	});

	document.addEventListener('keydown', function (e) {
		if (e.key === 'Escape') {
			cerrarModal();
		}
	});
});

function abrirModal() {
	document.getElementById('modal').style.display = 'flex';
	ir(document.getElementById('rutaModal').value || buscarCarpetaConfig.rutaInicial);
}

function cerrarModal() {
	document.getElementById('modal').style.display = 'none';
}

function seleccionarActual() {
	var ruta = document.getElementById('rutaModal').value;
	if (buscarCarpetaConfig.campoDestino) {
		buscarCarpetaConfig.campoDestino.value = ruta;
	}
	if (buscarCarpetaConfig.alSeleccionar) {
		buscarCarpetaConfig.alSeleccionar(ruta);
	}
	cerrarModal();
}

function chip(ruta, nombre, alClic) {
	var b = document.createElement('button');
	b.type = 'button';
	b.className = 'chip-unidad';
	b.textContent = nombre;
	b.title = ruta;
	b.addEventListener('click', alClic);
	return b;
}

function cargarUnidades() {
	var cont = document.getElementById('unidades');
	cont.innerHTML = '';
	fetch('/api/unidades')
		.then(function (r) { return r.json(); })
		.then(function (unidades) {
			unidades.forEach(function (u) {
				cont.appendChild(chip(u.ruta, u.nombre, function () { ir(u.ruta); }));
			});
			cont.appendChild(chip('', '\\ Red', cargarRed));
			cont.appendChild(ayudaRed());
		})
		.catch(function () {
			cont.innerHTML = '<p class="error">No se pudieron cargar las unidades.</p>';
		});
}

function cargarRed() {
	var cont = document.getElementById('unidades');
	cont.innerHTML = '<p class="cargando">Buscando equipos en red…</p>';
	fetch('/api/red')
		.then(function (r) { return r.json(); })
		.then(function (servidores) {
			cont.innerHTML = '';
			cont.appendChild(chip('', '\\ Unidades', cargarUnidades));
			if (servidores.length === 0) {
				var p = document.createElement('p');
				p.className = 'vacio';
				p.textContent = 'No se encontraron equipos. Probá pegar la ruta directa en el campo de arriba.';
				cont.appendChild(p);
				cont.appendChild(ayudaRed());
				return;
			}
			servidores.forEach(function (s) {
				cont.appendChild(chip(s.ruta, s.nombre, function () { ir(s.ruta); }));
			});
			cont.appendChild(ayudaRed());
		})
		.catch(function () {
			cont.innerHTML = '<p class="error">No se pudo explorar la red.</p>';
		});
}

function ayudaRed() {
	var p = document.createElement('p');
	p.className = 'ayuda-red';
	p.textContent = 'Tip: podés pegar una ruta de red directa, ej. \\\\servidor\\carpeta';
	return p;
}

function cargarRecientes() {
	var seccion = document.getElementById('seccionRecientes');
	var cont = document.getElementById('recientes');
	if (!seccion || !cont) {
		return;
	}
	fetch('/api/recientes')
		.then(function (r) { return r.json(); })
		.then(function (lista) {
			if (!lista || lista.length === 0) {
				seccion.style.display = 'none';
				return;
			}
			seccion.style.display = '';
			cont.innerHTML = '';
			lista.forEach(function (r) {
				cont.appendChild(chip(r.ruta, r.ruta, function () { seleccionarReciente(r.ruta); }));
			});
		})
		.catch(function () {
			seccion.style.display = 'none';
		});
}

function seleccionarReciente(ruta) {
	document.getElementById('rutaModal').value = ruta;
	seleccionarActual();
}

function ir(ruta) {
	if (!ruta) {
		return;
	}
	document.getElementById('rutaModal').value = ruta;
	document.getElementById('rutaActual').textContent = ruta;
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
			cont.innerHTML = '<p class="error">No se pudo acceder a la carpeta (sin permisos o equipo sin conexión).</p>';
		});
}

function cargarBreadcrumb(ruta) {
	var cont = document.getElementById('breadcrumb');
	cont.innerHTML = '';
	if (ruta.indexOf('\\\\') === 0) {
		var resto = ruta.replace(/^\\\\+/, '');
		var partes = resto.split('\\').filter(function (p) { return p !== ''; });
		if (partes.length === 0) {
			cont.innerHTML = '<span class="crumb">Red</span>';
			return;
		}
		var acum = '\\\\';
		partes.forEach(function (p, i) {
			acum += p;
			var sp = document.createElement('span');
			sp.className = 'crumb';
			sp.textContent = p;
			sp.addEventListener('click', function () { ir(acum + '\\'); });
			cont.appendChild(sp);
			if (i < partes.length - 1) {
				acum += '\\';
				var sep = document.createElement('span');
				sep.className = 'sep';
				sep.textContent = '>';
				cont.appendChild(sep);
			}
		});
		return;
	}

	var norm = ruta;
	if (!norm.endsWith('\\') && !norm.endsWith('/')) {
		norm += '\\';
	}
	var partesLocal = norm.split('\\').filter(function (p) { return p !== ''; });
	var acumLocal = '';
	partesLocal.forEach(function (p, i) {
		acumLocal += p + '\\';
		var sp = document.createElement('span');
		sp.className = 'crumb';
		sp.textContent = p;
		sp.addEventListener('click', function () { ir(acumLocal); });
		cont.appendChild(sp);
		if (i < partesLocal.length - 1) {
			var sep = document.createElement('span');
			sep.className = 'sep';
			sep.textContent = '>';
			cont.appendChild(sep);
		}
	});
	if (partesLocal.length === 0) {
		cont.innerHTML = '<span class="crumb">\\</span>';
	}
}
