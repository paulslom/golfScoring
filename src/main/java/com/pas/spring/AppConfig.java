package com.pas.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.pas.spring"},
	excludeFilters={@Filter(org.springframework.stereotype.Controller.class)})
public class AppConfig {}