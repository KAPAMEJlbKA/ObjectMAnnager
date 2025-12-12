@param String activeTab
@param java.util.List<com.kapamejlbka.objectmanager.domain.user.AppUser> users
@param java.util.List<com.kapamejlbka.objectmanager.domain.user.AppRole> roles
@param com.kapamejlbka.objectmanager.domain.user.dto.UserCreateRequest userForm
@param com.kapamejlbka.objectmanager.domain.user.dto.UserUpdateRequest editUserForm
@param com.kapamejlbka.objectmanager.domain.settings.dto.CalculationSettingsDto settings
@param java.util.List<com.kapamejlbka.objectmanager.domain.material.Material> materials
@param java.util.List<com.kapamejlbka.objectmanager.domain.material.MaterialCategory> materialCategories
@param com.kapamejlbka.objectmanager.domain.material.dto.MaterialForm materialForm
@param String materialQuery
@param com.kapamejlbka.objectmanager.domain.material.MaterialCategory materialCategory
@param com.kapamejlbka.objectmanager.domain.material.dto.MaterialForm editMaterialForm
@param java.util.List<com.kapamejlbka.objectmanager.domain.material.MaterialNorm> norms
@param com.kapamejlbka.objectmanager.domain.material.dto.MaterialNormForm normForm
@param java.util.List<com.kapamejlbka.objectmanager.domain.material.Material> normMaterials
@param java.util.List<com.kapamejlbka.objectmanager.domain.material.MaterialNormContext> normContexts
@param java.util.Map<String, String> normContextNames
@param com.kapamejlbka.objectmanager.domain.material.dto.MaterialNormForm editNormForm
@param String flashSuccess
@param String flashError
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Object Manager — Админка</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="page">
<header class="topbar">
    <div class="topbar__brand">Object Manager</div>
    <div class="topbar__actions">
        <a class="button" href="/logout">Выйти</a>
    </div>
</header>

