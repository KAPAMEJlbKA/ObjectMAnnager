@import com.kapamejlbka.objectmanager.domain.customer.Customer
@import com.kapamejlbka.objectmanager.domain.customer.Site

@param Customer customer
@param java.util.List<Site> sites
@param String flashSuccess = null
@param String flashError = null

@templates.layout(title = customer.getName(), activePage = "customers", flashSuccess = flashSuccess, flashError = flashError) {
    <div class="om-page-card mb-3">
        <div class="d-flex justify-content-between align-items-start mb-3">
            <div>
                <p class="text-muted mb-1">Заказчик</p>
                <h2 class="h4">@customer.getName()</h2>
            </div>
            <a class="btn btn-primary" href="/customers/@customer.getId()/sites/new">Добавить объект</a>
        </div>
        <div class="row g-3">
            <div class="col-md-6">
                <div class="om-card-header"><h3 class="h6 mb-0">Реквизиты</h3></div>
                <p class="mb-1"><strong>УНП:</strong> @(customer.getTaxNumber() != null ? customer.getTaxNumber() : "—")</p>
                <p class="mb-1"><strong>Адрес:</strong>
                    <span>@(customer.getAddressLine() != null ? customer.getAddressLine() : "—")</span>
                    @if(customer.getCity() != null) {
                        <span>, @customer.getCity()</span>
                    }
                    @if(customer.getCountry() != null) {
                        <span class="text-muted"> (@customer.getCountry())</span>
                    }
                </p>
                <p class="mb-1"><strong>Email:</strong> @(customer.getContactEmail() != null ? customer.getContactEmail() : "—")</p>
                <p class="mb-0"><strong>Примечание:</strong> @(customer.getNotes() != null ? customer.getNotes() : "—")</p>
            </div>
            <div class="col-md-6">
                <div class="om-card-header"><h3 class="h6 mb-0">Контакт</h3></div>
                <p class="mb-1"><strong>Имя:</strong> @(customer.getContactName() != null ? customer.getContactName() : "—")</p>
                <p class="mb-1"><strong>Должность:</strong> @(customer.getContactPosition() != null ? customer.getContactPosition() : "—")</p>
                <p class="mb-1"><strong>Телефон:</strong> @(customer.getContactPhone() != null ? customer.getContactPhone() : "—")</p>
                <p class="mb-0"><strong>Email:</strong> @(customer.getContactEmail() != null ? customer.getContactEmail() : "—")</p>
            </div>
        </div>
    </div>

    <div class="om-page-card">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="h5 mb-0">Объекты</h2>
            <a class="btn btn-outline-secondary" href="/customers/@customer.getId()/sites/new">Добавить объект</a>
        </div>
        @if(sites == null || sites.isEmpty()) {
            <p class="text-muted">Объекты ещё не добавлены.</p>
        } else {
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0 om-table">
                    <thead>
                    <tr>
                        <th>Наименование</th>
                        <th>Адрес</th>
                        <th>Координаты</th>
                        <th class="text-end">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    @for(Site site : sites) {
                        <tr>
                            <td>@site.getName()</td>
                            <td>@(site.getFullAddress() != null ? site.getFullAddress() : "—")</td>
                            <td>
                                @if(site.getLatitude() != null && site.getLongitude() != null) {
                                    @site.getLatitude(), @site.getLongitude()
                                } else {
                                    <span class="text-muted">не указано</span>
                                }
                            </td>
                            <td class="text-end table-action">
                                <div class="btn-group" role="group">
                                    <a class="btn btn-sm btn-outline-secondary" href="/sites/@site.getId()">Открыть объект</a>
                                    <a class="btn btn-sm btn-outline-secondary" href="/sites/@site.getId()/edit">Редактировать</a>
                                </div>
                            </td>
                        </tr>
                    }
                    </tbody>
                </table>
            </div>
        }
    </div>
}
