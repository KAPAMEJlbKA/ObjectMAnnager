@import com.kapamejlbka.objectmanager.domain.user.AppUser
@import com.kapamejlbka.objectmanager.domain.user.AppRole
@import com.kapamejlbka.objectmanager.domain.user.dto.UserCreateRequest
@import com.kapamejlbka.objectmanager.domain.user.dto.UserUpdateRequest
@import com.kapamejlbka.objectmanager.domain.settings.dto.CalculationSettingsDto
@import com.kapamejlbka.objectmanager.domain.material.Material
@import com.kapamejlbka.objectmanager.domain.material.MaterialCategory
@import com.kapamejlbka.objectmanager.domain.material.MaterialNorm
@import com.kapamejlbka.objectmanager.domain.material.MaterialNormContext
@import com.kapamejlbka.objectmanager.domain.material.dto.MaterialForm
@import com.kapamejlbka.objectmanager.domain.material.dto.MaterialNormForm
@import org.springframework.security.web.csrf.CsrfToken

@param String activeTab = "users"
@param java.util.List<AppUser> users
@param java.util.List<AppRole> roles
@param UserCreateRequest userForm
@param UserUpdateRequest editUserForm = null
@param CalculationSettingsDto settings
@param java.util.List<Material> materials
@param java.util.List<MaterialCategory> materialCategories
@param MaterialForm materialForm
@param MaterialForm editMaterialForm = null
@param String materialQuery = ""
@param MaterialCategory materialCategory = null
@param java.util.List<MaterialNorm> norms
@param MaterialNormForm normForm
@param MaterialNormForm editNormForm = null
@param java.util.List<MaterialNormContext> normContexts
@param java.util.List<Material> normMaterials
@param String flashSuccess = null
@param String flashError = null
@param CsrfToken _csrf

@templates.layout(title = "Администрирование", activePage = "admin", flashSuccess = flashSuccess, flashError = flashError) {
    <section class="page-card">
        <div class="page-header">
            <div>
                <p class="eyebrow">Дашборд</p>
                <h1 class="page-title">Администрирование</h1>
                <p class="page-subtitle">Управление пользователями, справочниками и настройками расчёта</p>
            </div>
        </div>

        <div class="om-page-card">
            <ul class="nav nav-tabs">
                <li class="nav-item">
                    <a class="nav-link @("users".equals(activeTab) ? "active" : "")" href="/admin?tab=users">Пользователи</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link @("settings".equals(activeTab) ? "active" : "")" href="/admin?tab=settings">Настройки</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link @("materials".equals(activeTab) ? "active" : "")" href="/admin?tab=materials">Материалы</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link @("norms".equals(activeTab) ? "active" : "")" href="/admin?tab=norms">Нормы расчёта</a>
                </li>
            </ul>

            <div class="tab-content mt-3">
                <div class="tab-pane @("users".equals(activeTab) ? "active" : "")">
                    @templates.admin.tabs.users(users = users, roles = roles, userForm = userForm, editUserForm = editUserForm, _csrf = _csrf)
                </div>
                <div class="tab-pane @("settings".equals(activeTab) ? "active" : "")">
                    @templates.admin.tabs.settings(settings = settings, _csrf = _csrf)
                </div>
                <div class="tab-pane @("materials".equals(activeTab) ? "active" : "")">
                    @templates.admin.tabs.materials(
                        materials = materials,
                        categories = materialCategories,
                        materialForm = materialForm,
                        editMaterialForm = editMaterialForm,
                        searchQuery = materialQuery,
                        selectedCategory = materialCategory,
                        _csrf = _csrf)
                </div>
                <div class="tab-pane @("norms".equals(activeTab) ? "active" : "")">
                    @templates.admin.tabs.norms(
                        norms = norms,
                        normForm = normForm,
                        editNormForm = editNormForm,
                        normContexts = normContexts,
                        materials = normMaterials,
                        _csrf = _csrf)
                </div>
            </div>
        </div>
    </section>
}
