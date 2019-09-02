package ServerPart;

import story.*;
import work_with_collection.ConcurrentCollectionManager;

import java.sql.*;
import java.time.LocalDateTime;

public class CollectionReader {
    
    private Connection connection;
    
    CollectionReader(Connection connection) {
        this.connection = connection;
    }
    
    ConcurrentCollectionManager createCollection(ConcurrentCollectionManager manager) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet jailFinder = statement.executeQuery("select * from jails");
        while (jailFinder.next()) {
            Jail jail = readJail(jailFinder);
            int jailId = jailFinder.getInt("jail_id");
            PreparedStatement ps = connection.prepareStatement("select * from squads where jail_id = ?");
            ps.setInt(1, jailId);
            ResultSet finder = ps.executeQuery();
            while (finder.next()){
                jail.addPoliceman(readPoliceman(finder));
                
            }
            ps = connection.prepareStatement("select * from prisoners where jail_id = ?");
            ps.setInt(1, jailId);
            finder = ps.executeQuery();
            while (finder.next()){
                jail.addShorty(readShorty(finder));
            }
            ps = connection.prepareStatement("select * from deads where jail_id = ?");
            ps.setInt(1, jailId);
            finder = ps.executeQuery();
            while (finder.next()){
                jail.addShorty(readShorty(finder));
            }
            
            manager.add(jail);
        }
        System.out.println("Collection is ready");
        return manager;
    }
    
    int getUserId(String login) throws SQLException {
        try {
            PreparedStatement ps = connection.prepareStatement("select user_id from users where name = ?");
            ps.setString(1, login);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            }
            return 0;
        }catch (NullPointerException e){
            return 0;
        }
    }
    
    int getOwnerId(Jail jail) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select user_id from jails where name = ? and abscissa = ? and ordinate = ?");
        ps.setString(1, jail.getName());
        ps.setInt(2, jail.getAbscissa());
        ps.setInt(3, jail.getOrdinate());
        ResultSet resultSet = ps.executeQuery();
        if (resultSet.next()) {
            
            return resultSet.getInt("user_id");
        }
        return 0;
    }
    
    int getCraneId(Crane crane) throws SQLException {
        try {
            PreparedStatement ps = connection.prepareStatement("select crane_id from cranes where name = ? and abscissa = ? and ordinate = ?");
            ps.setString(1, crane.getName());
            ps.setInt(2, crane.getAbscissa());
            ps.setInt(3, crane.getOrdinate());
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("crane_id");
            }
            return 0;
        }catch (NullPointerException e){
            return 0;
        }
    }
    
    int getStoveId(Stove stove) throws SQLException {
        try {
            PreparedStatement ps = connection.prepareStatement("select stove_id from stoves where name = ? and abscissa = ? and ordinate = ?");
            ps.setString(1, stove.getName());
            ps.setInt(2, stove.getAbscissa());
            ps.setInt(3, stove.getOrdinate());
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("stove_id");
            }
            return 0;
        } catch (NullPointerException e) {
            return 0;
        }
    }
    
    int getJailId(Jail jail) throws SQLException {
        try {
            PreparedStatement ps = connection.prepareStatement("select jail_id from jails where name = ? and abscissa = ? and ordinate = ?");
            ps.setString(1, jail.getName());
            ps.setInt(2, jail.getAbscissa());
            ps.setInt(3, jail.getOrdinate());
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("jail_id");
            }
            return 0;
        } catch (NullPointerException e) {
            return 0;
        }
    }
    
    private Jail readJail(ResultSet jailFinder) throws SQLException {
        int jailAbscissa = jailFinder.getInt("abscissa");
        int jailOrdinate = jailFinder.getInt("ordinate");
        String jailName = jailFinder.getString("name");
        int jailLength = jailFinder.getInt("length");
        int jailWidth = jailFinder.getInt("width");
        int craneId = jailFinder.getInt("crane_id");
        Crane crane = findCrane(craneId);
        int stoveId = jailFinder.getInt("stove_id");
        Stove stove = findStove(stoveId);
        Timestamp timestamp = (Timestamp)jailFinder.getObject("date");
        LocalDateTime dateTime = timestamp.toLocalDateTime();
        return new Jail(jailAbscissa, jailOrdinate, jailName, crane, stove, dateTime, jailLength, jailWidth);
    }
    
    private Crane findCrane(int craneId) throws SQLException {
        try {
            PreparedStatement ps = connection.prepareStatement("select * from cranes where crane_id = ?");
            ps.setInt(1, craneId);
            ResultSet craneFinder = ps.executeQuery();
            while (craneFinder.next()) {
                if (craneFinder.getInt("crane_id") == craneId) {
                    String name = craneFinder.getString("name");
                    int abscissa = craneFinder.getInt("abscissa");
                    int ordinate = craneFinder.getInt("ordinate");
                    try {
                        CraneCondition cond = craneFinder.getObject("condition", CraneCondition.class);
                        try {
                            WithWater water = craneFinder.getObject("water", WithWater.class);
                            return new Crane(abscissa, ordinate, name, cond, water);
                        } catch (NullPointerException e) {
                            return new Crane(abscissa, ordinate, name, cond);
                        }
                    } catch (NullPointerException e) {
                        try {
                            WithWater water = craneFinder.getObject("water", WithWater.class);
                            return new Crane(abscissa, ordinate, name, water);
                        } catch (NullPointerException e1) {
                            return new Crane(abscissa, ordinate, name);
                        }
                    }
                }
            }
            return null;
        } catch (NullPointerException e){
            return null;
        }
    }
    
    private Stove findStove(int stoveId) throws SQLException {
        try {
            PreparedStatement ps = connection.prepareStatement("select * from stoves where stove_id = ?");
            ps.setInt(1, stoveId);
            ResultSet stoveFinder = ps.executeQuery();
            while (stoveFinder.next()) {
                if (stoveFinder.getInt("stove_id") == stoveId) {
                    String name = stoveFinder.getString("name");
                    int abscissa = stoveFinder.getInt("abscissa");
                    int ordinate = stoveFinder.getInt("ordinate");
                    try {
                        WithFire fire = stoveFinder.getObject("fire", WithFire.class);
                        return new Stove(name, abscissa, ordinate, fire);
                    } catch (NullPointerException e) {
                        return new Stove(name, abscissa, ordinate);
                    }
                }
            }
            return null;
        } catch (NullPointerException e){
            return null;
        }
    }
    
    private Policeman readPoliceman(ResultSet policemanFinder) throws SQLException {
        try {
            String policemanName = policemanFinder.getString("name");
            int policemanAbscissa = policemanFinder.getInt("abscissa");
            int policemanOrdinate = policemanFinder.getInt("ordinate");
            try {
                boolean policemanArmament = policemanFinder.getBoolean("armament");
                return new Policeman(policemanName, policemanAbscissa, policemanOrdinate, policemanArmament);
            } catch (NullPointerException e) {
                return new Policeman(policemanName, policemanAbscissa, policemanOrdinate);
            }
        }catch (NullPointerException e){
            return null;
        }
    }
    
    private Shorty readShorty(ResultSet shortyFinder) throws SQLException {
        try {
            String prisonerName = shortyFinder.getString("name");
            int prisonerAbscissa = shortyFinder.getInt("abscissa");
            int prisonerOrdinate = shortyFinder.getInt("ordinate");
            try {
                Condition prisonerCondition = shortyFinder.getObject("cond", Condition.class);
                try {
                    double prisonerEnergy = shortyFinder.getDouble("energy");
                    return new Shorty(prisonerName, prisonerCondition, prisonerAbscissa, prisonerOrdinate, prisonerEnergy);
                } catch (NullPointerException e) {
                    return new Shorty(prisonerName, prisonerCondition, prisonerAbscissa, prisonerOrdinate);
                }
            } catch (NullPointerException e) {
                return new Shorty(prisonerName, prisonerAbscissa, prisonerOrdinate);
            }
        }catch (NullPointerException e){
            return null;
        }
    }
}
