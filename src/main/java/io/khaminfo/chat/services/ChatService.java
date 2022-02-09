package io.khaminfo.chat.services;

import java.util.List;





import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.khaminfo.chat.domain.Chat;
import io.khaminfo.chat.domain.Person;
import io.khaminfo.chat.exceptions.AccessException;
import io.khaminfo.chat.repositories.ChatRepository;

@Service
public class ChatService {
	@Autowired
	private ChatRepository chatRepository;

	public Chat sendMessage( Chat message) {
		  
		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null) {
			throw new AccessException("Access Denied!!!!");
		}
		Chat chat = chatRepository.save(message);
		ScriptDropBox.change = true;
		return chat;
	}

	
	public List<Object[]> getMessages( long id){
		
		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null) {
			throw new AccessException("Access Denied!!!!");
		}
		
		Person user = (Person) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<Object[]> result =  chatRepository.getAllMessages(user.getId(), id);
		chatRepository.updateSeen(id, user.getId());
		return result;
	}
}
