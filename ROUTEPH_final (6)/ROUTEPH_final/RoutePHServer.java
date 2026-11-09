import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Route PH - Java web server.
 *  - serves the HTML / CSS / JavaScript files in the "web" folder
 *  - keeps the routes, user accounts, comments, replies and notifications
 *
 * Compile:  javac RoutePHServer.java
 * Run:      java RoutePHServer
 * Open:     http://localhost:8080
 */
public class RoutePHServer {

    static final int PORT = 8080;
    static final String DATA_FILE = "routeph-data.ser";   // accounts + comments are saved here

    // ================= ROUTE DATA =================
    // route = String[][]  ->  row 0 = {name, title, time, tag, path, note}, other rows = {step, detail, vehicle}
    static final Map<String, String[][][]> ROUTES = new LinkedHashMap<>();
    static {
        ROUTES.put("Caloocan", new String[][][]{
            {{"1st Route South Caloocan", "LRT Line 1 / Line 2", "45 mins", "RECOMMENDED • FASTEST", "1 transfer • Walk Pureza to Recto • Transfer Recto to 5th Avenue • Tricycle to Landaska", "trains every 8 mins"},
             {"Walk/ride to LRT-2 Pureza Station", "Approx. 3 mins walking speed", "Train", "Pureza LRT-2 Station, Manila"},
             {"Ride LRT-2 Pureza Train", "Toward LRT-2 Recto Station", "Train", "Recto LRT-2 Station, Manila"},
             {"Transfer to LRT-1 Doroteo Jose Station", "Follow the elevated walkway connecting LRT-2 Recto to LRT-1 D. Jose", "Train", "Doroteo Jose LRT-1 Station, Manila"},
             {"Ride LRT-1 Doroteo Jose Train", "Toward LRT-1 5th Avenue Station", "Train", "5th Avenue LRT-1 Station, Caloocan"},
             {"Get off at LRT-1 5th Avenue Station", "Take a tricycle to C3 Caloocan", "Walk", "5th Avenue LRT-1 Station, Caloocan"},
             {"Tricycle to Landaska", "Ride along C3 Road", "E-Bike", "Landaska, Caloocan City"},
             {"Landaska to your destination", "Arrive in South Caloocan", "Walk", "South Caloocan, Caloocan City"}},
            {{"2nd Route South Caloocan", "LRT Line 2 to Traditional / Modern Jeep", "1 hr 30 mins", "ALTERNATIVE", "Pureza to Recto • Recto Jeep to C3 Caloocan • Tricycle to Landaska • E-Bike", "heavy traffic along Blumentritt and Abad Santos"},
             {"Walk/ride to LRT-2 Pureza Station", "Approx. 3 mins walking speed", "Train", "Pureza LRT-2 Station, Manila"},
             {"Ride LRT-2 Pureza Train", "Toward LRT-2 Recto Station", "Train", "Recto LRT-2 Station, Manila"},
             {"Exit LRT-2 Recto Station", "Walk toward the jeep stop", "Walk", "Recto LRT-2 Station, Manila"},
             {"Jeep to C3 Caloocan", "Ride the jeep heading to Caloocan", "Jeep", "C-3 Road, Caloocan City"},
             {"C3 Caloocan to Landaska", "Take a tricycle", "Tricycle", "Landaska, Caloocan City"},
             {"Landaska to your destination", "Take an E-Bike", "E-Bike", "South Caloocan, Caloocan City"}}
        });
        ROUTES.put("Bulacan", new String[][][]{
            {{"1st Route Bulacan", "LRT Line 2 / LRT Line 1", "1 hr 30 mins", "RECOMMENDED • FASTEST", "1 transfer • Pureza to Recto • Doroteo Jose to Monumento • Meycauayan Jeep", "trains every 8 mins"},
             {"Walk/ride to LRT-2 Pureza Station", "Approx. 3 mins walking speed", "Train", "Pureza LRT-2 Station, Manila"},
             {"Ride LRT-2 Pureza Train", "Toward LRT-2 Recto Station", "Train", "Recto LRT-2 Station, Manila"},
             {"Transfer to LRT-1 Doroteo Jose Station", "Walk to the LRT-1 platform", "Walk", "Doroteo Jose LRT-1 Station, Manila"},
             {"Ride LRT-1 Doroteo Jose Train", "Toward LRT-1 Monumento Station", "Train", "Monumento LRT-1 Station, Caloocan"},
             {"Get off at LRT-1 Monumento Station", "Head to the back of Victory Mall", "Walk", "Victory Mall, Monumento, Caloocan"},
             {"Ride at Meycauayan Jeep Terminal", "Take the jeep toward Bulacan", "Jeep", "Meycauayan Jeepney Terminal, Monumento, Caloocan"},
             {"Walk to your destination", "After the jeepney reaches the last stop", "Walk", "Olympic Avenue, Deca Homes Subdivision, Loma de Gato, Marilao, Bulacan"}},
            {{"2nd Route Bulacan", "LRT Line 2 to Bus (Santrans)", "1 hr 45 mins", "ALTERNATIVE", "Pureza to Recto • Walk to Santrans Terminal • Bus to Duhat • Tricycle", "bus leaves when full"},
             {"Walk/ride to LRT-2 Pureza Station", "Approx. 3 mins walking speed", "Train", "Pureza LRT-2 Station, Manila"},
             {"Ride LRT-2 Pureza Train", "Toward LRT-2 Recto Station", "Train", "Recto LRT-2 Station, Manila"},
             {"Exit LRT-2 Recto Station", "Walk toward BUS Santrans Terminal", "Walk", "Santrans Bus Terminal, Manila"},
             {"Ride Bus to Duhat", "Get off at the Marilao Exit", "Bus", "Duhat, Bocaue, Bulacan"},
             {"Ride Tricycle from Duhat", "Ride to your destination", "Tricycle", "Olympic Avenue, Deca Homes Subdivision, Loma de Gato, Marilao, Bulacan"}},
            {{"3rd Route Bulacan", "LRT Line 2 / Bus / Jeep", "2 hrs", "ALTERNATIVE", "Pureza to Cubao • Bus to SM Fairview • Jeep to Bagong Silang • Tricycle", "heavy traffic along Commonwealth Avenue"},
             {"Walk/ride to LRT-2 Pureza Station", "Approx. 3 mins walking speed", "Train", "Pureza LRT-2 Station, Manila"},
             {"Ride LRT-2 Pureza Train", "Toward LRT-2 Cubao Station", "Train", "Cubao LRT-2 Station, Quezon City"},
             {"Exit LRT-2 Cubao Station", "Take a bus to SM Fairview", "Bus", "SM City Fairview, Quezon City"},
             {"Get off at SM Fairview", "Take a jeep to Kaliwa Bagong Silang", "Jeep", "Bagong Silang, Caloocan City"},
             {"Get off at Bagong Silang last stop", "Take a tricycle to Olympic Avenue, Deca Homes, Loma de Gato, Marilao", "Tricycle", "Olympic Avenue, Deca Homes Subdivision, Loma de Gato, Marilao, Bulacan"}}
        });
    }

