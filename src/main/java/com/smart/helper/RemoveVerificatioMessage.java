package com.smart.helper;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class RemoveVerificatioMessage {

	
	public static void removeVerificationMessage() {
        try {
            //HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpServletRequest request=((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        	HttpSession session = request.getSession();
            session.removeAttribute("message");
        } catch (RuntimeException ex) {
        	System.out.println("ERROR "+ex.getMessage());
			 ex.printStackTrace();

        }
    }
}
