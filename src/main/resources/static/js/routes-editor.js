(function () {
    const editor = document.querySelector('.om-routes-editor');
    if (!editor) {
        return;
    }

    const calcId = editor.dataset.calcId;
    const apiBase = `/api/calculations/${calcId}/routes`;
    const topologyApi = `/api/calculations/${calcId}/topology`;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

    const routesList = document.getElementById('routes-list');
    const nodesLayer = document.getElementById('routes-nodes-layer');
    const linksLayer = document.getElementById('routes-links-layer');
    const inspector = document.getElementById('routes-inspector');
    const createForm = document.getElementById('route-create-form');
    const createNameInput = document.getElementById('route-create-name');
    const createTypeInput = document.getElementById('route-create-type');
    const createSurfaceInput = document.getElementById('route-create-surface');
    const canvasWrapper = document.querySelector('.om-routes-canvas-wrapper');

    const ROUTE_TYPES = ['CORRUGATED_PIPE', 'CABLE_CHANNEL', 'TRAY_OR_STRUCTURE', 'WIRE_ROPE', 'BARE_CABLE'];
    const SURFACE_TYPES = ['WALL', 'CEILING', 'STRUCTURE', 'BETON_OR_BRICK', 'METAL', 'WOOD', 'GYPSUM'];
    const ROUTE_COLORS = ['#2563eb', '#059669', '#f97316', '#8b5cf6', '#14b8a6', '#ef4444', '#0ea5e9', '#f59e0b'];

    let routes = [];
    let topology = {nodes: [], devices: [], links: []};
    let selected = {type: null, id: null};
    let colorMap = {};

    Promise.all([apiFetch(topologyApi), apiFetch(apiBase)])
        .then(([topologyResponse, routesResponse]) => Promise.all([topologyResponse.json(), routesResponse.json()]))
        .then(([topologyData, routesData]) => {
            topology = topologyData;
            topology.links = routesData.links || [];
            routes = routesData.routes || [];
            renderAll();
        })
        .catch(() => {
            inspector.textContent = 'Не удалось загрузить данные о трассах';
        });

    canvasWrapper?.addEventListener('click', () => {
        selected = {type: null, id: null};
        renderRoutesList();
        renderLinks();
        renderInspector();
    });

    createForm?.addEventListener('submit', (e) => {
        e.preventDefault();
        const payload = {
            name: createNameInput.value.trim(),
            routeType: createTypeInput.value,
            surfaceType: createSurfaceInput.value,
        };
        apiFetch(apiBase, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload),
        })
            .then((r) => (r.ok ? r.json() : Promise.reject()))
            .then(() => refreshRoutes())
            .then(() => {
                createNameInput.value = '';
                createSurfaceInput.value = '';
            })
            .catch(() => alert('Не удалось добавить трассу'));
    });

    function refreshRoutes() {
        return Promise.all([apiFetch(topologyApi), apiFetch(apiBase)])
            .then(([topologyResponse, routesResponse]) => Promise.all([topologyResponse.json(), routesResponse.json()]))
            .then(([topologyData, routesData]) => {
                topology = topologyData;
                topology.links = routesData.links || [];
                routes = routesData.routes || [];
                colorMap = {};
                renderAll();
            });
    }

    function renderAll() {
        renderRoutesList();
        renderNodes();
        renderLinks();
        renderInspector();
    }

    function renderRoutesList() {
        routesList.innerHTML = '';
        const counts = countLinksByRoute();
        routes
            .sort((a, b) => a.name.localeCompare(b.name))
            .forEach((route, idx) => {
                const item = document.createElement('div');
                item.className = 'om-routes-list-item';
                item.dataset.id = route.id;
                if (selected.type === 'route' && selected.id === route.id) {
                    item.classList.add('active');
                }
                const color = getRouteColor(route.id, idx);
                item.innerHTML = `<div><span class="om-route-color" style="background:${color}"></span>${route.name}<br><small class="text-muted">${route.routeType}</small></div><div class="text-muted small">${counts[route.id] || 0} линий</div>`;
                item.addEventListener('click', () => selectRoute(route.id));
                routesList.appendChild(item);
            });
    }

    function renderNodes() {
        nodesLayer.innerHTML = '';
        const defaults = {node: 40, device: 60};
        topology.nodes.forEach((node, idx) => {
            const el = document.createElement('div');
            el.className = 'om-route-node';
            el.textContent = `${node.code}\n${node.name}`;
            el.dataset.id = node.id;
            el.dataset.type = 'node';
            el.style.left = `${node.x ?? defaults.node * (idx + 1)}px`;
            el.style.top = `${node.y ?? defaults.node * (idx + 1)}px`;
            nodesLayer.appendChild(el);
        });
        topology.devices.forEach((device, idx) => {
            const el = document.createElement('div');
            el.className = 'om-route-device';
            el.textContent = `${device.code}\n${device.name}`;
            el.dataset.id = device.id;
            el.dataset.type = 'device';
            el.style.left = `${device.x ?? defaults.device * (idx + 1)}px`;
            el.style.top = `${(device.y ?? defaults.device * (idx + 1)) + 140}px`;
            nodesLayer.appendChild(el);
        });
    }

    function renderLinks() {
        const wrapperRect = nodesLayer.getBoundingClientRect();
        linksLayer.setAttribute('width', `${wrapperRect.width}`);
        linksLayer.setAttribute('height', `${wrapperRect.height}`);
        linksLayer.innerHTML = '';

        topology.links.forEach((link) => {
            const fromEl = getEndpointElement(link.fromNodeId, link.fromDeviceId);
            const toEl = getEndpointElement(link.toNodeId, link.toDeviceId);
            if (!fromEl || !toEl) {
                return;
            }
            const fromPos = getCenter(fromEl, wrapperRect);
            const toPos = getCenter(toEl, wrapperRect);
            const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
            line.setAttribute('x1', fromPos.x);
            line.setAttribute('y1', fromPos.y);
            line.setAttribute('x2', toPos.x);
            line.setAttribute('y2', toPos.y);
            line.dataset.id = link.id;
            line.classList.add('om-route-line');
            if (!link.routeId) {
                line.classList.add('unassigned');
            }
            const routeIdx = routes.findIndex((r) => r.id === link.routeId);
            if (routeIdx >= 0) {
                line.setAttribute('stroke', getRouteColor(link.routeId, routeIdx));
            }
            if (selected.type === 'link' && selected.id === link.id) {
                line.classList.add('active-line');
            }
            line.addEventListener('click', (e) => {
                e.stopPropagation();
                if (selected.type === 'route') {
                    assignLink(link.id, selected.id);
                } else {
                    selectLink(link.id);
                }
            });
            linksLayer.appendChild(line);
        });
    }

    function selectRoute(id) {
        selected = {type: 'route', id};
        renderRoutesList();
        renderLinks();
        renderInspector();
    }

    function selectLink(id) {
        selected = {type: 'link', id};
        renderRoutesList();
        renderLinks();
        renderInspector();
    }

    function renderInspector() {
        inspector.innerHTML = '';
        inspector.className = 'om-routes-inspector';
        if (selected.type === 'route') {
            renderRouteInspector();
        } else if (selected.type === 'link') {
            renderLinkInspector();
        } else {
            inspector.classList.add('text-muted', 'small');
            inspector.textContent = 'Выберите трассу или линию для просмотра свойств.';
        }
    }

    function renderRouteInspector() {
        const route = routes.find((r) => r.id === selected.id);
        if (!route) {
            inspector.textContent = 'Трасса не найдена';
            return;
        }
        const wrapper = document.createElement('div');
        const nameInput = document.createElement('input');
        nameInput.className = 'form-control form-control-sm mb-2';
        nameInput.value = route.name;
        const typeSelect = document.createElement('select');
        typeSelect.className = 'form-select form-select-sm mb-2';
        ROUTE_TYPES.forEach((t) => {
            const option = document.createElement('option');
            option.value = t;
            option.textContent = t;
            option.selected = route.routeType === t;
            typeSelect.appendChild(option);
        });
        const surfaceSelect = document.createElement('select');
        surfaceSelect.className = 'form-select form-select-sm mb-2';
        const surfaces = [...SURFACE_TYPES];
        if (route.surfaceType && !surfaces.includes(route.surfaceType)) {
            surfaces.push(route.surfaceType);
        }
        surfaces.forEach((s) => {
            const option = document.createElement('option');
            option.value = s;
            option.textContent = s;
            option.selected = (route.surfaceType || route.mountSurface) === s;
            surfaceSelect.appendChild(option);
        });
        const lengthField = document.createElement('div');
        lengthField.className = 'small text-muted mb-2';
        lengthField.textContent = `Длина: ${route.length ?? 0} м`;

        const saveBtn = document.createElement('button');
        saveBtn.className = 'btn btn-primary btn-sm w-100';
        saveBtn.textContent = 'Сохранить';
        saveBtn.addEventListener('click', () => {
            apiFetch(`${apiBase}/${route.id}`, {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    name: nameInput.value.trim(),
                    routeType: typeSelect.value,
                    surfaceType: surfaceSelect.value,
                    lengthMeters: route.length,
                }),
            })
                .then((r) => (r.ok ? r.json() : Promise.reject()))
                .then(() => refreshRoutes())
                .catch(() => alert('Не удалось сохранить трассу'));
        });

        wrapper.append(labelWithControl('Имя', nameInput));
        wrapper.append(labelWithControl('Тип', typeSelect));
        wrapper.append(labelWithControl('Поверхность', surfaceSelect));
        wrapper.append(lengthField);
        wrapper.append(saveBtn);
        inspector.appendChild(wrapper);
    }

    function renderLinkInspector() {
        const link = topology.links.find((l) => l.id === selected.id);
        if (!link) {
            inspector.textContent = 'Линия не найдена';
            return;
        }
        const routeName = routes.find((r) => r.id === link.routeId)?.name || 'Не привязана';
        const wrapper = document.createElement('div');
        wrapper.innerHTML = `
            <p class="mb-1"><strong>Линия #${link.id}</strong></p>
            <p class="mb-1">Откуда: <span class="text-muted">${describeEndpoint(link.fromNodeId, link.fromDeviceId)}</span></p>
            <p class="mb-1">Куда: <span class="text-muted">${describeEndpoint(link.toNodeId, link.toDeviceId)}</span></p>
            <p class="mb-1">Тип: <span class="text-muted">${link.linkType || '—'}</span></p>
            <p class="mb-1">Длина: <span class="text-muted">${link.length ?? '—'} м</span></p>
            <p class="mb-2">Трасса: <span class="text-muted">${routeName}</span></p>
        `;
        if (routes.length) {
            const assignWrapper = document.createElement('div');
            assignWrapper.className = 'mb-2';
            const assignLabel = document.createElement('label');
            assignLabel.className = 'form-label form-label-sm';
            assignLabel.textContent = 'Назначить на трассу';
            const assignSelect = document.createElement('select');
            assignSelect.className = 'form-select form-select-sm';
            routes.forEach((route) => {
                const option = document.createElement('option');
                option.value = route.id;
                option.textContent = route.name;
                option.selected = route.id === link.routeId || (!link.routeId && routes[0]?.id === route.id);
                assignSelect.appendChild(option);
            });
            const assignBtn = document.createElement('button');
            assignBtn.className = 'btn btn-primary btn-sm w-100 mt-2';
            assignBtn.textContent = 'Добавить в трассу';
            assignBtn.addEventListener('click', () => {
                const routeId = Number(assignSelect.value);
                if (routeId) {
                    assignLink(link.id, routeId);
                }
            });
            assignWrapper.append(assignLabel, assignSelect, assignBtn);
            wrapper.appendChild(assignWrapper);
        }
        const unassignBtn = document.createElement('button');
        unassignBtn.className = 'btn btn-outline-secondary btn-sm';
        unassignBtn.textContent = 'Убрать из трассы';
        unassignBtn.disabled = !link.routeId;
        unassignBtn.addEventListener('click', () => {
            if (!link.routeId) return;
            apiFetch(`${apiBase}/${link.routeId}/unassign-link`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({linkId: link.id}),
            })
                .then(() => refreshRoutes())
                .catch(() => alert('Не удалось отвязать линию'));
        });
        wrapper.appendChild(unassignBtn);
        inspector.appendChild(wrapper);
    }

    function getEndpointElement(nodeId, deviceId) {
        if (nodeId) {
            return nodesLayer.querySelector(`.om-route-node[data-id="${nodeId}"]`);
        }
        if (deviceId) {
            return nodesLayer.querySelector(`.om-route-device[data-id="${deviceId}"]`);
        }
        return null;
    }

    function getCenter(el, wrapperRect) {
        const rect = el.getBoundingClientRect();
        return {
            x: rect.left - wrapperRect.left + rect.width / 2,
            y: rect.top - wrapperRect.top + rect.height / 2,
        };
    }

    function describeEndpoint(nodeId, deviceId) {
        if (nodeId) {
            const node = topology.nodes.find((n) => n.id === nodeId);
            return node ? `[Узел] ${node.name}` : `Узел #${nodeId}`;
        }
        if (deviceId) {
            const device = topology.devices.find((d) => d.id === deviceId);
            return device ? `[Устройство] ${device.name}` : `Устройство #${deviceId}`;
        }
        return '—';
    }

    function countLinksByRoute() {
        const counts = {};
        topology.links.forEach((link) => {
            if (!link.routeId) return;
            counts[link.routeId] = (counts[link.routeId] || 0) + 1;
        });
        return counts;
    }

    function assignLink(linkId, routeId) {
        apiFetch(`${apiBase}/${routeId}/assign-link`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({linkId}),
        })
            .then(() => refreshRoutes())
            .catch(() => alert('Не удалось привязать линию'));
    }

    function labelWithControl(label, control) {
        const wrapper = document.createElement('div');
        const lbl = document.createElement('label');
        lbl.className = 'form-label form-label-sm';
        lbl.textContent = label;
        wrapper.appendChild(lbl);
        wrapper.appendChild(control);
        return wrapper;
    }

    function getRouteColor(routeId, idx) {
        if (colorMap[routeId]) {
            return colorMap[routeId];
        }
        const color = ROUTE_COLORS[idx % ROUTE_COLORS.length];
        colorMap[routeId] = color;
        return color;
    }

    function apiFetch(url, options = {}) {
        const headers = options.headers ? {...options.headers} : {};
        if (csrfToken) {
            headers[csrfHeader] = csrfToken;
        }
        return fetch(url, {...options, headers});
    }
})();
