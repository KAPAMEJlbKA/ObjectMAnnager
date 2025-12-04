@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation
@import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute
@import com.kapamejlbka.objectmanager.domain.topology.RouteSegmentLink
@import com.kapamejlbka.objectmanager.domain.topology.TopologyLink
@import com.kapamejlbka.objectmanager.domain.topology.dto.InstallationRouteCreateRequest
@import com.kapamejlbka.objectmanager.domain.topology.dto.RouteSegmentLinkCreateRequest
@import org.springframework.security.web.csrf.CsrfToken

@param SystemCalculation calculation
@param java.util.List<InstallationRoute> routes
@param java.util.Map<Long, java.util.List<RouteSegmentLink>> routeLinks
@param java.util.List<TopologyLink> topologyLinks
@param InstallationRouteCreateRequest routeForm
@param java.util.List<String> routeTypes
@param java.util.List<String> mountSurfaces
@param java.util.List<String> orientations
@param java.util.List<String> fixingMethods
@param RouteSegmentLinkCreateRequest routeLinkForm
@param CsrfToken _csrf
@param String flashSuccess = null
@param String flashError = null

@templates.layout(title = "Шаг 5 — трассы", activePage = "sites", flashSuccess = flashSuccess, flashError = flashError) {
    <section class="page-card">
        @templates.fragments.`wizard-header`(calculation = calculation, step = 5)
        <h2 class="h4">Шаг 5. Трассы и материалы</h2>
        <p class="page-subtitle">Группируйте линии в трассы и задайте тип прокладки для каждой.</p>

        <div class="om-page-card">
            <div class="table-scroll">
                <table class="table table-hover align-middle om-table mb-3">
                    <thead>
                        <tr>
                            <th>Имя</th>
                            <th>Тип</th>
                            <th>Длина, м</th>
                            <th>Поверхность</th>
                            <th>Ориентация</th>
                            <th>Крепление</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        @if(routes.isEmpty()) {
                            <tr><td colspan="7" class="text-center text-muted">Трассы не добавлены</td></tr>
                        } else {
                            @for(InstallationRoute route : routes) {
                                <tr>
                                    <td>@route.getName()</td>
                                    <td>@route.getRouteType()</td>
                                    <td>@route.getLengthMeters()</td>
                                    <td>@(route.getMountSurface() == null ? "—" : route.getMountSurface())</td>
                                    <td>@(route.getOrientation() == null ? "—" : route.getOrientation())</td>
                                    <td>@(route.getFixingMethod() == null ? "—" : route.getFixingMethod())</td>
                                    <td class="text-end">
                                        <form method="post" action="/calculations/@calculation.getId()/wizard/step5/routes/@route.getId()/delete">
                                            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                                            <button class="btn btn-outline-danger btn-sm" type="submit">Удалить</button>
                                        </form>
                                    </td>
                                </tr>
                                <tr class="table-secondary">
                                    <td colspan="7">
                                        <strong>Линии:</strong>
                                        @for(RouteSegmentLink rl : routeLinks.getOrDefault(route.getId(), java.util.List.of())) {
                                            <span class="badge bg-light text-dark me-1">#@rl.getTopologyLink().getId()</span>
                                        }
                                    </td>
                                </tr>
                            }
                        }
                    </tbody>
                </table>
            </div>

            <h5 class="mt-3">Добавить трассу</h5>
            <form method="post" action="/calculations/@calculation.getId()/wizard/step5/routes" class="row g-2 align-items-end">
                <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                <div class="col-auto">
                    <label class="form-label">Имя</label>
                    <input name="name" class="form-control" required value="@(routeForm.getName() == null ? "" : routeForm.getName())">
                </div>
                <div class="col-auto">
                    <label class="form-label">Тип</label>
                    <select name="routeType" class="form-select" required>
                        @for(String t : routeTypes) { <option value="@t">@t</option> }
                    </select>
                </div>
                <div class="col-auto">
                    <label class="form-label">Длина</label>
                    <input name="lengthMeters" type="number" step="0.1" class="form-control" required value="@(routeForm.getLengthMeters() == null ? "" : routeForm.getLengthMeters())">
                </div>
                <div class="col-auto">
                    <label class="form-label">Поверхность</label>
                    <select name="mountSurface" class="form-select">
                        @for(String s : mountSurfaces) { <option value="@s">@s</option> }
                    </select>
                </div>
                <div class="col-auto">
                    <label class="form-label">Ориентация (гофра)</label>
                    <select name="orientation" class="form-select">
                        <option value="">—</option>
                        @for(String o : orientations) { <option value="@o">@o</option> }
                    </select>
                </div>
                <div class="col-auto">
                    <label class="form-label">Крепление (bare)</label>
                    <select name="fixingMethod" class="form-select">
                        <option value="">—</option>
                        @for(String f : fixingMethods) { <option value="@f">@f</option> }
                    </select>
                </div>
                <div class="col-auto">
                    <button class="btn btn-primary btn-sm" type="submit">Добавить трассу</button>
                </div>
            </form>

            <h5 class="mt-4">Привязка линий к трассам</h5>
            <form method="post" action="/calculations/@calculation.getId()/wizard/step5/route-links" class="row g-2 align-items-end">
                <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                <div class="col-auto">
                    <label class="form-label">Трасса</label>
                    <select name="routeId" class="form-select" required>
                        @for(InstallationRoute route : routes) { <option value="@route.getId()">@route.getName()</option> }
                    </select>
                </div>
                <div class="col-auto">
                    <label class="form-label">Линии</label>
                    <select name="topologyLinkIds" multiple class="form-select" size="4">
                        @for(TopologyLink link : topologyLinks) {
                            <option value="@link.getId()">@link.getId() @link.getLinkType()</option>
                        }
                    </select>
                </div>
                <div class="col-auto">
                    <button class="btn btn-primary btn-sm" type="submit">Привязать</button>
                </div>
            </form>
        </div>

        <div class="d-flex justify-content-between mt-4">
            <a class="btn btn-outline-secondary" href="/calculations/@calculation.getId()/wizard/step4">Назад</a>
            <a class="btn btn-primary" href="/calculations/@calculation.getId()/wizard/step6">Далее</a>
        </div>
    </section>
}
