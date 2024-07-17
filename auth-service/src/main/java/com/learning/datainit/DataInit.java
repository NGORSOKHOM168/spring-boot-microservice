package com.learning.datainit;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.learning.entity.Authority;
import com.learning.entity.Role;
import com.learning.entity.User;
import com.learning.entity.UserRole;
import com.learning.repository.AuthorityRepository;
import com.learning.repository.RoleRepository;
import com.learning.repository.UserRepository;
import com.learning.repository.UserRoleRepository;
import com.learning.util.JwtUtil;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInit {
	
	private final AuthorityRepository authorityRepository;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired 
	AuthenticationManager authenticationManager;
	
	@Autowired 
	JwtUtil jwtService;
	
	// Create authorities's records
	Authority userRead = Authority.builder().name("user:read").build();
	Authority userWrite = Authority.builder().name("user:write").build();
	Authority userUpdate = Authority.builder().name("user:update").build();
	Authority userDelete = Authority.builder().name("user:delete").build();
	//====================================================================
	Authority accountRead = Authority.builder().name("account:read").build();
	Authority accountWrite = Authority.builder().name("account:write").build();
	Authority accountUpdate = Authority.builder().name("account:update").build();
	Authority accountDelete = Authority.builder().name("account:delete").build();
	//===========================================================================
	Authority transactionRead = Authority.builder().name("transaction:read").build();
	Authority transactionWrite = Authority.builder().name("transaction:write").build();
	Authority transactionUpdate = Authority.builder().name("transaction:Update").build();
	Authority transactionDelete = Authority.builder().name("transaction:delete").build();
	// End authorities ==================================================================
	
	// Create roles's records
	Role roleAdmin = Role.builder()
		.name("ADMIN")
		.authorities(List.of(
				userRead, userWrite, userUpdate, userDelete
				,accountRead, accountWrite, accountUpdate, accountDelete
				,transactionRead, transactionWrite, transactionUpdate, transactionDelete 
		))
		.build();
	
	Role roleManager = Role.builder()
		.name("MANAGER")
		.authorities(List.of(
				userRead, userUpdate, userDelete
				,accountRead, accountUpdate, accountDelete
				,transactionRead, transactionUpdate, transactionDelete 
		        ))
		.build();
	Role roleCustomer = Role.builder()
		.name("CUSTOMER")
		.authorities(List.of(
				userRead, userWrite, userUpdate
				,accountRead, accountWrite, accountUpdate
				,transactionRead, transactionWrite 
		        ))
		.build();
	// End roles 
	
	@PostConstruct
	public void init() {	
		
		User user = userRepository.findUserByUsername("SOKHOMNGOR");	
		if(user == null) {				
			// Insert data into table authorities
			List<Authority> authorities = List.of(
					userRead, userWrite, userUpdate, userDelete
					,accountRead, accountWrite, accountUpdate, accountDelete
					,transactionRead, transactionWrite, transactionUpdate, transactionDelete 
		    );
			authorityRepository.saveAll(authorities);
			
			// Insert data into table roles
			List<Role> roles = List.of(
					roleAdmin, roleManager, roleCustomer
	        );
			roleRepository.saveAll(roles);
			
			user = User.builder()
					.uuid(UUID.randomUUID().toString())
					.fullName("Ngor Sokhom")
					.gender("Male")
					.phone("0886883580")
					.email("admin@gmail.com")
					.address("Phnom Penh")
					.username("SOKHOMNGOR")
					.password(passwordEncoder.encode("123456"))
					.isActived(true)
					.isDisabled(false)
					.isBlocked(false)
					.createdBy("SYSTEM")
					.build();
			
			// Save user
			userRepository.save(user);	

			UserRole userRoleAdmin = UserRole.builder()
					.user(user)
					.role(roleAdmin)
					.build();
			
			// save user roles
			userRoleRepository.save(userRoleAdmin);		
			
			
		}
	}

}