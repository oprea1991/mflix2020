package mflix.api.daos;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import mflix.api.models.Session;
import mflix.api.models.User;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.swing.plaf.basic.BasicSpinnerUI;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class UserDao extends AbstractMFlixDao {

    private final MongoCollection<User> usersCollection;
    //TODO> Ticket: User Management - do the necessary changes so that the sessions collection
    //returns a Session object
    private final MongoCollection<Session> sessionsCollection;

    private final Logger log;

    @Autowired
    public UserDao(
            MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
        super(mongoClient, databaseName);
        CodecRegistry pojoCodecRegistry =
                fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        usersCollection = db.getCollection("users", User.class).withCodecRegistry(pojoCodecRegistry);
        log = LoggerFactory.getLogger(this.getClass());
        //TODO> Ticket: User Management - implement the necessary changes so that the sessions
        // collection returns a Session objects instead of Document objects.
        sessionsCollection = db.getCollection("sessions", Session.class).withCodecRegistry(pojoCodecRegistry);
    }

    /**
     * Inserts the `user` object in the `users` collection.
     *
     * @param user - User object to be added
     * @return True if successful, throw IncorrectDaoOperation otherwise
     */
    public boolean addUser(User user) {
        //TODO > Ticket: Durable Writes -  you might want to use a more durable write concern here!
//        usersCollection.insertOne(user);
        usersCollection.withWriteConcern(WriteConcern.MAJORITY).insertOne(user);
        return true;
        //TODO > Ticket: Handling Errors - make sure to only add new users
        // and not users that already exist.

    }

    /**
     * Creates session using userId and jwt token.
     *
     * @param userId - user string identifier
     * @param jwt    - jwt string token
     * @return true if successful
     */
    public boolean createUserSession(
            final String userId,
            final String jwt) {
        final Session session = new Session();
        session.setUserId(userId);
        session.setJwt(jwt);
        if (Optional.ofNullable(sessionsCollection.find(Filters.eq("user_id", userId)).first())
                .isPresent()) {
            sessionsCollection.updateOne(Filters.eq("user_id", userId), Updates.set("jwt", jwt));
        } else {
            sessionsCollection.insertOne(session);
        }
        return true;
    }

    /**
     * Returns the User object matching the an email string value.
     *
     * @param email - email string to be matched.
     * @return User object or null.
     */
    public User getUser(String email) {
        User user = null;
        //TODO> Ticket: User Management - implement the query that returns the first User object.
        user = usersCollection.find(Filters.eq("email", email)).first();
        return user;
    }

    /**
     * Given the userId, returns a Session object.
     *
     * @param userId - user string identifier.
     * @return Session object or null.
     */
    public Session getUserSession(String userId) {
        //TODO> Ticket: User Management - implement the method that returns Sessions for a given
        // userId
        Session session =  sessionsCollection.find(Filters.eq("user_id", userId)).first();
        return session;
    }

    public boolean deleteUserSessions(String userId) {
            //TODO> Ticket: User Management - implement the delete user sessions method
            sessionsCollection.deleteMany(Filters.eq("user_id", userId));
            return true;
    }

    /**
     * Removes the user document that match the provided email.
     *
     * @param email - of the user to be deleted.
     * @return true if user successfully removed
     */
    public boolean deleteUser(String email) {
        // remove user sessions
        //TODO> Ticket: User Management - implement the delete user method
        deleteUserSessions(email);
        usersCollection.deleteOne(Filters.eq("email", email));
        //TODO > Ticket: Handling Errors - make this method more robust by
        // handling potential exceptions.
        return true;
    }

    /**
     * Updates the preferences of an user identified by `email` parameter.
     *
     * @param email           - user to be updated email
     * @param userPreferences - set of preferences that should be stored and replace the existing
     *                        ones. Cannot be set to null value
     * @return User object that just been updated.
     */
    public boolean updateUserPreferences(String email, Map<String, ?> userPreferences) {
        //TODO> Ticket: User Preferences - implement the method that allows for user preferences to
        // be updated.
        System.out.println(email);
        System.out.println(userPreferences);
        if (userPreferences == null) {
           throw  new IncorrectDaoOperation("usePreferences cannot be null");
        }

        Bson queryFilter = new Document("email",email);
        Bson userPref = Updates.set("preferences",userPreferences);
        System.out.println(queryFilter);
        System.out.println(userPref);
        UpdateResult res = usersCollection.updateOne(queryFilter, userPref);

        /*Document userUpdate = new Document();
        for ( Map.Entry<String, ?> mapset : userPreferences.entrySet()){
           userUpdate.put(mapset.getKey(),mapset.getValue());
        }
        Bson u =userUpdate ;
         usersCollection.updateOne(queryFilter,userUpdate);
        // usersCollection.updateOne(queryFilter,userPref)*/;
        //TODO > Ticket: Handling Errors - make this method more robust by
        // handling potential exceptions when updating an entry.
        return true;
    }
}
