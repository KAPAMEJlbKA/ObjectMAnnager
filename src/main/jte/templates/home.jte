@param java.util.List<com.kapamejlbka.objectmanager.domain.customer.Customer> customers
@param String query
@param String sort
@param int pageSize
@param int currentPage
@param int totalPages
@param int totalCount
@param java.util.Map<Long, Long> siteCounts
@import java.time.format.DateTimeFormatter
@var DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Object Manager — Заказчики</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="page">
<header class="topbar">
    <div class="topbar__brand">Object Manager</div>
    <div class="topbar__actions">
        <a class="button" href="/logout">Выйти</a>
    </div>
</header>

<main class="container">
    <div class="page-heading">
        <div>
            <p class="muted">Главная</p>
            <h1>Заказчики</h1>
        </div>
        <div class="actions">
            <a class="button" href="/admin">Админка</a>
        </div>
    </div>

    <section class="card">
        <div class="card__header">
            <div>
                <p class="muted">Поиск</p>
                <h2>Найти заказчика</h2>
            </div>
        </div>
        <form class="form form--inline" method="get" action="/">
            <label class="form__label form__label--grow">Имя или ИНН
                <input class="form__control" type="text" name="q" placeholder="Название или ИНН" value="${query}">
            </label>

            <label class="form__label">Сортировка
                <select class="form__control" name="sort">
                    <option value="name" selected?="${sort != null && "name".equalsIgnoreCase(sort)}">По названию</option>
                    <option value="updated" selected?="${sort != null && "updated".equalsIgnoreCase(sort)}">По обновлению</option>
                    <option value="created" selected?="${sort != null && "created".equalsIgnoreCase(sort)}">По созданию</option>
                </select>
            </label>

            <label class="form__label">На странице
                <select class="form__control" name="size">
                    <option value="6" selected?="${pageSize == 6}">6</option>
                    <option value="12" selected?="${pageSize == 12}">12</option>
                    <option value="24" selected?="${pageSize == 24}">24</option>
                </select>
            </label>

            <button class="button button--primary" type="submit">Искать</button>
        </form>
    </section>

    <section class="card">
        <div class="card__header">
            <div>
                <p class="muted">Результаты (${totalCount})</p>
                <h2>Список заказчиков</h2>
            </div>
        </div>

        @if(customers == null || customers.isEmpty())
            <p class="muted">Нет заказчиков по заданным условиям.</p>
        @else
            <div class="table">
                <div class="table__row table__row--head">
                    <div>Название</div>
                    <div>Контакты</div>
                    <div>ИНН</div>
                    <div>Город</div>
                    <div>Объекты</div>
                    <div>Действия</div>
                </div>

                @for(com.kapamejlbka.objectmanager.domain.customer.Customer customer : customers)
                    <div class="table__row">
                        <div>
                            <div class="table__title">${customer.getName()}</div>
                            <div class="muted">Создан: ${customer.getCreatedAt().format(dateTimeFormatter)}</div>
                        </div>

                        <div>
                            <div class="table__title">
                                ${customer.getContactName() == null || customer.getContactName().isBlank() ? "—" : customer.getContactName()}
                            </div>
                            <div class="muted">
                                ${customer.getContactPhone() == null || customer.getContactPhone().isBlank() ? "" : customer.getContactPhone()}
                            </div>
                            <div class="muted">
                                ${customer.getContactEmail() == null || customer.getContactEmail().isBlank() ? "" : customer.getContactEmail()}
                            </div>
                        </div>

                        <div>${customer.getTaxNumber() == null || customer.getTaxNumber().isBlank() ? "—" : customer.getTaxNumber()}</div>
                        <div>${customer.getCity() == null || customer.getCity().isBlank() ? "—" : customer.getCity()}</div>
                        <div>${siteCounts == null ? 0 : siteCounts.getOrDefault(customer.getId(), 0L)}</div>

                        <div class="actions">
                            <a class="button button--ghost" href="/customers/${customer.getId()}">Карточка</a>
                            <a class="button" href="/customers/${customer.getId()}/sites/new">Новый объект</a>
                        </div>
                    </div>
                @endfor
            </div>
        @endif

        @if(totalPages > 1)
            <div class="pagination">
                @if(currentPage > 1)
                    <a class="button"
                       href="/?page=${currentPage - 1}&size=${pageSize}&sort=${sort}${query != null && !query.isBlank() ? "&q=" + query : ""}">
                        Назад
                    </a>
                @endif

                <span class="muted">Страница ${currentPage} из ${totalPages}</span>

                @if(currentPage < totalPages)
                    <a class="button"
                       href="/?page=${currentPage + 1}&size=${pageSize}&sort=${sort}${query != null && !query.isBlank() ? "&q=" + query : ""}">
                        Вперёд
                    </a>
                @endif
            </div>
        @endif
    </section>
</main>
</body>
</html>
