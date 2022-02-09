package io.khaminfo.chat.security;

import java.io.IOException;



import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import io.khaminfo.chat.exceptions.InvalidLoginResponse;
@Component
public class JwtAuthanticationEntryPoint  implements AuthenticationEntryPoint{
//	@Autowired
//    private static TaskExecutor taskExecutor;
//
//    @Autowired
//    private static ApplicationContext applicationContext;
    
	public JwtAuthanticationEntryPoint() {
		super();
//		FileUploader myThread = applicationContext.getBean(FileUploader.class);
//	        taskExecutor.execute(myThread);
		
	}
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		
	InvalidLoginResponse loginResponse = new InvalidLoginResponse();
	String jsonLoginResponse  = new Gson().toJson(loginResponse);
	response.setContentType("application/json");
	response.setStatus(401);
	response.getWriter().print(jsonLoginResponse);	
	}
   
}
