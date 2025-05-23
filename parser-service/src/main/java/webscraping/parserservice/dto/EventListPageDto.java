package webscraping.parserservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventListPageDto {

    private List<EventPageDto> eventPageDtoList;
}
