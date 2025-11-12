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
    <section class="card">
        <div class="card-header">
            <h2>Площадки</h2>
            <div class="card-actions">
                <form method="get" action="/sites" class="inline-form">
                    <input type="text" name="q" value="@(q == null ? "" : q)" placeholder="Поиск по названию, городу или адресу">
                    <button type="submit">Поиск</button>
                    @if(q != null && !q.isBlank()) {
                        <a class="button secondary" href="/sites">Сбросить</a>
                    }
                </form>
                <a class="button" href="/sites/new">Добавить</a>
            </div>
        </div>
        @if(sitesPage.isEmpty()) {
            <p>Площадки не найдены.</p>
        } else {
            <div class="table-container">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Название</th>
                        <th>Город</th>
                        <th>Адрес</th>
                        <th>Контакт</th>
                        <th>Телефон</th>
                        <th>Создано</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    @for(Site site : sitesPage.getContent()) {
                        <tr>
                            <td>@site.getName()</td>
                            <td>@(site.getCity() == null ? "—" : site.getCity())</td>
                            <td>@(site.getAddress() == null ? "—" : site.getAddress())</td>
                            <td>@(site.getContactName() == null ? "—" : site.getContactName())</td>
                            <td>@(site.getContactPhone() == null ? "—" : site.getContactPhone())</td>
                            <td>@createdAtFormatter.format(site.getCreatedAt())</td>
                            <td class="actions">
                                <div class="card-actions">
                                    <a class="button secondary" href="/sites/@site.getId()/edit">Изменить</a>
                                    <form method="post" action="/sites/@site.getId()/delete">
                                        <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                                        <button type="submit" class="button danger">Удалить</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    }
                    </tbody>
                </table>
            </div>
            @templates.fragments.pagination(page = sitesPage, basePath = "/sites", query = q)
        }
    </section>
}
