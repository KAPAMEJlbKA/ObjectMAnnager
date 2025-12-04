@param String flashSuccess = null
@param String flashError = null

@if(flashSuccess != null || flashError != null) {
    <div class="flash-stack">
        @if(flashSuccess != null) {
            <div class="flash-banner success">
                <div class="flash-indicator"></div>
                <div class="flash-content">
                    <div class="flash-title">Успешно</div>
                    <div class="flash-text">@flashSuccess</div>
                </div>
            </div>
        }
        @if(flashError != null) {
            <div class="flash-banner error">
                <div class="flash-indicator"></div>
                <div class="flash-content">
                    <div class="flash-title">Ошибка</div>
                    <div class="flash-text">@flashError</div>
                </div>
            </div>
        }
    </div>
}
