(function () {
    document.addEventListener('DOMContentLoaded', () => {
        const modalElement = document.getElementById('attachmentPreviewModal');
        const modalTitle = document.getElementById('attachmentPreviewTitle');
        const modalBody = document.getElementById('attachmentPreviewBody');

        if (!modalElement || !modalTitle || !modalBody) {
            return;
        }

        const previewModal = new bootstrap.Modal(modalElement);

        const renderFallback = (downloadUrl) => {
            modalBody.innerHTML = `
                <div class="text-center text-muted py-5">
                    <p class="mb-3">Невозможно отобразить предпросмотр. Используйте скачивание.</p>
                    <a class="btn btn-primary" href="${downloadUrl}" target="_blank">Скачать</a>
                </div>
            `;
        };

        const previewLinks = document.querySelectorAll('.om-attachment-preview-link');
        previewLinks.forEach((link) => {
            link.addEventListener('click', async (event) => {
                event.preventDefault();
                const attachmentId = link.dataset.id;
                if (!attachmentId) {
                    return;
                }

                const downloadUrl = `/attachments/${attachmentId}`;
                modalTitle.textContent = (link.textContent || '').trim() || 'Просмотр файла';
                modalBody.innerHTML = '<div class="text-center text-muted py-5">Загрузка...</div>';

                let contentType = link.dataset.contentType || '';
                try {
                    const response = await fetch(`/attachments/${attachmentId}/meta`);
                    if (response.ok) {
                        const meta = await response.json();
                        contentType = meta.contentType || contentType;
                        if (meta.fileName) {
                            modalTitle.textContent = meta.fileName;
                        }
                    }
                } catch (error) {
                    console.error('Не удалось получить метаданные файла', error);
                }

                if (contentType && contentType.startsWith('image/')) {
                    modalBody.innerHTML = `<img src="${downloadUrl}" class="img-fluid" alt="${modalTitle.textContent}">`;
                } else if (contentType && contentType.startsWith('application/pdf')) {
                    modalBody.innerHTML = `<iframe src="${downloadUrl}" style="width:100%;height:600px;border:none;"></iframe>`;
                } else {
                    renderFallback(downloadUrl);
                }

                previewModal.show();
            });
        });
    });
})();
