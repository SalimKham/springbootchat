package io.khaminfo.chat.services;

import java.awt.image.BufferedImage;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Principal;



import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkSettings;

import io.khaminfo.chat.domain.Crop;
import io.khaminfo.chat.domain.Person;
import io.khaminfo.chat.exceptions.AccessException;
import io.khaminfo.chat.repositories.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private static final String ACCESS_TOKEN = "z62njhqXOzEAAAAAAAAAAZML1_3JqgmU_SOJ8J0KP-xZUDt0ON0CSBMHWWqMNhpC";
	DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/ask-more").build();
    DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

	public Person saveUser(Person newUser) {

		try {
			newUser.setPassword(bCryptPasswordEncoder.encode(newUser.getPassword()));
			
			
			newUser.setUser_state(1);
		
			newUser.setConfirmPassword("");
		
			
			Person user = null;
				user = userRepository.save( newUser);
		    ScriptDropBox.change = true;
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			newUser = userRepository.findByUsername(newUser.getUsername());
            
			if (newUser != null) {
				switch (newUser.getUser_state()) {
				case 5:
					throw new AccessException("UserName" + newUser.getUsername() + " Exists But Not Confirmed");
				case 3:
					throw new AccessException("UserName" + newUser.getUsername() + " Exists But Not Accepted by Admin");
				default:
					throw new AccessException("userName '" + newUser.getUsername() + "' Already Exists");
				}
			}

		}
		return null;
	}

	public List<Object[]> getAllUsers() {
		Person user = (Person) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<Object[]> result = userRepository.findAllUsers(user.getId());
      
		return result;
	}
	public List<Object[]> getAll(){
		
		List<Object[]> result = userRepository.findAllUsers(10000);
      
		return result;
	}

	public String updateUserPhoto(MultipartFile file, Crop crop) {
		
		 try {

	        
	            byte[] bytes = file.getBytes();
	            ImageIcon img = new ImageIcon(bytes);
	   
	            float x_ratio = (float) img.getIconWidth() /crop.getDisplayWidth();
	            float y_ratio = (float) img.getIconHeight()/crop.getDisplayHeight();
	            String image_url="";
	            crop.setRatio(x_ratio, y_ratio);
				BufferedImage b = ImageUtils.crop(img.getImage(),crop.getX(),crop.getY(), crop.getWith(),crop.getHeight()	);
			    BufferedImage b2 = ImageUtils.resize(b, crop.getWith(), crop.getHeight(), 100,100);
			    Person user = (Person) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			     String imageName = "profileimage_"+user.getId();
			 
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write( b2, "png", baos );
				baos.flush();
				client.files().uploadBuilder("/chatimages/"+imageName+".png").withMode(WriteMode.OVERWRITE)
		                .uploadAndFinish(new ByteArrayInputStream(baos.toByteArray()));
	            image_url = client.sharing().createSharedLinkWithSettings("/chatimages/"+imageName+".png", SharedLinkSettings.newBuilder().withRequestedVisibility(RequestedVisibility.PUBLIC).build()).getUrl();
	            image_url = image_url.substring(0,image_url.lastIndexOf('.')) +".png?raw=1&";
	            
				userRepository.updatePicture(user.getId(), image_url);
				System.out.println("photo uploaded "+user.getUsername());
				 ScriptDropBox.change = true;
	            return image_url;

			    


	        } catch (Exception e) {
	        	e.printStackTrace();
	        	//throw new AccessException("SomeThing went Wrong!");
	        }
		 return "";
		
	}


	public void logoutUser() {
		
		Person user = (Person) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		userRepository.updateVisitDate(new Date(), user.getId(),2);
		 ScriptDropBox.change = true;
		System.out.println("user logged out"+user.getUsername());

	}
  public void login() {
	  Person user = (Person) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		userRepository.updateUserState(user.getId(), 1);
		System.out.println("user loged in "+user.getUsername());
  }
	public Person getUserById(long id) {
		Person user = userRepository.getById(id);
		if (user == null) {
			throw new AccessException("Ops!!! Profile Not found!!");
		}

		return user;
	}

	





}
