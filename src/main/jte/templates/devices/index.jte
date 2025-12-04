@import com.kapamejlbka.objectmannage.domain.Device
@import com.kapamejlbka.objectmannage.domain.DeviceType
@import com.kapamejlbka.objectmannage.domain.Site
@import org.springframework.data.domain.Page
@import org.springframework.security.web.csrf.CsrfToken
@import java.time.ZoneId
@import java.time.format.DateTimeFormatter
@import java.util.LinkedHashMap
@import java.util.List
@import java.util.Map
@import java.util.UUID

@param Page<Device> devicesPage
@param List<Site> sites
@param DeviceType[] deviceTypes
@param UUID selectedSiteId = null
@param DeviceType selectedType = null
@param String q = null
@param CsrfToken _csrf
@param String flashSuccess = null
@param String flashError = null

<% DateTimeFormatter createdFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(ZoneId.systemDefault());
   DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
   Map<String, String> paginationParams = new LinkedHashMap<>();
   if (selectedSiteId != null) {
       paginationParams.put("siteId", selectedSiteId.toString());
   }
   if (selectedType != null) {
       paginationParams.put("type", selectedType.name());
   }
   if (q != null && !q.isBlank()) {
       paginationParams.put("q", q);
   }
%>

@templates.layout(
        title = "Оборудование",
        activePage = "devices",
        flashSuccess = flashSuccess,
        flashError = flashError
) {
    <section class="page-card">
        <div class="page-header">
            <div>
                <p class="eyebrow">Каталог оборудования</p>
                <h1 class="page-title">Оборудование</h1>
                <p class="page-subtitle">Фильтр по площадке, типу и поиску по модели, серийному номеру или IP.</p>
            </div>
            <a class="button primary" href="/devices/new">Добавить устройство</a>
        </div>

        <div class="filter-panel">
            <form method="get" action="/devices" class="filter-grid">
                <label class="filter-field">
                    <span>Площадка</span>
                    <select name="siteId">
                        <option value="">Все площадки</option>
                        @for(Site site : sites) {
                            <option value="@site.getId()" @(selectedSiteId != null && selectedSiteId.equals(site.getId()) ? "selected" : "")>@site.getName()</option>
                        }
                    </select>
                </label>
                <label class="filter-field">
                    <span>Тип</span>
                    <select name="type">
                        <option value="">Все типы</option>
                        @for(DeviceType type : deviceTypes) {
                            <option value="@type.name()" @(selectedType == type ? "selected" : "")>@type.getDisplayName()</option>
                        }
                    </select>
                </label>
                <label class="filter-field">
                    <span>Поиск</span>
                    <input type="text" name="q" value="@(q == null ? "" : q)" placeholder="Модель, серийный номер или IP">
                </label>
                <div class="filter-actions">
                    <button type="submit" class="button">Применить</button>
                    @if((selectedSiteId != null) || (selectedType != null) || (q != null && !q.isBlank())) {
                        <a class="button ghost" href="/devices">Сбросить</a>
                    }
                </div>
            </form>
        </div>

        @if(devicesPage.isEmpty()) {
            <div class="empty-state">
                <div class="empty-icon">📦</div>
                <div>
                    <h3>Ничего не найдено</h3>
                    <p class="muted">Измените параметры поиска или добавьте устройство.</p>
                </div>
            </div>
        } else {
            <div class="table-card">
                <div class="table-scroll">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>Площадка</th>
                            <th>Тип</th>
                            <th>Модель</th>
                            <th>Серийный номер</th>
                            <th>IP</th>
                            <th>Дата установки</th>
                            <th>Местоположение</th>
                            <th>Создано</th>
                            <th class="actions-col"></th>
                        </tr>
                        </thead>
                        <tbody>
                        @for(Device device : devicesPage.getContent()) {
                            <tr>
                                <td>
                                    <div class="cell-main">@(device.getSite() != null ? device.getSite().getName() : "—")</div>
                                    <div class="cell-sub">@device.getId()</div>
                                </td>
                                <td><span class="pill">@device.getType().getDisplayName()</span></td>
                                <td>@(device.getModel() == null ? "—" : device.getModel())</td>
                                <td>@(device.getSerial() == null ? "—" : device.getSerial())</td>
                                <td>@(device.getIp() == null ? "—" : device.getIp())</td>
                                <td>@(device.getInstalledAt() == null ? "—" : dateFormatter.format(device.getInstalledAt()))</td>
                                <td>@(device.getLocationNote() == null ? "—" : device.getLocationNote())</td>
                                <td>@createdFormatter.format(device.getCreatedAt())</td>
                                <td class="row-actions">
                                    <a class="button ghost" href="/devices/@device.getId()/edit">Изменить</a>
                                    <form method="post" action="/devices/@device.getId()/delete">
                                        <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                                        <button type="submit" class="button danger">Удалить</button>
                                    </form>
                                </td>
                            </tr>
                        }
                        </tbody>
                    </table>
                </div>
                @templates.fragments.pagination(page = devicesPage, basePath = "/devices", params = paginationParams)
            </div>
        }
    </section>
}
