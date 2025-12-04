@import com.kapamejlbka.objectmannage.domain.DeviceType
@import com.kapamejlbka.objectmannage.domain.Site
@import com.kapamejlbka.objectmannage.web.form.DeviceForm
@import org.springframework.security.web.csrf.CsrfToken
@import org.springframework.validation.BindingResult
@import org.springframework.validation.FieldError
@import java.util.List
@import java.util.UUID

@param DeviceForm form
@param boolean isEdit = false
@param UUID deviceId = null
@param BindingResult binding = null
@param List<Site> sites
@param DeviceType[] deviceTypes
@param CsrfToken _csrf
@param String flashSuccess = null
@param String flashError = null

<% boolean hasSiteError = binding != null && binding.hasFieldErrors("siteId");
   boolean hasTypeError = binding != null && binding.hasFieldErrors("type");
   boolean hasModelError = binding != null && binding.hasFieldErrors("model");
   boolean hasSerialError = binding != null && binding.hasFieldErrors("serial");
   boolean hasIpError = binding != null && binding.hasFieldErrors("ip");
   boolean hasLocationError = binding != null && binding.hasFieldErrors("locationNote");
   boolean hasInstalledAtError = binding != null && binding.hasFieldErrors("installedAt");
%>

@templates.layout(
        title = isEdit ? "Редактирование оборудования" : "Новое оборудование",
        activePage = "devices",
        flashSuccess = flashSuccess,
        flashError = flashError
) {
    <section class="page-card">
        <div class="page-header">
            <div>
                <p class="eyebrow">@(isEdit ? "Карточка устройства" : "Создание записи")</p>
                <h1 class="page-title">@(isEdit ? "Редактировать оборудование" : "Новое оборудование")</h1>
                <p class="page-subtitle">Все обязательные поля помечены звёздочкой. Введите данные площадки, тип, модель и вспомогательную информацию.</p>
            </div>
            <a class="button ghost" href="/devices">Назад к списку</a>
        </div>

        <form method="post" action="@(isEdit && deviceId != null ? "/devices/" + deviceId : "/devices")" class="form-grid">
            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">

            <div class="form-field @((hasSiteError) ? "has-error" : "")">
                <label for="siteId">Площадка *</label>
                <select id="siteId" name="siteId" required>
                    <option value="">Выберите площадку</option>
                    @for(Site site : sites) {
                        <option value="@site.getId()" @((form.getSiteId() != null && form.getSiteId().equals(site.getId())) ? "selected" : "")>@site.getName()</option>
                    }
                </select>
                @if(hasSiteError) {
                    @for(FieldError error : binding.getFieldErrors("siteId")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-field @((hasTypeError) ? "has-error" : "")">
                <label for="type">Тип *</label>
                <select id="type" name="type" required>
                    <option value="">Выберите тип</option>
                    @for(DeviceType type : deviceTypes) {
                        <option value="@type.name()" @(form.getType() == type ? "selected" : "")>@type.getDisplayName()</option>
                    }
                </select>
                @if(hasTypeError) {
                    @for(FieldError error : binding.getFieldErrors("type")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-field @((hasModelError) ? "has-error" : "")">
                <label for="model">Модель</label>
                <input type="text" id="model" name="model" maxlength="100" value="@(form.getModel() == null ? "" : form.getModel())" placeholder="Например, Cisco WS-C2960">
                @if(hasModelError) {
                    @for(FieldError error : binding.getFieldErrors("model")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-field @((hasSerialError) ? "has-error" : "")">
                <label for="serial">Серийный номер</label>
                <input type="text" id="serial" name="serial" maxlength="100" value="@(form.getSerial() == null ? "" : form.getSerial())" placeholder="SN / Asset tag">
                @if(hasSerialError) {
                    @for(FieldError error : binding.getFieldErrors("serial")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-field @((hasIpError) ? "has-error" : "")">
                <label for="ip">IP</label>
                <input type="text" id="ip" name="ip" maxlength="45" value="@(form.getIp() == null ? "" : form.getIp())" placeholder="10.0.0.1">
                @if(hasIpError) {
                    @for(FieldError error : binding.getFieldErrors("ip")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-field @((hasInstalledAtError) ? "has-error" : "")">
                <label for="installedAt">Дата установки</label>
                <input type="date" id="installedAt" name="installedAt" value="@(form.getInstalledAt() == null ? "" : form.getInstalledAt().toString())">
                @if(hasInstalledAtError) {
                    @for(FieldError error : binding.getFieldErrors("installedAt")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-field full-width @((hasLocationError) ? "has-error" : "")">
                <label for="locationNote">Местоположение</label>
                <textarea id="locationNote" name="locationNote" maxlength="255" rows="3" placeholder="Комната, стойка, полка">@(form.getLocationNote() == null ? "" : form.getLocationNote())</textarea>
                @if(hasLocationError) {
                    @for(FieldError error : binding.getFieldErrors("locationNote")) {
                        <p class="field-error">@error.getDefaultMessage()</p>
                    }
                }
            </div>

            <div class="form-actions">
                <button type="submit" class="button primary">@(isEdit ? "Сохранить" : "Создать")</button>
                <a class="button ghost" href="/devices">Отмена</a>
            </div>
        </form>
    </section>
}
