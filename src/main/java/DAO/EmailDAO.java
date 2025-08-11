package DAO;

import org.example.Email;
import org.example.EmailRecipient;
import org.example.EmailService;
import org.example.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import Util.HibernateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

import static Util.HibernateUtil.sessionFactory;

public class EmailDAO {
    EmailService emailService = new EmailService();

    public void saveEmail(Email email) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(email);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public Email findByCode(String code) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Email WHERE code = :code", Email.class)
                    .setParameter("code", code)
                    .uniqueResult();
        }
    }




    public List<EmailRecipient> getAllEmail(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
            FROM EmailRecipient er
            WHERE er.recipient.id = :userId
            ORDER BY er.email.sentAt DESC
            """, EmailRecipient.class)
                    .setParameter("userId", userId)
                    .list();
        }
    }


    public List<EmailRecipient> getUnreadMessages(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
                            FROM EmailRecipient er\s
                            WHERE er.recipient.id = :userId\s
                            AND er.isRead = false\s
                            ORDER BY er.email.sentAt DESC
                            
            """, EmailRecipient.class)
                    .setParameter("userId", userId)
                    .list();
        }
    }


    public List<Email> getSentEmails(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
            from Email e
            where e.sender.id = :userId
            order by e.sentAt desc
            """, Email.class)
                    .setParameter("userId", userId)
                    .list();
        }
    }

    public List<EmailRecipient> getReadMessages(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
                    FROM EmailRecipient er
                    WHERE er.recipient.id = :userId
                    AND er.isRead = true
                    ORDER BY er.readAt DESC
                    """, EmailRecipient.class)
                    .setParameter("userId", userId)
                    .list();
        }

    }
}



