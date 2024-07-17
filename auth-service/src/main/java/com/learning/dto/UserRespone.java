package com.learning.dto;

import lombok.Builder;

@Builder
public record UserRespone(	
		String fullName,
		String gender,
		String phone,	
		String email,		
		String address,
		String username	
		) {

}
