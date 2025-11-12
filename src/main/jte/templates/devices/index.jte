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
    <section class="card">
        <div class="card-header">
            <h2>Оборудование</h2>
            <div class="card-actions">
                <form method="get" action="/devices" class="inline-form">
                    <select name="siteId">
                        <option value="">Все площадки</option>
                        @for(Site site : sites) {
                            <option value="@site.getId()" @(selectedSiteId != null && selectedSiteId.equals(site.getId()) ? "selected" : "")>@site.getName()</option>
                        }
                    </select>
                    <select name="type">
                        <option value="">Все типы</option>
                        @for(DeviceType type : deviceTypes) {
                            <option value="@type.name()" @(selectedType == type ? "selected" : "")>@type.getDisplayName()</option>
                        }
                    </select>
                    <input type="text" name="q" value="@(q == null ? "" : q)" placeholder="Поиск по модели, серийному номеру или IP">
                    <button type="submit">Поиск</button>
                    @if((selectedSiteId != null) || (selectedType != null) || (q != null && !q.isBlank())) {
                        <a class="button secondary" href="/devices">Сбросить</a>
                    }
                </form>
                <a class="button" href="/devices/new">Добавить</a>
            </div>
        </div>
        @if(devicesPage.isEmpty()) {
            <p>Оборудование не найдено.</p>
        } else {
            <div class="table-container">
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
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    @for(Device device : devicesPage.getContent()) {
                        <tr>
                            <td>@(device.getSite() != null ? device.getSite().getName() : "—")</td>
                            <td>@device.getType().getDisplayName()</td>
                            <td>@(device.getModel() == null ? "—" : device.getModel())</td>
                            <td>@(device.getSerial() == null ? "—" : device.getSerial())</td>
                            <td>@(device.getIp() == null ? "—" : device.getIp())</td>
                            <td>@(device.getInstalledAt() == null ? "—" : dateFormatter.format(device.getInstalledAt()))</td>
                            <td>@(device.getLocationNote() == null ? "—" : device.getLocationNote())</td>
                            <td>@createdFormatter.format(device.getCreatedAt())</td>
                            <td class="actions">
                                <div class="card-actions">
                                    <a class="button secondary" href="/devices/@device.getId()/edit">Изменить</a>
                                    <form method="post" action="/devices/@device.getId()/delete">
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
            @templates.fragments.pagination(page = devicesPage, basePath = "/devices", params = paginationParams)
        }
    </section>
}
