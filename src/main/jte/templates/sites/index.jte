@import com.kapamejlbka.objectmannage.domain.Site
@import org.springframework.data.domain.Page
@import org.springframework.security.web.csrf.CsrfToken
@import java.time.ZoneId
@import java.time.format.DateTimeFormatter

@param Page<Site> sitesPage
@param String q = null
@param CsrfToken _csrf
@param String flashSuccess = null
@param String flashError = null

<% DateTimeFormatter createdAtFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(ZoneId.systemDefault()); %>

@templates.layout(
        title = "Площадки",
        activePage = "sites",
        flashSuccess = flashSuccess,
        flashError = flashError
) {
    <section class="page-card">
        <div class="page-header">
            <div>
                <p class="eyebrow">Адреса объектов</p>
                <h1 class="page-title">Площадки</h1>
                <p class="page-subtitle">Поиск по названию, городу или адресу. Управляйте площадками и контактами в одном месте.</p>
            </div>
            <a class="button primary" href="/sites/new">Добавить площадку</a>
        </div>

        <div class="filter-panel">
            <form method="get" action="/sites" class="filter-grid single">
                <label class="filter-field">
                    <span>Поиск</span>
                    <input type="text" name="q" value="@(q == null ? "" : q)" placeholder="Поиск по названию, городу или адресу">
                </label>
                <div class="filter-actions">
                    <button type="submit" class="button">Найти</button>
                    @if(q != null && !q.isBlank()) {
                        <a class="button ghost" href="/sites">Сбросить</a>
                    }
                </div>
            </form>
        </div>

        @if(sitesPage.isEmpty()) {
            <div class="empty-state">
                <div class="empty-icon">📍</div>
                <div>
                    <h3>Площадки не найдены</h3>
                    <p class="muted">Попробуйте изменить запрос или создайте новую площадку.</p>
                </div>
            </div>
        } else {
            <div class="table-card">
                <div class="table-scroll">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>Название</th>
                            <th>Город</th>
                            <th>Адрес</th>
                            <th>Контакт</th>
                            <th>Телефон</th>
                            <th>Создано</th>
                            <th class="actions-col"></th>
                        </tr>
                        </thead>
                        <tbody>
                        @for(Site site : sitesPage.getContent()) {
                            <tr>
                                <td>
                                    <div class="cell-main">@site.getName()</div>
                                    <div class="cell-sub">@site.getId()</div>
                                </td>
                                <td>@(site.getCity() == null ? "—" : site.getCity())</td>
                                <td>@(site.getAddress() == null ? "—" : site.getAddress())</td>
                                <td>@(site.getContactName() == null ? "—" : site.getContactName())</td>
                                <td>@(site.getContactPhone() == null ? "—" : site.getContactPhone())</td>
                                <td>@createdAtFormatter.format(site.getCreatedAt())</td>
                                <td class="row-actions">
                                    <a class="button ghost" href="/sites/@site.getId()/edit">Изменить</a>
                                    <form method="post" action="/sites/@site.getId()/delete">
                                        <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                                        <button type="submit" class="button danger">Удалить</button>
                                    </form>
                                </td>
                            </tr>
                        }
                        </tbody>
                    </table>
                </div>
                @templates.fragments.pagination(page = sitesPage, basePath = "/sites", query = q)
            </div>
        }
    </section>
}
