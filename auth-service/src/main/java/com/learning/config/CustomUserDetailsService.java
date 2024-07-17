package com.learning.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.learning.entity.User;
import com.learning.repository.UserRepository;

import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	
	@Autowired
    private UserRepository userRepository;
	

	@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException 
		{
		
        User user = userRepository.findUserByUsername(username);
        
        if(user==null) {
        	throw new UsernameNotFoundException("User not found: " + username);
        }
        List<GrantedAuthority> authorities = List.of(user.getUserRole().get(0).getRole()).stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()+":"))
                .collect(Collectors.toList());  
        
        authorities.addAll(user.getUserRole().get(0).getRole().getAuthorities().stream()
                .map(auth -> new SimpleGrantedAuthority(auth.getName()))
                .collect(Collectors.toList()));

        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                authorities                
        );
        
    }
}