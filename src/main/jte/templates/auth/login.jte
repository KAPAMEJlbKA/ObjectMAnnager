@param boolean error
@param boolean logout
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Object Manager — Вход</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="page page--auth">
<div class="auth-card">
    <div class="auth-card__brand">
        <div class="auth-card__logo">OM</div>
        <div>
            <div class="auth-card__title">Object Manager</div>
            <div class="auth-card__subtitle">Рабочая панель администратора</div>
        </div>
    </div>

    <div class="auth-card__panel">
        <h1>Вход</h1>
        <p class="muted">Используйте учётные данные администратора</p>

        @if(error)
            <div class="alert alert--error">Неверный логин или пароль</div>
        @endif
        @if(logout)
            <div class="alert alert--success">Вы успешно вышли из системы</div>
        @endif

        <form class="form" method="post" action="/login">
            <label class="form__label">Логин
                <input class="form__control" type="text" name="username" autocomplete="username" required>
            </label>
            <label class="form__label">Пароль
                <input class="form__control" type="password" name="password" autocomplete="current-password" required>
            </label>
            <button class="button button--primary" type="submit">Войти</button>
        </form>
    </div>
</div>
</body>
</html>
