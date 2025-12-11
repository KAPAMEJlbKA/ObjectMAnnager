@import com.kapamejlbka.objectmanager.domain.customer.dto.CustomerCreateRequest
@import org.springframework.security.web.csrf.CsrfToken

@param CustomerCreateRequest customerForm
@param String error = null
@param CsrfToken _csrf = null
@param String flashSuccess = null
@param String flashError = null

@templates.layout(title = "Новый заказчик", activePage = "customers", flashSuccess = flashSuccess, flashError = flashError) {
    <div class="om-page-card">
        <div class="d-flex justify-content-between align-items-start mb-3">
            <div>
                <p class="text-muted mb-1">Заказчики</p>
                <h2 class="h4 mb-0">Новый заказчик</h2>
            </div>
            <a class="btn btn-outline-secondary" href="/customers">Назад к списку</a>
        </div>
        @if(error != null) {
            <div class="alert alert-danger">@error</div>
        }
        <form action="/customers" method="post">
            @if(_csrf != null) {
                <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
            }
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Наименование *</label>
                    <input type="text" class="form-control" name="name" value="@(customerForm != null ? customerForm.getName() : "")" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">УНП</label>
                    <input type="text" class="form-control" name="taxNumber" value="@(customerForm != null ? customerForm.getTaxNumber() : "")">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Страна</label>
                    <input type="text" class="form-control" name="country" value="@(customerForm != null ? customerForm.getCountry() : "")">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Город</label>
                    <input type="text" class="form-control" name="city" value="@(customerForm != null ? customerForm.getCity() : "")">
                </div>
                <div class="col-12">
                    <label class="form-label">Адрес</label>
                    <input type="text" class="form-control" name="addressLine" value="@(customerForm != null ? customerForm.getAddressLine() : "")">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Контактное лицо</label>
                    <input type="text" class="form-control" name="contactName" value="@(customerForm != null ? customerForm.getContactName() : "")">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Должность</label>
                    <input type="text" class="form-control" name="contactPosition" value="@(customerForm != null ? customerForm.getContactPosition() : "")">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Телефон</label>
                    <input type="text" class="form-control" name="contactPhone" value="@(customerForm != null ? customerForm.getContactPhone() : "")">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Email</label>
                    <input type="email" class="form-control" name="contactEmail" value="@(customerForm != null ? customerForm.getContactEmail() : "")">
                </div>
                <div class="col-12">
                    <label class="form-label">Примечание</label>
                    <textarea class="form-control" name="notes" rows="3">@(customerForm != null && customerForm.getNotes() != null ? customerForm.getNotes() : "")</textarea>
                </div>
            </div>
            <div class="d-flex justify-content-end gap-2 mt-3">
                <a class="btn btn-outline-secondary" href="/customers">Отмена</a>
                <button type="submit" class="btn btn-primary">Сохранить</button>
            </div>
        </form>
    </div>
}
