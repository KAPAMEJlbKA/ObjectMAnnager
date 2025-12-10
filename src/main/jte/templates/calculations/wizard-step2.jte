@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation
@import com.kapamejlbka.objectmanager.domain.device.NetworkNode
@import com.kapamejlbka.objectmanager.domain.device.dto.NetworkNodeCreateRequest
@import com.kapamejlbka.objectmanager.domain.topology.MountSurfaceType
@import org.springframework.security.web.csrf.CsrfToken

@param SystemCalculation calculation
@param java.util.List<NetworkNode> nodes
@param NetworkNodeCreateRequest nodeForm
@param java.util.List<MountSurfaceType> mountSurfaces
@param CsrfToken _csrf
@param String flashSuccess = null
@param String flashError = null

<% String displaySurface(String code) {
       if (code == null) return "—";
       try {
           return MountSurfaceType.valueOf(code).getDisplayNameRu();
       } catch (Exception ignored) {
           return code;
       }
   }
%>

@templates.layout(title = "Шаг 2 — узлы", activePage = "sites", flashSuccess = flashSuccess, flashError = flashError) {
    <section class="page-card">
        @templates.fragments.`wizard-header`(calculation = calculation, step = 2)
        <h2 class="h4">Шаг 2. Узлы и шкафы</h2>
        <p class="page-subtitle">Опишите шкафы и точки коммутации для системы.</p>

        <div class="om-page-card">
            <div class="table-scroll">
                <table class="table table-hover align-middle om-table mb-3">
                    <thead>
                        <tr>
                            <th>Код</th>
                            <th>Имя</th>
                            <th>Поверхность</th>
                            <th>Размер шкафа</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        @if(nodes.isEmpty()) {
                            <tr><td colspan="5" class="text-center text-muted">Узлы не добавлены</td></tr>
                        } else {
                            @for(NetworkNode node : nodes) {
                                <tr>
                                    <td>@node.getCode()</td>
                                    <td>@node.getName()</td>
                                    <td>@displaySurface(node.getMountSurface())</td>
                                    <td>@(node.getCabinetSize() == null ? "Авто" : node.getCabinetSize())</td>
                                    <td class="text-end text-muted">Базовые автоматы: @node.getBaseCircuitBreakers(), розетки: @node.getBaseSockets()</td>
                                </tr>
                            }
                        }
                    </tbody>
                </table>
            </div>

            <form method="post" action="/calculations/@calculation.getId()/wizard/step2/nodes" class="row g-2 align-items-end mt-3">
                <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                <div class="col-auto">
                    <label class="form-label">Код</label>
                    <input name="code" class="form-control" required value="@(nodeForm.getCode() == null ? "" : nodeForm.getCode())">
                </div>
                <div class="col-auto">
                    <label class="form-label">Имя</label>
                    <input name="name" class="form-control" required value="@(nodeForm.getName() == null ? "" : nodeForm.getName())">
                </div>
                <div class="col-auto">
                    <label class="form-label">Поверхность</label>
                    <select name="mountSurface" class="form-select">
                        @for(MountSurfaceType s : mountSurfaces) {
                            <option value="@s.getCode()" @if(s.getCode().equals(nodeForm.getMountSurface())){ selected }>@s.getDisplayNameRu()</option>
                        }
                    </select>
                </div>
                <div class="col-auto">
                    <button class="btn btn-primary btn-sm" type="submit">Добавить</button>
                </div>
            </form>
        </div>

        <div class="d-flex justify-content-between mt-4">
            <a class="btn btn-outline-secondary" href="/calculations/@calculation.getId()/wizard/step1">Назад</a>
            <a class="btn btn-primary" href="/calculations/@calculation.getId()/wizard/step3">Далее</a>
        </div>
    </section>
}
