package com.pas.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.pas.beans", "com.pas.spring", "com.pas.dao", "com.pas.util", "com.pas.dynamodb"},
	excludeFilters={@Filter(org.springframework.stereotype.Controller.class)})
public class AppConfig {}