@import org.springframework.data.domain.Page
@import java.net.URLEncoder
@import java.nio.charset.StandardCharsets
@import java.util.LinkedHashMap
@import java.util.Map
@import java.util.stream.Collectors

@param Page<?> page
@param String basePath
@param String query = null
@param Map<String, String> params = null

@if(page != null && page.getTotalPages() > 1) {
    <% Map<String, String> combinedParams = new LinkedHashMap<>();
       if (params != null) {
           for (Map.Entry<String, String> entry : params.entrySet()) {
               if (entry.getValue() != null && !entry.getValue().isBlank()) {
                   combinedParams.put(entry.getKey(), entry.getValue());
               }
           }
       }
       if (query != null && !query.isBlank() && !combinedParams.containsKey("q")) {
           combinedParams.put("q", query);
       }
       String encodedQuery = combinedParams.entrySet().stream()
               .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
               .collect(Collectors.joining("&"));
       String sizeParam = "&size=" + page.getSize();
       String queryParam = encodedQuery.isEmpty() ? "" : "&" + encodedQuery;
       int current = page.getNumber();
    %>
    <nav class="pagination-bar">
        <a class="page-link ghost @(page.hasPrevious() ? "" : "disabled")" href="@(page.hasPrevious() ? basePath + "?page=" + (current - 1) + sizeParam + queryParam : "#")">Назад</a>
        <div class="page-dots">
            @for(int index = 0; index < page.getTotalPages(); index++) {
                boolean active = index == current;
                <a class="page-link @(active ? "active" : "")" href="@(basePath + "?page=" + index + sizeParam + queryParam)">@{index + 1}</a>
            }
        </div>
        <a class="page-link ghost @(page.hasNext() ? "" : "disabled")" href="@(page.hasNext() ? basePath + "?page=" + (current + 1) + sizeParam + queryParam : "#")">Вперёд</a>
    </nav>
}
