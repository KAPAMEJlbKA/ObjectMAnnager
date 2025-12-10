@import com.kapamejlbka.objectmanager.domain.customer.Site
@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation
@import com.kapamejlbka.objectmanager.domain.calcengine.CalculationResult
@import java.time.format.DateTimeFormatter
@import java.time.ZoneId

@param Site site
@param java.util.List<SystemCalculation> calculations
@param SystemCalculation calculation = null
@param Long deviceCount = null
@param Long nodeCount = null
@param Integer materialItemCount = null
@param CalculationResult calculationResult = null
@param String flashSuccess = null
@param String flashError = null

<% DateTimeFormatter updatedFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault()); %>

@templates.layout(title = site.getName(), activePage = "sites", flashSuccess = flashSuccess, flashError = flashError) {
    <section class="page-card">
        <div class="page-header">
            <div>
                <p class="eyebrow">Объект</p>
                <h1 class="page-title">@site.getName()</h1>
                <p class="page-subtitle">@(site.getFullAddress() == null ? "Адрес не указан" : site.getFullAddress())</p>
            </div>
            <a class="button primary" href="/sites/@site.getId()/calculations/start">Заполнить данные по объекту</a>
        </div>

        <div class="om-page-card mt-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h2 class="h5 mb-0">Данные по объекту</h2>
                <div class="d-flex gap-2">
                    @if(calculation != null) {
                        <a href="/calculations/@calculation.getId()/result" class="btn btn-outline-secondary btn-sm">Показать ведомость материалов</a>
                    }
                    <a href="/sites/@site.getId()/calculations/start" class="btn btn-primary btn-sm">Открыть заполнение данных</a>
                </div>
            </div>

            @if(calculation == null) {
                <div class="empty-state">
                    <div class="empty-icon">📄</div>
                    <div>
                        <h3>Данные по объекту ещё не заполнены</h3>
                        <p class="muted">Начните с добавления устройств и узлов, чтобы построить расчёт.</p>
                    </div>
                </div>
            } else {
                <div class="row g-3">
                    <div class="col-md-3 col-6">
                        <div class="metric-label">Статус</div>
                        <div class="metric-value">@("CALCULATED".equalsIgnoreCase(calculation.getStatus()) ? "Рассчитан" : "Черновик")</div>
                    </div>
                    <div class="col-md-3 col-6">
                        <div class="metric-label">Конечных устройств</div>
                        <div class="metric-value">@(deviceCount == null ? "—" : deviceCount)</div>
                    </div>
                    <div class="col-md-3 col-6">
                        <div class="metric-label">Узлов/шкафов</div>
                        <div class="metric-value">@(nodeCount == null ? "—" : nodeCount)</div>
                    </div>
                    <div class="col-md-3 col-6">
                        <div class="metric-label">Позиции материалов</div>
                        <div class="metric-value">@(materialItemCount == null ? "—" : materialItemCount)</div>
                    </div>
                    <div class="col-md-3 col-6">
                        <div class="metric-label">Последний расчёт</div>
                        <div class="metric-value">@(calculationResult == null || calculationResult.executedAt() == null ? "—" : updatedFormatter.format(calculationResult.executedAt()))</div>
                    </div>
                    <div class="col-md-3 col-6">
                        <div class="metric-label">Обновлено</div>
                        <div class="metric-value">@(calculation.getUpdatedAt() == null ? "—" : updatedFormatter.format(calculation.getUpdatedAt()))</div>
                    </div>
                </div>
            }
        </div>
    </section>
}
