@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation
@import com.kapamejlbka.objectmanager.domain.device.EndpointDevice
@import com.kapamejlbka.objectmanager.domain.device.dto.EndpointDeviceCreateRequest
@import com.kapamejlbka.objectmanager.web.view.DeviceTypeDisplayUtil
@import org.springframework.security.web.csrf.CsrfToken

@param SystemCalculation calculation
@param java.util.List<EndpointDevice> devices
@param EndpointDeviceCreateRequest deviceForm
@param java.util.List<DeviceTypeDisplayUtil.DeviceTypeOption> deviceTypes
@param java.util.List<String> mountSurfaces
@param CsrfToken _csrf
@param String flashSuccess = null
@param String flashError = null

@templates.layout(title = "Шаг 1 — устройства", activePage = "sites", flashSuccess = flashSuccess, flashError = flashError) {
    <section class="page-card">
        @templates.fragments.`wizard-header`(calculation = calculation, step = 1)
        <h2 class="h4">Шаг 1. Конечные устройства</h2>
        <p class="page-subtitle">Добавьте камеры, точки доступа, считыватели, турникеты и другие устройства.</p>

        <div class="om-page-card">
            <div class="table-scroll">
                <table class="table table-hover align-middle om-table mb-3">
                    <thead>
                        <tr>
                            <th>Тип</th>
                            <th>Код</th>
                            <th>Название</th>
                            <th>Место</th>
                            <th>Глубина</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        @if(devices.isEmpty()) {
                            <tr><td colspan="6" class="text-center text-muted">Устройства не добавлены</td></tr>
                        } else {
                            @for(EndpointDevice device : devices) {
                                <tr>
                                    <td>@DeviceTypeDisplayUtil.displayName(device.getType())</td>
                                    <td>@(device.getCode() == null ? "—" : device.getCode())</td>
                                    <td>@device.getName()</td>
                                    <td>@(device.getLocationDescription() == null ? "—" : device.getLocationDescription())</td>
                                    <td>@(device.getViewDepth() == null ? "—" : device.getViewDepth())</td>
                                    <td class="text-end">
                                        <form method="post" action="/calculations/@calculation.getId()/wizard/step1/devices/@device.getId()/delete">
                                            <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                                            <button type="submit" class="btn btn-outline-danger btn-sm">Удалить</button>
                                        </form>
                                    </td>
                                </tr>
                            }
                        }
                    </tbody>
                </table>
            </div>

            <form method="post" action="/calculations/@calculation.getId()/wizard/step1/devices" class="row g-2 align-items-end mt-3">
                <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                <div class="col-auto">
                    <label class="form-label">Тип</label>
                    <select name="type" class="form-select" required>
                        @for(DeviceTypeDisplayUtil.DeviceTypeOption type : deviceTypes) {
                            <option value="@type.code()">@type.displayName()</option>
                        }
                    </select>
                </div>
                <div class="col-auto">
                    <label class="form-label">Код</label>
                    <input name="code" class="form-control" value="@(deviceForm.getCode() == null ? "" : deviceForm.getCode())">
                </div>
                <div class="col-auto">
                    <label class="form-label">Имя</label>
                    <input name="name" class="form-control" required value="@(deviceForm.getName() == null ? "" : deviceForm.getName())">
                </div>
                <div class="col-auto">
                    <label class="form-label">Место установки</label>
                    <input name="locationDescription" class="form-control" value="@(deviceForm.getLocationDescription() == null ? "" : deviceForm.getLocationDescription())">
                </div>
                <div class="col-auto">
                    <label class="form-label">Глубина просмотра</label>
                    <input name="viewDepth" type="number" step="0.1" class="form-control" value="@(deviceForm.getViewDepth() == null ? "" : deviceForm.getViewDepth())">
                </div>
                <div class="col-auto">
                    <label class="form-label">Поверхность</label>
                    <select name="mountSurface" class="form-select">
                        @for(String s : mountSurfaces) {
                            <option value="@s">@s</option>
                        }
                    </select>
                </div>
                <div class="col-auto">
                    <button class="btn btn-primary btn-sm" type="submit">Добавить</button>
                </div>
            </form>
        </div>

        <div class="d-flex justify-content-between mt-4">
            <a class="btn btn-outline-secondary disabled">Назад</a>
            <a class="btn btn-primary" href="/calculations/@calculation.getId()/wizard/step2">Далее</a>
        </div>
    </section>
}
