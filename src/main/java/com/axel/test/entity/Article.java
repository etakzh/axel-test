package com.axel.test.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Article implements Serializable{

    private String id;

    private String header;

    private String description;

    private String text;

    private LocalDate publishDate;

    private List<String> authorList;

    private List<String> keywordList;
}
