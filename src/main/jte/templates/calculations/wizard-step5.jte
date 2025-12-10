@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation

@param SystemCalculation calculation
@param java.util.List<String> routeTypes
@param java.util.List<String> mountSurfaces
@param String flashSuccess = null
@param String flashError = null

@templates.layout(title = "Шаг 5 — трассы", activePage = "sites", flashSuccess = flashSuccess, flashError = flashError) {
    <link rel="stylesheet" href="/css/topology.css">
    <link rel="stylesheet" href="/css/routes-editor.css">
    <section class="page-card">
        @templates.fragments.`wizard-header`(calculation = calculation, step = 5)
        <div class="om-page-card om-routes-editor" data-calc-id="@calculation.getId()">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <p class="text-muted small mb-1">Мастер заполнения — шаг 5 из 7</p>
                    <h2 class="h5 mb-0">Редактор трасс</h2>
                </div>
                <div class="d-flex gap-2">
                    <a class="btn btn-outline-secondary" href="/calculations/@calculation.getId()/wizard/step4">Назад</a>
                    <a class="btn btn-primary" href="/calculations/@calculation.getId()/wizard/step6">Далее</a>
                </div>
            </div>

            <div class="d-flex" style="height: 620px;">
                <div class="om-routes-side om-routes-side-left">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <div>
                            <h6 class="mb-0">Трассы</h6>
                            <p class="text-muted small mb-0">Выберите трассу для редактирования</p>
                        </div>
                    </div>
                    <form id="route-create-form" class="om-route-form mb-3">
                        <div class="mb-2">
                            <label class="form-label form-label-sm">Название</label>
                            <input type="text" class="form-control form-control-sm" id="route-create-name" placeholder="Новая трасса" required>
                        </div>
                        <div class="row g-2">
                            <div class="col-6">
                                <label class="form-label form-label-sm">Тип</label>
                                <select class="form-select form-select-sm" id="route-create-type" required>
                                    @for(String type : routeTypes) { <option value="@type">@type</option> }
                                </select>
                            </div>
                            <div class="col-6">
                                <label class="form-label form-label-sm">Поверхность</label>
                                <select class="form-select form-select-sm" id="route-create-surface" required>
                                    <option value="">—</option>
                                    @for(String surface : mountSurfaces) { <option value="@surface">@surface</option> }
                                </select>
                            </div>
                        </div>
                        <button type="submit" class="btn btn-primary btn-sm w-100 mt-2">Добавить трассу</button>
                    </form>
                    <div id="routes-list" class="om-routes-list"></div>
                </div>

                <div class="om-routes-canvas-wrapper flex-grow-1 mx-3">
                    <svg id="routes-links-layer"></svg>
                    <div id="routes-nodes-layer"></div>
                </div>

                <div class="om-routes-side om-routes-side-right">
                    <div id="routes-inspector" class="text-muted small">Выберите трассу или линию для просмотра свойств.</div>
                </div>
            </div>
        </div>
    </section>
    <script src="/js/routes-editor.js"></script>
}
