@param com.kapamejlbka.objectmanager.domain.customer.Customer customer
@param com.kapamejlbka.objectmanager.domain.customer.dto.SiteCreateRequest siteForm
@param boolean isEdit
@param Long siteId
@param String error
@param String flashSuccess
@param String flashError
@{
    var formAction = (isEdit && siteId != null)
        ? "/sites/" + siteId
        : "/customers/" + customer.getId() + "/sites";

    // safe values
    var name = (siteForm != null && siteForm.getName() != null) ? siteForm.getName() : "";
    var fullAddress = (siteForm != null && siteForm.getFullAddress() != null) ? siteForm.getFullAddress() : "";
    var description = (siteForm != null && siteForm.getDescription() != null) ? siteForm.getDescription() : "";

    var latitude = (siteForm != null && siteForm.getLatitude() != null) ? String.valueOf(siteForm.getLatitude()) : "";
    var longitude = (siteForm != null && siteForm.getLongitude() != null) ? String.valueOf(siteForm.getLongitude()) : "";

    var useCustomerContact = (siteForm != null) && siteForm.isUseCustomerContact();

    var contactName = (siteForm != null && siteForm.getContactName() != null) ? siteForm.getContactName() : "";
    var contactPosition = (siteForm != null && siteForm.getContactPosition() != null) ? siteForm.getContactPosition() : "";
    var contactPhone = (siteForm != null && siteForm.getContactPhone() != null) ? siteForm.getContactPhone() : "";
    var contactEmail = (siteForm != null && siteForm.getContactEmail() != null) ? siteForm.getContactEmail() : "";

    var cancelHref = (isEdit && siteId != null)
        ? "/sites/" + siteId
        : "/customers/" + customer.getId() + "/view";
}
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isEdit ? "Редактирование объекта" : "Новый объект"} — Object Manager</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="page">
<header class="topbar">
    <div class="topbar__brand">Object Manager</div>
    <div class="topbar__actions">
        <a class="button" href="/customers/${customer.getId()}/view">К заказчику</a>
        @if(isEdit && siteId != null)
            <a class="button" href="/sites/${siteId}">К объекту</a>
        @endif
        <a class="button" href="/logout">Выйти</a>
    </div>
</header>

<main class="container">
    <div class="page-heading">
        <div>
            <p class="muted">${customer.getName()}</p>
            <h1>${isEdit ? "Редактирование объекта" : "Создание объекта"}</h1>
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
                <p class="muted">Данные объекта</p>
                <h2>${isEdit ? "Обновление информации" : "Основная информация"}</h2>
            </div>
        </div>

        <form class="form" method="post" action="${formAction}">
            <div class="form__grid form__grid--two">
                <label class="form__label">Название объекта
                    <input class="form__control" type="text" name="name" value="${name}" required>
                </label>
                <label class="form__label">Полный адрес
                    <textarea class="form__control form__control--textarea" name="fullAddress" rows="2" placeholder="Город, улица, дом">${fullAddress}</textarea>
                </label>
            </div>

            <label class="form__label">Описание объекта
                <textarea class="form__control form__control--textarea" name="description" rows="4" placeholder="Характеристики площадки, доступы, этажность">${description}</textarea>
            </label>

            <div class="form__grid form__grid--two">
                <label class="form__label">Широта
                    <input class="form__control" type="number" step="any" name="latitude" value="${latitude}">
                </label>
                <label class="form__label">Долгота
                    <input class="form__control" type="number" step="any" name="longitude" value="${longitude}">
                </label>
            </div>

            <div class="card__header" style="padding: 0; margin-top: 8px;">
                <div>
                    <p class="muted">Контакты</p>
                    <h3>Представители на объекте</h3>
                </div>
            </div>

            <label class="form__label form__label--inline form__label--wrap">
                <input type="checkbox" name="useCustomerContact" value="true" ${useCustomerContact ? "checked" : ""}>
                <span>Использовать контакт заказчика как основной</span>
            </label>

            <div class="form__grid form__grid--two">
                <label class="form__label">Контактное лицо
                    <input class="form__control" type="text" name="contactName" value="${contactName}">
                </label>
                <label class="form__label">Должность/роль
                    <input class="form__control" type="text" name="contactPosition" value="${contactPosition}">
                </label>
                <label class="form__label">Телефон
                    <input class="form__control" type="tel" name="contactPhone" value="${contactPhone}">
                </label>
                <label class="form__label">Email
                    <input class="form__control" type="email" name="contactEmail" value="${contactEmail}">
                </label>
            </div>

            <div class="actions">
                <button class="button button--primary" type="submit">${isEdit ? "Сохранить" : "Создать"}</button>
                <a class="button" href="${cancelHref}">Отмена</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>
