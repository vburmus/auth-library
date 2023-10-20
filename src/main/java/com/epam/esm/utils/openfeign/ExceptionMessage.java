package com.epam.esm.utils.openfeign;

public record ExceptionMessage(
         String title,
         int status,
         String detail){
    }