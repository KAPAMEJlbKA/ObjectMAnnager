@import com.kapamejlbka.objectmanager.domain.customer.Site
@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation
@import java.time.format.DateTimeFormatter
@import java.time.ZoneId

@param Site site
@param java.util.List<SystemCalculation> calculations
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
            <a class="button primary" href="/sites/@site.getId()/calculations/new">Создать расчёт</a>
        </div>

        <div class="om-page-card mt-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h2 class="h5 mb-0">Расчёты</h2>
                <a href="/sites/@site.getId()/calculations/new" class="btn btn-primary btn-sm">Создать расчёт</a>
            </div>

            @if(calculations.isEmpty()) {
                <div class="empty-state">
                    <div class="empty-icon">🧮</div>
                    <div>
                        <h3>Пока нет расчётов</h3>
                        <p class="muted">Добавьте первый расчёт, чтобы начать планирование системы.</p>
                    </div>
                </div>
            } else {
                <table class="table table-hover align-middle mb-0 om-table">
                    <thead>
                        <tr>
                            <th>Имя</th>
                            <th>Тип</th>
                            <th>Статус</th>
                            <th>Обновлено</th>
                            <th class="text-end">Действия</th>
                        </tr>
                    </thead>
                    <tbody>
                        @for(SystemCalculation calc : calculations) {
                            <tr>
                                <td>@calc.getName()</td>
                                <td>@calc.getSystemType()</td>
                                <td>
                                    @if("CALCULATED".equalsIgnoreCase(calc.getStatus())) {
                                        <span class="badge bg-success">Рассчитан</span>
                                    } else {
                                        <span class="badge bg-secondary">Черновик</span>
                                    }
                                </td>
                                <td>@(calc.getUpdatedAt() == null ? "—" : updatedFormatter.format(calc.getUpdatedAt()))</td>
                                <td class="text-end">
                                    <a class="btn btn-outline-primary btn-sm" href="@(\"CALCULATED\".equalsIgnoreCase(calc.getStatus()) ? "/calculations/" + calc.getId() : "/calculations/" + calc.getId() + "/wizard/step1")">Открыть</a>
                                </td>
                            </tr>
                        }
                    </tbody>
                </table>
            }
        </div>
    </section>
}
