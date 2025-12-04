@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation
@import com.kapamejlbka.objectmanager.domain.device.NetworkNode
@import com.kapamejlbka.objectmanager.domain.device.dto.NetworkNodeSettingsForm
@import org.springframework.security.web.csrf.CsrfToken

@param SystemCalculation calculation
@param java.util.List<NetworkNode> nodes
@param NetworkNodeSettingsForm nodeSettings
@param java.util.List<Integer> cabinetSizes
@param java.util.List<String> mountSurfaces
@param CsrfToken _csrf
@param String flashSuccess = null
@param String flashError = null

@templates.layout(title = "Шаг 6 — узлы", activePage = "sites", flashSuccess = flashSuccess, flashError = flashError) {
    <section class="page-card">
        @templates.fragments.`wizard-header`(calculation = calculation, step = 6)
        <h2 class="h4">Шаг 6. Узлы: начинка и питание</h2>
        <p class="page-subtitle">Укажите параметры шкафов, автоматы и розетки. Кол-во приходящих линий отображается справочно.</p>

        <form method="post" action="/calculations/@calculation.getId()/wizard/step6">
            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
            <div class="om-page-card">
                <div class="table-scroll">
                    <table class="table table-hover align-middle om-table mb-3">
                        <thead>
                            <tr>
                                <th>Код/имя</th>
                                <th>Поверхность</th>
                                <th>Размер шкафа</th>
                                <th>Авто</th>
                                <th>Базовые автоматы</th>
                                <th>Доп. автоматы</th>
                                <th>Базовые розетки</th>
                                <th>Доп. розетки</th>
                                <th>Приходящие линии</th>
                            </tr>
                        </thead>
                        <tbody>
                            @for(int i = 0; i < nodes.size(); i++) {
                                NetworkNode node = nodes.get(i);
                                <tr>
                                    <td>
                                        <div class="fw-bold">@node.getCode()</div>
                                        <div class="text-muted">@node.getName()</div>
                                        <input type="hidden" name="nodes[@i].id" value="@node.getId()">
                                        <input type="hidden" name="nodes[@i].code" value="@node.getCode()">
                                        <input type="hidden" name="nodes[@i].name" value="@node.getName()">
                                    </td>
                                    <td>
                                        <select name="nodes[@i].mountSurface" class="form-select">
                                            @for(String s : mountSurfaces) { <option value="@s" @(s.equals(node.getMountSurface()) ? "selected" : "")>@s</option> }
                                        </select>
                                    </td>
                                    <td>
                                        <select name="nodes[@i].cabinetSize" class="form-select">
                                            <option value="">—</option>
                                            @for(Integer size : cabinetSizes) { <option value="@size" @(node.getCabinetSize() != null && node.getCabinetSize().equals(size) ? "selected" : "")>@size</option> }
                                        </select>
                                    </td>
                                    <td class="text-center">
                                        <input type="checkbox" name="nodes[@i].cabinetSizeAuto" value="true" @(Boolean.TRUE.equals(node.getCabinetSizeAuto()) ? "checked" : "")>
                                    </td>
                                    <td><input type="number" min="0" class="form-control" name="nodes[@i].baseCircuitBreakers" value="@node.getBaseCircuitBreakers()"></td>
                                    <td><input type="number" min="0" class="form-control" name="nodes[@i].extraCircuitBreakers" value="@node.getExtraCircuitBreakers()"></td>
                                    <td><input type="number" min="0" class="form-control" name="nodes[@i].baseSockets" value="@node.getBaseSockets()"></td>
                                    <td><input type="number" min="0" class="form-control" name="nodes[@i].extraSockets" value="@node.getExtraSockets()"></td>
                                    <td class="text-muted">@(node.getIncomingLinesCount() == null ? "—" : node.getIncomingLinesCount())</td>
                                </tr>
                            }
                            @if(nodes.isEmpty()) {
                                <tr><td colspan="9" class="text-center text-muted">Нет узлов для настройки</td></tr>
                            }
                        </tbody>
                    </table>
                </div>
                <button class="btn btn-primary" type="submit">Сохранить изменения</button>
            </div>
        </form>

        <div class="d-flex justify-content-between mt-4">
            <a class="btn btn-outline-secondary" href="/calculations/@calculation.getId()/wizard/step5">Назад</a>
            <a class="btn btn-primary" href="/calculations/@calculation.getId()/wizard/step7">Далее</a>
        </div>
    </section>
}
