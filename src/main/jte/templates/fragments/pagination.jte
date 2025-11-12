@import org.springframework.data.domain.Page
@import java.net.URLEncoder
@import java.nio.charset.StandardCharsets

@param Page<?> page
@param String basePath
@param String query = null

@if(page != null && page.getTotalPages() > 1) {
    <% String encodedQuery = null;
       if (query != null && !query.isBlank()) {
           encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
       }
       String sizeParam = "&size=" + page.getSize();
    %>
    <nav class="pagination">
        <% int current = page.getNumber();
           String queryParam = encodedQuery == null ? "" : "&q=" + encodedQuery;
        %>
        <a class="page-link @(page.hasPrevious() ? "" : "disabled")" href="@(page.hasPrevious() ? basePath + "?page=" + (current - 1) + sizeParam + queryParam : "#")">Назад</a>
        @for(int index = 0; index < page.getTotalPages(); index++) {
            boolean active = index == current;
            <a class="page-link @(active ? "active" : "")" href="@(basePath + "?page=" + index + sizeParam + queryParam)">@{index + 1}</a>
        }
        <a class="page-link @(page.hasNext() ? "" : "disabled")" href="@(page.hasNext() ? basePath + "?page=" + (current + 1) + sizeParam + queryParam : "#")">Вперёд</a>
    </nav>
}
