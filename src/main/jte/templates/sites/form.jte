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
    <section class="card">
        <h2>@(isEdit ? "Редактировать площадку" : "Новая площадка")</h2>
        <form method="post" action="@(isEdit && siteId != null ? "/sites/" + siteId : "/sites")" class="grid-form">
            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
            <div class="form-row">
                <label for="name">Название *</label>
                <input type="text" id="name" name="name" required maxlength="100" value="@(form.getName() == null ? "" : form.getName())">
                @if(binding != null && binding.hasFieldErrors("name")) {
                    @for(FieldError error : binding.getFieldErrors("name")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>
            <div class="form-row">
                <label for="city">Город</label>
                <input type="text" id="city" name="city" maxlength="80" value="@(form.getCity() == null ? "" : form.getCity())">
                @if(binding != null && binding.hasFieldErrors("city")) {
                    @for(FieldError error : binding.getFieldErrors("city")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>
            <div class="form-row">
                <label for="address">Адрес</label>
                <input type="text" id="address" name="address" maxlength="255" value="@(form.getAddress() == null ? "" : form.getAddress())">
                @if(binding != null && binding.hasFieldErrors("address")) {
                    @for(FieldError error : binding.getFieldErrors("address")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>
            <div class="form-row">
                <label for="contactName">Контактное лицо</label>
                <input type="text" id="contactName" name="contactName" maxlength="80" value="@(form.getContactName() == null ? "" : form.getContactName())">
                @if(binding != null && binding.hasFieldErrors("contactName")) {
                    @for(FieldError error : binding.getFieldErrors("contactName")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>
            <div class="form-row">
                <label for="contactPhone">Телефон</label>
                <input type="text" id="contactPhone" name="contactPhone" maxlength="32" value="@(form.getContactPhone() == null ? "" : form.getContactPhone())">
                @if(binding != null && binding.hasFieldErrors("contactPhone")) {
                    @for(FieldError error : binding.getFieldErrors("contactPhone")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>
            <div class="form-row">
                <button type="submit">@(isEdit ? "Сохранить" : "Создать")</button>
                <a class="button secondary" href="/sites">Отмена</a>
            </div>
        </form>
    </section>
}
