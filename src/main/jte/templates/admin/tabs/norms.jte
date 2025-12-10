@import com.kapamejlbka.objectmanager.domain.material.Material
@import com.kapamejlbka.objectmanager.domain.material.MaterialNorm
@import com.kapamejlbka.objectmanager.domain.material.MaterialNormContext
@import com.kapamejlbka.objectmanager.domain.material.dto.MaterialNormForm
@import org.springframework.security.web.csrf.CsrfToken

@param java.util.List<MaterialNorm> norms
@param MaterialNormForm normForm
@param MaterialNormForm editNormForm = null
@param java.util.List<MaterialNormContext> normContexts
@param java.util.List<Material> materials
@param CsrfToken _csrf

<div class="d-flex justify-content-between align-items-center mb-3">
    <h2 class="h5 mb-0">Нормы расчёта</h2>
    <a class="btn btn-primary" href="/admin?tab=norms">Добавить норму</a>
</div>

<div class="om-page-card mb-3">
    <div class="d-flex justify-content-between align-items-center mb-2">
        <h3 class="h5 mb-0">Таблица норм</h3>
        <span class="text-muted">@norms.size() записей</span>
    </div>
    <table class="table table-hover align-middle">
        <thead>
            <tr>
                <th>Контекст</th>
                <th>Материал</th>
                <th>Формула</th>
                <th>Описание</th>
                <th class="text-end">Действия</th>
            </tr>
        </thead>
        <tbody>
        @for(MaterialNorm norm : norms) {
            <tr>
                <td class="fw-bold">@norm.getContextType().getDisplayNameRu()</td>
                <td>@(norm.getMaterial() == null ? "" : norm.getMaterial().getCode() + " — " + norm.getMaterial().getName())</td>
                <td><code>@norm.getFormula()</code></td>
                <td>@(norm.getDescription() == null ? "" : norm.getDescription())</td>
                <td class="text-end">
                    <div class="d-flex gap-2 justify-content-end">
                        <a class="btn btn-outline-secondary btn-sm" href="/admin?tab=norms&editNormId=@norm.getId()">Редактировать</a>
                        <form method="post" action="/admin/norms/@norm.getId()/delete" onsubmit="return confirm('Удалить норму?');">
                            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                            <button type="submit" class="btn btn-outline-danger btn-sm">Удалить</button>
                        </form>
                    </div>
                </td>
            </tr>
        }
        </tbody>
    </table>
</div>

<div class="om-page-card">
    <h3 class="h5 mb-3">Добавить норму</h3>
    <form method="post" action="/admin/norms">
        <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
        <div class="form-grid">
            <div class="form-field">
                <label for="contextType">Контекст</label>
                <select id="contextType" name="contextType" class="form-select" required>
                    <option value="">Выберите контекст</option>
                    @for(MaterialNormContext context : normContexts) {
                        <option value="@context.name()" @(context.equals(normForm.getContextType()) ? "selected" : "")>@context.getDisplayNameRu()</option>
                    }
                </select>
                <p class="text-muted mb-0">Контексты определяют, где применяется материал</p>
            </div>
            <div class="form-field">
                <label for="materialId">Материал</label>
                <select id="materialId" name="materialId" class="form-select" required>
                    <option value="">Выберите материал</option>
                    @for(Material material : materials) {
                        <option value="@material.getId()" @(material.getId().equals(normForm.getMaterialId()) ? "selected" : "")>@material.getCode() — @material.getName()</option>
                    }
                </select>
            </div>
            <div class="form-field">
                <label for="formula">Формула</label>
                <input id="formula" name="formula" class="form-control" required placeholder="ceil(length / 0.4)" value="@normForm.getFormula()" />
                <p class="text-muted mb-0">Доступные переменные: length, deviceCount, extraSockets, fiberCores. Функции: ceil, floor, max, min.</p>
            </div>
            <div class="form-field">
                <label for="description">Описание</label>
                <textarea id="description" name="description" class="form-control" rows="3">@normForm.getDescription()</textarea>
            </div>
        </div>
        <div class="form-actions">
            <button type="submit" class="btn btn-primary">Сохранить</button>
        </div>
    </form>
</div>

@if(editNormForm != null) {
    <div class="om-page-card mt-3">
        <h3 class="h5 mb-3">Редактирование нормы</h3>
        <form method="post" action="/admin/norms/@editNormForm.getId()">
            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
            <div class="form-grid">
                <div class="form-field">
                    <label for="editContextType">Контекст</label>
                    <select id="editContextType" name="contextType" class="form-select" required>
                        @for(MaterialNormContext context : normContexts) {
                            <option value="@context.name()" @(context.equals(editNormForm.getContextType()) ? "selected" : "")>@context.getDisplayNameRu()</option>
                        }
                    </select>
                </div>
                <div class="form-field">
                    <label for="editMaterialId">Материал</label>
                    <select id="editMaterialId" name="materialId" class="form-select" required>
                        @for(Material material : materials) {
                            <option value="@material.getId()" @(material.getId().equals(editNormForm.getMaterialId()) ? "selected" : "")>@material.getCode() — @material.getName()</option>
                        }
                    </select>
                </div>
                <div class="form-field">
                    <label for="editFormula">Формула</label>
                    <input id="editFormula" name="formula" class="form-control" required value="@editNormForm.getFormula()" />
                </div>
                <div class="form-field">
                    <label for="editDescription">Описание</label>
                    <textarea id="editDescription" name="description" class="form-control" rows="3">@editNormForm.getDescription()</textarea>
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Обновить</button>
                <a href="/admin?tab=norms" class="btn">Отмена</a>
            </div>
        </form>
    </div>
}
