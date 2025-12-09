@import com.kapamejlbka.objectmanager.domain.user.AppUser
@import com.kapamejlbka.objectmanager.domain.user.AppRole
@import com.kapamejlbka.objectmanager.domain.user.dto.UserCreateRequest
@import com.kapamejlbka.objectmanager.domain.user.dto.UserUpdateRequest
@import org.springframework.security.web.csrf.CsrfToken

@param java.util.List<AppUser> users
@param java.util.List<AppRole> roles
@param UserCreateRequest userForm
@param UserUpdateRequest editUserForm = null
@param CsrfToken _csrf

<div class="d-flex justify-content-between align-items-center mb-3">
    <h2 class="h5 mb-0">Управление пользователями</h2>
    <a class="btn btn-primary" href="/admin?tab=users">Создать пользователя</a>
</div>

<div class="om-page-card mb-3">
    <div class="d-flex justify-content-between align-items-center mb-2">
        <h3 class="h5 mb-0">Список пользователей</h3>
    </div>
    <table class="table table-hover align-middle">
        <thead>
            <tr>
                <th>Логин</th>
                <th>ФИО</th>
                <th>Email</th>
                <th>Роли</th>
                <th>Статус</th>
                <th class="text-end">Действия</th>
            </tr>
        </thead>
        <tbody>
        @for(AppUser user : users) {
            <tr>
                <td class="fw-bold">@user.getUsername()</td>
                <td>@user.getFullName()</td>
                <td>@user.getEmail()</td>
                <td>
                    @for(AppRole role : user.getRoles()) {
                        <span class="badge bg-secondary me-1">@role.getName()</span>
                    }
                </td>
                <td>
                    <span class="badge @(user.isEnabled() ? "bg-success" : "bg-secondary")">@(user.isEnabled() ? "Включён" : "Отключён")</span>
                </td>
                <td class="text-end">
                    <div class="d-flex gap-2 justify-content-end">
                        <a class="btn btn-outline-secondary btn-sm" href="/admin?tab=users&editUserId=@user.getId()">Редактировать</a>
                        <form method="post" action="/admin/users/@user.getId()/status">
                            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                            <input type="hidden" name="enabled" value="@(user.isEnabled() ? "false" : "true")" />
                            <button type="submit" class="btn btn-outline-danger btn-sm">@(user.isEnabled() ? "Заблокировать" : "Разблокировать")</button>
                        </form>
                    </div>
                </td>
            </tr>
        }
        </tbody>
    </table>
</div>

<div class="om-page-card">
    <h3 class="h5 mb-3">Создать пользователя</h3>
    <form method="post" action="/admin/users">
        <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
        <div class="form-grid">
            <div class="form-field">
                <label for="username">Логин</label>
                <input id="username" name="username" class="form-control" value="@userForm.getUsername()" required />
            </div>
            <div class="form-field">
                <label for="password">Временный пароль</label>
                <input id="password" name="password" type="password" class="form-control" value="@userForm.getPassword()" required />
            </div>
            <div class="form-field">
                <label for="fullName">ФИО</label>
                <input id="fullName" name="fullName" class="form-control" value="@userForm.getFullName()" />
            </div>
            <div class="form-field">
                <label for="email">Email</label>
                <input id="email" name="email" type="email" class="form-control" value="@userForm.getEmail()" required />
            </div>
            <div class="form-field">
                <label for="roles">Роли</label>
                <select id="roles" name="roles" class="form-select" multiple size="3">
                    @for(AppRole role : roles) {
                        <option value="@role.getName()" @(userForm.getRoles().contains(role.getName()) ? "selected" : "")>@role.getName()</option>
                    }
                </select>
            </div>
            <div class="form-field">
                <label for="enabled">Статус</label>
                <div class="d-flex align-items-center mt-2">
                    <input type="hidden" name="enabled" value="false" />
                    <input type="checkbox" id="enabled" name="enabled" @(userForm.isEnabled() ? "checked" : "") />
                    <label for="enabled" class="ms-2">Включён</label>
                </div>
            </div>
        </div>
        <div class="form-actions">
            <button type="submit" class="btn btn-primary">Создать</button>
        </div>
    </form>
</div>

@if(editUserForm != null) {
    <div class="om-page-card mt-3">
        <h3 class="h5 mb-3">Редактирование пользователя</h3>
        <form method="post" action="/admin/users/@editUserForm.getId()">
            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
            <div class="form-grid">
                <div class="form-field">
                    <label for="editUsername">Логин</label>
                    <input id="editUsername" class="form-control" value="@editUserForm.getUsername()" disabled />
                </div>
                <div class="form-field">
                    <label for="newPassword">Новый пароль (по желанию)</label>
                    <input id="newPassword" name="newPassword" type="password" class="form-control" />
                </div>
                <div class="form-field">
                    <label for="editFullName">ФИО</label>
                    <input id="editFullName" name="fullName" class="form-control" value="@editUserForm.getFullName()" />
                </div>
                <div class="form-field">
                    <label for="editEmail">Email</label>
                    <input id="editEmail" name="email" type="email" class="form-control" value="@editUserForm.getEmail()" required />
                </div>
                <div class="form-field">
                    <label for="editRoles">Роли</label>
                    <select id="editRoles" name="roles" class="form-select" multiple size="3">
                        @for(AppRole role : roles) {
                            <option value="@role.getName()" @(editUserForm.getRoles().contains(role.getName()) ? "selected" : "")>@role.getName()</option>
                        }
                    </select>
                </div>
                <div class="form-field">
                    <label for="editEnabled">Статус</label>
                    <div class="d-flex align-items-center mt-2">
                        <input type="hidden" name="enabled" value="false" />
                        <input type="checkbox" id="editEnabled" name="enabled" @(editUserForm.isEnabled() ? "checked" : "") />
                        <label for="editEnabled" class="ms-2">Включён</label>
                    </div>
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Сохранить изменения</button>
                <a href="/admin?tab=users" class="btn">Отмена</a>
            </div>
        </form>
    </div>
}
