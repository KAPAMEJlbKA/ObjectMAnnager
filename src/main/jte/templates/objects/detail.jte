@import java.time.format.DateTimeFormatter
@param com.kapamejlbka.objectmanager.domain.customer.ManagedObject object
@param java.util.List<com.kapamejlbka.objectmanager.domain.customer.ObjectChange> changes
@param boolean isAuthor
@param boolean canRequestDeletion
@param boolean canRevokeDeletion
@param boolean canDeletePermanently
@param String flashSuccess
@param String flashError
@var DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${object.getName()} — Object Manager</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="page">
<header class="topbar">
    <div class="topbar__brand">Object Manager</div>
    <div class="topbar__actions">
        <a class="button" href="/">На главную</a>
        <a class="button" href="/logout">Выйти</a>
    </div>
</header>

<main class="container">
    <div class="page-heading">
        <div>
            <p class="muted">${object.getCustomer().getName()}</p>
            <h1>${object.getName()}</h1>
        </div>
        <div class="actions">
            @if(canRequestDeletion && !object.isDeletionRequested())
                <form method="post" action="/objects/${object.getId()}/deletion/request">
                    <button class="button button--ghost" type="submit">Запросить удаление</button>
                </form>
            @elseif(canRevokeDeletion && object.isDeletionRequested())
                <form method="post" action="/objects/${object.getId()}/deletion/revoke">
                    <button class="button button--ghost" type="submit">Отозвать</button>
                </form>
            @endif
            @if(canDeletePermanently)
                <form method="post" action="/objects/${object.getId()}/delete" style="margin-left: 8px;">
                    <button class="button" type="submit">Удалить навсегда</button>
                </form>
            @endif
        </div>
    </div>

    @if(flashSuccess != null)
        <div class="alert alert--success">${flashSuccess}</div>
    @endif
    @if(flashError != null)
        <div class="alert alert--error">${flashError}</div>
    @endif

    <section class="grid">
        <div class="card">
            <div class="card__header">
                <div>
                    <p class="muted">Статус</p>
                    <h2>Управление удалением</h2>
                </div>
            </div>
            <p class="muted">${object.isDeletionRequested() ? "Запрос на удаление отправлен" : "Удаление не запрошено"}</p>
            @if(object.isDeletionRequested())
                <p>Инициатор: ${object.getDeletionRequestedBy() == null ? "—" : object.getDeletionRequestedBy().getUsername()}</p>
                <p>Время: ${object.getDeletionRequestedAt() == null ? "—" : object.getDeletionRequestedAt().format(dateTimeFormatter)}</p>
            @endif
            <p class="muted" style="margin-top: 12px;">Мягкое удаление доступно автору объекта, полное — только администратору.</p>
        </div>

        <div class="card">
            <div class="card__header">
                <div>
                    <p class="muted">Описание</p>
                    <h2>Информация об объекте</h2>
                </div>
            </div>
            <div class="form__grid form__grid--two">
                <div>
                    <p class="muted">Описание</p>
                    <p>${object.getDescription() == null || object.getDescription().isBlank() ? "—" : object.getDescription()}</p>
                </div>
                <div>
                    <p class="muted">Первичные данные</p>
                    <p>${object.getPrimaryData() == null || object.getPrimaryData().isBlank() ? "—" : object.getPrimaryData()}</p>
                </div>
                <div>
                    <p class="muted">Координаты</p>
                    <p>${object.getLatitude() != null && object.getLongitude() != null ? object.getLatitude() + ", " + object.getLongitude() : "—"}</p>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card__header">
                <div>
                    <p class="muted">Редактор</p>
                    <h2>Статус расчётов</h2>
                </div>
                <div class="actions">
                    @if(object.getStatus() != com.kapamejlbka.objectmanager.domain.customer.ManagedObjectStatus.IN_PROGRESS)
                        <form method="post" action="/objects/${object.getId()}/editor/start">
                            <button class="button button--ghost" type="submit">Открыть редактор</button>
                        </form>
                    @endif
                    <form method="post" action="/objects/${object.getId()}/calculation/reset" style="margin-left: 8px;">
                        <button class="button button--ghost" type="submit">Сбросить</button>
                    </form>
                    <form method="post" action="/objects/${object.getId()}/calculation/complete" style="margin-left: 8px;">
                        <button class="button" type="submit">Отметить как рассчитан</button>
                    </form>
                </div>
            </div>
            <p class="muted">Текущий статус: ${object.getStatus()}</p>
            @if(object.getStatus() == com.kapamejlbka.objectmanager.domain.customer.ManagedObjectStatus.IN_PROGRESS)
                <div class="alert alert--error" style="margin-top: 12px;">Редактор уже открыт. Параллельное редактирование запрещено.</div>
            @elseif(object.getStatus() == com.kapamejlbka.objectmanager.domain.customer.ManagedObjectStatus.CALCULATED)
                <p class="muted" style="margin-top: 12px;">Последний расчёт успешно завершён.</p>
            @else
                <p class="muted" style="margin-top: 12px;">Расчёт ещё не запускался или был сброшен.</p>
            @endif
        </div>
    </section>

    <section class="card">
        <div class="card__header">
            <div>
                <p class="muted">История</p>
                <h2>Изменения объекта</h2>
            </div>
        </div>
        @if(changes.isEmpty())
            <p class="muted">История изменений пока пуста.</p>
        @else
            <div class="table">
                <div class="table__row table__row--head">
                    <div>Время</div>
                    <div>Пользователь</div>
                    <div>Действие</div>
                    <div>Поле</div>
                </div>
                @for(var change : changes)
                    <div class="table__row">
                        <div>${change.getChangedAt() == null ? "—" : change.getChangedAt().format(dateTimeFormatter)}</div>
                        <div>${change.getUser() == null ? "—" : change.getUser().getUsername()}</div>
                        <div>${change.getSummary()}</div>
                        <div>${change.getFieldName() == null ? "—" : change.getFieldName()}</div>
                    </div>
                @endfor
            </div>
        @endif
    </section>
</main>
</body>
</html>
