@import com.kapamejlbka.objectmanager.domain.customer.Customer

@param java.util.List<Customer> customers
@param String flashSuccess = null
@param String flashError = null

@templates.layout(title = "Список заказчиков", activePage = "customers", flashSuccess = flashSuccess, flashError = flashError) {
    <div class="om-page-card">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="h4 mb-0">Заказчики</h2>
            <a class="btn btn-primary" href="/customers/create">Добавить заказчика</a>
        </div>
        @if(customers != null && !customers.isEmpty()) {
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0 om-table">
                    <thead>
                    <tr>
                        <th>Наименование</th>
                        <th>Город</th>
                        <th>Контактное лицо</th>
                        <th>Телефон</th>
                        <th class="text-end">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    @for(Customer customer : customers) {
                        <tr>
                            <td>@customer.getName()</td>
                            <td>@(customer.getCity() != null ? customer.getCity() : "—")</td>
                            <td>@(customer.getContactName() != null ? customer.getContactName() : "—")</td>
                            <td>@(customer.getContactPhone() != null ? customer.getContactPhone() : "—")</td>
                            <td class="text-end table-action">
                                <a class="btn btn-sm btn-outline-secondary" href="/customers/@customer.getId()">Открыть</a>
                            </td>
                        </tr>
                    }
                    </tbody>
                </table>
            </div>
        } else {
            <p class="text-muted mb-0">Заказчики ещё не созданы.</p>
        }
    </div>
}
