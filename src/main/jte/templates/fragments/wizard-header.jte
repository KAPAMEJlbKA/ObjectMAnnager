@import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation

@param SystemCalculation calculation
@param int step

<div class="om-wizard-header">
    <div>
        <p class="eyebrow mb-1">Мастер заполнения данных</p>
        <h1 class="page-title">@calculation.getSite().getName() &mdash; @calculation.getName()</h1>
        <p class="page-subtitle">Заполните данные по объекту шаг за шагом. Текущий статус: @calculation.getStatus()</p>
    </div>
</div>

<ul class="nav nav-pills om-wizard-steps mb-3">
    <li class="nav-item"><a class="nav-link @(step == 1 ? "active" : (step > 1 ? "done" : ""))">1. Устройства</a></li>
    <li class="nav-item"><a class="nav-link @(step == 2 ? "active" : (step > 2 ? "done" : ""))">2. Узлы</a></li>
    <li class="nav-item"><a class="nav-link @(step == 3 ? "active" : (step > 3 ? "done" : ""))">3. Связи</a></li>
    <li class="nav-item"><a class="nav-link @(step == 4 ? "active" : (step > 4 ? "done" : ""))">4. Длины</a></li>
    <li class="nav-item"><a class="nav-link @(step == 5 ? "active" : (step > 5 ? "done" : ""))">5. Трассы</a></li>
    <li class="nav-item"><a class="nav-link @(step == 6 ? "active" : (step > 6 ? "done" : ""))">6. Узлы</a></li>
    <li class="nav-item"><a class="nav-link @(step == 7 ? "active" : "")">7. Расчёт</a></li>
</ul>
