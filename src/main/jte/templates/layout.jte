@import gg.jte.Content
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
<body>
<header class="topbar">
    <div>
        <strong>Object Manager</strong>
    </div>
    <nav>
        <a href="/" class="@("".equals(activePage) ? "active" : "")">Главная</a>
        <a href="/sites" class="@(activePage.equals("sites") ? "active" : "")">Площадки</a>
    </nav>
</header>
<main class="content">
    @templates.fragments.flash(flashSuccess = flashSuccess, flashError = flashError)
    @body()
</main>
</body>
</html>
