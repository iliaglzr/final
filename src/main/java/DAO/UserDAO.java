package DAO;


import org.example.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import Util.HibernateUtil;

import java.util.List;

public class UserDAO {

    public void saveUser(User user) {
        Transaction tx=null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
         tx = session.beginTransaction();
        session.persist(user);
        tx.commit();
        }
        catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }



    public User FindByUsername(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();
        }
    }


    public List<User> getAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createNativeQuery("from User", User.class).list();
        }
    }

}

