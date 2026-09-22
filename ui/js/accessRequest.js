console.log("Access Request Script Loaded");

(function() {
    
    const modalOverlay = document.createElement("div");
    modalOverlay.id = "workgroup-modal-overlay";
    modalOverlay.className = "workgroup-modal-overlay";
    modalOverlay.style.display = "none";

    const modalContent = document.createElement("div");
    modalContent.className = "workgroup-modal-content";

    const closeBtn = document.createElement("span");
    closeBtn.className = "workgroup-modal-close";
    closeBtn.innerHTML = "&times;";

    const modalTitle = document.createElement("h3");
    modalTitle.id = "workgroup-modal-title";
    modalTitle.innerText = "Gente en el Workgroup";

    const peopleList = document.createElement("ul");
    peopleList.id = "workgroup-people-list";

    modalContent.appendChild(closeBtn);
    modalContent.appendChild(modalTitle);
    modalContent.appendChild(peopleList);
    modalOverlay.appendChild(modalContent);
    document.body.appendChild(modalOverlay);

    // Close modal events
    closeBtn.addEventListener("click", function() {
        modalOverlay.style.display = "none";
    });

    modalOverlay.addEventListener("click", function(e) {
        if (e.target === modalOverlay) {
            modalOverlay.style.display = "none";
        }
    });

    function openWorkgroupModal(targetName) {
        modalOverlay.style.display = "flex";
        loadWorkgroupPeople(targetName);
    }

    function loadWorkgroupPeople(targetName) {
        peopleList.innerHTML = "<li>Cargando miembros...</li>";
        modalTitle.innerText = "Buscando: " + targetName;

        let contextPath = (typeof SailPoint !== 'undefined' && SailPoint.CONTEXT_PATH) ? SailPoint.CONTEXT_PATH : '/identityiq';
        let url = contextPath + '/plugin/rest/WorkGroupInfoPlugin/workgroupMembers?name=' + encodeURIComponent(targetName);
        
        // Obtener token CSRF si es necesario
        let csrfToken = '';
        if (typeof PluginHelper !== 'undefined') {
            csrfToken = PluginHelper.getCsrfToken();
        } else if (typeof SailPoint !== 'undefined' && SailPoint.CSRF_TOKEN) {
            csrfToken = SailPoint.CSRF_TOKEN;
        }

        fetch(url, {
            method: 'GET',
            headers: {
                'X-XSRF-TOKEN': csrfToken
            }
        })
        .then(res => {
            if (!res.ok) throw new Error('Network response was not ok');
            return res.json();
        })
        .then(data => {
            peopleList.innerHTML = "";
            
            if (data.status === "error") {
                modalTitle.innerText = "Error";
                peopleList.innerHTML = `<li>${data.message}</li>`;
                return;
            }

            // Cambiar el título del modal según si es un Workgroup o Identidad individual
            if (data.isWorkgroup) {
                modalTitle.innerText = "Miembros de " + data.objectName;
            } else {
                modalTitle.innerText = "Usuario: " + data.objectName;
            }

            if (data.members && data.members.length > 0) {
                data.members.forEach(member => {
                    let li = document.createElement("li");
                    li.innerText = member;
                    peopleList.appendChild(li);
                });
            } else {
                peopleList.innerHTML = "<li>No se encontraron miembros</li>";
            }
        })
        .catch(err => {
            console.error(err);
            modalTitle.innerText = "Error";
            peopleList.innerHTML = "<li>Error al cargar la información</li>";
        });
    }

    // Function to scan the DOM and make workgroup references clickable
    function scanAndInject() {
        // Usamos TreeWalker para buscar nodos de texto de manera eficiente y segura
        const walker = document.createTreeWalker(
            document.body,
            NodeFilter.SHOW_TEXT,
            {
                acceptNode: function(node) {
                    const parent = node.parentNode;
                    if (!parent) return NodeFilter.FILTER_REJECT;

                    // Evitar procesar dentro de elementos interactivos, scripts o estilos
                    const tagName = parent.tagName;
                    if (['SCRIPT', 'STYLE', 'BUTTON', 'INPUT', 'SELECT', 'A', 'TEXTAREA'].includes(tagName)) {
                        return NodeFilter.FILTER_REJECT;
                    }

                    // Evitar procesar si ya está dentro de un span inyectado por nosotros
                    if (parent.classList.contains('clickable-identity')) {
                        return NodeFilter.FILTER_REJECT;
                    }

                    // Coincidir con cualquier palabra que contenga "workgroup" (case-insensitive)
                    // P.ej. "SelfRegistration-Workgroup-Test", "MyWorkgroup", "Workgroup-A"
                    const regex = /(\S*workgroup\S*)/gi;
                    if (regex.test(node.nodeValue)) {
                        return NodeFilter.FILTER_ACCEPT;
                    }

                    return NodeFilter.FILTER_REJECT;
                }
            }
        );

        const nodesToReplace = [];
        while (walker.nextNode()) {
            nodesToReplace.push(walker.currentNode);
        }

        nodesToReplace.forEach(node => {
            const text = node.nodeValue;
            const regex = /(\S*workgroup\S*)/gi;
            regex.lastIndex = 0;

            const parent = node.parentNode;
            if (!parent) return;

            const fragment = document.createDocumentFragment();
            let lastIndex = 0;
            let match;

            while ((match = regex.exec(text)) !== null) {
                const matchText = match[1];
                const matchIndex = match.index;

                // Agregar el texto previo al match
                if (matchIndex > lastIndex) {
                    fragment.appendChild(document.createTextNode(text.substring(lastIndex, matchIndex)));
                }

                // Sanitizar el nombre del workgroup quitando signos de puntuación iniciales o finales (p.ej. puntos, comas, paréntesis)
                const cleanMatchText = matchText.replace(/^[^\w\u00C0-\u017F]+|[^\w\u00C0-\u017F]+$/g, '');

                // Crear el span interactivo discreto con subrayado punteado
                const span = document.createElement('span');
                span.className = 'clickable-identity';
                span.style.cursor = 'pointer';
                span.style.borderBottom = '1px dotted #888';
                span.style.color = 'inherit';
                span.style.fontWeight = 'inherit';
                span.title = 'Ver miembros del workgroup';
                span.textContent = cleanMatchText;

                // Listener de click para disparar la llamada al modal
                span.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    openWorkgroupModal(cleanMatchText);
                });

                fragment.appendChild(span);
                lastIndex = regex.lastIndex;
            }

            // Agregar el texto restante después de la última coincidencia
            if (lastIndex < text.length) {
                fragment.appendChild(document.createTextNode(text.substring(lastIndex)));
            }

            parent.replaceChild(fragment, node);
        });
    }

    // Ejecutar el escaneo periódicamente cada 1.5 segundos ya que SailPoint usa AngularJS (SPA)
    // y las vistas se renderizan dinámicamente.
    setInterval(scanAndInject, 1500);

    // Ejecutar inmediatamente también
    scanAndInject();
})();
