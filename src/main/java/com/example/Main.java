/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.sql.DataSource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
  }


  @RequestMapping("/need")
  String need() {
    return "need";
  }


  @PostMapping("/api/need")
  public String handleFileUpload(@RequestParam("title") String title,
                                 @RequestParam("message") String message,
                                 @RequestParam("imagefile") MultipartFile file,
                                 RedirectAttributes redirectAttributes) throws Exception {
    Map<String, Object> model = new HashMap<String, Object>();

    // saveUploadedFiles(title, message, file, model);
    //storageService.store(file);
//    redirectAttributes.addFlashAttribute("message",
//            "You successfully uploaded " + file.getOriginalFilename() + "!");
//
    return "redirect:/";
  }

  private String saveUploadedFiles(String title, String message, MultipartFile file, Map<String, Object> model) throws Exception {
    byte[] bytes = file.getBytes();
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();

      PreparedStatement ps = connection.prepareStatement("INSERT INTO needs(title,message,image) VALUES (?,?,?)");
      ps.setString(1, title);
      ps.setString(2, message);
      ps.setBytes(3, bytes);
      ps.executeUpdate();
      ps.close();

      ResultSet rs = stmt.executeQuery("SELECT title FROM needs");


      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB: " + rs.getString("title"));
      }

      //model.put("records", output);
      //return "db";
      return String.join(" and ", output);
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }


  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS needs (needId SERIAL, " +
              "title varchar(100)," +
              "message varchar(1000)," +
              "image bytea)");

      ResultSet rs = stmt.executeQuery("SELECT needId, title, message FROM needs");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB needId: " + rs.getString("needId"));
        output.add("Read from DB title: " + rs.getString("title"));
        output.add("Read from DB message: " + rs.getString("message"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @RequestMapping("/api/Image/{id:.+}")
  public ResponseEntity<byte[]> getImage(@PathVariable("id") String id) {
    byte[] image = readPicture(Integer.parseInt(id));
    return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
  }

  public byte[] readPicture(int id) {
    // update sql
    String selectSQL = "SELECT image FROM materials WHERE id=?";
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    byte[] imageBytes = null;

    try (Connection conn = dataSource.getConnection()) {
      pstmt = conn.prepareStatement(selectSQL);
      pstmt.setInt(1, id);
      rs = pstmt.executeQuery();


      while (rs.next()) {
        imageBytes = rs.getBytes("image");

      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (pstmt != null) {
          pstmt.close();
        }
      } catch (SQLException e) {
        System.out.println(e.getMessage());
      }
    }
    return imageBytes;
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}
