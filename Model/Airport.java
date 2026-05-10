package model;

/*
                                                   ---Airport.java--- 
 BY: YOUSEF                                                                                                
 Represents a single airport node in the flight graph.
 Each airport is identified by a unique code (e.g., "CAI", "LHR").
 Used as a vertex in the FlightGraph adjacency list. 
 DSA Role: Graph vertex / HashMap key
 */
public class Airport {

    
    private String code;	// Unique code (e.g., "CAI" for Cairo International)
    private String name;	// Full name of the airport
    private String city;	// City the airport serves
    private String country;	// Country the airport is located in

 
   // Constructor to create an Airport object. //
 
    public Airport(String code, String name, String city, String country) {
        this.code = code;
        this.name = name;
        this.city = city;
        this.country = country;
    }

    // ──────────────── Getters ────────────────//

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    // ──────────────── Setters ────────────────//

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

  
     //Returns a human-readable string representation.
    //Example: CAI - Cairo International (Cairo, Egypt)
 
    @Override
    public String toString() {
        return code + " - " + name + " (" + city + ", " + country + ")";
    }

    /*
     * Two airports are equal if they share the same IATA code.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Airport)) return false;
        Airport other = (Airport) obj;
        return this.code.equalsIgnoreCase(other.code);
    }

    
    // HashCode based on code for use in HashMaps.
    @Override
    public int hashCode() {
        return code.toUpperCase().hashCode();
    }
}
