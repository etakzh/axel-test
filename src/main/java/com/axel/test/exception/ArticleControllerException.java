package com.axel.test.exception;

public class ArticleControllerException extends RuntimeException {

    public ArticleControllerException()
    {
    }

    public ArticleControllerException(String message)
    {
        super(message);
    }
}
