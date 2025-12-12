@param com.kapamejlbka.objectmanager.domain.customer.Customer customer
@param java.util.List<com.kapamejlbka.objectmanager.domain.customer.Site> sites
@import java.time.format.DateTimeFormatter
@var DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${customer.getName()} — Заказчик</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="page">
<header class="topbar">
    <div class="topbar__brand">Object Manager</div>
    <div class="topbar__actions">
        <a class="button" href="/">К заказчикам</a>
        <a class="button" href="/customers/${customer.getId()}/sites/new">Новый объект</a>
        <a class="button" href="/logout">Выйти</a>
    </div>
</header>

<main class="container">
    <div class="page-heading">
        <div>
            <p class="muted">Карточка заказчика</p>
            <h1>${customer.getName()}</h1>
        </div>
    </div>

    <section class="grid">
        <div class="card">
            <div class="card__header">
                <div>
                    <p class="muted">Общие данные</p>
                    <h2>Профиль</h2>
                </div>
            </div>
            <div class="form__grid form__grid--two">
                <div>
                    <p class="muted">ИНН</p>
                    <p>${customer.getTaxNumber() == null || customer.getTaxNumber().isBlank() ? "—" : customer.getTaxNumber()}</p>
                </div>
                <div>
                    <p class="muted">Город</p>
                    <p>${customer.getCity() == null || customer.getCity().isBlank() ? "—" : customer.getCity()}</p>
                </div>
                <div>
                    <p class="muted">Страна</p>
                    <p>${customer.getCountry() == null || customer.getCountry().isBlank() ? "—" : customer.getCountry()}</p>
                </div>
                <div>
                    <p class="muted">Адрес</p>
                    <p>${customer.getAddressLine() == null || customer.getAddressLine().isBlank() ? "—" : customer.getAddressLine()}</p>
                </div>
                <div>
                    <p class="muted">Создан</p>
                    <p>${customer.getCreatedAt().format(dateTimeFormatter)}</p>
                </div>
                <div>
                    <p class="muted">Обновлён</p>
                    <p>${customer.getUpdatedAt().format(dateTimeFormatter)}</p>
                </div>
            </div>

            <div class="card__header" style="padding: 0; margin-top: 12px;">
                <div>
                    <p class="muted">Контакты</p>
                    <h3>Ответственные лица</h3>
                </div>
            </div>
            <div class="form__grid form__grid--two">
                <div>
                    <p class="muted">Контактное лицо</p>
                    <p>${customer.getContactName() == null || customer.getContactName().isBlank() ? "—" : customer.getContactName()}</p>
                </div>
                <div>
                    <p class="muted">Должность</p>
                    <p>${customer.getContactPosition() == null || customer.getContactPosition().isBlank() ? "—" : customer.getContactPosition()}</p>
                </div>
                <div>
                    <p class="muted">Телефон</p>
                    <p>${customer.getContactPhone() == null || customer.getContactPhone().isBlank() ? "—" : customer.getContactPhone()}</p>
                </div>
                <div>
                    <p class="muted">Email</p>
                    <p>${customer.getContactEmail() == null || customer.getContactEmail().isBlank() ? "—" : customer.getContactEmail()}</p>
                </div>
                <div style="grid-column: span 2;">
                    <p class="muted">Заметки</p>
                    <p>${customer.getNotes() == null || customer.getNotes().isBlank() ? "Нет заметок" : customer.getNotes()}</p>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card__header">
                <div>
                    <p class="muted">Объекты</p>
                    <h2>Площадки заказчика</h2>
                </div>
                <a class="button button--ghost" href="/customers/${customer.getId()}/sites/new">Добавить объект</a>
            </div>

            @if(sites.isEmpty())
                <p class="muted">Для заказчика ещё нет объектов.</p>
            @else
                <div class="table">
                    <div class="table__row table__row--head">
                        <div>Название</div>
                        <div>Адрес</div>
                        <div>Контакты</div>
                        <div>Действия</div>
                    </div>
                    @for(com.kapamejlbka.objectmanager.domain.customer.Site site : sites)
                        <div class="table__row">
                            <div class="table__title">${site.getName()}</div>
                            <div>${site.getFullAddress() == null || site.getFullAddress().isBlank() ? "—" : site.getFullAddress()}</div>
                            <div>
                                <div class="table__title">${site.getContactName() == null || site.getContactName().isBlank() ? "—" : site.getContactName()}</div>
                                <div class="muted">${site.getContactPhone() == null || site.getContactPhone().isBlank() ? "" : site.getContactPhone()}</div>
                                <div class="muted">${site.getContactEmail() == null || site.getContactEmail().isBlank() ? "" : site.getContactEmail()}</div>
                            </div>
                            <div class="actions">
                                <a class="button button--ghost" href="/sites/${site.getId()}">Открыть</a>
                                <a class="button" href="/sites/${site.getId()}/edit">Редактировать</a>
                            </div>
                        </div>
                    @endfor
                </div>
            @endif
        </div>
    </section>
</main>
</body>
</html>
