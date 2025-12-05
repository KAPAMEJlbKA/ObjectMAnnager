(function () {
    const editor = document.querySelector('.om-topology-editor');
    if (!editor) {
        return;
    }

    const calcId = editor.dataset.calcId;
    const apiBase = `/api/calculations/${calcId}/topology`;
    const deviceList = document.getElementById('device-list');
    const nodesLayer = document.getElementById('topology-nodes-layer');
    const linksLayer = document.getElementById('topology-links-layer');
    const inspectorEmpty = document.getElementById('inspector-empty');
    const inspectorContent = document.getElementById('inspector-content');
    const linkOptions = ['UTP', 'FIBER', 'POWER', 'WIFI'];

    let topology = {nodes: [], devices: [], links: []};
    let selected = {type: null, id: null};

    function fetchTopology() {
        fetch(apiBase)
            .then((r) => r.json())
            .then((data) => {
                topology = data;
                renderAll();
            })
            .catch(() => alert('Не удалось загрузить схему.'));
    }

    function renderAll() {
        renderDeviceList();
        renderNodes();
        renderLinks();
        clearSelection();
    }

    function renderDeviceList() {
        deviceList.innerHTML = '';
        if (!topology.devices.length) {
            deviceList.innerHTML = '<p class="text-muted small mb-0">Устройства не добавлены.</p>';
            return;
        }
        topology.devices.forEach((device) => {
            const item = document.createElement('div');
            item.className = 'om-topology-list-item';
            item.textContent = `${device.code} — ${device.name}`;
            item.addEventListener('click', () => selectItem('device', device.id));
            deviceList.appendChild(item);
        });
    }

    function renderNodes() {
        nodesLayer.innerHTML = '';
        const defaultPositions = {node: 40, device: 60};

        topology.nodes.forEach((node, idx) => {
            const el = document.createElement('div');
            el.className = 'om-node';
            el.textContent = `${node.code}\n${node.name}`;
            el.dataset.type = 'node';
            el.dataset.id = node.id;
            el.style.left = `${(node.x ?? (defaultPositions.node * (idx + 1)))}px`;
            el.style.top = `${(node.y ?? (defaultPositions.node * (idx + 1)))}px`;
            attachDrag(el, node, 'node');
            el.addEventListener('click', (e) => {
                e.stopPropagation();
                selectItem('node', node.id);
            });
            nodesLayer.appendChild(el);
        });

        topology.devices.forEach((device, idx) => {
            const el = document.createElement('div');
            el.className = 'om-device';
            el.textContent = `${device.code}\n${device.name}`;
            el.dataset.type = 'device';
            el.dataset.id = device.id;
            el.style.left = `${(device.x ?? (defaultPositions.device * (idx + 1)))}px`;
            el.style.top = `${(device.y ?? (defaultPositions.device * (idx + 1)) + 120)}px`;
            attachDrag(el, device, 'device');
            el.addEventListener('click', (e) => {
                e.stopPropagation();
                selectItem('device', device.id);
            });
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
            line.classList.add('om-link-line');
            line.classList.add(link.linkType ? link.linkType.toLowerCase() : '');
            line.addEventListener('click', (e) => {
                e.stopPropagation();
                selectItem('link', link.id);
            });
            linksLayer.appendChild(line);
        });
    }

    function getEndpointElement(nodeId, deviceId) {
        if (nodeId) {
            return nodesLayer.querySelector(`.om-node[data-id="${nodeId}"]`);
        }
        if (deviceId) {
            return nodesLayer.querySelector(`.om-device[data-id="${deviceId}"]`);
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

    function attachDrag(el, item, type) {
        let startX = 0;
        let startY = 0;
        let startLeft = 0;
        let startTop = 0;
        let dragging = false;

        const onMouseDown = (e) => {
            dragging = true;
            startX = e.clientX;
            startY = e.clientY;
            startLeft = parseInt(el.style.left || '0', 10);
            startTop = parseInt(el.style.top || '0', 10);
            document.addEventListener('mousemove', onMouseMove);
            document.addEventListener('mouseup', onMouseUp);
        };

        const onMouseMove = (e) => {
            if (!dragging) return;
            const dx = e.clientX - startX;
            const dy = e.clientY - startY;
            const newLeft = startLeft + dx;
            const newTop = startTop + dy;
            el.style.left = `${newLeft}px`;
            el.style.top = `${newTop}px`;
            updateTopologyPosition(type, item.id, newLeft, newTop);
            renderLinks();
        };

        const onMouseUp = () => {
            if (!dragging) return;
            dragging = false;
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
            persistPosition(type, item.id);
        };

        el.addEventListener('mousedown', onMouseDown);
    }

    function updateTopologyPosition(type, id, x, y) {
        if (type === 'node') {
            topology.nodes = topology.nodes.map((n) => (n.id === id ? {...n, x, y} : n));
        } else {
            topology.devices = topology.devices.map((d) => (d.id === id ? {...d, x, y} : d));
        }
    }

    function persistPosition(type, id) {
        const target =
            type === 'node'
                ? topology.nodes.find((n) => n.id === id)
                : topology.devices.find((d) => d.id === id);
        if (!target) return;
        fetch(`${apiBase}/${type === 'node' ? 'nodes' : 'devices'}/${id}/position`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({x: Math.round(target.x ?? 0), y: Math.round(target.y ?? 0)}),
        }).catch(() => console.warn('Не удалось сохранить позицию'));
    }

    function selectItem(type, id) {
        selected = {type, id};
        highlightSelection();
        renderInspector();
    }

    function clearSelection() {
        selected = {type: null, id: null};
        highlightSelection();
        inspectorContent.classList.add('d-none');
        inspectorEmpty.classList.remove('d-none');
        inspectorContent.innerHTML = '';
    }

    function highlightSelection() {
        nodesLayer.querySelectorAll('.om-node, .om-device').forEach((el) => {
            el.classList.toggle('om-selected',
                selected.type !== null && el.dataset.type === selected.type && Number(el.dataset.id) === selected.id);
        });
        linksLayer.querySelectorAll('.om-link-line').forEach((line) => {
            line.classList.toggle('om-selected', selected.type === 'link' && Number(line.dataset.id) === selected.id);
        });
    }

    function renderInspector() {
        inspectorContent.innerHTML = '';
        if (!selected.type) {
            inspectorContent.classList.add('d-none');
            inspectorEmpty.classList.remove('d-none');
            return;
        }
        inspectorContent.classList.remove('d-none');
        inspectorEmpty.classList.add('d-none');

        if (selected.type === 'link') {
            renderLinkInspector();
            return;
        }
        const info = document.createElement('div');
        info.className = 'text-muted small';
        info.textContent = selected.type === 'node'
            ? 'Узел выбран. Редактирование параметров будет добавлено позже.'
            : 'Устройство выбрано. Редактирование параметров будет добавлено позже.';
        inspectorContent.appendChild(info);
    }

    function renderLinkInspector() {
        const link = topology.links.find((l) => l.id === selected.id);
        if (!link) {
            clearSelection();
            return;
        }
        const wrapper = document.createElement('div');
        wrapper.className = 'om-topology-inspector';
        wrapper.innerHTML = `
            <div class="mb-2">
                <label class="form-label">Тип линии</label>
                <select class="form-select form-select-sm" id="link-type">
                    ${linkOptions.map((o) => `<option value="${o}" ${link.linkType === o ? 'selected' : ''}>${o}</option>`).join('')}
                </select>
            </div>
            <div class="mb-2">
                <label class="form-label">Длина (м)</label>
                <input type="number" step="0.1" class="form-control form-control-sm" id="link-length" value="${link.length ?? ''}">
            </div>
            <div class="mb-2 fiber-only ${link.linkType === 'FIBER' ? '' : 'd-none'}" id="fiber-fields">
                <label class="form-label">Количество волокон</label>
                <input type="number" min="1" class="form-control form-control-sm mb-2" id="fiber-cores" value="${link.fiberCores ?? ''}">
                <label class="form-label">Сварки</label>
                <input type="number" min="0" class="form-control form-control-sm mb-2" id="fiber-splice" value="${link.fiberSpliceCount ?? ''}">
                <label class="form-label">Соединения</label>
                <input type="number" min="0" class="form-control form-control-sm" id="fiber-connector" value="${link.fiberConnectorCount ?? ''}">
            </div>
            <div class="d-flex gap-2 mt-3">
                <button class="btn btn-primary btn-sm" id="save-link">Сохранить</button>
                <button class="btn btn-outline-danger btn-sm" id="delete-link">Удалить</button>
            </div>
        `;
        inspectorContent.appendChild(wrapper);

        const typeSelect = wrapper.querySelector('#link-type');
        const fiberFields = wrapper.querySelector('#fiber-fields');
        typeSelect.addEventListener('change', () => {
            fiberFields.classList.toggle('d-none', typeSelect.value !== 'FIBER');
        });

        wrapper.querySelector('#save-link').addEventListener('click', () => {
            const payload = {
                linkType: typeSelect.value,
                length: parseFloatValue(wrapper.querySelector('#link-length').value),
                fiberCores: parseIntValue(wrapper.querySelector('#fiber-cores').value),
                fiberSpliceCount: parseIntValue(wrapper.querySelector('#fiber-splice').value),
                fiberConnectorCount: parseIntValue(wrapper.querySelector('#fiber-connector').value)
            };
            fetch(`${apiBase}/links/${link.id}`, {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            })
                .then((r) => {
                    if (!r.ok) {
                        throw new Error('failed');
                    }
                    return r.json();
                })
                .then((updated) => {
                    topology.links = topology.links.map((l) => (l.id === updated.id ? updated : l));
                    renderLinks();
                    renderInspector();
                })
                .catch(() => alert('Не удалось сохранить изменения линии'));
        });

        wrapper.querySelector('#delete-link').addEventListener('click', () => {
            if (!confirm('Удалить линию?')) {
                return;
            }
            fetch(`${apiBase}/links/${link.id}`, {method: 'DELETE'})
                .then((r) => {
                    if (!r.ok) {
                        throw new Error('failed');
                    }
                    topology.links = topology.links.filter((l) => l.id !== link.id);
                    clearSelection();
                    renderLinks();
                })
                .catch(() => alert('Не удалось удалить линию'));
        });
    }

    function parseIntValue(value) {
        if (value === undefined || value === null || value === '') {
            return null;
        }
        const parsed = parseInt(value, 10);
        return Number.isNaN(parsed) ? null : parsed;
    }

    function parseFloatValue(value) {
        if (value === undefined || value === null || value === '') {
            return null;
        }
        const parsed = parseFloat(value);
        return Number.isNaN(parsed) ? null : parsed;
    }

    editor.addEventListener('click', (e) => {
        if (e.target === editor || e.target.classList.contains('om-topology-editor')) {
            clearSelection();
        }
    });

    window.addEventListener('resize', () => renderLinks());

    fetchTopology();
})();