<main class="container">
    <div class="page-heading">
        <div>
            <p class="muted">Панель администратора</p>
            <h1>Управление системой</h1>
        </div>
    </div>

    @if(flashSuccess != null)
        <div class="alert alert--success">${flashSuccess}</div>
    @endif
    @if(flashError != null)
        <div class="alert alert--error">${flashError}</div>
    @endif

    <div class="tabs">
        <a class="tabs__item ${"users".equals(activeTab) ? "tabs__item--active" : ""}" href="/admin?tab=users">Пользователи</a>
        <a class="tabs__item ${"settings".equals(activeTab) ? "tabs__item--active" : ""}" href="/admin?tab=settings">Настройки расчётов</a>
        <a class="tabs__item ${"materials".equals(activeTab) ? "tabs__item--active" : ""}" href="/admin?tab=materials">Материалы</a>
        <a class="tabs__item ${"norms".equals(activeTab) ? "tabs__item--active" : ""}" href="/admin?tab=norms">Нормы</a>
    </div>

    @if("users".equals(activeTab))
        <section class="grid">
            <div class="card">
                <div class="card__header">
                    <div>
                        <p class="muted">Создание</p>
                        <h2>Новый пользователь</h2>
                    </div>
                </div>
                <form class="form" method="post" action="/admin/users">
                    <div class="form__grid form__grid--two">
                        <label class="form__label">Логин
                            <input class="form__control" type="text" name="username" value="${userForm.getUsername()}" required>
                        </label>
                        <label class="form__label">Пароль
                            <input class="form__control" type="password" name="password" required>
                        </label>
                        <label class="form__label">ФИО
                            <input class="form__control" type="text" name="fullName" value="${userForm.getFullName()}">
                        </label>
                        <label class="form__label">Email
                            <input class="form__control" type="email" name="email" value="${userForm.getEmail()}">
                        </label>
                    </div>
                    <div class="form__grid form__grid--two">
                        <div class="form__label">Роли
                            <div class="chips">
                                @for(role : roles)
                                    <label class="chip">
                                        <input type="checkbox" name="roles" value="${role.getName()}">
                                        <span>${role.getName()}</span>
                                    </label>
                                @endfor
                            </div>
                        </div>
                        <label class="form__label form__label--inline">
                            <input type="checkbox" name="enabled" value="true" checked>
                            <span>Включен</span>
                        </label>
                    </div>
                    <button class="button button--primary" type="submit">Создать</button>
                </form>
            </div>

            <div class="card">
                <div class="card__header">
                    <div>
                        <p class="muted">Список</p>
                        <h2>Все пользователи</h2>
                    </div>
                </div>
                <div class="table">
                    <div class="table__row table__row--head">
                        <div>ID</div>
                        <div>Логин</div>
                        <div>Роли</div>
                        <div>Статус</div>
                        <div>Действия</div>
                    </div>
                    @for(user : users)
                        <div class="table__row">
                            <div>${user.getId()}</div>
                            <div>
                                <div class="table__title">${user.getUsername()}</div>
                                <div class="muted">${user.getFullName()}</div>
                                <div class="muted">${user.getEmail()}</div>
                            </div>
                            <div>
                                <div class="chips">
                                    @for(role : user.getRoles())
                                        <span class="chip chip--static">${role.getName()}</span>
                                    @endfor
                                </div>
                            </div>
                            <div>
                                @if(user.isEnabled())
                                    <span class="badge badge--green">Активен</span>
                                @else
                                    <span class="badge badge--gray">Заблокирован</span>
                                @endif
                            </div>
                            <div class="actions">
                                <a class="button button--ghost" href="/admin?tab=users&editUserId=${user.getId()}">Править</a>
                                <form method="post" action="/admin/users/${user.getId()}/status">
                                    <input type="hidden" name="enabled" value="${!user.isEnabled()}">
                                    <button class="button" type="submit">${user.isEnabled() ? "Заблокировать" : "Разблокировать"}</button>
                                </form>
                            </div>
                        </div>
                    @endfor
                </div>
            </div>
        </section>

        @if(editUserForm != null)
            <section class="card">
                <div class="card__header">
                    <div>
                        <p class="muted">Редактирование</p>
                        <h2>Пользователь #${editUserForm.getId()}</h2>
                    </div>
                </div>
                <form class="form" method="post" action="/admin/users/${editUserForm.getId()}">
                    <div class="form__grid form__grid--two">
                        <label class="form__label">Логин
                            <input class="form__control" type="text" name="username" value="${editUserForm.getUsername()}" required>
                        </label>
                        <label class="form__label">Новый пароль
                            <input class="form__control" type="password" name="newPassword" placeholder="Оставьте пустым, чтобы не менять">
                        </label>
                        <label class="form__label">ФИО
                            <input class="form__control" type="text" name="fullName" value="${editUserForm.getFullName()}">
                        </label>
                        <label class="form__label">Email
                            <input class="form__control" type="email" name="email" value="${editUserForm.getEmail()}">
                        </label>
                    </div>
                    <div class="form__grid form__grid--two">
                        <div class="form__label">Роли
                            <div class="chips">
                                @for(role : roles)
                                    <label class="chip">
                                        <input type="checkbox" name="roles" value="${role.getName()}" @if(editUserForm.getRoles().contains(role.getName()))checked@endif>
                                        <span>${role.getName()}</span>
                                    </label>
                                @endfor
                            </div>
                        </div>
                        <label class="form__label form__label--inline">
                            <input type="checkbox" name="enabled" value="true" @if(editUserForm.isEnabled())checked@endif>
                            <span>Включен</span>
                        </label>
                    </div>
                    <button class="button button--primary" type="submit">Сохранить</button>
                    <a class="button" href="/admin?tab=users">Отмена</a>
                </form>
            </section>
        @endif
    @endif

    @if("settings".equals(activeTab))
        <section class="card">
            <div class="card__header">
                <div>
                    <p class="muted">Расчёты</p>
                    <h2>Базовые настройки</h2>
                </div>
            </div>
            <form class="form form--narrow" method="post" action="/admin/settings">
                <label class="form__label">Стандартная длина сброса шкафа, м
                    <input class="form__control" type="number" step="0.01" name="standardCabinetDropLengthMeters" value="${settings.getStandardCabinetDropLengthMeters()}">
                </label>
                <label class="form__label">Шаг клипс по горизонтали, м
                    <input class="form__control" type="number" step="0.01" name="defaultHorizontalClipStep" value="${settings.getDefaultHorizontalClipStep()}">
                </label>
                <label class="form__label">Шаг клипс по вертикали, м
                    <input class="form__control" type="number" step="0.01" name="defaultVerticalClipStep" value="${settings.getDefaultVerticalClipStep()}">
                </label>
                <button class="button button--primary" type="submit">Сохранить</button>
            </form>
        </section>
    @endif

    @if("materials".equals(activeTab))
        <section class="grid">
            <div class="card">
                <div class="card__header">
                    <div>
                        <p class="muted">Создание</p>
                        <h2>Новый материал</h2>
                    </div>
                </div>
                <form class="form" method="post" action="/admin/materials">
                    <div class="form__grid form__grid--two">
                        <label class="form__label">Код
                            <input class="form__control" type="text" name="code" value="${materialForm.getCode()}" required>
                        </label>
                        <label class="form__label">Название
                            <input class="form__control" type="text" name="name" value="${materialForm.getName()}" required>
                        </label>
                        <label class="form__label">Единица
                            <input class="form__control" type="text" name="unit" value="${materialForm.getUnit()}" required>
                        </label>
                        <label class="form__label">Категория
                            <select class="form__control" name="category">
                                @for(category : materialCategories)
                                    <option value="${category.name()}" ${category.equals(materialForm.getCategory()) ? "selected" : ""}>${category.getDisplayNameRu()}</option>
                                @endfor
                            </select>
                        </label>
                    </div>
                    <label class="form__label">Комментарий
                        <textarea class="form__control" name="notes" rows="3">${materialForm.getNotes()}</textarea>
                    </label>
                    <button class="button button--primary" type="submit">Добавить</button>
                </form>
            </div>

            <div class="card">
                <div class="card__header">
                    <div>
                        <p class="muted">Список</p>
                        <h2>Материалы</h2>
                    </div>
                    <form class="filters" method="get" action="/admin">
                        <input type="hidden" name="tab" value="materials">
                        <input class="form__control" type="text" name="search" placeholder="Поиск" value="${materialQuery}">
                        <select class="form__control" name="category">
                            <option value="">Все категории</option>
                            @for(category : materialCategories)
                                <option value="${category.name()}" ${category.equals(materialCategory) ? "selected" : ""}>${category.getDisplayNameRu()}</option>
                            @endfor
                        </select>
                        <button class="button" type="submit">Найти</button>
                    </form>
                </div>

                <div class="table">
                    <div class="table__row table__row--head">
                        <div>Код</div>
                        <div>Название</div>
                        <div>Категория</div>
                        <div>Ед.</div>
                        <div>Действия</div>
                    </div>
                    @for(material : materials)
                        <div class="table__row">
                            <div>${material.getCode()}</div>
                            <div>
                                <div class="table__title">${material.getName()}</div>
                                @if(material.getNotes() != null)
                                    <div class="muted">${material.getNotes()}</div>
                                @endif
                            </div>
                            <div>${material.getCategory().getDisplayNameRu()}</div>
                            <div>${material.getUnit()}</div>
                            <div class="actions">
                                <a class="button button--ghost" href="/admin?tab=materials&editMaterialId=${material.getId()}">Править</a>
                                <form method="post" action="/admin/materials/${material.getId()}/delete">
                                    <button class="button" type="submit">Удалить</button>
                                </form>
                            </div>
                        </div>
                    @endfor
                </div>
            </div>
        </section>

        @if(editMaterialForm != null)
            <section class="card">
                <div class="card__header">
                    <div>
                        <p class="muted">Редактирование</p>
                        <h2>${editMaterialForm.getName()}</h2>
                    </div>
                </div>
                <form class="form" method="post" action="/admin/materials/${editMaterialForm.getId()}">
                    <div class="form__grid form__grid--two">
                        <label class="form__label">Код
                            <input class="form__control" type="text" name="code" value="${editMaterialForm.getCode()}" required>
                        </label>
                        <label class="form__label">Название
                            <input class="form__control" type="text" name="name" value="${editMaterialForm.getName()}" required>
                        </label>
                        <label class="form__label">Единица
                            <input class="form__control" type="text" name="unit" value="${editMaterialForm.getUnit()}" required>
                        </label>
                        <label class="form__label">Категория
                            <select class="form__control" name="category">
                                @for(category : materialCategories)
                                    <option value="${category.name()}" ${category.equals(editMaterialForm.getCategory()) ? "selected" : ""}>${category.getDisplayNameRu()}</option>
                                @endfor
                            </select>
                        </label>
                    </div>
                    <label class="form__label">Комментарий
                        <textarea class="form__control" name="notes" rows="3">${editMaterialForm.getNotes()}</textarea>
                    </label>
                    <button class="button button--primary" type="submit">Сохранить</button>
                    <a class="button" href="/admin?tab=materials">Отмена</a>
                </form>
            </section>
        @endif
    @endif

    @if("norms".equals(activeTab))
        <section class="grid">
            <div class="card">
                <div class="card__header">
                    <div>
                        <p class="muted">Связка</p>
                        <h2>Создать норму</h2>
                    </div>
                </div>
                <form class="form" method="post" action="/admin/norms">
                    <label class="form__label">Контекст
                        <select class="form__control" name="contextType">
                            @for(context : normContexts)
                                <option value="${context.name()}" ${context.equals(normForm.getContextType()) ? "selected" : ""}>${normContextNames.get(context.name())}</option>
                            @endfor
                        </select>
                    </label>
                    <label class="form__label">Материал
                        <select class="form__control" name="materialId">
                            @for(material : normMaterials)
                                <option value="${material.getId()}" ${material.getId().equals(normForm.getMaterialId()) ? "selected" : ""}>${material.getCode()} — ${material.getName()}</option>
                            @endfor
                        </select>
                    </label>
                    <label class="form__label">Формула
                        <input class="form__control" type="text" name="formula" value="${normForm.getFormula()}" required>
                    </label>
                    <label class="form__label">Комментарий
                        <textarea class="form__control" name="description" rows="3">${normForm.getDescription()}</textarea>
                    </label>
                    <button class="button button--primary" type="submit">Сохранить</button>
                </form>
            </div>

            <div class="card">
                <div class="card__header">
                    <div>
                        <p class="muted">Справочник</p>
                        <h2>Нормы расхода</h2>
                    </div>
                </div>
                <div class="table">
                    <div class="table__row table__row--head">
                        <div>Контекст</div>
                        <div>Материал</div>
                        <div>Формула</div>
                        <div>Действия</div>
                    </div>
                    @for(norm : norms)
                        <div class="table__row">
                            <div>
                                <div class="table__title">${normContextNames.get(norm.getContextType().name())}</div>
                                @if(norm.getDescription() != null)
                                    <div class="muted">${norm.getDescription()}</div>
                                @endif
                            </div>
                            <div>${norm.getMaterial().getCode()} — ${norm.getMaterial().getName()}</div>
                            <div>${norm.getFormula()}</div>
                            <div class="actions">
                                <a class="button button--ghost" href="/admin?tab=norms&editNormId=${norm.getId()}">Править</a>
                                <form method="post" action="/admin/norms/${norm.getId()}/delete">
                                    <button class="button" type="submit">Удалить</button>
                                </form>
                            </div>
                        </div>
                    @endfor
                </div>
            </div>
        </section>

        @if(editNormForm != null)
            <section class="card">
                <div class="card__header">
                    <div>
                        <p class="muted">Редактирование</p>
                        <h2>Норма #${editNormForm.getId()}</h2>
                    </div>
                </div>
                <form class="form" method="post" action="/admin/norms/${editNormForm.getId()}">
                    <label class="form__label">Контекст
                        <select class="form__control" name="contextType">
                            @for(context : normContexts)
                                <option value="${context.name()}" ${context.equals(editNormForm.getContextType()) ? "selected" : ""}>${normContextNames.get(context.name())}</option>
                            @endfor
                        </select>
                    </label>
                    <label class="form__label">Материал
                        <select class="form__control" name="materialId">
                            @for(material : normMaterials)
                                <option value="${material.getId()}" ${material.getId().equals(editNormForm.getMaterialId()) ? "selected" : ""}>${material.getCode()} — ${material.getName()}</option>
                            @endfor
                        </select>
                    </label>
                    <label class="form__label">Формула
                        <input class="form__control" type="text" name="formula" value="${editNormForm.getFormula()}" required>
                    </label>
                    <label class="form__label">Комментарий
                        <textarea class="form__control" name="description" rows="3">${editNormForm.getDescription()}</textarea>
                    </label>
                    <button class="button button--primary" type="submit">Сохранить</button>
                    <a class="button" href="/admin?tab=norms">Отмена</a>
                </form>
            </section>
        @endif
    @endif
</main>
</body>
</html>
