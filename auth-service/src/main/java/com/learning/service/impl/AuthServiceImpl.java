package com.learning.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.learning.dto.ResponeBase;
import com.learning.dto.UserRespone;
import com.learning.dto.UserLogin;
import com.learning.dto.UserSignup;
import com.learning.entity.Role;
import com.learning.entity.User;
import com.learning.entity.UserRole;
import com.learning.repository.RoleRepository;
import com.learning.repository.UserRepository;
import com.learning.repository.UserRoleRepository;
import com.learning.service.AuthService;
import com.learning.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
	
	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired 
	AuthenticationManager authenticationManager;
	
	@Autowired 
	JwtUtil jwtUtil;

	@Override
	public ResponeBase<?> register(UserSignup userSignup) {

		log.info("Inside register, Username: {}", userSignup.fullName());
		
		// Check user already existed
		User user = userRepository.findUserByUsername(userSignup.username());
		if(user != null) {
			return ResponeBase.builder().isSuccessful(false).code(HttpStatus.BAD_REQUEST.value())
					.message("User is token please choose other").timespamp(LocalDateTime.now())
					.payload(null).build();
		}
		
		// Map request to User
		User newUser = User.builder().uuid(UUID.randomUUID().toString())
				.fullName(userSignup.fullName()).gender(userSignup.gender()).phone(userSignup.phone())
				.email(userSignup.email()).address(userSignup.address()).username(userSignup.username())
				.password(passwordEncoder.encode(userSignup.password())).isActived(true).isDisabled(false)
				.isBlocked(false).createdBy("SYSTEM").build();
		
		userRepository.save(newUser);
				
		// save user_roles
        Role roleCustomer = roleRepository.findByName("CUSTOMER");        
		UserRole userRoleAdmin = UserRole.builder().user(user).role(roleCustomer).build();
		userRoleRepository.save(userRoleAdmin);	
		
		UserRespone userResponse = UserRespone.builder()
				 .fullName(newUser.getFullName()).gender(newUser.getGender()).phone(newUser.getPhone())
				 .email(newUser.getEmail()).address(newUser.getAddress()).build();	
		
		return ResponeBase.builder().isSuccessful(true).code(HttpStatus.OK.value())
				.message("User has been created successful").timespamp(LocalDateTime.now())
				.payload(userResponse).build();
	}

	@Override
	public ResponeBase<?> login(UserLogin userLogin) {		
		log.info("Inside login, Username: {}", userLogin.username());		
		try {			
			
			Authentication authentication  = authenticationManager.authenticate(
					  	new UsernamePasswordAuthenticationToken(userLogin.username(), userLogin.password())
					);

			if(authentication.isAuthenticated()) {
				
				var token = jwtUtil.generateAccessToken(authentication);				
		        User user = userRepository.findUserByUsername(userLogin.username());
		        user.setAccessToken(token);
		        userRepository.save(user);			     
		        
				return ResponeBase.builder().isSuccessful(true).code(HttpStatus.OK.value())
						.message("Request token successful").timespamp(LocalDateTime.now())
						.payload("Token: "+ token).build();
				
			}else {
				return ResponeBase.builder().isSuccessful(true).code(HttpStatus.BAD_REQUEST.value())
						.message("Invalid token").timespamp(LocalDateTime.now())
						.payload(null).build();
			}
			
		} catch (Exception e) {
			log.error("Inside signup {}", e.getMessage());
			return ResponeBase.builder().isSuccessful(true).code(HttpStatus.BAD_REQUEST.value())
					.message("Request token has bean failed").timespamp(LocalDateTime.now())
					.payload("Error Message: "+e.getMessage()).build();
		}
	}

}