    // ================= ACCOUNTS / COMMENTS =================
    static class Reply implements Serializable {
        String user, text, at; boolean read;
    }
    static class Comment implements Serializable {
        String id, key, user, text, at;
        List<Reply> replies = new ArrayList<>();
    }

    static Map<String, String> users = new LinkedHashMap<>();   // username -> password hash
    static List<Comment> comments = new ArrayList<>();

    @SuppressWarnings("unchecked")
    static void loadData() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            users = (Map<String, String>) in.readObject();
            comments = (List<Comment>) in.readObject();
        } catch (Exception e) {
            System.out.println("Could not read saved data (starting fresh): " + e.getMessage());
        }
    }

    static synchronized void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(users);
            out.writeObject(comments);
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    static String hash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return s; }
    }

    static String now() { return new SimpleDateFormat("M/d/yyyy, h:mm a").format(new Date()); }

    // ================= MAIN =================
    public static void main(String[] args) throws IOException {
        loadData();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/routes", RoutePHServer::routes);
        server.createContext("/api/register", RoutePHServer::register);
        server.createContext("/api/login", RoutePHServer::login);
        server.createContext("/api/comments", RoutePHServer::commentsApi);
        server.createContext("/api/reply", RoutePHServer::reply);
        server.createContext("/api/notifications", RoutePHServer::notifications);
        server.createContext("/", RoutePHServer::files);
        server.start();
        System.out.println("======================================");
        System.out.println(" Route PH is running!");
        System.out.println(" Open this in Chrome:");
        System.out.println(" http://localhost:" + PORT);
        System.out.println(" (press Ctrl+C to stop)");
        System.out.println("======================================");
    }

    // ================= API =================
    static void routes(HttpExchange ex) throws IOException {
        String dest = query(ex).getOrDefault("dest", "").trim();
        String key = null;
        for (String k : ROUTES.keySet()) if (k.equalsIgnoreCase(dest)) key = k;
        if (key == null) {
            json(ex, 404, "{\"error\":\"Destination not available. Please enter only Caloocan or Bulacan.\"}");
            return;
        }
        StringBuilder sb = new StringBuilder("{\"dest\":" + q(key) + ",\"routes\":[");
        String[][][] list = ROUTES.get(key);
        for (int i = 0; i < list.length; i++) {
            String[][] r = list[i];
            if (i > 0) sb.append(",");
            sb.append("{\"name\":").append(q(r[0][0])).append(",\"title\":").append(q(r[0][1]))
              .append(",\"time\":").append(q(r[0][2])).append(",\"tag\":").append(q(r[0][3]))
              .append(",\"path\":").append(q(r[0][4])).append(",\"note\":").append(q(r[0][5]))
              .append(",\"steps\":[");
            for (int s = 1; s < r.length; s++) {
                if (s > 1) sb.append(",");
                sb.append("[").append(q(r[s][0])).append(",").append(q(r[s][1])).append(",").append(q(r[s][2])).append(",").append(q(r[s][3])).append("]");
            }
            sb.append("]}");
        }
        json(ex, 200, sb.append("]}").toString());
    }

    static void register(HttpExchange ex) throws IOException {
        Map<String, String> f = body(ex);
        String name = f.getOrDefault("user", "").trim(), pass = f.getOrDefault("pass", "");
        if (name.length() < 3) { err(ex, "Username must be at least 3 characters."); return; }
        if (!name.matches("[A-Za-z0-9_.]+")) { err(ex, "Username: letters, numbers, _ and . only."); return; }
        if (pass.length() < 4) { err(ex, "Password must be at least 4 characters."); return; }
        synchronized (RoutePHServer.class) {
            for (String u : users.keySet())
                if (u.equalsIgnoreCase(name)) { err(ex, "That username is already taken."); return; }
            users.put(name, hash(pass));
            saveData();
        }
        json(ex, 200, "{\"user\":" + q(name) + "}");
    }

    static void login(HttpExchange ex) throws IOException {
        Map<String, String> f = body(ex);
        String name = f.getOrDefault("user", "").trim(), pass = f.getOrDefault("pass", "");
        if (name.isEmpty() || pass.isEmpty()) { err(ex, "Please enter your username and password."); return; }
        String found = null;
        for (String u : users.keySet()) if (u.equalsIgnoreCase(name)) found = u;
        if (found == null) { err(ex, "Account not found. Tap \"Create account\"."); return; }
        if (!users.get(found).equals(hash(pass))) { err(ex, "Wrong password."); return; }
        json(ex, 200, "{\"user\":" + q(found) + "}");
    }

    static void commentsApi(HttpExchange ex) throws IOException {
        if (ex.getRequestMethod().equalsIgnoreCase("POST")) {
            Map<String, String> f = body(ex);
            String text = f.getOrDefault("text", "").trim(), user = f.getOrDefault("user", "");
            if (!users.containsKey(user)) { err(ex, "Please log in first."); return; }
            if (text.isEmpty()) { json(ex, 200, "{\"message\":\"No comment entered.\"}"); return; }
            Comment c = new Comment();
            c.id = UUID.randomUUID().toString().substring(0, 8);
            c.key = f.getOrDefault("key", ""); c.user = user; c.text = text; c.at = now();
            synchronized (RoutePHServer.class) { comments.add(0, c); saveData(); }
            json(ex, 200, "{\"message\":\"Comment posted successfully!\"}");
            return;
        }
        String key = query(ex).getOrDefault("key", "");
        StringBuilder sb = new StringBuilder("{\"comments\":[");
        boolean first = true;
        synchronized (RoutePHServer.class) {
            for (Comment c : comments) {
                if (!c.key.equals(key)) continue;
                if (!first) sb.append(","); first = false;
                sb.append("{\"id\":").append(q(c.id)).append(",\"user\":").append(q(c.user))
                  .append(",\"text\":").append(q(c.text)).append(",\"at\":").append(q(c.at)).append(",\"replies\":[");
                for (int i = 0; i < c.replies.size(); i++) {
                    Reply r = c.replies.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{\"user\":").append(q(r.user)).append(",\"text\":").append(q(r.text))
                      .append(",\"at\":").append(q(r.at)).append("}");
                }
                sb.append("]}");
            }
        }
        json(ex, 200, sb.append("]}").toString());
    }

    static void reply(HttpExchange ex) throws IOException {
        Map<String, String> f = body(ex);
        String user = f.getOrDefault("user", ""), text = f.getOrDefault("text", "").trim();
        if (!users.containsKey(user) || text.isEmpty()) { err(ex, "Reply not sent."); return; }
        synchronized (RoutePHServer.class) {
            for (Comment c : comments) {
                if (c.id.equals(f.get("id"))) {
                    Reply r = new Reply();
                    r.user = user; r.text = text; r.at = now(); r.read = c.user.equals(user);
                    c.replies.add(r);
                    saveData();
                    json(ex, 200, "{\"ok\":true}");
                    return;
                }
            }
        }
        err(ex, "Comment not found.");
    }

    // replies written by OTHER people on MY comments
    static void notifications(HttpExchange ex) throws IOException {
        Map<String, String> qs = query(ex);
        String user = qs.getOrDefault("user", "");
        boolean markRead = "1".equals(qs.get("read"));
        int unread = 0;
        StringBuilder items = new StringBuilder();
        synchronized (RoutePHServer.class) {
            for (Comment c : comments) {
                if (!c.user.equals(user)) continue;
                for (Reply r : c.replies) {
                    if (r.user.equals(user)) continue;
                    if (!r.read) unread++;
                    if (items.length() > 0) items.append(",");
                    items.append("{\"by\":").append(q(r.user)).append(",\"text\":").append(q(r.text))
                         .append(",\"at\":").append(q(r.at)).append(",\"on\":").append(q(c.text))
                         .append(",\"unread\":").append(!r.read).append("}");
                    if (markRead) r.read = true;
                }
            }
            if (markRead) saveData();
        }
        json(ex, 200, "{\"unread\":" + unread + ",\"items\":[" + items + "]}");
    }

    // ================= FILES (web folder) =================
    static void files(HttpExchange ex) throws IOException {
        String p = ex.getRequestURI().getPath();
        if (p.equals("/")) p = "/index.html";
        Path web = Path.of("web").toAbsolutePath().normalize();
        Path file = web.resolve(p.substring(1)).normalize();
        if (!file.startsWith(web) || !Files.isRegularFile(file)) {
            send(ex, 404, "text/plain", "Not found. Run the program from the folder that contains the 'web' folder.");
            return;
        }
        String type = p.endsWith(".html") ? "text/html" : p.endsWith(".css") ? "text/css"
                    : p.endsWith(".js") ? "application/javascript" : "application/octet-stream";
        send(ex, 200, type, new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
    }

    // ================= HELPERS =================
    static void json(HttpExchange ex, int code, String s) throws IOException { send(ex, code, "application/json", s); }
    static void err(HttpExchange ex, String m) throws IOException { json(ex, 400, "{\"error\":" + q(m) + "}"); }

    static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }

    static String q(Object o) {
        String s = String.valueOf(o);
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "") + "\"";
    }

    static Map<String, String> query(HttpExchange ex) {
        String raw = ex.getRequestURI().getRawQuery();
        return raw == null ? new HashMap<>() : parse(raw);
    }

    static Map<String, String> body(HttpExchange ex) throws IOException {
        return parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
    }

    static Map<String, String> parse(String s) {
        Map<String, String> m = new HashMap<>();
        for (String pair : s.split("&")) {
            int i = pair.indexOf('=');
            if (i < 0) continue;
            m.put(URLDecoder.decode(pair.substring(0, i), StandardCharsets.UTF_8),
                  URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8));
        }
        return m;
    }
}
