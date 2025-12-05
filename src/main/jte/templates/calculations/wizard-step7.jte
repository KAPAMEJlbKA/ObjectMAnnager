@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation
@import com.kapamejlbka.objectmanager.domain.calcengine.CalculationResult
@import com.kapamejlbka.objectmanager.domain.calcengine.MaterialItemResult

@param SystemCalculation calculation
@param CalculationResult result
@param String flashSuccess = null
@param String flashError = null

<% String statusLabel(String status) {
       if (status != null && "CALCULATED".equalsIgnoreCase(status)) return "Рассчитан";
       return "Черновик";
   }
%>

@templates.layout(title = "Шаг 7 — результаты", activePage = "sites", flashSuccess = flashSuccess, flashError = flashError) {
    <section class="page-card">
        @templates.fragments.`wizard-header`(calculation = calculation, step = 7)
        <h2 class="h4">Результаты расчёта</h2>
        <p class="page-subtitle">Итоговые материалы. Расчёт выполнен @(result == null || result.executedAt() == null ? "—" : result.executedAt()).</p>

        <div class="om-page-card">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <div class="text-muted">Статус</div>
                    <div class="fw-bold">@statusLabel(calculation.getStatus())</div>
                </div>
                <div class="d-flex gap-2">
                    <a class="btn btn-outline-secondary btn-sm disabled">Скачать в Excel</a>
                    <a class="btn btn-primary btn-sm" href="/sites/@calculation.getSite().getId()">Вернуться к объекту</a>
                </div>
            </div>

            <div class="table-scroll">
                <table class="table table-hover align-middle om-table mb-0">
                    <thead>
                        <tr>
                            <th>Материал</th>
                            <th>Категория</th>
                            <th>Ед. изм.</th>
                            <th>Количество</th>
                        </tr>
                    </thead>
                    <tbody>
                        @if(result == null || result.items().isEmpty()) {
                            <tr><td colspan="4" class="text-center text-muted">Нет данных расчёта</td></tr>
                        } else {
                            @for(MaterialItemResult item : result.items()) {
                                <tr>
                                    <td>@item.materialName()</td>
                                    <td>@item.materialCode()</td>
                                    <td>@item.unit()</td>
                                    <td>@String.format("%.2f", item.quantity())</td>
                                </tr>
                            }
                        }
                    </tbody>
                </table>
            </div>
        </div>

        <div class="d-flex justify-content-between mt-4">
            <a class="btn btn-outline-secondary" href="/calculations/@calculation.getId()/wizard/step6">Назад</a>
            <span class="btn btn-primary disabled">Рассчитать</span>
        </div>
    </section>
}
