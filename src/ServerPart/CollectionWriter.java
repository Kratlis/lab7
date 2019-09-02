package ServerPart;

import story.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class CollectionWriter {
    private Connection connection;
    
    public CollectionWriter(Connection connection) {
        this.connection = connection;
    }
    
    public void writeJail(Jail jail, String login) throws SQLException {
        CollectionReader collectionReader = new CollectionReader(connection);
        System.out.println( collectionReader+" created+");
        try {
            writeCrane(jail.getCrane());
        }catch (NullPointerException i){}
        try{writeStove(jail.getStove());
        }catch (NullPointerException i){}
        try{
        writeSquad(jail.getSquad(), collectionReader.getJailId(jail));
        }catch (NullPointerException i){}
    try{
        writePrisoners(jail.getPrisoners(), collectionReader.getJailId(jail));
    }catch (NullPointerException i){}
    try{
        writeDeads(jail.getTheDead(), collectionReader.getJailId(jail));
    }catch (NullPointerException i){}
    
        PreparedStatement ps = connection.prepareStatement("insert into jails(crane_id, stove_id, user_id, name, abscissa, ordinate, date)" +
                "values (?, ?, ?, ?, ?, ?, ?)");
        ps.setInt(1, collectionReader.getCraneId(jail.getCrane()));
        ps.setInt(2, collectionReader.getStoveId(jail.getStove()));
        ps.setInt(3, collectionReader.getUserId(login));
//        ps.setInt(1, 0);
//        ps.setInt(2, 0);
//        ps.setInt(3, 0);
//        ps.setString(4, null);
//        ps.setInt(5,j);
//        ps.setInt(6, 0);
        ps.setObject(7, Timestamp.valueOf(jail.getInitDate()));
        ps.setString(4, jail.getName());
        ps.setInt(5, jail.getAbscissa());
        ps.setInt(6, jail.getOrdinate());
        ps.executeUpdate();
        ps.close();
    }
    
    private void writeDeads(ArrayList<Shorty> theDead, int jailId) throws SQLException {
        for (Shorty sh: theDead) {
            PreparedStatement ps = connection.prepareStatement("insert into deads(jail_id, name, abscissa, ordinate, cond, energy) values (?, ?, ?, ?, ?, ?)");
            ps.setString(2, sh.getName());
            ps.setInt(3, sh.getAbscissa());
            ps.setInt(4, sh.getOrdinate());
            ps.setInt(1, jailId);
            ps.setObject(5, sh.getCond());
            ps.setDouble(6, sh.getEnergy());
            ps.executeUpdate();
            ps.close();
        }
    }
    
    private void writePrisoners(ArrayList<Shorty> prisoners, int jailId) throws SQLException {
        for (Shorty sh: prisoners) {
            PreparedStatement ps = connection.prepareStatement("insert into prisoners(jail_id, name, abscissa, ordinate, cond, energy) values (?, ?, ?, ?, ?, ?)");
            ps.setString(2, sh.getName());
            ps.setInt(3, sh.getAbscissa());
            ps.setInt(4, sh.getOrdinate());
            ps.setInt(1, jailId);
            ps.setObject(5, sh.getCond());
            ps.setDouble(6, sh.getEnergy());
            ps.executeUpdate();
            ps.close();
        }
    }
    
    private void writeSquad(ArrayList<Policeman> squad, int id) throws SQLException {
        for (Policeman p: squad) {
            PreparedStatement ps = connection.prepareStatement("insert into squads(name, abscissa, ordinate, armament, jail_id) values (?, ?, ?, ?, ?)");
            ps.setString(1, p.getName());
            ps.setInt(2, p.getAbscissa());
            ps.setInt(3, p.getOrdinate());
            ps.setBoolean(4, p.isArmament());
            ps.setInt(5, id);
            ps.executeUpdate();
            ps.close();
        }
    }
    
    public boolean checkOwner(Jail jail, String login) throws SQLException {
        CollectionReader collectionReader = new CollectionReader(connection);
        int id = collectionReader.getUserId(login);
        System.out.println(login+" это прислали");
        return collectionReader.getOwnerId(jail) == id;
    }
    
    public void writeCrane(Crane crane) throws SQLException {
        PreparedStatement stat = connection.prepareStatement("insert into cranes(condition, water, name, abscissa, ordinate)" +
                "values (?, ?, ?, ?, ?)");
        stat.setObject(1, crane.getCraneCondition());
        stat.setObject(2, crane.getWater());
        stat.setString(3, crane.getName());
        stat.setInt(4, crane.getAbscissa());
        stat.setInt(5, crane.getOrdinate());
        stat.executeUpdate();
        stat.close();
    }
    public void writeStove(Stove stove) throws SQLException {
        PreparedStatement stat = connection.prepareStatement("insert into stoves(fire, name, abscissa, ordinate) " +
                "values (?, ?, ?, ?)");
        stat.setObject(1, stove.getFire());
        stat.setString(2, stove.getName());
        stat.setInt(3, stove.getAbscissa());
        stat.setObject(4, stove.getOrdinate());
        stat.executeUpdate();
        stat.close();
    }
    
    public void deleteJail(Jail jail, String login) throws SQLException {
        CollectionReader cr = new CollectionReader(connection);
        int jailId = cr.getJailId(jail);
        int craneId = cr.getCraneId(jail.getCrane());
        int stoveId = cr.getStoveId(jail.getStove());
        PreparedStatement ps = connection.prepareStatement("delete from cranes where crane_id = ?");
        PreparedStatement ps1 = connection.prepareStatement("delete from stoves where stove_id = ?");
        PreparedStatement ps2 = connection.prepareStatement("delete from prisoners where jail_id = ?");
        PreparedStatement ps3 = connection.prepareStatement("delete from squads where jail_id = ?");
        PreparedStatement ps4 = connection.prepareStatement("delete from deads where jail_id = ?");
        PreparedStatement ps5 = connection.prepareStatement("delete from jails where jail_id = ?");
        ps.setInt(1, craneId);
        ps1.setInt(1, stoveId);
        ps2.setInt(1, jailId);
        ps3.setInt(1, jailId);
        ps4.setInt(1, jailId);
        ps5.setInt(1, jailId);
        ps.executeUpdate();
        ps1.executeUpdate();
        ps2.executeUpdate();
        ps3.executeUpdate();
        ps4.executeUpdate();
        ps5.executeUpdate();
        ps.close();
        ps1.close();
        ps2.close();
        ps3.close();
        ps4.close();
        ps5.close();
    }
}
