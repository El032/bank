package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Schema(description = "Постраничный ответ с данными и информацией о пагинации")
public class PageResponse<T> {

    @Schema( description = "Список данных текущей страницы",
            example = "[{\"id\":1,\"username\":\"ivan123\"}]" )

    private List<T> content;// данные текущей страницы

    @Schema( description = "Номер текущей страницы. Нумерация начинается с нуля",
            example = "0", minimum = "0" )

    private int page;// номер страницы (с нуля)

    @Schema( description = "Количество элементов на странице",
            example = "10", minimum = "1" )

    private int size;// размер страницы

    @Schema( description = "Общее количество элементов во всех страницах",
            example = "45", minimum = "0" )

    private long totalElements;     // всего записей

    @Schema( description = "Общее количество страниц",
            example = "5", minimum = "0" )

    private int totalPages;         // всего страниц

    @Schema( description = "Признак того, что текущая страница является первой",
            example = "true" )

    private boolean first;          // это первая страница?

    @Schema( description = "Признак того, что текущая страница является последней",
            example = "false" )

    private boolean last;           // это последняя страница?

    /** * Создает PageResponse из объекта Page с преобразованием элементов.
     *
     * @param page исходная страница
     * @param mapper функция преобразования элементов
     * @param <S> тип элементов исходной страницы
     * @param <T> тип элементов результата
     * @return объект PageResponse
     */

    // Статический фабричный метод — создать из Page<S> с конвертацией элементов
    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        PageResponse<T> response = new PageResponse<>();
        response.content = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());
        response.page = page.getNumber();
        response.size = page.getSize();
        response.totalElements = page.getTotalElements();
        response.totalPages = page.getTotalPages();
        response.first = page.isFirst();
        response.last = page.isLast();
        return response;
    }

    // Геттеры
    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}