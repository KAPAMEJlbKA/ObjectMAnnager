@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation
@import com.kapamejlbka.objectmanager.domain.topology.TopologyLink
@import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkLengthsForm
@import org.springframework.security.web.csrf.CsrfToken

@param SystemCalculation calculation
@param java.util.List<TopologyLink> links
@param TopologyLinkLengthsForm lengthsForm
@param CsrfToken _csrf
@param String flashSuccess = null
@param String flashError = null

<% String formatEndpoint(com.kapamejlbka.objectmanager.domain.device.NetworkNode n, com.kapamejlbka.objectmanager.domain.device.EndpointDevice d) {
       if (n != null) return "[NODE] " + n.getName();
       if (d != null) return "[DEVICE] " + d.getName();
       return "—";
   }
%>

@templates.layout(title = "Шаг 4 — длины", activePage = "sites", flashSuccess = flashSuccess, flashError = flashError) {
    <section class="page-card">
        @templates.fragments.`wizard-header`(calculation = calculation, step = 4)
        <h2 class="h4">Шаг 4. Длины линий</h2>
        <p class="page-subtitle">Укажите длину для каждой кабельной линии. WIFI можно пропустить.</p>

        <form method="post" action="/calculations/@calculation.getId()/wizard/step4">
            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
            <div class="om-page-card">
                <div class="table-scroll">
                    <table class="table table-hover align-middle om-table mb-3">
                        <thead>
                            <tr>
                                <th>Откуда</th>
                                <th>Куда</th>
                                <th>Тип</th>
                                <th>Длина, м</th>
                            </tr>
                        </thead>
                        <tbody>
                            @for(int i = 0; i < links.size(); i++) {
                                TopologyLink link = links.get(i);
                                <tr>
                                    <td>@formatEndpoint(link.getFromNode(), link.getFromDevice())</td>
                                    <td>@formatEndpoint(link.getToNode(), link.getToDevice())</td>
                                    <td>@link.getLinkType()</td>
                                    <td>
                                        <input type="hidden" name="links[@i].id" value="@link.getId()">
                                        <input type="number" step="0.1" class="form-control" name="links[@i].length" value="@(lengthsForm.getLinks().size() > i && lengthsForm.getLinks().get(i).getLength() != null ? lengthsForm.getLinks().get(i).getLength() : "")">
                                    </td>
                                </tr>
                            }
                            @if(links.isEmpty()) {
                                <tr><td colspan="4" class="text-center text-muted">Нет линий для указания длины</td></tr>
                            }
                        </tbody>
                    </table>
                </div>
                <button type="submit" class="btn btn-primary">Сохранить изменения</button>
            </div>
        </form>

        <div class="d-flex justify-content-between mt-4">
            <a class="btn btn-outline-secondary" href="/calculations/@calculation.getId()/wizard/step3">Назад</a>
            <a class="btn btn-primary" href="/calculations/@calculation.getId()/wizard/step5">Далее</a>
        </div>
    </section>
}
