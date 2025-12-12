@param com.kapamejlbka.objectmanager.domain.customer.dto.CustomerCreateRequest customerForm
@param boolean isEdit
@param String error
@param Long customerId
@param String flashSuccess
@param String flashError
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isEdit ? "Редактирование заказчика" : "Новый заказчик"} — Object Manager</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="page">
<header class="topbar">
    <div class="topbar__brand">Object Manager</div>
    <div class="topbar__actions">
        <a class="button" href="/customers">К списку</a>
        @if(isEdit && customerId != null)
            <a class="button" href="/customers/${customerId}/view">Карточка</a>
        @endif
        <a class="button" href="/logout">Выйти</a>
    </div>
</header>

<main class="container">
    <div class="page-heading">
        <div>
            <p class="muted">${isEdit ? "Обновление данных" : "Создание записи"}</p>
            <h1>${isEdit ? "Редактирование заказчика" : "Новый заказчик"}</h1>
        </div>
    </div>

    @if(flashSuccess != null)
        <div class="alert alert--success">${flashSuccess}</div>
    @endif
    @if(error != null)
        <div class="alert alert--error">${error}</div>
    @endif
    @if(flashError != null)
        <div class="alert alert--error">${flashError}</div>
    @endif

    <section class="card">
        <div class="card__header">
            <div>
                <p class="muted">Основные данные</p>
                <h2>${isEdit ? "Изменение заказчика" : "Добавление заказчика"}</h2>
            </div>
        </div>

        <form class="form" method="post" action="${isEdit && customerId != null ? "/customers/" + customerId : "/customers"}">
            <div class="form__grid form__grid--two">
                <label class="form__label">Название / имя заказчика
                    <input class="form__control" type="text" name="name" value="${customerForm.getName()}" required>
                </label>
                <label class="form__label">ИНН / реквизиты
                    <input class="form__control" type="text" name="taxNumber" value="${customerForm.getTaxNumber()}">
                </label>
                <label class="form__label">Страна
                    <input class="form__control" type="text" name="country" value="${customerForm.getCountry()}">
                </label>
                <label class="form__label">Город
                    <input class="form__control" type="text" name="city" value="${customerForm.getCity()}">
                </label>
            </div>

            <label class="form__label">Адрес
                <input class="form__control" type="text" name="addressLine" value="${customerForm.getAddressLine()}">
            </label>

            <div class="card__header" style="padding: 0; margin-top: 8px;">
                <div>
                    <p class="muted">Контакты</p>
                    <h3>Основное контактное лицо</h3>
                </div>
            </div>

            <div class="form__grid form__grid--two">
                <label class="form__label">Контактное лицо
                    <input class="form__control" type="text" name="contactName" value="${customerForm.getContactName()}">
                </label>
                <label class="form__label">Должность/роль
                    <input class="form__control" type="text" name="contactPosition" value="${customerForm.getContactPosition()}">
                </label>
                <label class="form__label">Телефон
                    <input class="form__control" type="tel" name="contactPhone" value="${customerForm.getContactPhone()}">
                </label>
                <label class="form__label">Email
                    <input class="form__control" type="email" name="contactEmail" value="${customerForm.getContactEmail()}">
                </label>
            </div>

            <label class="form__label">Заметки
                <textarea class="form__control form__control--textarea" name="notes" rows="4" placeholder="Комментарии и договоренности">${customerForm.getNotes()}</textarea>
            </label>

            <div class="actions">
                <button class="button button--primary" type="submit">${isEdit ? "Сохранить" : "Создать"}</button>
                <a class="button" href="${isEdit && customerId != null ? "/customers/" + customerId + "/view" : "/customers"}">Отмена</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>
