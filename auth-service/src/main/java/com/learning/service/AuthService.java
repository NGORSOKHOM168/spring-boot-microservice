package com.learning.service;

import com.learning.dto.UserSignup;
import com.learning.dto.ResponeBase;
import com.learning.dto.UserLogin;

public interface AuthService {

	public ResponeBase<?> register(UserSignup userSignup);
	public ResponeBase<?> login(UserLogin userLogin);
	
}
