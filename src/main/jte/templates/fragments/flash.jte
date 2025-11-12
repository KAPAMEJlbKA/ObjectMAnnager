@param String flashSuccess = null
@param String flashError = null

@if(flashSuccess != null) {
    <div class="flash success">@flashSuccess</div>
}
@if(flashError != null) {
    <div class="flash error">@flashError</div>
}
