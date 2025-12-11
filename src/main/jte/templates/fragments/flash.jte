@param String flashSuccess = null
@param String flashError = null

@if(flashSuccess != null || flashError != null) {
    <div class="d-flex flex-column gap-2 mb-3">
        @if(flashSuccess != null) {
            <div class="alert alert-success mb-0" role="alert">@flashSuccess</div>
        }
        @if(flashError != null) {
            <div class="alert alert-danger mb-0" role="alert">@flashError</div>
        }
    </div>
}
