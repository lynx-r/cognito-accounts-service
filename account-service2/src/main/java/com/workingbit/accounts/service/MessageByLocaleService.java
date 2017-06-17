package com.workingbit.accounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageByLocaleService {

    private final MessageSource messageSource;

    @Autowired
    public MessageByLocaleService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String id) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(id,null,locale);
    }
}