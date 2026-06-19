package com.grievio.util;

import java.util.*;

public final class LocationData {
    private LocationData() {}

    public static final String[] DISTRICTS = {
        "Agra", "Ahmedabad", "Ajmer", "Aligarh", "Allahabad (Prayagraj)", "Alwar",
        "Ambala", "Amritsar", "Anand", "Aurangabad", "Azamgarh",
        "Bangalore Urban", "Bareilly", "Barmer", "Bhopal", "Bikaner",
        "Chandigarh", "Chennai", "Coimbatore", "Cuttack",
        "Dehradun", "Delhi", "Dhanbad", "Dharwad",
        "Faridabad", "Firozabad", "Gandhinagar", "Ghaziabad", "Gorakhpur", "Gurgaon (Gurugram)",
        "Guwahati", "Gwalior", "Hapur", "Hisar", "Howrah", "Hyderabad",
        "Indore", "Jaipur", "Jalandhar", "Jammu", "Jhansi", "Jodhpur",
        "Kanpur Nagar", "Karnal", "Kochi", "Kolkata", "Lucknow", "Ludhiana",
        "Madurai", "Mathura", "Meerut", "Moradabad", "Mumbai", "Mysuru",
        "Nagpur", "Nashik", "New Delhi", "Noida (Gautam Buddha Nagar)", "Panipat", "Patna", "Pune",
        "Raipur", "Rajkot", "Ranchi", "Rohtak", "Salem", "Saharanpur", "Shahjahanpur", "Srinagar", "Surat",
        "Thane", "Tiruchirappalli", "Tiruppur", "Udaipur", "Vadodara", "Varanasi", "Vijayawada", "Visakhapatnam",
        "Warangal", "Yamuna Nagar"
    };

    private static final Map<String, String[]> SECTOR_MAP = new LinkedHashMap<>();
    private static final Map<String, String[]> SOCIETY_MAP = new LinkedHashMap<>();
    static {
        SECTOR_MAP.put("Delhi", new String[]{"Sector 1","Sector 3","Sector 7","Sector 12","Sector 15","Sector 21","Rohini Sector 5","Rohini Sector 11","Dwarka Sector 6","Dwarka Sector 12"});
        SECTOR_MAP.put("New Delhi", new String[]{"Connaught Place","Karol Bagh","Paharganj","Lodhi Colony","Khan Market","South Extension","Lajpat Nagar","Vasant Kunj"});
        SECTOR_MAP.put("Noida (Gautam Buddha Nagar)", new String[]{"Sector 15","Sector 18","Sector 22","Sector 27","Sector 44","Sector 50","Sector 62","Sector 63","Sector 76","Sector 137"});
        SECTOR_MAP.put("Gurgaon (Gurugram)", new String[]{"Sector 14","Sector 17","Sector 29","Sector 31","Sector 44","Sector 46","Sector 56","Sector 65","DLF Phase 1","DLF Phase 4"});
        SECTOR_MAP.put("Chandigarh", new String[]{"Sector 7","Sector 8","Sector 11","Sector 15","Sector 17","Sector 22","Sector 35","Sector 43","Sector 45","Industrial Area Phase I"});
        SECTOR_MAP.put("Faridabad", new String[]{"Sector 12","Sector 15","Sector 16","Sector 21","Sector 28","Sector 31","Sector 37","Sector 46","NIT Area","Old Faridabad"});
        SECTOR_MAP.put("Jaipur", new String[]{"Vaishali Nagar","Mansarovar","Jagatpura","Sanganer","Tonk Phatak","Shyam Nagar","C-Scheme","Malviya Nagar","Sodala","Pratap Nagar"});
        SECTOR_MAP.put("Pune", new String[]{"Shivajinagar","Kothrud","Hadapsar","Wakad","Hinjewadi","Baner","Viman Nagar","Kharadi","Kondhwa","Warje"});
        SECTOR_MAP.put("Mumbai", new String[]{"Andheri East","Andheri West","Bandra East","Bandra West","Borivali East","Dadar","Goregaon","Kurla","Malad","Thane West"});
        SECTOR_MAP.put("Hyderabad", new String[]{"Banjara Hills","Jubilee Hills","Madhapur","Gachibowli","Begumpet","Secunderabad","Ameerpet","Hitech City","Kondapur","Kukatpally"});
        SECTOR_MAP.put("Bangalore Urban", new String[]{"Whitefield","Koramangala","Indiranagar","Jayanagar","JP Nagar","HSR Layout","Electronic City","Hebbal","Yelahanka","Marathahalli"});
        SECTOR_MAP.put("Chennai", new String[]{"Anna Nagar","Adyar","T. Nagar","Velachery","Perambur","Mylapore","Tambaram","Avadi","Chromepet","Porur"});

        SOCIETY_MAP.put("Sector 62", new String[]{"Mahagun Moderne","Logix Blossom Zest","Stellar One","Amrapali Mayfair","Paras Tierea"});
        SOCIETY_MAP.put("Sector 50", new String[]{"Supertech Eco Village","Lotus Panache","Gaur City","Palm Olympia","Parsvnath Prestige"});
        SOCIETY_MAP.put("DLF Phase 1", new String[]{"DLF Magnolias","DLF The Aralias","Hamilton Court","Qutab Plaza","Central Arcade"});
        SOCIETY_MAP.put("Sector 44", new String[]{"The Mews","Emaar MGF Palm Drive","Beverly Park","Laburnum","Nirvana Country"});
        SOCIETY_MAP.put("Koramangala", new String[]{"Mantri Residency","Prestige Shantiniketan","Sobha Chrysanthemum","Brigade Cosmopolis","Godrej Eden Garden"});
        SOCIETY_MAP.put("Whitefield", new String[]{"Prestige White Meadows","Embassy Springs","Salarpuria Serenity","Brigade Meadows","Vaishnavi Terraces"});
        SOCIETY_MAP.put("Banjara Hills", new String[]{"Lotus Pond","Rock Heights","Cyan Woods","Ruchi Rainbow","Fortune Nirvana"});
        SOCIETY_MAP.put("Andheri East", new String[]{"Lodha Belmondo","Hiranandani Estate","Kanakia Wallstreet","Oberoi Realty","Rustomjee Urbania"});
        SOCIETY_MAP.put("Sector 17", new String[]{"CHB Flats Sector 17","Private Colony Block A","Government Quarters"});
        SOCIETY_MAP.put("Mansarovar", new String[]{"Shri Ram Colony","Vasundhara Enclave","Patrakar Colony","Pratap Nagar Housing","New Sanganer Road Society"});
    }

