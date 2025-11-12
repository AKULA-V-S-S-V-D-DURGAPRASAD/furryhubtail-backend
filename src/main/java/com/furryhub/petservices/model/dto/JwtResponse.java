
package com.furryhub.petservices.model.dto;

public class JwtResponse {
    private String token;
    private String type;
    private String role;
    private String firstName;
    private String lastName;
	public JwtResponse(String token, String type, String role, String firstName, String lastName) {
		super();
		this.token = token;
		this.type = type;
		this.role = role;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
   
    
   
}