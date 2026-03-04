package vn.edu.hcmut.cse.adsoftweng.lab.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.hcmut.cse.adsoftweng.lab.service.StudentService;
import vn.edu.hcmut.cse.adsoftweng.lab.entity.Student;
import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentWebController {
    @Autowired
    private StudentService service;

    @GetMapping
    public String getAllStudents(@RequestParam(required = false) String keyword, Model model) {
        List<Student> students;
        if (keyword != null && !keyword.isEmpty()) {
            students = service.searchByName(keyword);
        } else {
            students = service.getAll();
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("dsSinhVien", students);
        return "students";
    }

    @GetMapping("/{id}")
    public String getStudentDetail(@PathVariable String id, Model model) {
        Student student = service.getById(id);
        if (student == null) {
            return "redirect:/students?error=notfound";
        }
        model.addAttribute("student", student);
        return "student-detail";
    }

    @GetMapping("/add")
    public String addStudentForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("action", "add");
        return "student-form";
    }

    @PostMapping("/add")
    public String addStudent(@ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        try {
            service.save(student);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sinh viên thành công!");
            return "redirect:/students";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm sinh viên: " + e.getMessage());
            return "redirect:/students/add";
        }
    }

    @GetMapping("/{id}/edit")
    public String editStudentForm(@PathVariable String id, Model model) {
        Student student = service.getById(id);
        if (student == null) {
            return "redirect:/students?error=notfound";
        }
        model.addAttribute("student", student);
        model.addAttribute("action", "edit");
        return "student-form";
    }

    @PostMapping("/{id}/edit")
    public String editStudent(@PathVariable String id, @ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        try {
            student.setId(id);
            service.save(student);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sinh viên thành công!");
            return "redirect:/students/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            return "redirect:/students/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteStudent(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            if (!service.existsById(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Sinh viên không tồn tại!");
                return "redirect:/students";
            }
            service.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sinh viên thành công!");
            return "redirect:/students";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa: " + e.getMessage());
            return "redirect:/students/" + id;
        }
    }
}
