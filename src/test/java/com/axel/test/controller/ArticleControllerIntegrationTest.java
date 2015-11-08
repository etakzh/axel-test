package com.axel.test.controller;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.axel.test.dto.ArticleDto;
import com.axel.test.dto.ArticleDtoList;
import com.axel.test.exception.ArticleControllerException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import com.axel.test.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
public class ArticleControllerIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private URI base;
    private URI articleBase;
    private URI articlesBase;
    private URI getByKeyword;
    private URI getByPeriod;
    private URI getByAuthor;
    private RestTemplate template;

    @Before
    public void setUp() {
        this.base = URI.create("http://localhost:" + port + "/");
        this.articleBase = URI.create(base.toString() + "article");
        this.articlesBase = URI.create(articleBase.toString() + "s");
        this.getByAuthor = URI.create(articlesBase.toString() + "/author/");
        this.getByKeyword = URI.create(articlesBase.toString() + "/keyword/");
        this.getByPeriod = URI.create(articlesBase.toString() + "/period/");
        template = new TestRestTemplate();
    }

    @Test
    public void getDefault() {
        // when
        ResponseEntity<String> response = template.getForEntity(base, String.class);

        //then
        assertThat(response.getBody(), equalTo("Alex Test"));
    }

    @Test
    public void testAddArticle() {
        // given
        ArticleDto dto = getDTO();

        // when
        ResponseEntity<ArticleDto> response = template.postForEntity(articleBase, dto, ArticleDto.class);

        // then
        ArticleDto actual = response.getBody();
        assertEquals(dto, actual);
    }

    @Test
    public void testGetArticle() {
        // given
        ArticleDto dto = getDTO();

        // when
        ResponseEntity<ArticleDto> response = template.postForEntity(articleBase, dto, ArticleDto.class);
        ResponseEntity<ArticleDto> getResponse = template.getForEntity(URI.create(articleBase.toString() + "/" + response.getBody().getId()), ArticleDto.class);

        // then
        ArticleDto actual = getResponse.getBody();
        assertEquals(dto, actual);
    }

    @Test
    public void testGetArticlesByKeyword() {
        // given
        ArticleDto dtoRomantic = getDTO();
        ArticleDto dtoCars = getDTO();
        dtoCars.setKeywordList(Collections.singletonList("cars"));

        // when
        template.postForEntity(articleBase, dtoRomantic, ArticleDto.class);
        ResponseEntity<ArticleDto> responseCars = template.postForEntity(articleBase, dtoCars, ArticleDto.class);
        ResponseEntity<ArticleDtoList> getResponse = template.getForEntity(URI.create(getByKeyword.toString() + "cars"), ArticleDtoList.class);

        // then
        final ArticleDtoList actual = getResponse.getBody();
        assertTrue(actual.getArticles().size() > 0);
        assertThat(actual.getArticles().get(0), is(responseCars.getBody()));
    }

    @Test
    public void testGetArticlesByAuthor() {
        // given
        ArticleDto dtoKoelo = getDTO();
        ArticleDto dtoStroustrup = getDTO();
        dtoStroustrup.setAuthorList(Collections.singletonList("Bjarne Stroustrup"));

        // when
        template.postForEntity(articleBase, dtoKoelo, ArticleDto.class);
        ResponseEntity<ArticleDto> responseStroustrup = template.postForEntity(articleBase, dtoStroustrup, ArticleDto.class);
        ResponseEntity<ArticleDtoList> getResponse = template.getForEntity(URI.create(getByAuthor.toString() + "Bjarne+Stroustrup"), ArticleDtoList.class);

        // then
        final ArticleDtoList actual = getResponse.getBody();
        assertTrue(actual.getArticles().size() > 0);
        assertThat(actual.getArticles().get(0), is(responseStroustrup.getBody()));
    }

    @Test
    public void testGetArticlesByPeriod() {
        // given
        ArticleDto dtoNow = getDTO();
        ArticleDto dtoYesterday = getDTO();
        LocalDate yesterday = dtoNow.getPublishDate().minusDays(1);
        dtoYesterday.setPublishDate(yesterday);

        // when
        template.postForEntity(articleBase, dtoNow, ArticleDto.class);
        ResponseEntity<ArticleDto> responseYesterday = template.postForEntity(articleBase, dtoYesterday, ArticleDto.class);
        ResponseEntity<ArticleDtoList> getResponse = template.getForEntity(URI.create(getByPeriod.toString() + yesterday.minusDays(1) + "/" + yesterday), ArticleDtoList.class);

        // then
        final ArticleDtoList actual = getResponse.getBody();
        assertTrue(actual.getArticles().size() > 0);
        assertThat(actual.getArticles().get(0), is(responseYesterday.getBody()));
    }

    @Test
    public void testDeleteArticle() {
        // given
        ArticleDto dto = getDTO();

        // when
        ResponseEntity<ArticleDto> response = template.postForEntity(articleBase, dto, ArticleDto.class);
        template.delete(URI.create(articleBase.toString() + "/" + response.getBody().getId()));

        // then should throw exception when trying to get by id
        ResponseEntity<ArticleControllerException> getResponse = template.getForEntity(URI.create(articleBase.toString() + "/" + response.getBody().getId()), ArticleControllerException.class);
        assertNotNull(getResponse.getBody());
    }

    @Test
    public void testUpdateArticle() throws Exception {
        // given
        ArticleDto dto = getDTO();

        // when
        ResponseEntity<ArticleDto> addResponse = template.postForEntity(articleBase, dto, ArticleDto.class);
        final ArticleDto changedArticleDto = addResponse.getBody().clone();
        changedArticleDto.setText("changed");
        template.put(articleBase, changedArticleDto);
        ResponseEntity<ArticleDto> getResponse = template.getForEntity(URI.create(articleBase.toString() + "/" + changedArticleDto.getId()), ArticleDto.class);

        // then
        ArticleDto actual = getResponse.getBody();
        assertThat(changedArticleDto.getId(), is(actual.getId()));
        assertEquals(changedArticleDto, actual);
    }

    @Test
    public void testExceptionOnUpdateArticle() throws Exception {
        // given
        ArticleDto dto = getDTO();

        // when
        try {
            template.put(articleBase, dto);
        } catch (ArticleControllerException ex) {
            assertNotNull(ex);
        }
    }

    private void assertEquals(ArticleDto dto, ArticleDto actual) {
        assertThat(actual.getAuthorList(), is(dto.getAuthorList()));
        assertThat(actual.getDescription(), is(dto.getDescription()));
        assertThat(actual.getHeader(), is(dto.getHeader()));
        assertThat(actual.getText(), is(dto.getText()));
        assertThat(actual.getPublishDate(), is(dto.getPublishDate()));
        assertThat(actual.getKeywordList(), is(dto.getKeywordList()));
    }

    private ArticleDto getDTO() {
        return getDTO(Arrays.asList("Paolo Koelo", "Leo Tolstoy"), Arrays.asList("romantic", "psychology"), LocalDate.now());
    }

    private ArticleDto getDTO(final List<String> authors, List<String> keywords, final LocalDate publishDate) {
        final ArticleDto dto = new ArticleDto();
        dto.setId("test");
        dto.setAuthorList(authors);
        dto.setDescription("desc");
        dto.setHeader("header");
        dto.setKeywordList(keywords);
        dto.setPublishDate(publishDate);
        dto.setText("text");
        return dto;
    }
}
