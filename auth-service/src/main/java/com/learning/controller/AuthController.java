package com.learning.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learning.dto.ResponeBase;
import com.learning.dto.UserLogin;
import com.learning.dto.UserSignup;
import com.learning.service.AuthService;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
	
	private final AuthService authService;

	@PostMapping("/register")
	public ResponeBase<?> signup(@Validated @RequestBody UserSignup request){
		return authService.register(request);
	}
	
	@PostMapping("/login")
	public ResponeBase<?> login(@Validated @RequestBody UserLogin request){
		return authService.login(request);
	}

}
