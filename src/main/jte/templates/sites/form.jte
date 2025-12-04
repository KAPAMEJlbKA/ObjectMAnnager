@import com.kapamejlbka.objectmannage.web.form.SiteForm
@import org.springframework.security.web.csrf.CsrfToken
@import org.springframework.validation.BindingResult
@import org.springframework.validation.FieldError
@import java.util.UUID

@param SiteForm form
@param boolean isEdit = false
@param UUID siteId = null
@param BindingResult binding = null
@param CsrfToken _csrf
@param String flashSuccess = null
@param String flashError = null

@templates.layout(
        title = isEdit ? "Редактирование площадки" : "Новая площадка",
        activePage = "sites",
        flashSuccess = flashSuccess,
        flashError = flashError
) {
    <section class="page-card">
        <div class="page-header">
            <div>
                <p class="eyebrow">@(isEdit ? "Карточка площадки" : "Создание записи")</p>
                <h1 class="page-title">@(isEdit ? "Редактировать площадку" : "Новая площадка")</h1>
                <p class="page-subtitle">Заполните основные сведения и контактные данные площадки.</p>
            </div>
            <a class="button ghost" href="/sites">Назад к списку</a>
        </div>

        <form method="post" action="@(isEdit && siteId != null ? "/sites/" + siteId : "/sites")" class="form-grid">
            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">

            <div class="form-field @((binding != null && binding.hasFieldErrors("name")) ? "has-error" : "")">
                <label for="name">Название *</label>
                <input type="text" id="name" name="name" required maxlength="100" value="@(form.getName() == null ? "" : form.getName())" placeholder="Главный офис">
                @if(binding != null && binding.hasFieldErrors("name")) {
                    @for(FieldError error : binding.getFieldErrors("name")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-field @((binding != null && binding.hasFieldErrors("city")) ? "has-error" : "")">
                <label for="city">Город</label>
                <input type="text" id="city" name="city" maxlength="80" value="@(form.getCity() == null ? "" : form.getCity())" placeholder="Москва">
                @if(binding != null && binding.hasFieldErrors("city")) {
                    @for(FieldError error : binding.getFieldErrors("city")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-field full-width @((binding != null && binding.hasFieldErrors("address")) ? "has-error" : "")">
                <label for="address">Адрес</label>
                <input type="text" id="address" name="address" maxlength="255" value="@(form.getAddress() == null ? "" : form.getAddress())" placeholder="Улица, дом, корпус">
                @if(binding != null && binding.hasFieldErrors("address")) {
                    @for(FieldError error : binding.getFieldErrors("address")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-field @((binding != null && binding.hasFieldErrors("contactName")) ? "has-error" : "")">
                <label for="contactName">Контактное лицо</label>
                <input type="text" id="contactName" name="contactName" maxlength="80" value="@(form.getContactName() == null ? "" : form.getContactName())" placeholder="Иван Иванов">
                @if(binding != null && binding.hasFieldErrors("contactName")) {
                    @for(FieldError error : binding.getFieldErrors("contactName")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-field @((binding != null && binding.hasFieldErrors("contactPhone")) ? "has-error" : "")">
                <label for="contactPhone">Телефон</label>
                <input type="text" id="contactPhone" name="contactPhone" maxlength="32" value="@(form.getContactPhone() == null ? "" : form.getContactPhone())" placeholder="+7 (999) 000-00-00">
                @if(binding != null && binding.hasFieldErrors("contactPhone")) {
                    @for(FieldError error : binding.getFieldErrors("contactPhone")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-actions">
                <button type="submit" class="button primary">@(isEdit ? "Сохранить" : "Создать")</button>
                <a class="button ghost" href="/sites">Отмена</a>
            </div>
        </form>
    </section>
}
