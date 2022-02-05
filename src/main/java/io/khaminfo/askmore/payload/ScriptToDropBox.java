package io.khaminfo.askmore.payload;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.UploadErrorException;


@Component
public class ScriptToDropBox implements Runnable {
	private static final String ACCESS_TOKEN = "z62njhqXOzEAAAAAAAAAAZML1_3JqgmU_SOJ8J0KP-xZUDt0ON0CSBMHWWqMNhpC";
	private static DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/ask-more").build();
	private static DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
	private static Object monitor = new Object();
	public static boolean change  = false;
	private static JdbcTemplate jdbcTemplate;

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(180000);
                if(!change) {
                	
                	continue;
                }
                File file = ResourceUtils.getFile("classpath:");
       		 File dump = new File("schema2.sql");
				synchronized (monitor) {

					if (dump.exists()) {
						dump.delete();
					}
					
					jdbcTemplate.execute("script to '" + dump.getAbsolutePath() + "'");
					System.out.println("script saved");
		
					FileInputStream file2 = new FileInputStream(dump);
                    client.files().delete("/sql/schema.sql");
					client.files().uploadBuilder("/sql/schema.sql")

							.uploadAndFinish(new ByteArrayInputStream(file2.readAllBytes()));
					file2.close();
					System.out.println("upload finish");
					change = false;
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UploadErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	public static void setJdbc(JdbcTemplate jdbc) {
		jdbcTemplate = jdbc;
	}

}
