package DAO;

import org.example.EmailRecipient;
import org.hibernate.Session;
import org.hibernate.Transaction;
import Util.HibernateUtil;

import java.util.List;

import static Util.HibernateUtil.sessionFactory;

public class EmailRecipientDao {

    public void saveRecipient(EmailRecipient recipient) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(recipient);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void markAsRead(String userEmail) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createQuery("UPDATE EmailRecipient er SET er.isRead = true WHERE er.recipient.email = :email")
                    .setParameter("email", userEmail)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }




    public List<EmailRecipient> getRecipientsByEmailId(int emailId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM EmailRecipient WHERE email.id = :emailId", EmailRecipient.class)
                    .setParameter("emailId", emailId)
                    .list();
        }
    }
}
