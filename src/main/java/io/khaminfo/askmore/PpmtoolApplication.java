package io.khaminfo.askmore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.ResourceUtils;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import io.khaminfo.askmore.payload.ScriptToDropBox;


@EnableAsync
@SpringBootApplication

public class PpmtoolApplication {

	private static final String ACCESS_TOKEN = "z62njhqXOzEAAAAAAAAAAZML1_3JqgmU_SOJ8J0KP-xZUDt0ON0CSBMHWWqMNhpC";
	private static DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/ask-more").build();
   private static  DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
    @Bean 
    BCryptPasswordEncoder bCryptPasswordEncoder() {
    	return new BCryptPasswordEncoder();
    
    
    
    }
    @PostConstruct
    public void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+3"));     // set Time Zone to moscow
    }
    

    
    public static void main(String[] args) {
    	try
        {
    		File file = ResourceUtils.getFile("classpath:");
    		 File dump = new File(file.getAbsolutePath()+"/schema.sql");
             if (dump.exists()) {
                 dump.delete();
             }
             File data = new File("data.sql");
             if (data.exists()) {
                 data.delete();
             }
            FileOutputStream downloadFile = new FileOutputStream(dump);
            System.out.println("creating the file");
            try
            {
            FileMetadata metadata = client.files().downloadBuilder("/sql/schema.sql")
                    .download(downloadFile);
            }
            finally
            {
                downloadFile.close();

                System.out.println("download finish");
            }
            
            ApplicationContext applicationContext = SpringApplication.run(PpmtoolApplication.class, args);
            JdbcTemplate jdbc = applicationContext.getBean(JdbcTemplate.class);
            System.out.println("jdbctemplate "+jdbc.toString());
            ScriptToDropBox myThread = applicationContext.getBean(ScriptToDropBox.class);
            myThread.setJdbc(jdbc);
            System.out.println("end of main "+myThread.toString());
           new Thread(myThread).start();
           System.out.println("Thread started");
           System.out.println(dump.getAbsolutePath());
        }
        //exception handled
        catch (DbxException e)
        {
        e.printStackTrace();
        }
        catch (IOException e)
        {
           System.out.println(e.getMessage());
            
        }
        
        
    }
    private   void process(String s) {
    	  System.out.println(s);
    	}
   
}
