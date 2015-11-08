package com.axel.test.controller;

import com.axel.test.dto.ArticleDtoList;
import com.axel.test.entity.Article;
import com.axel.test.exception.ArticleControllerException;
import com.axel.test.service.ArticleManager;
import com.axel.test.service.CachingServiceImpl;
import com.axel.test.service.CachingService;
import com.axel.test.transformer.ArticleDTOTransformer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.util.*;

import com.axel.test.dto.ArticleDto;

@RestController
public class ArticleController {

    @Autowired
    private ArticleManager articleManager;

    @Autowired
    private ArticleDTOTransformer articleDTOTransformer;

    @RequestMapping("/")
    public String index() {
        return "Alex Test";
    }

    @RequestMapping(value = "/article", method = RequestMethod.POST)
    @ResponseBody
    public ArticleDto add(@RequestBody ArticleDto dto) {
        final Article article = articleDTOTransformer.fromDTO(dto);
        articleManager.addArticle(article);
        return articleDTOTransformer.toDTO(article);
    }

    @RequestMapping(value = "/article", method = RequestMethod.PUT)
    public void update(@RequestBody ArticleDto dto) {
        if (dto.getId() == null) {
            throw new ArticleControllerException("Article id is null");
        }
        final Article article = articleDTOTransformer.fromDTO(dto);
        articleManager.updateArticle(article);
    }

    @RequestMapping(value = "/article/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable(value = "id") String id) {
        articleManager.deleteArticle(id);
    }

    @RequestMapping(value = "/article/{id}")
    @ResponseBody
    public ArticleDto showById(@PathVariable(value = "id") String id) {
        final ArticleDto dto = getArticleDtoById(id);
        if (dto != null) return dto;
        throw new ArticleControllerException("Article is not found by id " + id);
    }

    @RequestMapping(value = "/articles/author/{name}")
    @ResponseBody
    public ArticleDtoList showByAuthorName(@PathVariable(value = "name") String name) throws UnsupportedEncodingException {
        if (name.trim().isEmpty()) {
            throw new ArticleControllerException("name is empty");
        }
        final Set<String> articles = articleManager.getIdsByAuthor(URLDecoder.decode(name.trim(), "UTF-8"));
        return new ArticleDtoList(getArticleDtos(articles));
    }

    @RequestMapping(value = "/articles/keyword/{keyword}")
    @ResponseBody
    public ArticleDtoList showByKeyword(@PathVariable(value = "keyword") String keyword) throws UnsupportedEncodingException {
        if (keyword.trim().isEmpty()) {
            throw new ArticleControllerException("keyword is empty");
        }
        final Set<String> articles = articleManager.getIdsByKeyword(URLDecoder.decode(keyword.trim(), "UTF-8"));
        return new ArticleDtoList(getArticleDtos(articles));
    }

    @RequestMapping(value = "/articles/period/{periodFrom}/{periodTo}")
    @ResponseBody
    public ArticleDtoList showByPeriod(@PathVariable(value = "periodFrom") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate periodFrom,
                                       @PathVariable(value = "periodTo") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate periodTo) {
        final Set<String> articles = articleManager.getIdsByPeriod(periodFrom, periodTo);
        return new ArticleDtoList(getArticleDtos(articles));
    }

    private ArticleDto getArticleDtoById(String id) {
        final Article article = articleManager.getArticleById(id);
        if (article != null) {
            return articleDTOTransformer.toDTO(article);
        }
        return null;
    }

    private List<ArticleDto> getArticleDtos(Set<String> articles) {
        final List<ArticleDto> articleDtos = new ArrayList<>();
        if (!articles.isEmpty()) {
            for (String id : articles) {
                final ArticleDto dto = getArticleDtoById(id);
                if (dto != null) {
                    articleDtos.add(dto);
                }
            }
        }
        return articleDtos;
    }
}
