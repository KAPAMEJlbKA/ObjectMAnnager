@param com.kapamejlbka.objectmanager.domain.customer.Site site
@param java.util.List<com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation> calculations
@param com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation calculation
@param long deviceCount
@param long nodeCount
@param com.kapamejlbka.objectmanager.domain.calcengine.CalculationResult calculationResult
@param Integer materialItemCount
@param java.util.List<com.kapamejlbka.objectmanager.domain.customer.SiteAttachment> attachments
@param String flashSuccess
@param String flashError
@import java.time.format.DateTimeFormatter
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${site.getName()} — Object Manager</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="page">
<header class="topbar">
    <div class="topbar__brand">Object Manager</div>
    <div class="topbar__actions">
        <a class="button" href="/customers/${site.getCustomer().getId()}/view">К заказчику</a>
        <a class="button" href="/sites/${site.getId()}/edit">Редактировать</a>
        <a class="button" href="/logout">Выйти</a>
    </div>
</header>

<main class="container">
    <div class="page-heading">
        <div>
            <p class="muted">${site.getCustomer().getName()}</p>
            <h1>${site.getName()}</h1>
        </div>
        <div class="actions">
            <a class="button button--primary" href="/sites/${site.getId()}/calculations/start">Заполнить данные для расчёта</a>
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
                    <p class="muted">Статистика</p>
                    <h2>Сводка по объекту</h2>
                </div>
            </div>
            <div class="stats">
                <div class="stat">
                    <div class="stat__value">${calculations.size()}</div>
                    <div class="stat__label">Расчётов</div>
                </div>
                <div class="stat">
                    <div class="stat__value">${deviceCount}</div>
                    <div class="stat__label">Устройства</div>
                </div>
                <div class="stat">
                    <div class="stat__value">${nodeCount}</div>
                    <div class="stat__label">Узлы</div>
                </div>
                <div class="stat">
                    <div class="stat__value">${materialItemCount == null ? "—" : String.valueOf(materialItemCount)}</div>
                    <div class="stat__label">Материалы</div>
                </div>
            </div>
            @if(calculation != null)
                <div class="actions" style="margin-top: 12px;">
                    <a class="button button--ghost" href="/calculations/${calculation.getId()}/wizard/step1">Открыть расчёт</a>
                    <a class="button" href="/sites/${site.getId()}/calculations/start">Пересоздать</a>
                </div>
            @else
                <p class="muted" style="margin-top: 12px;">Расчёт ещё не создан. Запустите мастер, чтобы внести исходные данные.</p>
            @endif
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
                    <p class="muted">Адрес</p>
                    <p>${site.getFullAddress() == null || site.getFullAddress().isBlank() ? "—" : site.getFullAddress()}</p>
                </div>
                <div>
                    <p class="muted">Координаты</p>
                    <p>${site.getLatitude() != null && site.getLongitude() != null ? site.getLatitude() + ", " + site.getLongitude() : "—"}</p>
                </div>
                <div style="grid-column: span 2;">
                    <p class="muted">Описание</p>
                    <p>${site.getDescription() == null || site.getDescription().isBlank() ? "Нет описания" : site.getDescription()}</p>
                </div>
            </div>

            <div class="card__header" style="padding: 0; margin-top: 12px;">
                <div>
                    <p class="muted">Контакты</p>
                    <h3>Ответственные лица</h3>
                </div>
            </div>
            @if(site.isUseCustomerContact())
                <p class="muted">Используются контактные данные заказчика.</p>
            @endif
            <div class="form__grid form__grid--two">
                <div>
                    <p class="muted">Контактное лицо</p>
                    <p>${site.getContactName() == null || site.getContactName().isBlank() ? "—" : site.getContactName()}</p>
                </div>
                <div>
                    <p class="muted">Должность</p>
                    <p>${site.getContactPosition() == null || site.getContactPosition().isBlank() ? "—" : site.getContactPosition()}</p>
                </div>
                <div>
                    <p class="muted">Телефон</p>
                    <p>${site.getContactPhone() == null || site.getContactPhone().isBlank() ? "—" : site.getContactPhone()}</p>
                </div>
                <div>
                    <p class="muted">Email</p>
                    <p>${site.getContactEmail() == null || site.getContactEmail().isBlank() ? "—" : site.getContactEmail()}</p>
                </div>
            </div>
        </div>
    </section>

    <section class="card">
        <div class="card__header">
            <div>
                <p class="muted">Расчёты</p>
                <h2>История расчётов</h2>
            </div>
            <a class="button button--ghost" href="/sites/${site.getId()}/calculations/start">Новый расчёт</a>
        </div>

        @if(calculations.isEmpty())
            <p class="muted">Расчёты по объекту ещё не создавались.</p>
        @else
            <div class="table">
                <div class="table__row table__row--head">
                    <div>ID</div>
                    <div>Название</div>
                    <div>Тип системы</div>
                    <div>Статус</div>
                    <div>Действия</div>
                </div>
                @for(var calc : calculations)
                    <div class="table__row">
                        <div>${calc.getId()}</div>
                        <div>
                            <div class="table__title">${calc.getName()}</div>
                            <div class="muted">${calc.getDescription()}</div>
                        </div>
                        <div>${calc.getSystemType() == null ? "—" : calc.getSystemType().toString()}</div>
                        <div>${calc.getStatus() == null ? "—" : calc.getStatus().toString()}</div>
                        <div class="actions">
                            <a class="button button--primary" href="/calculations/${calc.getId()}/wizard/step1">Открыть</a>
                        </div>
                    </div>
                @endfor
            </div>
        @endif
    </section>

    <section class="card">
        <div class="card__header">
            <div>
                <p class="muted">Материалы</p>
                <h2>Последний расчёт</h2>
            </div>
        </div>
        @if(calculationResult == null)
            <p class="muted">Нет выполненного расчёта. Заполните данные, чтобы увидеть материалы.</p>
        @else
            <p class="muted">Время расчёта: ${calculationResult.executedAt() == null
                ? "—"
                : calculationResult.executedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}</p>
            <div class="table">
                <div class="table__row table__row--head">
                    <div>Код</div>
                    <div>Материал</div>
                    <div>Категория</div>
                    <div>Ед.</div>
                    <div>Кол-во</div>
                </div>
                @for(var item : calculationResult.items())
                    <div class="table__row">
                        <div>${item.materialCode()}</div>
                        <div class="table__title">${item.materialName()}</div>
                        <div>${item.category() == null ? "" : item.category().toString()}</div>
                        <div>${item.unit()}</div>
                        <div>${item.quantity()}</div>
                    </div>
                @endfor
            </div>
        @endif
    </section>

    <section class="card">
        <div class="card__header">
            <div>
                <p class="muted">Документы</p>
                <h2>Вложения</h2>
            </div>
        </div>

        @if(attachments.isEmpty())
            <p class="muted">Нет приложенных файлов.</p>
        @else
            <div class="table">
                <div class="table__row table__row--head">
                    <div>ID</div>
                    <div>Файл</div>
                    <div>Тип</div>
                    <div>Размер</div>
                    <div>Действия</div>
                </div>
                @for(var attachment : attachments)
                    <div class="table__row">
                        <div>${attachment.getId()}</div>
                        <div>
                            <div class="table__title">${attachment.getFileName()}</div>
                            <div class="muted">${attachment.getAttachmentType() == null ? "" : attachment.getAttachmentType().toString()}</div>
                        </div>
                        <div>${attachment.getContentType() == null ? "—" : attachment.getContentType()}</div>
                        <div>${attachment.getSize() == null ? "—" : attachment.getSize() + " байт"}</div>
                        <div class="actions">
                            <a class="button button--ghost" href="/attachments/${attachment.getId()}" target="_blank">Открыть</a>
                            <form method="post" action="/attachments/${attachment.getId()}">
                                <input type="hidden" name="_method" value="delete">
                                <button class="button" type="submit">Удалить</button>
                            </form>
                        </div>
                    </div>
                @endfor
            </div>
        @endif
    </section>
</main>
</body>
</html>