    private static final String[][] MAP_GRID = {
        {"Srinagar","Jammu","Amritsar","Ludhiana","Ambala","Chandigarh","Dehradun","Hapur","Ghaziabad","Delhi"},
        {"Hisar","Rohtak","Panipat","Karnal","Yamuna Nagar","Saharanpur","Meerut","Moradabad","Bareilly","Noida (Gautam Buddha Nagar)"},
        {"Alwar","Gurgaon (Gurugram)","Faridabad","New Delhi","Aligarh","Mathura","Agra","Firozabad","Shahjahanpur","Lucknow"},
        {"Jaipur","Ajmer","Bikaner","Jodhpur","Barmer","Gwalior","Jhansi","Kanpur Nagar","Varanasi","Gorakhpur"},
        {"Udaipur","Ahmedabad","Gandhinagar","Anand","Bhopal","Indore","Allahabad (Prayagraj)","Azamgarh","Patna","Dhanbad"},
        {"Rajkot","Vadodara","Surat","Nashik","Aurangabad","Nagpur","Raipur","Ranchi","Cuttack","Howrah"},
        {"Mumbai","Thane","Pune","Dharwad","Hyderabad","Warangal","Vijayawada","Visakhapatnam","Kolkata","Guwahati"},
        {"Goa","Hubli","Bangalore Urban","Salem","Coimbatore","Chennai","Madurai","Tiruchirappalli","Tiruppur","Cuttack"},
        {"Kochi","Mysuru","Thiruvananthapuram","Tiruppur","Visakhapatnam","Vijayawada","Kakinada","Nellore","Kolkata","Guwahati"},
        {"Lakshadweep","Kozhikode","Mangaluru","Belagavi","Gulbarga","Nizamabad","Karimnagar","Warangal","Nalgonda","Adilabad"}
    };

    public static String[] getDistricts() {
        String[] copy = Arrays.copyOf(DISTRICTS, DISTRICTS.length);
        Arrays.sort(copy, String.CASE_INSENSITIVE_ORDER);
        return copy;
    }

    public static String[] getSectorsFor(String district) {
        return SECTOR_MAP.getOrDefault(district, new String[0]);
    }

    public static String[] getSocietiesFor(String sector) {
        return SOCIETY_MAP.getOrDefault(sector, new String[0]);
    }

    public static LocationResult reverseGeocode(int pixelX, int pixelY, int mapW, int mapH) {
        int cols = 10, rows = 10;
        int col = Math.max(0, Math.min(cols - 1, (int) ((double) pixelX / Math.max(1, mapW) * cols)));
        int row = Math.max(0, Math.min(rows - 1, (int) ((double) pixelY / Math.max(1, mapH) * rows)));
        String district = MAP_GRID[row][col];

        String sector = "";
        String[] sectors = getSectorsFor(district);
        if (sectors.length > 0) sector = sectors[Math.abs((pixelX * 13 + pixelY * 7) % sectors.length)];

        String society = "";
        String[] societies = getSocietiesFor(sector);
        if (societies.length > 0) society = societies[Math.abs((pixelX + pixelY * 3) % societies.length)];

        String houseNo = (10 + (pixelX % 90)) + "/" + (1 + (pixelY % 20));
        String address = !society.isEmpty() ? houseNo + ", " + society + ", " + (sector.isEmpty() ? district : sector) + ", " + district
                : !sector.isEmpty() ? houseNo + ", " + sector + ", " + district
                : houseNo + ", " + district;
        return new LocationResult(district, sector, society, address);
    }

    public record LocationResult(String district, String sector, String society, String address) {}
}
