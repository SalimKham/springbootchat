package io.khaminfo.askmore.services;




import java.io.ByteArrayInputStream;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkSettings;

import io.khaminfo.askmore.domain.Groupe;
import io.khaminfo.askmore.domain.Person;
import io.khaminfo.askmore.domain.Question;
import io.khaminfo.askmore.domain.Questionnary;
import io.khaminfo.askmore.domain.Response;
import io.khaminfo.askmore.domain.Teacher;
import io.khaminfo.askmore.domain.Tutorial;
import io.khaminfo.askmore.exceptions.AccessException;
import io.khaminfo.askmore.payload.ScriptToDropBox;
import io.khaminfo.askmore.repositories.QuestionRepository;
import io.khaminfo.askmore.repositories.QuestionnaryRepository;
import io.khaminfo.askmore.repositories.ResponseRepository;
import io.khaminfo.askmore.repositories.SubjectRepository;
import io.khaminfo.askmore.repositories.TutorialRepository;

@Service
public class TutorialService {
	@Autowired
	private TutorialRepository tutorialRepository;
	@Autowired
	private SubjectRepository subjectRepository;
	@Autowired
	private QuestionnaryRepository questionnaryRepository;
	@Autowired
	private QuestionRepository questionRepository;
	@Autowired
	private ResponseRepository responseRepository;
	@Autowired
	private GroupeService groupeService;
	private static final String ACCESS_TOKEN = "z62njhqXOzEAAAAAAAAAAZML1_3JqgmU_SOJ8J0KP-xZUDt0ON0CSBMHWWqMNhpC";
	DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/ask-more").build();
    DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
	
	public Tutorial addTutorial(long subject , String allowedGroupes , Tutorial tutorial ,MultipartFile file  ) {
		Person user = (Person) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (user.getType() != 3) {
			throw new AccessException("Access Denied!!!!");
		}
		try {
             if(file != null) {
            	
 	            byte[] bytes = file.getBytes();
 	            
 	            String name = ImageUtils.getRandomName();
 	           client.files().uploadBuilder("/pdfs/"+name+".pdf")
               .uploadAndFinish(new ByteArrayInputStream(bytes));
       String url = client.sharing().createSharedLinkWithSettings("/pdfs/"+name+".pdf", SharedLinkSettings.newBuilder().withRequestedVisibility(RequestedVisibility.PUBLIC).build()).getUrl();	
 	            tutorial.setContent(url.substring(0,url.lastIndexOf('.'))+".pdf?raw=1");	 
             }
             
             if(allowedGroupes.length() != 0) {
            	 Iterable<Groupe> result = new ArrayList<>();
            	 List<Groupe> list = new ArrayList<>();
            	 
            	 result = groupeService.getTeacherGroupes();
            	  for (Groupe groupe: result) {
            		 
      	            if (allowedGroupes.indexOf(""+groupe.getId()) != -1) {
      	            	
      	                list.add(groupe);
      	            }
      	        }
            	  tutorial.setAllowedGroupes(list);
             }
             tutorial.setTeacher((Teacher) user);
	         tutorial.setSubject(subjectRepository.getById(subject));
	         
	         tutorialRepository.save(tutorial);
		     ScriptToDropBox.change = true;
		} catch (Exception e) {
			
			throw new AccessException("Please Choose Another Name");
		}

		return null;
	}


	public Tutorial getTutorialById(long id) {
		tutorialRepository.updateNbrVisits(id);
		return tutorialRepository.getById(id);
	}
	
	public Questionnary addQuestionnary(Long id ,String [] questionsArry, String [] ResponsesArray) {
		
		Tutorial tutorial = tutorialRepository.getById(id);
		Questionnary newQuestionnary = new Questionnary();
		newQuestionnary.setTutorial(tutorial);
		newQuestionnary = questionnaryRepository.save(newQuestionnary);
		for (int i = 0; i < questionsArry.length; i++) {
			String [] question = questionsArry[i].split(":");
			Question newQuestion = new Question();
			newQuestion.setMark(Integer.parseInt(question[0]));
			newQuestion.setQuestion(question[1]);
			newQuestion.setQuestinnary(newQuestionnary);
			newQuestion = questionRepository.save(newQuestion);
			for (int j = 0; j < ResponsesArray.length; j++) {
			String [] answer = ResponsesArray[j].split(":");
			int order = 1;
			if(Integer.parseInt(answer[0]) == (i+1)) {
				Response newAnswer = new Response();
				newAnswer.setContent(answer[1]);
				newAnswer.setQuestion(newQuestion);
				newAnswer.setResponseOrder(order);
				newAnswer.setValide(Boolean.parseBoolean(answer[2]));
				responseRepository.save(newAnswer);
				order ++;
			}
			
			}
		}
		ScriptToDropBox.change = true;
		return newQuestionnary;
	}
	
	public void deleteQuestionnary(long id_questionnary) {
		Person user = (Person) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (user.getType() != 3) {
			throw new AccessException("Access Denied!!!!");
		}
		Questionnary questionnary = questionnaryRepository.getById(id_questionnary);
		if(questionnary != null) {
			if(questionnary.getTutorial().getTeacher().getId() == user.getId()) {
				questionnaryRepository.deleteById(id_questionnary);
				ScriptToDropBox.change = true;
			}else {
				throw new AccessException("Access Denied!!!!");
			}
		}
	}


	public Iterable<Tutorial> getAll() {
		// TODO Auto-generated method stub
		return tutorialRepository.findAll();
	}


	public void delete(long id) {
		Person user = (Person) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Tutorial tutorial = tutorialRepository.getById(id);
		if(tutorial.getTeacher().getId() != user.getId())
			throw new AccessException("Access Denied!!!!");
		tutorialRepository.deleteById(id);
		ScriptToDropBox.change = true;
		
	}

}

