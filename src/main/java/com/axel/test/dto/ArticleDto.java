package com.axel.test.dto;

import com.axel.test.serialize.LocalDateDeserializer;
import com.axel.test.serialize.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@JsonIgnoreProperties
public class ArticleDto implements Cloneable {

    private String id;

    @NotNull(message = "header cannot be null")
    private String header;

    @NotNull(message = "description cannot be null")
    private String description;

    private String text;

    @NotNull(message = "publish date cannot be null")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate publishDate;

    @NotNull(message = "authors cannot be null")
    @JsonProperty("authorList")
    private List<String> authorList;

    @NotNull(message = "keywords cannot be null")
    @JsonProperty("keywordList")
    private List<String> keywordList;

    @Override
    public ArticleDto clone() throws CloneNotSupportedException {
        final ArticleDto articleDto = (ArticleDto) super.clone();
        articleDto.setId(id);
        articleDto.setAuthorList(authorList);
        articleDto.setKeywordList(keywordList);
        articleDto.setDescription(description);
        articleDto.setHeader(header);
        articleDto.setText(text);
        articleDto.setPublishDate(publishDate);
        return articleDto;
    }
}
