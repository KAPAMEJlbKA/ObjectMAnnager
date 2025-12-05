@import com.kapamejlbka.objectmanager.domain.customer.Site
@import com.kapamejlbka.objectmanager.domain.calculation.dto.SystemCalculationCreateRequest
@import org.springframework.security.web.csrf.CsrfToken

@param Site site
@param SystemCalculationCreateRequest calculationForm
@param String error = null
@param CsrfToken _csrf
@param String flashSuccess = null
@param String flashError = null

@templates.layout(title = "Заполнение данных", activePage = "sites", flashSuccess = flashSuccess, flashError = flashError) {
    <section class="page-card">
        <div class="page-header">
            <div>
                <p class="eyebrow">Объект: @site.getName()</p>
                <h1 class="page-title">Начало заполнения данных</h1>
                <p class="page-subtitle">Укажите название и описание, чтобы перейти к мастеру заполнения по шагам.</p>
            </div>
            <a class="button ghost" href="/sites/@site.getId()">Назад к объекту</a>
        </div>

        @if(error != null) {
            <div class="flash-banner error">
                <div class="flash-indicator"></div>
                <div class="flash-content">
                    <div class="flash-title">Ошибка</div>
                    <div class="flash-text">@error</div>
                </div>
            </div>
        }

        <form method="post" action="/sites/@site.getId()/calculations" class="form-grid">
            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">

            <div class="form-field full-width">
                <label for="name">Наименование *</label>
                <input id="name" name="name" type="text" required value="@(calculationForm.getName() == null ? "" : calculationForm.getName())" placeholder="Например, СКУД офисного этажа">
            </div>

            <div class="form-field full-width">
                <label for="description">Описание</label>
                <textarea id="description" name="description" rows="3" placeholder="Краткий контекст или требования">@(calculationForm.getDescription() == null ? "" : calculationForm.getDescription())</textarea>
            </div>

            <button type="submit" class="button primary">Создать и продолжить</button>
        </form>
    </section>
}
