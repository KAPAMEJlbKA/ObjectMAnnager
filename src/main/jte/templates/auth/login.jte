@import org.springframework.security.web.csrf.CsrfToken

@param boolean error = false
@param boolean logout = false
@param CsrfToken _csrf = null

@templates.layout(title = "Вход в систему", activePage = "login") {
    <div class="om-login-wrapper">
        <div class="om-login-card">
            <h1 class="om-login-title">Войти в ObjectMAnnager</h1>
            @if(error) {
                <div class="alert alert-danger" role="alert">
                    Неверное имя пользователя или пароль
                </div>
            }
            @if(logout) {
                <div class="alert alert-success" role="alert">
                    Вы вышли из системы
                </div>
            }
            <form method="post" action="/login" class="needs-validation" novalidate>
                @if(_csrf != null) {
                    <input type="hidden" name="@_csrf.getParameterName()" value="@_csrf.getToken()">
                }
                <div class="mb-3">
                    <label for="username" class="form-label">Имя пользователя</label>
                    <input type="text" class="form-control" id="username" name="username" required autofocus>
                </div>
                <div class="mb-3">
                    <label for="password" class="form-label">Пароль</label>
                    <input type="password" class="form-control" id="password" name="password" required>
                </div>
                <button type="submit" class="btn btn-primary w-100 mt-3">Войти</button>
            </form>
        </div>
    </div>
}
