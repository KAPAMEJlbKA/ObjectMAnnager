@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation
@import com.kapamejlbka.objectmanager.domain.calcengine.CalculationResult
@import com.kapamejlbka.objectmanager.domain.calcengine.MaterialItemResult

@param SystemCalculation calculation
@param CalculationResult result
@param Integer itemsCount = null

@templates.layout(title = calculation.getName() + " — ведомость материалов", activePage = "customers") {
    <div class="mb-3">
        <h1 class="h4 mb-1">Ведомость материалов</h1>
        <p class="text-muted mb-1">@calculation.getSite().getName() + " — " + calculation.getName()</p>
        <div class="d-flex gap-2 flex-wrap mt-2">
            <span class="badge text-bg-light">Всего позиций: @((itemsCount == null ? result.items().size() : itemsCount))</span>
        </div>
    </div>

    <div class="d-flex gap-2 mb-3 flex-wrap">
        <a href="/sites/@calculation.getSite().getId()" class="btn btn-outline-secondary btn-sm">Вернуться к объекту</a>
        <a href="/calculations/@calculation.getId()/wizard/step1" class="btn btn-outline-primary btn-sm">Открыть заполнение данных</a>
        <a href="/calculations/@calculation.getId()/result.xlsx" class="btn btn-outline-secondary btn-sm">Экспорт в Excel</a>
    </div>

    <div class="om-page-card">
        @if(result.items().isEmpty()) {
            <p class="text-muted">Результаты расчёта отсутствуют.</p>
        } else {
            <div class="table-responsive">
                <table class="table table-striped align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th>Наименование материала</th>
                        <th>Категория</th>
                        <th>Ед. изм.</th>
                        <th>Количество</th>
                    </tr>
                    </thead>
                    <tbody>
                    @for(MaterialItemResult item : result.items()) {
                        <tr>
                            <td>
                                <div class="fw-semibold">@item.materialName()</div>
                                <div class="text-muted small">@item.materialCode()</div>
                            </td>
                            <td>@item.category()</td>
                            <td>@item.unit()</td>
                            <td>@String.format(java.util.Locale.US, "%.2f", item.quantity())</td>
                        </tr>
                    }
                    </tbody>
                </table>
            </div>
        }
    </div>
}
