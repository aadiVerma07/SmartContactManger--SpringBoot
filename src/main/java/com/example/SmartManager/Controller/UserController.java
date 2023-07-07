package com.example.SmartManager.Controller;

import com.example.SmartManager.Dao.ContactRepository;
import com.example.SmartManager.Dao.UserRepository;
import com.example.SmartManager.Entity.Contact;
import com.example.SmartManager.Entity.User;
import com.example.SmartManager.Helper.Message;
import jakarta.jws.WebParam;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal){
        return "normal/user_dashboard";
    }
    @RequestMapping("/addContact")
    public String addContact(Model m){
        m.addAttribute("title","Add Contact");
        m.addAttribute("contact",new Contact());
        return "normal/add_contact";
    }
    // method for adding common data
    @ModelAttribute
    public void addCommonData(Model model,Principal principal){
        String username=principal.getName();
        User user=userRepository.getUserByUserName(username);
        model.addAttribute("user",user);
    }
    // processing add contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal,RedirectAttributes redirAttrs){
        try {
            String username=principal.getName();
            User user=this.userRepository.getUserByUserName(username);
            contact.setUser(user);
            if(file.isEmpty()){
                System.out.println("file is empty");
                contact.setImage("contact.png");
            }
            else{
                // upload the file to the folder and then in contact
                contact.setImage(file.getOriginalFilename());
                File file1=new ClassPathResource("static/Img").getFile();

               Path path= Paths.get(file1.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File is uploaded");

            }
            user.getContact().add(contact);
            this.userRepository.save(user);
            redirAttrs.addFlashAttribute("success", "Contact Added Successfully...");
            System.out.println("Added to database");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("ERROR : "+e.getMessage());
        }
        return "redirect:/user/addContact";
    }
    @GetMapping("/show-Contact/{page}")
    public String showContact(@PathVariable("page") Integer page,Model m,Principal principal){
        m.addAttribute("title","View Contact");
        String username=principal.getName();
        User user=this.userRepository.getUserByUserName(username);
       Pageable pageable= PageRequest.of(page,7);
       Page<Contact> contactList= this.contactRepository.findContactByUser(user.getId(),pageable);
       m.addAttribute("contact",contactList);
       m.addAttribute("currentPage",page);
       m.addAttribute("totalPage",contactList.getTotalPages());
        return "normal/show_contact";
    }
    // showing specific contact details
    @GetMapping("/{CId}/contact")
    public String showContactDetails(@PathVariable("CId") Integer CId,Model m,Principal principal){
        System.out.println("CID"+CId);
      Optional<Contact>contact= contactRepository.findById(CId);
      Contact contact1=contact.get();
        String username=principal.getName();
        User user=this.userRepository.getUserByUserName(username);
      if(user.getId()==contact1.getUser().getId()){
          m.addAttribute("contact",contact1);
          m.addAttribute("title",contact1.getName());
        }
        return "normal/contact_detail";
    }
    // delete contact handler
    @GetMapping("/delete/{CId}")
    public String deleteContact(@PathVariable("CId") Integer CId, Model m, Principal principal, RedirectAttributes redirAttrs){
      Contact contact=  this.contactRepository.findById(CId).get();
        String username=principal.getName();
        User user=this.userRepository.getUserByUserName(username);
        if(user.getId()==contact.getUser().getId()){
            this.contactRepository.delete(contact);
            redirAttrs.addFlashAttribute("success", "Contact Deleted Successfully...");
        }
        return "redirect:/user/show-Contact/0";
    }
    // update contact handler
    @PostMapping("/update/{CId}")
    public String updateContact(@PathVariable("CId") Integer CId,Model m){
        m.addAttribute("title","Update Contact");
      Contact contact=  this.contactRepository.findById(CId).get();
      m.addAttribute("contact",contact);
        return "normal/update_contact";
    }
    // for update handler
    @RequestMapping(value = "/update-contact",method = RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,Model m,RedirectAttributes redirAttrs,Principal principal){
     Contact contact1=this.contactRepository.findById(contact.getCId()).get();
      try{
          if(!file.isEmpty()){
              // delete old image
              File deleteFile=new ClassPathResource("static/Img").getFile();
              File file2=new File(deleteFile,contact1.getImage());
              file2.delete();


              // update new image
              File file1=new ClassPathResource("static/Img").getFile();
              Path path= Paths.get(file1.getAbsolutePath()+File.separator+file.getOriginalFilename());
              Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
              contact.setImage(file.getOriginalFilename());
          }
          else{
              contact.setImage(contact1.getImage());
          }
          User user=this.userRepository.getUserByUserName(principal.getName());
          contact.setUser(user);
          this.contactRepository.save(contact);
          redirAttrs.addFlashAttribute("success", "Contact Updated Successfully...");
      }catch (Exception e){
          e.printStackTrace();
      }
        return "redirect:/user/show-Contact/0";
    }
    // handler for your profile
    @GetMapping("/profile")
    public String yourProfile(Model m){
        m.addAttribute("title","Profile");
        return "/normal/profile";
    }
}
