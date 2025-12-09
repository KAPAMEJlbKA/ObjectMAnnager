@import com.kapamejlbka.objectmanager.domain.settings.dto.CalculationSettingsDto
@import org.springframework.security.web.csrf.CsrfToken

@param CalculationSettingsDto settings
@param CsrfToken _csrf

<div class="om-page-card">
    <h3 class="h5 mb-3">Настройки расчёта</h3>
    <form method="post" action="/admin/settings">
        <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
        <div class="form-grid">
            <div class="form-field">
                <label for="standardCabinetDropLengthMeters">Стандартный опуск кабеля к шкафу, м</label>
                <input type="number" step="0.1" min="0" id="standardCabinetDropLengthMeters" name="standardCabinetDropLengthMeters"
                       class="form-control" placeholder="1.5" value="@(settings.getStandardCabinetDropLengthMeters() == null ? "" : settings.getStandardCabinetDropLengthMeters())" />
            </div>
        </div>
        <div class="form-actions">
            <button type="submit" class="btn btn-primary">Сохранить</button>
        </div>
    </form>
</div>
