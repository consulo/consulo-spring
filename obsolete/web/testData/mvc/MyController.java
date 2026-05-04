import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/class.form")
@Controller
public class MyController {

    @RequestMapping({"/method.form", "/anotherUrl.form"})
    public void process() {

    }
}
