@import com.kapamejlbka.objectmanager.domain.material.Material
@import com.kapamejlbka.objectmanager.domain.material.MaterialCategory
@import com.kapamejlbka.objectmanager.domain.material.dto.MaterialForm
@import org.springframework.security.web.csrf.CsrfToken

@param java.util.List<Material> materials
@param java.util.List<MaterialCategory> categories
@param MaterialForm materialForm
@param MaterialForm editMaterialForm = null
@param String searchQuery = ""
@param MaterialCategory selectedCategory = null
@param CsrfToken _csrf

<div class="d-flex justify-content-between align-items-center mb-3">
    <h2 class="h5 mb-0">Справочник материалов</h2>
    <a class="btn btn-primary" href="/admin?tab=materials">Добавить материал</a>
</div>

<div class="om-page-card mb-3">
    <form class="row g-2 align-items-end" method="get" action="/admin">
        <input type="hidden" name="tab" value="materials" />
        <div class="col-auto">
            <label class="form-label" for="search">Поиск</label>
            <input class="form-control" id="search" name="search" value="@searchQuery" placeholder="Код или название" />
        </div>
        <div class="col-auto">
            <label class="form-label" for="category">Категория</label>
            <select class="form-select" id="category" name="category">
                <option value="">Все категории</option>
                @for(MaterialCategory category : categories) {
                    <option value="@category.name()" @(category.equals(selectedCategory) ? "selected" : "")>@category.getDisplayNameRu()</option>
                }
            </select>
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-primary">Найти</button>
        </div>
    </form>
</div>

<div class="om-page-card mb-3">
    <div class="d-flex justify-content-between align-items-center mb-2">
        <h3 class="h5 mb-0">Материалы</h3>
        <span class="text-muted">@materials.size() записей</span>
    </div>
    <table class="table table-hover align-middle">
        <thead>
            <tr>
                <th>Код</th>
                <th>Название</th>
                <th>Категория</th>
                <th>Ед. изм.</th>
                <th>Примечание</th>
                <th class="text-end">Действия</th>
            </tr>
        </thead>
        <tbody>
        @for(Material material : materials) {
            <tr>
                <td class="fw-bold">@material.getCode()</td>
                <td>@material.getName()</td>
                <td>@material.getCategory().getDisplayNameRu()</td>
                <td>@material.getUnit()</td>
                <td>@(material.getNotes() == null ? "" : material.getNotes())</td>
                <td class="text-end">
                    <div class="d-flex gap-2 justify-content-end">
                        <a class="btn btn-outline-secondary btn-sm" href="/admin?tab=materials&editMaterialId=@material.getId()">Редактировать</a>
                        <form method="post" action="/admin/materials/@material.getId()/delete" onsubmit="return confirm('Удалить материал?');">
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
    <h3 class="h5 mb-3">Добавить материал</h3>
    <form method="post" action="/admin/materials">
        <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
        <div class="form-grid">
            <div class="form-field">
                <label for="code">Код</label>
                <input id="code" name="code" class="form-control" required value="@materialForm.getCode()" />
            </div>
            <div class="form-field">
                <label for="name">Название</label>
                <input id="name" name="name" class="form-control" required value="@materialForm.getName()" />
            </div>
            <div class="form-field">
                <label for="categoryInput">Категория</label>
                <select id="categoryInput" name="category" class="form-select" required>
                    <option value="">Выберите категорию</option>
                    @for(MaterialCategory category : categories) {
                        <option value="@category.name()" @(category.equals(materialForm.getCategory()) ? "selected" : "")>@category.getDisplayNameRu()</option>
                    }
                </select>
            </div>
            <div class="form-field">
                <label for="unit">Единица измерения</label>
                <input id="unit" name="unit" class="form-control" required value="@materialForm.getUnit()" placeholder="шт / м" />
            </div>
            <div class="form-field full-width">
                <label for="notes">Примечание</label>
                <textarea id="notes" name="notes" class="form-control" rows="3">@materialForm.getNotes()</textarea>
            </div>
        </div>
        <div class="form-actions">
            <button type="submit" class="btn btn-primary">Сохранить</button>
        </div>
    </form>
</div>

@if(editMaterialForm != null) {
    <div class="om-page-card mt-3">
        <h3 class="h5 mb-3">Редактирование материала</h3>
        <form method="post" action="/admin/materials/@editMaterialForm.getId()">
            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
            <div class="form-grid">
                <div class="form-field">
                    <label for="editCode">Код</label>
                    <input id="editCode" class="form-control" name="code" required value="@editMaterialForm.getCode()" />
                </div>
                <div class="form-field">
                    <label for="editName">Название</label>
                    <input id="editName" class="form-control" name="name" required value="@editMaterialForm.getName()" />
                </div>
                <div class="form-field">
                    <label for="editCategory">Категория</label>
                    <select id="editCategory" class="form-select" name="category" required>
                        <option value="">Выберите категорию</option>
                        @for(MaterialCategory category : categories) {
                            <option value="@category.name()" @(category.equals(editMaterialForm.getCategory()) ? "selected" : "")>@category.getDisplayNameRu()</option>
                        }
                    </select>
                </div>
                <div class="form-field">
                    <label for="editUnit">Единица измерения</label>
                    <input id="editUnit" class="form-control" name="unit" required value="@editMaterialForm.getUnit()" />
                </div>
                <div class="form-field full-width">
                    <label for="editNotes">Примечание</label>
                    <textarea id="editNotes" name="notes" class="form-control" rows="3">@editMaterialForm.getNotes()</textarea>
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Обновить</button>
                <a href="/admin?tab=materials" class="btn">Отмена</a>
            </div>
        </form>
    </div>
}
