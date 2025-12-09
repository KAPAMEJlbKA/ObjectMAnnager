@import gg.jte.Content
@import com.kapamejlbka.objectmanager.web.view.SecurityViewUtil
@param String title
@param String activePage = ""
@param Content body
@param String flashSuccess = null
@param String flashError = null

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>@title</title>
    <link rel="stylesheet" href="/css/main.css">
</head>
<body class="app-body">
<div class="app-shell">
    <header class="app-topbar">
        <div class="brand">
            <div class="brand-mark">OM</div>
            <div class="brand-meta">
                <div class="brand-title">Object Manager</div>
                <div class="brand-subtitle">Учёт площадок и оборудования</div>
            </div>
        </div>
        <% boolean isAdmin = SecurityViewUtil.hasRole("ADMIN"); %>
        <nav class="topnav">
            <a href="/" class="nav-link @("".equals(activePage) ? "active" : "")">Главная</a>
            <a href="/sites" class="nav-link @("sites".equals(activePage) ? "active" : "")">Площадки</a>
            <a href="/devices" class="nav-link @("devices".equals(activePage) ? "active" : "")">Оборудование</a>
            @if(isAdmin) {
                <a href="/admin" class="nav-link @("admin".equals(activePage) ? "active" : "")">Администрирование</a>
            }
        </nav>
    </header>

    <main class="app-main">
        @templates.fragments.flash(flashSuccess = flashSuccess, flashError = flashError)
        @body()
    </main>
</div>
</body>
</html>
