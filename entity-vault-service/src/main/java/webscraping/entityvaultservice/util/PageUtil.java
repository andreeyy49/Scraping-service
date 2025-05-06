package webscraping.entityvaultservice.util;

import org.springframework.data.domain.PageRequest;
import webscraping.entityvaultservice.dto.PageDto;

import java.util.List;

public class PageUtil {

    public static  <T> PageDto<T> getPage(PageRequest request, List<T> products) {
        int totalProducts = products.size();
        int start = (int) request.getOffset();
        int end = Math.min(start + request.getPageSize(), totalProducts);
        int surplus = totalProducts % request.getPageSize();
        if (surplus > 0) {
            surplus = 1;
        }
        int totalPages = totalProducts / request.getPageSize() + surplus;

        List<T> content = products.subList(start, end);

        return new PageDto<>(content, request.getPageNumber(), request.getPageSize(), totalProducts, totalPages);
    }
}
