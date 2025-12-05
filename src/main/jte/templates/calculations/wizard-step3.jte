@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation
@import com.kapamejlbka.objectmanager.domain.device.EndpointDevice
@import com.kapamejlbka.objectmanager.domain.device.NetworkNode
@import com.kapamejlbka.objectmanager.domain.topology.TopologyLink
@import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkCreateRequest
@import com.kapamejlbka.objectmanager.web.view.DeviceTypeDisplayUtil
@import org.springframework.security.web.csrf.CsrfToken

@param SystemCalculation calculation
@param java.util.List<EndpointDevice> devices
@param java.util.List<NetworkNode> nodes
@param java.util.List<TopologyLink> links
@param TopologyLinkCreateRequest linkForm
@param java.util.List<String> linkTypes
@param CsrfToken _csrf
@param String flashSuccess = null
@param String flashError = null

<% String formatEndpoint(NetworkNode n, EndpointDevice d) {
       if (n != null) return "[NODE] " + n.getName();
       if (d != null) return "[DEVICE] " + DeviceTypeDisplayUtil.displayName(d.getType()) + " — " + d.getName();
       return "—";
   }
%>

@templates.layout(title = "Шаг 3 — связи", activePage = "sites", flashSuccess = flashSuccess, flashError = flashError) {
    <section class="page-card">
        @templates.fragments.`wizard-header`(calculation = calculation, step = 3)
        <h2 class="h4">Шаг 3. Связи (топология)</h2>
        <p class="page-subtitle">Опишите соединения между узлами и устройствами. Графика появится позже, пока табличный ввод.</p>

        <div class="om-page-card">
            <div class="table-scroll">
                <table class="table table-hover align-middle om-table mb-3">
                    <thead>
                        <tr>
                            <th>Откуда</th>
                            <th>Куда</th>
                            <th>Тип</th>
                            <th>Волокна</th>
                            <th>Длина</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        @if(links.isEmpty()) {
                            <tr><td colspan="6" class="text-center text-muted">Связи не добавлены</td></tr>
                        } else {
                            @for(TopologyLink link : links) {
                                <tr>
                                    <td>@formatEndpoint(link.getFromNode(), link.getFromDevice())</td>
                                    <td>@formatEndpoint(link.getToNode(), link.getToDevice())</td>
                                    <td>@link.getLinkType()</td>
                                    <td>@(link.getFiberCores() == null ? "—" : link.getFiberCores())</td>
                                    <td>@(link.getCableLength() == null ? "—" : link.getCableLength() + " м")</td>
                                    <td class="text-end">
                                        <form method="post" action="/calculations/@calculation.getId()/wizard/step3/links/@link.getId()/delete">
                                            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                                            <button class="btn btn-outline-danger btn-sm" type="submit">Удалить</button>
                                        </form>
                                    </td>
                                </tr>
                            }
                        }
                    </tbody>
                </table>
            </div>

            <form method="post" action="/calculations/@calculation.getId()/wizard/step3/links" class="row g-2 align-items-end mt-3">
                <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                <div class="col-auto">
                    <label class="form-label">От</label>
                    <select name="fromEndpoint" class="form-select" required>
                        @for(NetworkNode node : nodes) { <option value="NODE:@node.getId()">[NODE] @node.getName()</option> }
                        @for(EndpointDevice d : devices) { <option value="DEVICE:@d.getId()">[DEVICE] @DeviceTypeDisplayUtil.displayName(d.getType()) — @d.getName()</option> }
                    </select>
                </div>
                <div class="col-auto">
                    <label class="form-label">К</label>
                    <select name="toEndpoint" class="form-select" required>
                        @for(NetworkNode node : nodes) { <option value="NODE:@node.getId()">[NODE] @node.getName()</option> }
                        @for(EndpointDevice d : devices) { <option value="DEVICE:@d.getId()">[DEVICE] @DeviceTypeDisplayUtil.displayName(d.getType()) — @d.getName()</option> }
                    </select>
                </div>
                <div class="col-auto">
                    <label class="form-label">Тип линии</label>
                    <select name="linkType" class="form-select" required>
                        @for(String type : linkTypes) { <option value="@type">@type</option> }
                    </select>
                </div>
                <div class="col-auto">
                    <label class="form-label">Количество волокон (для FIBER)</label>
                    <input name="fiberCores" type="number" class="form-control" min="0" value="@(linkForm.getFiberCores() == null ? "" : linkForm.getFiberCores())">
                </div>
                <div class="col-auto">
                    <label class="form-label">Длина (м, опционально)</label>
                    <input name="cableLength" type="number" step="0.1" class="form-control" value="@(linkForm.getCableLength() == null ? "" : linkForm.getCableLength())">
                </div>
                <div class="col-auto">
                    <button type="submit" class="btn btn-primary btn-sm">Добавить</button>
                </div>
            </form>
        </div>

        <div class="d-flex justify-content-between mt-4">
            <a class="btn btn-outline-secondary" href="/calculations/@calculation.getId()/wizard/step2">Назад</a>
            <a class="btn btn-primary" href="/calculations/@calculation.getId()/wizard/step4">Далее</a>
        </div>
    </section>
}
