package webscraping.lemmasservice.util;

import webscraping.lemmasservice.dto.LemmaDto;
import webscraping.lemmasservice.model.Lemma;

public class LemmaMapper {

    public static Lemma dtoToLemma(LemmaDto dto) {
        Lemma lemma = new Lemma();

        lemma.setId(dto.getId());
        lemma.setSiteId(dto.getSiteId());
        lemma.setLemma(dto.getLemma());
        lemma.setFrequency(dto.getFrequency());
        lemma.setIndexes(dto.getIndexes().stream().map(IndexMapper::dtoToIndex).toList());

        return lemma;
    }

    public static LemmaDto lemmaToDto(Lemma lemma) {
        LemmaDto lemmaDto = new LemmaDto();

        lemmaDto.setId(lemma.getId());
        lemmaDto.setSiteId(lemma.getSiteId());
        lemmaDto.setLemma(lemma.getLemma());
        lemmaDto.setFrequency(lemma.getFrequency());
        lemmaDto.setIndexes(lemma.getIndexes().stream().map(IndexMapper::indexToDto).toList());

        return lemmaDto;
    }
}
