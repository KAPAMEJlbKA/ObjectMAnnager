@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation

@param SystemCalculation calculation
@param String flashSuccess = null
@param String flashError = null

@templates.layout(title = calculation.getName(), activePage = "sites", flashSuccess = flashSuccess, flashError = flashError) {
    <section class="page-card">
        <div class="page-header">
            <div>
                <p class="eyebrow">Расчёт</p>
                <h1 class="page-title">@calculation.getName()</h1>
                <p class="page-subtitle">Тип: @calculation.getSystemType() · Статус: @calculation.getStatus()</p>
            </div>
            <a class="button primary" href="/calculations/@calculation.getId()/wizard/step1">Открыть мастер</a>
        </div>

        <div class="om-page-card">
            <p><strong>Объект:</strong> @calculation.getSite().getName()</p>
            <p><strong>Описание:</strong> @(calculation.getDescription() == null ? "—" : calculation.getDescription())</p>
            <p><strong>Создано:</strong> @(calculation.getCreatedAt() == null ? "—" : calculation.getCreatedAt())</p>
            <p><strong>Обновлено:</strong> @(calculation.getUpdatedAt() == null ? "—" : calculation.getUpdatedAt())</p>
        </div>
    </section>
}
