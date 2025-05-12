package webscraping.lemmasservice.util;

import webscraping.lemmasservice.dto.IndexDto;
import webscraping.lemmasservice.model.Index;

public class IndexMapper {

    public static Index dtoToIndex(IndexDto dto) {
        Index index = new Index();

        index.setId(dto.getId());
        index.setPageId(dto.getPageId());
        index.setRank(dto.getRank());

        return index;
    }

    public static IndexDto indexToDto(Index index){
        IndexDto indexDto = new IndexDto();

        indexDto.setId(index.getId());
        indexDto.setPageId(index.getPageId());
        indexDto.setRank(index.getRank());

        return indexDto;
    }
}
