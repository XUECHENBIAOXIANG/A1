import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {

//            Map<String, Integer> map = new TreeMap<>();
//            for (Course course : courses) {
//                String institution = course.institution;
//                if (map.containsKey(institution)) {
//                    map.put(institution, map.get(institution) + course.participants);
//                } else {
//                    map.put(institution, course.participants);
//                }
//            }
//            return map;
        Map<String, Integer> map = courses.stream().sorted(Comparator.comparing(Course::getInstitution).reversed()).collect(Collectors.groupingBy(Course::getInstitution, Collectors.summingInt(Course::getParticipants)));
        return map;
    }


    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> map = courses.stream().
                sorted(Comparator.comparing(Course::getSubject))
                .sorted(Comparator.comparing(Course::getInstitution).reversed()).
                collect(Collectors.groupingBy(course -> course.institution + "-" + course.subject, Collectors.summingInt(Course::getParticipants))).
                entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return map;


    }

    //3
  public Map<String, List<List<String>>> getCourseListOfInstructor() throws FileNotFoundException {

             Map<String, List<List<String>>> map = new HashMap<>();
        for (Course course : courses) {
             String[] name = course.instructors.split(", ");
            Arrays.stream(name).forEach(element -> {
                if (map.containsKey(element)) {
                    if (name.length == 1 && !map.get(element).get(0).contains(course.title)) {map.get(element).get(0).add(course.title);
                    } else {if (name.length > 1 && !map.get(element).get(1).contains(course.title)) {map.get(element).get(1).add(course.title);
                        }
                    }
                } else {
                    List<String> a = new ArrayList<>();List<String> b = new ArrayList<>();List<List<String>> c = new ArrayList<>();c.add(a);c.add(b);
                    if (name.length == 1) {a.add(course.title);
                    } else {b.add(course.title);
                    }map.put(element, c);
                }
            });
        }
        for (List<List<String>> value : map.values()) {
//           Collections.sort(value.get(0));
//           Collections.sort(value.get(1));
            value.get(0).sort(String::compareTo);value.get(1).sort(String::compareTo);
        }
//        for (Map.Entry<String, List<List<String>>> entry : map.entrySet()) {
//            String key = entry.getKey();
//            List<List<String>> a = entry.getValue();
//            System.out.print(key);
//            System.out.print(" == ");
//            System.out.println(a);
//            // do something with key and value
//        }
        return map;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        List<String> courseTitles;
        if (by.equals("hours")) {
            courseTitles = courses.stream().sorted(Comparator.comparing(Course::getTotalHours).reversed().thenComparing(Course::getTitle)).map(Course::getTitle).distinct().limit(topK)
                    .collect(Collectors.toList());
        } else {
            courseTitles = courses.stream()
                    .sorted(Comparator.comparing(Course::getParticipants)
                            .reversed().thenComparing(Course::getTitle))
                    .map(Course::getTitle)
                    .distinct()
                    .limit(topK)
                    .collect(Collectors.toList());
        }
        return courseTitles;

    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<String> searchCourses = courses.stream().filter(course -> course.subject.toLowerCase().contains(courseSubject.toLowerCase()))
                .filter(course -> course.percentAudited >= percentAudited)
                .filter(course -> course.totalHours <= totalCourseHours).map(Course::getTitle).distinct().sorted().collect(Collectors.toList());
        return searchCourses;
    }


    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        Map<String, Double> avgMedianAge = courses.stream().
                collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getMedianAge)));
        Map<String, Double> avgMale = courses.stream().
                collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentMale)));
        Map<String, Double> avgdgr = courses.stream().
                collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentDegree)));
        List<String> r = new ArrayList<>();
        r = courses.stream().map(s -> {
                            double sim = Math.pow((age - avgMedianAge.get(s.number)), 2) + Math.pow(100 * gender - avgMale.get(s.number), 2) + Math.pow(100 * isBachelorOrHigher - avgdgr.get(s.number), 2);

                            return new simi(s.title, sim, s.launchDate, s.number);
                        }
                ).sorted(Comparator.comparing(simi::getSim).thenComparing(simi::getCourse_title))
                .collect(Collectors.toMap(simi::getNumber, Function.identity(), (a, b) -> {
                            if (a.launchDate.after(b.launchDate)) return a;
                            return b;
                        }
                        , LinkedHashMap::new))
                .values()
                .stream()
                .map(simi -> {
                    System.out.println(simi.sim);
                    return simi;
                })
                .map(simi::getCourse_title).distinct().limit(10)
                .collect(Collectors.toList());
        return r;
    }

}

class simi {
    String course_title;
    double sim;
    String number;

    public String getNumber() {
        return number;
    }

    public String getCourse_title() {
        return course_title;
    }

    public double getSim() {
        return sim;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    Date launchDate;

    public simi(String course_title, double sim, Date launchDate, String number) {
        this.course_title = course_title;
        this.sim = sim;
        this.launchDate = launchDate;
        this.number = number;
    }
}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;

    public int getParticipants() {
        return participants;
    }

    int honorCode;

    public String getNumber() {
        return number;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructors() {
        return instructors;
    }

    public int getYear() {
        return year;
    }

    public int getHonorCode() {
        return honorCode;
    }

    public int getAudited() {
        return audited;
    }

    public int getCertified() {
        return certified;
    }

    public double getPercentAudited() {
        return percentAudited;
    }

    public double getPercentCertified() {
        return percentCertified;
    }

    public double getPercentCertified50() {
        return percentCertified50;
    }

    public double getPercentVideo() {
        return percentVideo;
    }

    public double getPercentForum() {
        return percentForum;
    }

    public double getGradeHigherZero() {
        return gradeHigherZero;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public double getMedianHoursCertification() {
        return medianHoursCertification;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public double getPercentFemale() {
        return percentFemale;
    }

    public double getPercentDegree() {

        return percentDegree;
    }

    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    double percentDegree;

    public String getSubject() {
        return subject;
    }

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }


}