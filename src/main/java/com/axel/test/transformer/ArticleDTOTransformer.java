package com.axel.test.transformer;

import com.axel.test.dto.ArticleDto;
import com.axel.test.entity.Article;
import org.springframework.stereotype.Component;

/**
 * Transforms Article from DTO and back.
 */
@Component
public class ArticleDTOTransformer {

    public Article fromDTO(final ArticleDto dto) {
        final Article article = new Article();
        article.setId(dto.getId());
        article.setAuthorList(dto.getAuthorList());
        article.setDescription(dto.getDescription());
        article.setHeader(dto.getHeader());
        article.setKeywordList(dto.getKeywordList());
        article.setPublishDate(dto.getPublishDate());
        article.setText(dto.getText());
        return article;
    }

    public ArticleDto toDTO(final Article article) {
        final ArticleDto dto = new ArticleDto();
        dto.setId(article.getId());
        dto.setAuthorList(article.getAuthorList());
        dto.setDescription(article.getDescription());
        dto.setHeader(article.getHeader());
        dto.setKeywordList(article.getKeywordList());
        dto.setPublishDate(article.getPublishDate());
        dto.setText(article.getText());
        return dto;
    }
}
