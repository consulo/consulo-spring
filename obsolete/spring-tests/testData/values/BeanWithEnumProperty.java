import java.lang.Boolean;
import java.util.List;
import java.util.Map;

public class BeanWithEnumProperty {
    public void setEnumClass(EnumClass en) {}
    public void setEnumClassFields(EnumClassWithStaticFields en) {}
    public void setMap(Map<EnumClass, Boolean> map) {}
    public void setList(List<EnumClass> map) {}
}