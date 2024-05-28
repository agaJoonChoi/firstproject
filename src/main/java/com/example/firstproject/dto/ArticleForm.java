package com.example.firstproject.dto;

import com.example.firstproject.entity.Article;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class ArticleForm {
    private String title;
    private String content;


    /* @AllargsConstructor
    public ArticleForm(String title, String content) {
        this.title = title;
        this.content = content;
    }
    */

    /* @ToString
    @Override
    public String toString() {
        return "ArticleForm{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

     */

    public Article toEntity() {
        return new Article(null, title, content);
    }
}
