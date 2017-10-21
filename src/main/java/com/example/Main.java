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

import javax.activation.FileTypeMap;
import javax.sql.DataSource;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

  @RequestMapping("/index")
  String index2() {
    return "index";
  }


  @RequestMapping("/needs")
  String needs() {
    return "needs";
  }

  @RequestMapping("/login")
  String login() {
    return "login";
  }

  @RequestMapping("/log-in")
  String login2() {
    return "login";
  }


  @RequestMapping("/categories")
  String categories() {
    return "categories";
  }


  @RequestMapping("/listings")
  String listings() {
    return "listings";
  }

  @RequestMapping("/api/list")
  public @ResponseBody List<NeedItem> listOfNeeds() {
    List<NeedItem> items = new ArrayList<NeedItem>();

    items.add(new NeedItem("1", "test", "test Messages"));
//    try{
//      try (Connection connection = dataSource.getConnection()) {
//        Statement stmt = connection.createStatement();
//        ResultSet rs = stmt.executeQuery("SELECT needId, title, message FROM needs");
//
//        while (rs.next()) {
//          items.add(new NeedItem(
//                  rs.getString("needId"),
//                  rs.getString("title"),
//                  rs.getString("message")
//          ));
//        }
//
//      } catch (Exception e) {
//
//      }
//    } catch (Exception e) {
//      items.add(new NeedItem("1", "test", "test Messages"));
//    }

    return items;
  }


  @PostMapping("/api/need")
  public void handleFileUpload(@RequestParam("title") String title,
                                 @RequestParam("message") String message,
                                 @RequestParam("imagefile") MultipartFile file,
                                 RedirectAttributes redirectAttributes) throws Exception {
    Map<String, Object> model = new HashMap<String, Object>();

    saveUploadedFiles(title, message, file, model);
    //storageService.store(file);
//    redirectAttributes.addFlashAttribute("message",
//            "You successfully uploaded " + file.getOriginalFilename() + "!");
//
    //return "redirect:/";
  }

  private void saveUploadedFiles(String title, String message, MultipartFile file, Map<String, Object> model) throws Exception {
    byte[] bytes = file.getBytes();
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();

      PreparedStatement ps = connection.prepareStatement("INSERT INTO needs(title,message,image) VALUES (?,?,?)");
      ps.setString(1, title);
      ps.setString(2, message);
      ps.setBytes(3, bytes);
      ps.executeUpdate();
      ps.close();

    } catch (Exception e) {
      model.put("message", e.getMessage());
    }
  }


  @GetMapping("showme")
  public ResponseEntity<byte[]> getImage() throws IOException{
    byte[] image = readPicture(Integer.parseInt("2"));
    return ResponseEntity.ok().contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(MediaType.IMAGE_JPEG_VALUE))).body(image);
  }
//  @GetMapping("thing")
//  public ResponseEntity<byte[]> what() throws IOException{
//    byte[] image = readPicture(Integer.parseInt("1"));
//    return ResponseEntity.ok()
//            .header("Content-Disposition", "attachment; filename=" +file.getName())
//            .contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(file)))
//            .body(Files.readAllBytes(file.toPath()));
//  }

  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS needs (needId SERIAL, " +
              "title varchar(100)," +
              "message varchar(1000)," +
              "image bytea)");

      ResultSet rs = stmt.executeQuery("SELECT needId, title, message, length(image) as imgLen FROM needs");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB needId: " + rs.getString("needId"));
        output.add("Read from DB title: " + rs.getString("title"));
        output.add("Read from DB message: " + rs.getString("message"));
        output.add("Read from DB image: " + rs.getString("imgLen"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @RequestMapping("/api/image/{id:.+}")
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
    if(id != 1) {
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
//      }catch(Exception e){
//        Path path = Paths.get("\\src\\main\\resources\\public");
//        try {
//          imageBytes = Files.readAllBytes(path);
//        } catch (IOException e1) {
//          e1.printStackTrace();
//        }
//      }
    }else {
      try {
        imageBytes = recoverImageFromUrl("https://upload.wikimedia.org/wikipedia/commons/e/e0/JPEG_example_JPG_RIP_050.jpg");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return imageBytes;
  }
  public byte[] recoverImageFromUrl(String urlText) throws Exception {
    URL url = new URL(urlText);
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    try (InputStream inputStream = url.openStream()) {
      int n = 0;
      byte [] buffer = new byte[ 1024 ];
      while (-1 != (n = inputStream.read(buffer))) {
        output.write(buffer, 0, n);
      }
    }

    return output.toByteArray();
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
