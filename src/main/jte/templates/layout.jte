@import gg.jte.Content
@import com.kapamejlbka.objectmanager.web.view.SecurityViewUtil
@import org.springframework.security.core.Authentication
@import org.springframework.security.core.context.SecurityContextHolder
@import org.springframework.security.authentication.AnonymousAuthenticationToken

@param String title
@param String activePage = ""
@param Content body
@param String flashSuccess = null
@param String flashError = null
@param Content extraHead = null
@param Content extraScripts = null

<% Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
   boolean authenticated = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
   String username = authenticated ? authentication.getName() : null;
   boolean isAdmin = SecurityViewUtil.hasRole("ADMIN");
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>@title</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="/css/corporate-theme.css">
    <link rel="stylesheet" href="/css/main.css">
    @if(extraHead != null) {
        @extraHead()
    }
</head>
<body class="d-flex flex-column min-vh-100">
<header>
    <nav class="navbar navbar-expand-lg navbar-dark fixed-top shadow-sm">
        <div class="container-xl">
            <a class="navbar-brand fw-semibold" href="/">ObjectMAnnager</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNavbar" aria-controls="mainNavbar" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="mainNavbar">
                <ul class="navbar-nav mx-auto mb-2 mb-lg-0 gap-lg-3">
                    <li class="nav-item">
                        <a class="nav-link @("customers".equals(activePage) ? "active" : "")" href="/customers">Заказчики</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link @("sites".equals(activePage) ? "active" : "")" href="/sites">Площадки</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link @("devices".equals(activePage) ? "active" : "")" href="/devices">Оборудование</a>
                    </li>
                    @if(isAdmin) {
                        <li class="nav-item">
                            <a class="nav-link @("admin".equals(activePage) ? "active" : "")" href="/admin">Администрирование</a>
                        </li>
                    }
                </ul>
                <div class="d-flex align-items-center gap-3">
                    @if(authenticated) {
                        <span class="navbar-text d-flex align-items-center gap-2 text-white">
                            <i class="bi bi-person-circle"></i>
                            <span>@username</span>
                        </span>
                        <form method="post" action="/logout">
                            <button type="submit" class="btn btn-outline-light btn-sm">Выйти</button>
                        </form>
                    } else {
                        <a class="btn btn-outline-light btn-sm" href="/login">Войти</a>
                    }
                </div>
            </div>
        </div>
    </nav>
</header>
<main class="flex-grow-1">
    <div class="container-xl py-4">
        @templates.fragments.flash(flashSuccess = flashSuccess, flashError = flashError)
        @body()
    </div>
</main>
<footer class="border-top py-3 mt-auto text-center text-muted small">
    ObjectMAnnager © 2025
</footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
<script src="/js/main.js"></script>
@if(extraScripts != null) {
    @extraScripts()
}
</body>
</html>
